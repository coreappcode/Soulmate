import { createHash, randomUUID } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { AccessToken } from "livekit-server-sdk";
import { defineSecret, defineString } from "firebase-functions/params";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger, setGlobalOptions } from "firebase-functions/v2";

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
    throw new HttpsError("unauthenticated", "Sign in to place or answer a call.");
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

export const notifyOnChatMessage = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
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
    if (messageSettings.get("pushMessages") === false ||
        (Array.isArray(mutedConversations) && mutedConversations.includes(conversationId)) ||
        recipientBlock.exists || senderBlock.exists) {
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

    try {
      const result = await getMessaging().sendEachForMulticast({
        tokens: deviceEntries.map((device) => device.token),
        data: {
          type: "incoming_message",
          conversationId,
          messageId,
          senderId,
          senderName,
          body,
        },
        android: { priority: "normal" },
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
      if (result.failureCount > invalidTokenRefs.length) {
        logger.error("Some chat message notifications could not be delivered.", {
          conversationId,
          messageId,
          failureCount: result.failureCount,
        });
      }
    } catch (error) {
      logger.error("Could not send chat message notifications.", { conversationId, messageId, error });
    }
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
