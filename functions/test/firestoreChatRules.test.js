const assert = require("node:assert/strict");
const { after, before, beforeEach, test } = require("node:test");

const projectId = "soulmate-chat-rules-test";
const firestoreHost = process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8080";
const authHost = process.env.FIREBASE_AUTH_EMULATOR_HOST || "127.0.0.1:9099";
const firestoreBase =
  `http://${firestoreHost}/v1/projects/${projectId}/databases/(default)`;
const authBase = `http://${authHost}/identitytoolkit.googleapis.com/v1`;
let alice;
let bob;
let participants;
let conversationId;

function encodedValue(value) {
  if (value instanceof Date) return { timestampValue: value.toISOString() };
  if (value === null) return { nullValue: "NULL_VALUE" };
  if (typeof value === "string") return { stringValue: value };
  if (typeof value === "boolean") return { booleanValue: value };
  if (typeof value === "number") return { integerValue: String(value) };
  if (Array.isArray(value)) {
    return { arrayValue: { values: value.map(encodedValue) } };
  }
  return { mapValue: { fields: encodedFields(value) } };
}

function encodedFields(fields) {
  return Object.fromEntries(
    Object.entries(fields).map(([key, value]) => [key, encodedValue(value)])
  );
}

function documentName(path) {
  return `projects/${projectId}/databases/(default)/documents/${path}`;
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(body.error?.message ?? `HTTP ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return body;
}

async function signIn() {
  return requestJson(`${authBase}/accounts:signUp?key=fake-api-key`, {
    method: "POST",
    body: JSON.stringify({ returnSecureToken: true }),
  });
}

async function commit(writes, idToken = "owner") {
  return requestJson(`${firestoreBase}/documents:commit`, {
    method: "POST",
    headers: { Authorization: `Bearer ${idToken}` },
    body: JSON.stringify({ writes }),
  });
}

function updateWrite(path, fields, options = {}) {
  return {
    update: { name: documentName(path), fields: encodedFields(fields) },
    ...(options.updateMask ? { updateMask: { fieldPaths: options.updateMask } } : {}),
    ...(options.transforms ? { updateTransforms: options.transforms } : {}),
    ...(options.exists === undefined
      ? {}
      : { currentDocument: { exists: options.exists } }),
  };
}

function serverTimestampTransform(fieldPath) {
  return { fieldPath, setToServerValue: "REQUEST_TIME" };
}

async function seedConversation() {
  await commit([
    updateWrite(`conversations/${conversationId}`, {
      userIds: participants,
      archivedBy: [],
      lastMessage: "Existing message",
      lastMessageId: "existing-message",
      unreadCounts: { [alice.localId]: 1, [bob.localId]: 2 },
      readAt: {},
      typing: {},
    }, { exists: false }),
  ]);
}

before(async () => {
  alice = await signIn();
  bob = await signIn();
  participants = [alice.localId, bob.localId].sort();
  conversationId = participants.join("_");
});

beforeEach(async () => {
  await requestJson(
    `http://${firestoreHost}/emulator/v1/projects/${projectId}/databases/(default)/documents`,
    { method: "DELETE" }
  );
  await commit([
    updateWrite(`matches/${conversationId}`, { userIds: participants }, { exists: false }),
  ]);
});

after(async () => {
  await requestJson(
    `http://${firestoreHost}/emulator/v1/projects/${projectId}/databases/(default)/documents`,
    { method: "DELETE" }
  );
});

test("conversation creation rejects forged previews and extra fields", async () => {
  const write = updateWrite(`conversations/${conversationId}`, {
    userIds: participants,
    archivedBy: [],
    lastMessage: "Forged preview",
    unreadCounts: { [alice.localId]: 0, [bob.localId]: 0 },
    forgedField: true,
  }, {
    exists: false,
    updateMask: [
      "userIds",
      "archivedBy",
      "lastMessage",
      "unreadCounts",
      "updatedAt",
      "createdAt",
      "forgedField",
    ],
    transforms: [
      serverTimestampTransform("updatedAt"),
      serverTimestampTransform("createdAt"),
    ],
  });
  await assert.rejects(commit([write], alice.idToken));
});

test("conversation creation accepts only the canonical empty state", async () => {
  const write = updateWrite(`conversations/${conversationId}`, {
    userIds: participants,
    archivedBy: [],
    lastMessage: "",
    unreadCounts: { [alice.localId]: 0, [bob.localId]: 0 },
  }, {
    exists: false,
    updateMask: [
      "userIds",
      "archivedBy",
      "lastMessage",
      "unreadCounts",
      "updatedAt",
      "createdAt",
    ],
    transforms: [
      serverTimestampTransform("updatedAt"),
      serverTimestampTransform("createdAt"),
    ],
  });
  await commit([write], alice.idToken);
});

test("blocked pairs cannot create new conversations", async () => {
  await commit([
    updateWrite(`users/${alice.localId}/blocks/${bob.localId}`, {}, {
      exists: false,
      transforms: [serverTimestampTransform("createdAt")],
    }),
  ], alice.idToken);
  const write = updateWrite(`conversations/${conversationId}`, {
    userIds: participants,
    archivedBy: [],
    lastMessage: "",
    unreadCounts: { [alice.localId]: 0, [bob.localId]: 0 },
  }, {
    exists: false,
    updateMask: [
      "userIds",
      "archivedBy",
      "lastMessage",
      "unreadCounts",
      "updatedAt",
      "createdAt",
    ],
    transforms: [
      serverTimestampTransform("updatedAt"),
      serverTimestampTransform("createdAt"),
    ],
  });
  await assert.rejects(commit([write], alice.idToken));
});

test("blocked participants cannot update read receipts or unread counts", async () => {
  await seedConversation();
  await commit([
    updateWrite(`users/${alice.localId}/blocks/${bob.localId}`, {
      createdAt: new Date(),
    }, { exists: false }),
  ], alice.idToken);

  const write = updateWrite(`conversations/${conversationId}`, {
    unreadCounts: { [alice.localId]: 0, [bob.localId]: 2 },
    readAt: {},
  }, {
    updateMask: ["unreadCounts", "readAt"],
    transforms: [serverTimestampTransform(`readAt.${alice.localId}`)],
  });
  await assert.rejects(commit([write], alice.idToken));
});

test("blocking atomically clears private conversation preview state", async () => {
  await seedConversation();
  await commit([
    updateWrite(`users/${alice.localId}/blocks/${bob.localId}`, {
      createdAt: new Date(),
    }, { exists: false }),
    updateWrite(`conversations/${conversationId}`, {
      lastMessage: "",
      lastMessageId: "",
      unreadCounts: { [alice.localId]: 0, [bob.localId]: 0 },
      readAt: {},
      typing: {},
    }, {
      updateMask: ["lastMessage", "lastMessageId", "unreadCounts", "readAt", "typing"],
    }),
  ], alice.idToken);
});

test("profile documents and username claims can only be written by trusted backend", async () => {
  await assert.rejects(commit([
    updateWrite(`profiles/${alice.localId}`, {
      uid: alice.localId,
      fullName: "Alice Example",
      age: 30,
    }, { exists: false }),
  ], alice.idToken));
  await assert.rejects(commit([
    updateWrite(`usernames/user_${alice.localId}`, {
      uid: alice.localId,
      username: `user_${alice.localId}`,
    }, { exists: false }),
  ], alice.idToken));
});

test("clients cannot write swipes, Pro entitlements, or daily quota records directly", async () => {
  const protectedWrites = [
    updateWrite(`swipes/${alice.localId}_${bob.localId}`, {
      senderId: alice.localId,
      recipientId: bob.localId,
      liked: true,
    }, { exists: false }),
    updateWrite(`billingEntitlements/${alice.localId}`, { isPro: true }, { exists: false }),
    updateWrite(`dailySwipeUsage/${alice.localId}_2026-10-25`, {
      uid: alice.localId,
      swipesUsed: 0,
    }, { exists: false }),
  ];

  for (const write of protectedWrites) {
    await assert.rejects(commit([write], alice.idToken));
  }
});
