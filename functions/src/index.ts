import { createHash, randomUUID } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { FieldPath, FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { getStorage } from "firebase-admin/storage";
import { AccessToken } from "livekit-server-sdk";
import { defineSecret, defineString } from "firebase-functions/params";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger, setGlobalOptions } from "firebase-functions/v2";
import {
  chatAttachmentDownloadToken,
  firebaseDownloadUrl,
  storageObjectPathFromDownloadUrl,
} from "./chatAttachmentSecurity.js";
import {
  shouldNotifyChatMessage,
  shouldRetryChatNotificationFailure,
} from "./chatNotificationPolicy.js";
import {
  InvalidProfileError,
  validateProfilePayload,
} from "./profileValidation.js";

initializeApp();
setGlobalOptions({ region: "asia-southeast1", maxInstances: 10 });

const liveKitApiKey = defineSecret("LIVEKIT_API_KEY");
const liveKitApiSecret = defineSecret("LIVEKIT_API_SECRET");
const liveKitUrl = defineString("LIVEKIT_URL", {
  default: "wss://soulmate-d7kmmbyb.livekit.cloud",
});

const db = getFirestore();
const RING_TIMEOUT_MS = 45_000;
const CALL_LEASE_MS = 90_000;

type CallRecord = {
  callerId: string;
  calleeId: string;
  callerDeviceId: string;
  acceptedDeviceId?: string;
  participants: string[];
  roomName: string;
  callType: "audio" | "video";
  status: "ringing" | "accepted" | "declined" | "ended" | "missed";
  expiresAt: Timestamp;
};

function authenticatedUid(uid: string | undefined): string {
  if (!uid) {
    throw new HttpsError("unauthenticated", "Sign in to continue.");
  }
  return uid;
}

function stringValue(value: unknown): string | undefined {
  return typeof value === "string" && value.trim() ? value.trim() : undefined;
}

function profileName(profile: FirebaseFirestore.DocumentData | undefined): string {
  return stringValue(profile?.fullName) ??
    stringValue(profile?.name) ??
    stringValue(profile?.username) ??
    "Soulmate user";
}

export const saveMyProfile = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  let validated: ReturnType<typeof validateProfilePayload>;
  try {
    validated = validateProfilePayload(
      request.data?.profile,
      request.data?.privateProfile
    );
  } catch (error) {
    if (error instanceof InvalidProfileError) {
      throw new HttpsError("invalid-argument", error.message);
    }
    throw error;
  }

  const profile = request.data.profile as Record<string, unknown>;
  const userRef = db.collection("users").doc(uid);
  const profileRef = db.collection("profiles").doc(uid);
  const privacyRef = userRef.collection("settings").doc("privacy");
  const nextUsernameRef = db.collection("usernames").doc(validated.username);

  await db.runTransaction(async (transaction) => {
    const [oldProfile, nextUsername, privacy] = await Promise.all([
      transaction.get(profileRef),
      transaction.get(nextUsernameRef),
      transaction.get(privacyRef),
    ]);
    const oldUsername = stringValue(oldProfile.get("username"));
    const oldUsernameRef = oldUsername && oldUsername !== validated.username
      ? db.collection("usernames").doc(oldUsername)
      : undefined;
    const oldUsernameClaim = oldUsernameRef
      ? await transaction.get(oldUsernameRef)
      : undefined;

    if (nextUsername.exists && nextUsername.get("uid") !== uid) {
      throw new HttpsError("already-exists", "That username is already taken.");
    }

    if (oldUsernameRef && oldUsernameClaim?.get("uid") === uid) {
      transaction.delete(oldUsernameRef);
    }
    transaction.set(nextUsernameRef, { uid, username: validated.username });

    const showAge = privacy.get("showAge") !== false;
    transaction.set(profileRef, {
      ...profile,
      uid,
      age: showAge ? validated.age : null,
      showAge,
      verified: oldProfile.get("verified") === true,
      isComplete: true,
      showOnlineStatus: privacy.get("showOnlineStatus") !== false,
      updatedAt: FieldValue.serverTimestamp(),
    });
    transaction.set(userRef, {
      ...validated.privateProfile,
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
  });
  return { saved: true, age: validated.age };
});

export const registerCallDevice = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  const token = stringValue(request.data?.token);
  if (!token || token.length > 5_000) {
    throw new HttpsError("invalid-argument", "A valid messaging token is required.");
  }

  const tokenId = createHash("sha256").update(token).digest("hex");
  const targetRef = db.collection("users").doc(uid).collection("devices").doc(tokenId);
  await targetRef.set({ token, updatedAt: FieldValue.serverTimestamp() });
  return { registered: true };
});

export const unregisterCallDevice = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  const token = stringValue(request.data?.token);
  if (!token || token.length > 5_000) {
    throw new HttpsError("invalid-argument", "A valid messaging token is required.");
  }

  const tokenId = createHash("sha256").update(token).digest("hex");
  await db.collection("users").doc(uid).collection("devices").doc(tokenId).delete();
  return { unregistered: true };
});

export const notifyOnChatMessage = onDocumentCreated(
  {
    document: "conversations/{conversationId}/messages/{messageId}",
    retry: true,
  },
  async (event) => {
    const message = event.data?.data();
    const conversationId = event.params.conversationId;
    const messageId = event.params.messageId;
    const senderId = stringValue(message?.senderId);
    if (!message || !senderId) {
      logger.error("Ignoring a chat message without a valid sender.", { conversationId, messageId });
      return;
    }

    const conversationSnapshot = await db.collection("conversations").doc(conversationId).get();
    const participants = conversationSnapshot.get("userIds");
    if (!conversationSnapshot.exists || !Array.isArray(participants) ||
        participants.length !== 2 || !participants.includes(senderId)) {
      logger.error("Ignoring a chat message for an invalid conversation.", { conversationId, messageId });
      return;
    }
    const recipientId = participants.find((participant): participant is string =>
      typeof participant === "string" && participant !== senderId
    );
    if (!recipientId) {
      logger.error("Ignoring a chat message without a recipient.", { conversationId, messageId });
      return;
    }

    const settingsRef = db.collection("users").doc(recipientId).collection("settings");
    const recipientBlocksRef = db.collection("users").doc(recipientId)
      .collection("blocks").doc(senderId);
    const senderBlocksRef = db.collection("users").doc(senderId)
      .collection("blocks").doc(recipientId);
    const [messageSettings, chatSettings, devices, senderProfile, recipientBlock, senderBlock] =
      await Promise.all([
        settingsRef.doc("notifications").get(),
        settingsRef.doc("chatNotifications").get(),
        db.collection("users").doc(recipientId).collection("devices").get(),
        db.collection("profiles").doc(senderId).get(),
        recipientBlocksRef.get(),
        senderBlocksRef.get(),
      ]);

    const mutedConversations = chatSettings.get("mutedConversations");
    if (!shouldNotifyChatMessage({
      pushMessagesEnabled: messageSettings.get("pushMessages") !== false,
      conversationMuted: Array.isArray(mutedConversations) &&
        mutedConversations.includes(conversationId),
      hasBlock: recipientBlock.exists || senderBlock.exists,
    })) {
      return;
    }

    const deviceEntries = devices.docs
      .map((device) => ({ ref: device.ref, token: stringValue(device.get("token")) }))
      .filter((device): device is { ref: FirebaseFirestore.DocumentReference; token: string } =>
        device.token != null
      )
      .slice(0, 500);
    if (deviceEntries.length === 0) return;

    const text = stringValue(message.text);
    const attachmentType = stringValue(message.attachmentType);
    const body = (text ?? (
      attachmentType === "image" ? "Sent a photo" :
        attachmentType === "audio" ? "Sent an audio file" :
          attachmentType === "document" ? "Sent a document" : "Sent a message"
    )).slice(0, 500);
    const senderName = profileName(senderProfile.data());

    const result = await getMessaging().sendEachForMulticast({
      tokens: deviceEntries.map((device) => device.token),
      data: {
        type: "incoming_message",
        conversationId,
        messageId,
        recipientId,
        senderId,
        senderName,
        body,
      },
      android: {
        priority: "high",
        collapseKey: conversationId.slice(0, 64),
      },
    });

    const invalidTokenRefs = result.responses.flatMap((response, index) => {
      const errorCode = response.error?.code;
      return errorCode === "messaging/registration-token-not-registered" ||
        errorCode === "messaging/invalid-registration-token"
        ? [deviceEntries[index].ref]
        : [];
    });
    if (invalidTokenRefs.length > 0) {
      const batch = db.batch();
      invalidTokenRefs.forEach((ref) => batch.delete(ref));
      await batch.commit();
    }
    const retryableFailures = result.responses.filter((response) =>
      shouldRetryChatNotificationFailure(response.error?.code)
    );
    if (retryableFailures.length > 0) {
      throw new Error(
        `Temporary FCM failure for ${retryableFailures.length} device(s) in conversation ${conversationId}.`
      );
    }
    if (result.failureCount > invalidTokenRefs.length) {
      logger.error("Some chat message notifications could not be delivered.", {
        conversationId,
        messageId,
        failureCount: result.failureCount,
      });
    }
  }
);

function messagePreview(message: FirebaseFirestore.DocumentData | undefined): string {
  const text = stringValue(message?.text);
  if (text) return text;
  switch (stringValue(message?.attachmentType)) {
    case "image":
      return "Sent a photo";
    case "audio":
      return "Sent an audio file";
    case "document":
      return "Sent a document";
    default:
      return "";
  }
}

async function rotateConversationAttachmentTokens(
  conversationId: string,
  tokenSeed: string
): Promise<void> {
  const bucket = getStorage().bucket();
  const messages = db.collection("conversations").doc(conversationId).collection("messages");
  let cursor: FirebaseFirestore.QueryDocumentSnapshot | undefined;

  while (true) {
    let query: FirebaseFirestore.Query = messages
      .orderBy(FieldPath.documentId())
      .limit(100);
    if (cursor) query = query.startAfter(cursor);
    const page = await query.get();
    if (page.empty) return;

    const batch = db.batch();
    let hasUrlUpdates = false;
    for (const message of page.docs) {
      const oldUrl = stringValue(message.get("attachmentUrl"));
      if (!oldUrl) continue;
      const objectPath = storageObjectPathFromDownloadUrl(
        oldUrl,
        bucket.name,
        conversationId
      );
      if (!objectPath) continue;

      const token = chatAttachmentDownloadToken(tokenSeed, objectPath);
      const file = bucket.file(objectPath);
      try {
        const [metadata] = await file.getMetadata();
        await file.setMetadata({
          metadata: {
            ...(metadata.metadata ?? {}),
            firebaseStorageDownloadTokens: token,
          },
        });
        batch.update(message.ref, {
          attachmentUrl: firebaseDownloadUrl(bucket.name, objectPath, token),
        });
        hasUrlUpdates = true;
      } catch (error) {
        if (
          typeof error === "object" &&
          error !== null &&
          "code" in error &&
          (error.code === 404 || error.code === "storage/object-not-found")
        ) {
          logger.warn("Chat message references a missing Storage object.", {
            conversationId,
            messageId: message.id,
            objectPath,
          });
          continue;
        }
        throw error;
      }
    }
    if (hasUrlUpdates) await batch.commit();
    cursor = page.docs[page.docs.length - 1];
    if (page.size < 100) return;
  }
}

async function synchronizeConversationBlockState(
  conversationId: string,
  participantIds: string[]
): Promise<void> {
  if (participantIds.length !== 2) return;
  const [firstUid, secondUid] = participantIds;
  const conversationRef = db.collection("conversations").doc(conversationId);
  const securityRef = db.collection("chatSecurityInternal").doc(conversationId);
  const firstBlockRef = db.collection("users").doc(firstUid)
    .collection("blocks").doc(secondUid);
  const secondBlockRef = db.collection("users").doc(secondUid)
    .collection("blocks").doc(firstUid);

  const transition = await db.runTransaction(async (transaction) => {
    const [conversation, securityState, firstBlock, secondBlock] = await Promise.all([
      transaction.get(conversationRef),
      transaction.get(securityRef),
      transaction.get(firstBlockRef),
      transaction.get(secondBlockRef),
    ]);
    if (!conversation.exists) return undefined;

    const isBlocked = firstBlock.exists || secondBlock.exists;
    const currentStateKey = isBlocked
      ? [firstBlock.updateTime?.toMillis() ?? 0, secondBlock.updateTime?.toMillis() ?? 0].join(":")
      : "unblocked";
    const stateData = securityState.data() ?? {};
    const previousStateKey = stringValue(stateData.chatSecurityStateKey);

    if (isBlocked) {
      const tokenSeed = previousStateKey === currentStateKey
        ? stringValue(stateData.chatSecurityTokenSeed) ?? randomUUID()
        : randomUUID();
      transaction.update(conversationRef, {
        lastMessage: "",
        lastMessageId: "",
        unreadCounts: Object.fromEntries(participantIds.map((uid) => [uid, 0])),
        readAt: {},
        typing: {},
      });
      transaction.set(securityRef, {
        chatBlockSanitized: true,
        chatSecurityStateKey: currentStateKey,
        chatSecurityTokenSeed: tokenSeed,
        chatAttachmentsSecuredStateKey: stateData.chatAttachmentsSecuredStateKey ?? "",
      });
      return {
        blocked: true,
        stateKey: currentStateKey,
        tokenSeed,
        attachmentsSecured: stateData.chatAttachmentsSecuredStateKey === currentStateKey,
      };
    }

    if (stateData.chatBlockSanitized !== true) return undefined;
    return {
      blocked: false,
      stateKey: "unblocked",
      tokenSeed: undefined,
      attachmentsSecured: true,
    };
  });

  if (!transition) return;
  if (transition.blocked && !transition.attachmentsSecured && transition.tokenSeed) {
    await rotateConversationAttachmentTokens(conversationId, transition.tokenSeed);
    const secured = await db.runTransaction(async (transaction) => {
      const currentState = await transaction.get(securityRef);
      if (currentState.get("chatSecurityStateKey") !== transition.stateKey) return false;
      transaction.update(securityRef, {
        chatAttachmentsSecuredStateKey: transition.stateKey,
      });
      return true;
    });
    if (!secured) await synchronizeConversationBlockState(conversationId, participantIds);
    return;
  }
  if (transition.blocked) return;

  const restored = await db.runTransaction(async (transaction) => {
    const [conversation, securityState, firstBlock, secondBlock] = await Promise.all([
      transaction.get(conversationRef),
      transaction.get(securityRef),
      transaction.get(firstBlockRef),
      transaction.get(secondBlockRef),
    ]);
    if (!conversation.exists || firstBlock.exists || secondBlock.exists) return false;
    if (securityState.get("chatBlockSanitized") !== true) return true;

    const latestMessages = await transaction.get(
      conversationRef.collection("messages")
        .orderBy("createdAt", "desc")
        .limit(1)
    );
    const latest = latestMessages.docs[0];
    transaction.update(conversationRef, {
      lastMessage: messagePreview(latest?.data()),
      lastMessageId: latest?.id ?? "",
    });
    transaction.update(securityRef, {
      chatBlockSanitized: false,
      chatSecurityStateKey: "unblocked",
      chatSecurityTokenSeed: FieldValue.delete(),
    });
    return true;
  });
  if (!restored) {
    await synchronizeConversationBlockState(conversationId, participantIds);
  }
}

export const onChatBlockWritten = onDocumentWritten(
  "users/{uid}/blocks/{blockedUid}",
  async (event) => {
    const participants = [event.params.uid, event.params.blockedUid].sort();
    await synchronizeConversationBlockState(participants.join("_"), participants);
  }
);

async function updateChatBlockStatus(
  uid: string,
  targetUid: string,
  blocked: boolean
): Promise<void> {
  const participants = [uid, targetUid].sort();
  const blockRef = db.collection("users").doc(uid)
    .collection("blocks").doc(targetUid);
  const conversationRef = db.collection("conversations")
    .doc(participants.join("_"));

  await db.runTransaction(async (transaction) => {
    const [block, conversation] = await Promise.all([
      transaction.get(blockRef),
      transaction.get(conversationRef),
    ]);
    if (blocked && !block.exists) {
      transaction.set(blockRef, { createdAt: FieldValue.serverTimestamp() });
    } else if (!blocked && block.exists) {
      transaction.delete(blockRef);
    }
    if (blocked && conversation.exists) {
      transaction.update(conversationRef, {
        lastMessage: "",
        lastMessageId: "",
        unreadCounts: Object.fromEntries(participants.map((participant) => [participant, 0])),
        readAt: {},
        typing: {},
      });
    }
  });
  await synchronizeConversationBlockState(participants.join("_"), participants);
}

export const blockChatUser = onCall(
  { timeoutSeconds: 540, memory: "1GiB" },
  async (request) => {
    const uid = authenticatedUid(request.auth?.uid);
    const targetUid = stringValue(request.data?.targetId);
    if (!targetUid || targetUid === uid) {
      throw new HttpsError("invalid-argument", "Choose a valid account.");
    }
    await updateChatBlockStatus(uid, targetUid, true);
    return { blocked: true };
  }
);

export const unblockChatUser = onCall(
  { timeoutSeconds: 540, memory: "1GiB" },
  async (request) => {
    const uid = authenticatedUid(request.auth?.uid);
    const targetUid = stringValue(request.data?.targetId);
    if (!targetUid || targetUid === uid) {
      throw new HttpsError("invalid-argument", "Choose a valid account.");
    }
    await updateChatBlockStatus(uid, targetUid, false);
    return { unblocked: true };
  }
);

export const synchronizeMyChatBlockStates = onCall(
  { timeoutSeconds: 540, memory: "1GiB" },
  async (request) => {
    const uid = authenticatedUid(request.auth?.uid);
    const migrationRef = db.collection("chatSecurityMigrations").doc(uid);
    const migration = await migrationRef.get();
    if (migration.get("version") === 1) return { synchronized: 0 };

    const conversations = await db.collection("conversations")
      .where("userIds", "array-contains", uid)
      .get();
    let synchronized = 0;
    for (const conversation of conversations.docs) {
      const participants = conversation.get("userIds");
      if (!Array.isArray(participants) || participants.length !== 2) continue;
      await synchronizeConversationBlockState(conversation.id, participants);
      synchronized += 1;
    }
    await migrationRef.set({ version: 1, updatedAt: FieldValue.serverTimestamp() });
    return { synchronized };
  }
);

async function clearCallLocks(callId: string, participants: string[]): Promise<void> {
  await db.runTransaction(async (transaction) => {
    const refs = participants.map((uid) => db.collection("callLocks").doc(uid));
    const snapshots = await Promise.all(refs.map((ref) => transaction.get(ref)));
    snapshots.forEach((snapshot, index) => {
      if (snapshot.get("callId") === callId) {
        transaction.delete(refs[index]);
      }
    });
  });
}

export const createCall = onCall(async (request) => {
    const callerId = authenticatedUid(request.auth?.uid);
    const calleeId = stringValue(request.data?.calleeId);
    const callerDeviceId = stringValue(request.data?.callerDeviceId);
    const callType = request.data?.callType;

    if (!calleeId || calleeId === callerId || !callerDeviceId) {
      throw new HttpsError("invalid-argument", "Choose a valid person to call.");
    }
    if (callType !== "audio" && callType !== "video") {
      throw new HttpsError("invalid-argument", "Choose audio or video calling.");
    }

    const participants = [callerId, calleeId].sort();
    const conversationId = participants.join("_");
    const conversationRef = db.collection("conversations").doc(conversationId);
    const callerProfileRef = db.collection("profiles").doc(callerId);
    const calleeProfileRef = db.collection("profiles").doc(calleeId);
    const callerBlockRef = db.collection("users").doc(callerId).collection("blocks").doc(calleeId);
    const calleeBlockRef = db.collection("users").doc(calleeId).collection("blocks").doc(callerId);
    const callerDeviceRef = db.collection("users").doc(callerId)
      .collection("devices").doc(callerDeviceId);
    const deviceTokensRef = db.collection("users").doc(calleeId).collection("devices");
    const [conversation, callerProfile, calleeProfile, callerBlock, calleeBlock, callerDevice, devices] =
      await Promise.all([
        conversationRef.get(),
        callerProfileRef.get(),
        calleeProfileRef.get(),
        callerBlockRef.get(),
        calleeBlockRef.get(),
        callerDeviceRef.get(),
        deviceTokensRef.get(),
      ]);

    if (
      !conversation.exists ||
      !Array.isArray(conversation.get("userIds")) ||
      !participants.every((uid) => conversation.get("userIds").includes(uid))
    ) {
      throw new HttpsError("permission-denied", "Calls are available only between chat participants.");
    }
    if (callerBlock.exists || calleeBlock.exists) {
      throw new HttpsError("permission-denied", "This call is unavailable.");
    }
    if (!callerProfile.exists || !calleeProfile.exists) {
      throw new HttpsError("not-found", "A participant profile could not be found.");
    }
    if (!callerDevice.exists) {
      throw new HttpsError("failed-precondition", "Register this device before placing a call.");
    }

    const deviceTokens = [...new Set(
      devices.docs
        .map((device) => device.get("token"))
        .filter((token): token is string => typeof token === "string" && token.length > 0)
    )];
    if (deviceTokens.length === 0) {
      throw new HttpsError(
        "failed-precondition",
        "The recipient has no registered device for incoming call notifications."
      );
    }

    const callId = randomUUID();
    const roomName = `soulmate_${callId.replaceAll("-", "")}`;
    const callRef = db.collection("calls").doc(callId);
    const lockRefs = participants.map((uid) => db.collection("callLocks").doc(uid));
    const ringExpiresAt = Timestamp.fromMillis(Date.now() + RING_TIMEOUT_MS);

    await db.runTransaction(async (transaction) => {
      const locks = await Promise.all(lockRefs.map((ref) => transaction.get(ref)));
      for (const lock of locks) {
        const expiresAt = lock.get("expiresAt") as Timestamp | undefined;
        if (lock.exists && expiresAt && expiresAt.toMillis() > Date.now()) {
          throw new HttpsError("failed-precondition", "One of the participants is already on a call.");
        }
      }

      transaction.create(callRef, {
        callerId,
        calleeId,
        callerDeviceId,
        participants,
        roomName,
        callType,
        status: "ringing",
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
        expiresAt: ringExpiresAt,
      });

      lockRefs.forEach((ref) => transaction.set(ref, {
        callId,
        expiresAt: ringExpiresAt,
      }));
    });

    try {
      const pushResult = await getMessaging().sendEachForMulticast({
        tokens: deviceTokens,
        data: {
          type: "incoming_call",
          callId,
          callType,
          callerId,
          calleeId,
          callerName: profileName(callerProfile.data()),
          callerPhoto: stringValue(callerProfile.get("photos")?.[0]) ?? "",
        },
        android: { priority: "high", ttl: RING_TIMEOUT_MS },
      });
      if (pushResult.successCount === 0) {
        await callRef.update({
          status: "ended",
          updatedAt: FieldValue.serverTimestamp(),
          endedAt: FieldValue.serverTimestamp(),
        });
        await clearCallLocks(callId, participants);
        throw new HttpsError("unavailable", "The recipient could not be notified. Please try again.");
      }
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      logger.error("Failed to notify recipient about an incoming call.", { callId, error });
      await callRef.update({
        status: "ended",
        updatedAt: FieldValue.serverTimestamp(),
        endedAt: FieldValue.serverTimestamp(),
      });
      await clearCallLocks(callId, participants);
      throw new HttpsError("unavailable", "The recipient could not be notified. Please try again.");
    }

  return { callId };
});

export const respondToCall = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  const callId = stringValue(request.data?.callId);
  const accept = request.data?.accept;
  if (!callId || typeof accept !== "boolean") {
    throw new HttpsError("invalid-argument", "A call and response are required.");
  }

  const callRef = db.collection("calls").doc(callId);
  const snapshot = await callRef.get();
  if (!snapshot.exists) {
    throw new HttpsError("not-found", "This call no longer exists.");
  }
  const call = snapshot.data() as CallRecord;
  if (uid !== call.calleeId) {
    throw new HttpsError("permission-denied", "Only the invited recipient can answer this call.");
  }
  if (call.status !== "ringing" || call.expiresAt.toMillis() <= Date.now()) {
    throw new HttpsError("deadline-exceeded", "This call has expired.");
  }

  const deviceId = accept ? stringValue(request.data?.deviceId) : undefined;
  if (accept && !deviceId) {
    throw new HttpsError("invalid-argument", "Register this device before answering a call.");
  }
  if (accept && deviceId) {
    const device = await db.collection("users").doc(uid).collection("devices").doc(deviceId).get();
    if (!device.exists) {
      throw new HttpsError("failed-precondition", "Register this device before answering a call.");
    }
  }

  const outcome = await db.runTransaction(async (transaction) => {
    const current = await transaction.get(callRef);
    if (!current.exists || current.get("status") !== "ringing") {
      throw new HttpsError("failed-precondition", "This call is no longer ringing.");
    }
    const currentCall = current.data() as CallRecord;
    if (currentCall.expiresAt.toMillis() <= Date.now()) {
      throw new HttpsError("deadline-exceeded", "This call has expired.");
    }

    if (!accept) {
      transaction.update(callRef, {
        status: "declined",
        endedAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      });
      return { participants: currentCall.participants, accepted: false as const };
    }

    const lockRefs = currentCall.participants.map((participantId) =>
      db.collection("callLocks").doc(participantId)
    );
    const locks = await Promise.all(lockRefs.map((ref) => transaction.get(ref)));
    locks.forEach((lock) => {
      const expiresAt = lock.get("expiresAt") as Timestamp | undefined;
      if (lock.exists && expiresAt && expiresAt.toMillis() > Date.now() &&
          lock.get("callId") !== callId) {
        throw new HttpsError("failed-precondition", "One of the participants is already on another call.");
      }
    });

    const lockExpiry = Timestamp.fromMillis(Date.now() + CALL_LEASE_MS);
    transaction.update(callRef, {
      status: "accepted",
      acceptedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
      expiresAt: lockExpiry,
      acceptedDeviceId: deviceId,
    });
    lockRefs.forEach((ref) => transaction.set(ref, { callId, expiresAt: lockExpiry }));
    return { participants: currentCall.participants, accepted: true as const };
  });

  if (!outcome.accepted) await clearCallLocks(callId, outcome.participants);
  return { status: accept ? "accepted" : "declined" };
});

export const getCallToken = onCall(
  { secrets: [liveKitApiKey, liveKitApiSecret] },
  async (request) => {
    const uid = authenticatedUid(request.auth?.uid);
    const callId = stringValue(request.data?.callId);
    const deviceId = stringValue(request.data?.deviceId);
    if (!callId || !deviceId) {
      throw new HttpsError("invalid-argument", "A call is required.");
    }

    const snapshot = await db.collection("calls").doc(callId).get();
    if (!snapshot.exists) {
      throw new HttpsError("not-found", "This call no longer exists.");
    }
    const call = snapshot.data() as CallRecord;
    if (!call.participants.includes(uid)) {
      throw new HttpsError("permission-denied", "You are not part of this call.");
    }
    const authorizedDeviceId = uid === call.callerId ? call.callerDeviceId : call.acceptedDeviceId;
    if (deviceId !== authorizedDeviceId) {
      throw new HttpsError("permission-denied", "This call was answered on another device.");
    }
    if (call.status !== "accepted") {
      throw new HttpsError("failed-precondition", "The call must be answered before joining.");
    }
    if (call.expiresAt.toMillis() <= Date.now()) {
      throw new HttpsError("deadline-exceeded", "This call has expired.");
    }

    const profile = await db.collection("profiles").doc(uid).get();
    const accessToken = new AccessToken(liveKitApiKey.value(), liveKitApiSecret.value(), {
      identity: uid,
      name: profileName(profile.data()),
      ttl: "4h",
    });
    accessToken.addGrant({
      roomJoin: true,
      room: call.roomName,
      canPublish: true,
      canSubscribe: true,
      canPublishData: true,
    });
    return { token: await accessToken.toJwt(), url: liveKitUrl.value(), callType: call.callType };
  }
);

export const heartbeatCall = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  const callId = stringValue(request.data?.callId);
  const deviceId = stringValue(request.data?.deviceId);
  if (!callId || !deviceId) {
    throw new HttpsError("invalid-argument", "A call is required.");
  }

  const callRef = db.collection("calls").doc(callId);
  const leaseExpiry = Timestamp.fromMillis(Date.now() + CALL_LEASE_MS);
  await db.runTransaction(async (transaction) => {
    const call = await transaction.get(callRef);
    if (!call.exists) {
      throw new HttpsError("not-found", "This call no longer exists.");
    }
    const record = call.data() as CallRecord;
    if (!record.participants.includes(uid)) {
      throw new HttpsError("permission-denied", "You are not part of this call.");
    }
    const authorizedDeviceId = uid === record.callerId
      ? record.callerDeviceId
      : record.acceptedDeviceId;
    if (deviceId !== authorizedDeviceId) {
      throw new HttpsError("permission-denied", "This call belongs to another device.");
    }
    if (record.status !== "accepted") {
      throw new HttpsError("failed-precondition", "This call is no longer active.");
    }
    const lockRefs = record.participants.map((participantId) =>
      db.collection("callLocks").doc(participantId)
    );
    const locks = await Promise.all(lockRefs.map((ref) => transaction.get(ref)));
    if (locks.some((lock) => !lock.exists || lock.get("callId") !== callId)) {
      throw new HttpsError("failed-precondition", "This call is no longer active on this device.");
    }
    transaction.update(callRef, { expiresAt: leaseExpiry, updatedAt: FieldValue.serverTimestamp() });
    lockRefs.forEach((ref) => {
      transaction.set(ref, {
        callId,
        expiresAt: leaseExpiry,
      });
    });
  });
  return { expiresAt: leaseExpiry.toMillis() };
});

export const endCall = onCall(async (request) => {
  const uid = authenticatedUid(request.auth?.uid);
  const callId = stringValue(request.data?.callId);
  if (!callId) {
    throw new HttpsError("invalid-argument", "A call is required.");
  }

  const callRef = db.collection("calls").doc(callId);
  const snapshot = await callRef.get();
  if (!snapshot.exists) {
    return { status: "ended" };
  }
  const call = snapshot.data() as CallRecord;
  if (!call.participants.includes(uid)) {
    throw new HttpsError("permission-denied", "You are not part of this call.");
  }
  if (call.status !== "ended" && call.status !== "declined" && call.status !== "missed") {
    await callRef.update({
      status: "ended",
      endedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
  await clearCallLocks(callId, call.participants);
  return { status: "ended" };
});
