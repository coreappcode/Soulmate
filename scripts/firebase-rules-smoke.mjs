import { readFile } from "node:fs/promises";

const projectId = process.env.GCLOUD_PROJECT ?? "demo-soulmate";
const authEmulator = process.env.FIREBASE_AUTH_EMULATOR_HOST ?? "127.0.0.1:9099";
const firestoreEmulator = process.env.FIRESTORE_EMULATOR_HOST ?? "127.0.0.1:8080";
const storageEmulator = process.env.FIREBASE_STORAGE_EMULATOR_HOST ?? "127.0.0.1:9199";
const config = JSON.parse(await readFile(new URL("../app/google-services.json", import.meta.url), "utf8"));
const apiKey = config.client.find(
  (client) => client.client_info.android_client_info.package_name === "com.soulmate.androidapp"
)?.api_key[0].current_key;

if (!apiKey) throw new Error("Firebase config is missing the requested Android app registration.");

const docRoot =
  `http://${firestoreEmulator}/v1/projects/${projectId}/databases/(default)/documents`;
const resourceRoot =
  `projects/${projectId}/databases/(default)/documents`;
const bucket = config.project_info.storage_bucket;
let idToken;
let userId;
let storageObjectUrl;
let result = "not run";
let cleanupFailed = false;

async function request(url, { method = "GET", body, token = idToken, contentType = "application/json" } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers["Content-Type"] = contentType;
  const response = await fetch(url, {
    method,
    headers,
    body: body === undefined ? undefined : contentType === "application/json" ? JSON.stringify(body) : body
  });
  const responseText = await response.text();
  let data;
  try {
    data = responseText ? JSON.parse(responseText) : null;
  } catch {
    data = responseText;
  }
  return { response, data };
}

function assertStatus(label, result, expected) {
  const { response, data } = result;
  if (response.status !== expected) {
    throw new Error(
      `${label}: expected HTTP ${expected}, got ${response.status}: ${JSON.stringify(data)}`
    );
  }
}

try {
  const signup = await request(
    `http://${authEmulator}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${apiKey}`,
    { method: "POST", token: undefined, body: { returnSecureToken: true } }
  );
  assertStatus("Create anonymous emulator user", signup, 200);
  idToken = signup.data.idToken;
  userId = signup.data.localId;
  if (!idToken || !userId) throw new Error("Auth emulator did not return user credentials.");

  const userPath = `${docRoot}/users/${userId}`;
  const profilePath = `${docRoot}/profiles/${userId}`;
  const usernamePath = `${docRoot}/usernames/audit_${userId}`;
  const settingsPath = `${userPath}/settings/privacy`;

  const profileFields = {
    uid: { stringValue: userId },
    fullName: { stringValue: "Audit Test User" },
    fullNameLower: { stringValue: "audit test user" },
    username: { stringValue: `audit_${userId}` },
    usernameLower: { stringValue: `audit_${userId}` },
    age: { integerValue: "31" },
    showAge: { booleanValue: true },
    gender: { stringValue: "Woman" },
    bio: { stringValue: "Synthetic Firebase rules smoke test." },
    photos: { arrayValue: { values: [] } },
    interests: { arrayValue: { values: [] } },
    lookingFor: { arrayValue: { values: [] } },
    verified: { booleanValue: false },
    isComplete: { booleanValue: true }
  };

  const userFields = {
    phoneNumber: { stringValue: "+15555550123" },
    draft: {
      mapValue: {
        fields: {
          fullName: { stringValue: "Audit Test User" },
          username: { stringValue: `audit_${userId}` },
          age: { integerValue: "31" },
          gender: { stringValue: "Woman" },
          bio: { stringValue: "Synthetic Firebase rules smoke test." },
          interests: { arrayValue: { values: [] } },
          lookingFor: { arrayValue: { values: [] } }
        }
      }
    }
  };

  const create = await request(`${docRoot}:commit`, {
    method: "POST",
    body: {
      writes: [
        {
          update: { name: `${resourceRoot}/profiles/${userId}`, fields: profileFields },
          currentDocument: { exists: false }
        },
        {
          update: {
            name: `${resourceRoot}/users/${userId}`,
            fields: userFields
          },
          currentDocument: { exists: false }
        },
        {
          update: {
            name: `${resourceRoot}/usernames/audit_${userId}`,
            fields: {
              uid: { stringValue: userId },
              username: { stringValue: `audit_${userId}` }
            }
          },
          currentDocument: { exists: false }
        }
      ]
    }
  });
  assertStatus("Create user draft, profile, and username claim", create, 200);

  const draftRead = await request(userPath);
  assertStatus("Read own profile draft", draftRead, 200);
  if (draftRead.data.fields?.draft?.mapValue?.fields?.fullName?.stringValue !== "Audit Test User") {
    throw new Error("Profile draft did not persist the expected synthetic name.");
  }

  const profileRead = await request(profilePath);
  assertStatus("Read public profile", profileRead, 200);
  if (profileRead.data.fields?.username?.stringValue !== `audit_${userId}`) {
    throw new Error("Public profile did not persist the username.");
  }

  const privacyWrite = await request(`${docRoot}:commit`, {
    method: "POST",
    body: {
      writes: [
        {
          update: {
            name: `${resourceRoot}/users/${userId}/settings/privacy`,
            fields: { showAge: { booleanValue: false } }
          },
          currentDocument: { exists: false }
        }
      ]
    }
  });
  assertStatus("Save privacy setting", privacyWrite, 200);

  const hideAge = await request(
    `${profilePath}?updateMask.fieldPaths=showAge&updateMask.fieldPaths=age`,
    {
      method: "PATCH",
      body: {
        name: `${resourceRoot}/profiles/${userId}`,
        fields: { showAge: { booleanValue: false } }
      }
    }
  );
  assertStatus("Remove public age", hideAge, 200);

  const privateRead = await request(settingsPath);
  assertStatus("Read own privacy settings", privateRead, 200);
  const privateAge = privateRead.data.fields?.showAge?.booleanValue;
  if (privateAge !== false) throw new Error("Privacy setting did not persist.");

  const hiddenAgeRead = await request(profilePath);
  assertStatus("Read age-hidden public profile", hiddenAgeRead, 200);
  if (hiddenAgeRead.data.fields?.age !== undefined || hiddenAgeRead.data.fields?.showAge?.booleanValue !== false) {
    throw new Error("Age visibility was not enforced in the public profile.");
  }

  const forbiddenRead = await request(`${docRoot}/users/another-test-uid`);
  assertStatus("Deny another user's private draft", forbiddenRead, 403);

  const reportId = `audit_${userId}`;
  const report = await request(`${docRoot}:commit`, {
    method: "POST",
    body: {
      writes: [
        {
          update: {
            name: `${resourceRoot}/reports/${reportId}`,
            fields: {
              reporterId: { stringValue: userId },
              targetId: { stringValue: "another-test-uid" },
              reason: { stringValue: "Spam" },
              details: { stringValue: "Synthetic report rules test." },
              status: { stringValue: "open" }
            }
          },
          currentDocument: { exists: false },
          updateTransforms: [{ fieldPath: "createdAt", setToServerValue: "REQUEST_TIME" }]
        }
      ]
    }
  });
  assertStatus("Submit report", report, 200);

  const reportRead = await request(`${docRoot}/reports/${reportId}`);
  assertStatus("Deny reporter access to report queue", reportRead, 403);

  const selfReport = await request(`${docRoot}:commit`, {
    method: "POST",
    body: {
      writes: [
        {
          update: {
            name: `${resourceRoot}/reports/self_${userId}`,
            fields: {
              reporterId: { stringValue: userId },
              targetId: { stringValue: userId },
              reason: { stringValue: "Spam" },
              details: { stringValue: "" },
              status: { stringValue: "open" }
            }
          },
          currentDocument: { exists: false },
          updateTransforms: [{ fieldPath: "createdAt", setToServerValue: "REQUEST_TIME" }]
        }
      ]
    }
  });
  assertStatus("Deny self-report", selfReport, 403);

  const image = Buffer.from(
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAF//8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABBQJ//8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAgBAwEBPwF//8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAgBAgEBPwF//8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQAGPwJ//8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPyF//9k=",
    "base64"
  );
  const upload = await request(
    `http://${storageEmulator}/v0/b/${encodeURIComponent(bucket)}/o?name=${encodeURIComponent(
      `users/${userId}/profile/audit-smoke.jpg`
    )}`,
    { method: "POST", body: image, contentType: "image/jpeg" }
  );
  assertStatus("Upload profile image", upload, 200);

  const storageObject = `http://${storageEmulator}/v0/b/${encodeURIComponent(bucket)}/o/${encodeURIComponent(
    `users/${userId}/profile/audit-smoke.jpg`
  )}`;
  storageObjectUrl = storageObject;
  const deleteObject = await request(storageObject, { method: "DELETE" });
  if (![200, 204].includes(deleteObject.response.status)) {
    throw new Error(`Delete test image: expected HTTP 200 or 204, got ${deleteObject.response.status}`);
  }
  result = "passed";
} finally {
  if (storageObjectUrl && idToken) {
    const deleteObject = await request(storageObjectUrl, { method: "DELETE" });
    if (![200, 204, 404].includes(deleteObject.response.status)) {
      console.error(`Cleanup failed for emulator storage object (${deleteObject.response.status}).`);
      cleanupFailed = true;
    }
  }
  if (userId && idToken) {
    const cleanupWrites = [
      `users/${userId}/settings/privacy`,
      `profiles/${userId}`,
      `users/${userId}`,
      `usernames/audit_${userId}`
    ].map((path) => ({ delete: `${resourceRoot}/${path}` }));
    const clearFirestore = await request(`${docRoot}:commit`, {
      method: "POST",
      body: { writes: cleanupWrites }
    });
    if (clearFirestore.response.status !== 200) {
      console.error(`Cleanup failed for emulator Firestore documents (${clearFirestore.response.status}).`);
      cleanupFailed = true;
    }
  }
  if (idToken) {
    const deleteAuthUser = await request(
      `http://${authEmulator}/identitytoolkit.googleapis.com/v1/accounts:delete?key=${apiKey}`,
      { method: "POST", token: "", body: { idToken } }
    );
    if (![200, 204].includes(deleteAuthUser.response.status)) {
      console.error(
        `Cleanup failed for the smoke-test auth user (${deleteAuthUser.response.status}): ` +
        `${JSON.stringify(deleteAuthUser.data)}`
      );
      cleanupFailed = true;
    }
  }
}

if (result !== "passed" || cleanupFailed) process.exitCode = 1;
if (result === "passed" && !cleanupFailed) {
  console.log(
    "Firebase Auth/Firestore/Storage rules smoke test passed; mutable fixtures were cleared. " +
    "The immutable report fixture is discarded when the emulator shuts down."
  );
}
