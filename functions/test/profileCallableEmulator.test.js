const assert = require("node:assert/strict");
const { test } = require("node:test");

const projectId = "soulmate-chat-rules-test";
const authHost = process.env.FIREBASE_AUTH_EMULATOR_HOST || "127.0.0.1:9099";
const firestoreHost = process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8080";
const functionsHost = process.env.FUNCTIONS_EMULATOR_HOST || "127.0.0.1:5001";
const authBase = `http://${authHost}/identitytoolkit.googleapis.com/v1`;
const firestoreBase =
  `http://${firestoreHost}/v1/projects/${projectId}/databases/(default)/documents`;

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
  });
  const body = await response.json().catch(() => ({}));
  return { response, body };
}

async function createUser() {
  const { response, body } = await requestJson(`${authBase}/accounts:signUp?key=fake-api-key`, {
    method: "POST",
    body: JSON.stringify({ returnSecureToken: true }),
  });
  assert.equal(response.ok, true, JSON.stringify(body));
  return body;
}

function validPayload(dateOfBirth = "Jan 1, 2000", username = "alice_example") {
  const profile = {
    fullName: "Alice Example",
    fullNameLower: "alice example",
    username,
    usernameLower: username,
    gender: "Woman",
    bio: "A profile bio long enough to meet the minimum.",
    photos: ["photo-1", "photo-2"],
    interests: ["Books"],
    lookingFor: ["All"],
    countryName: "India",
    city: "Mumbai",
    height: "",
    job: "",
    education: "",
    zodiac: "",
  };
  const draft = {
    fullName: profile.fullName,
    dateOfBirth,
    age: 1,
    gender: profile.gender,
    username: profile.username,
    photos: profile.photos,
    bio: profile.bio,
    interests: profile.interests,
    lookingFor: profile.lookingFor,
    city: profile.city,
    height: profile.height,
    job: profile.job,
    education: profile.education,
    zodiac: profile.zodiac,
  };
  return {
    profile,
    privateProfile: {
      phoneNumber: "+911234567890",
      email: "alice@example.com",
      countryName: "India",
      countryDialCode: "+91",
      countryFlag: "🇮🇳",
      selfieCapturedUrl: null,
      locationPermissionGranted: false,
      notificationsPermissionGranted: false,
      draft,
    },
  };
}

async function callSaveProfile(user, payload) {
  return requestJson(
    `http://${functionsHost}/${projectId}/asia-southeast1/saveMyProfile`,
    {
      method: "POST",
      headers: { Authorization: `Bearer ${user.idToken}` },
      body: JSON.stringify({ data: payload }),
    }
  );
}

async function getDocument(path, idToken) {
  return requestJson(`${firestoreBase}/${path}`, {
    headers: { Authorization: `Bearer ${idToken}` },
  });
}

test("saveMyProfile writes a server-validated profile, private data, and username atomically", async () => {
  const user = await createUser();
  const payload = validPayload();
  const { response, body } = await callSaveProfile(user, payload);
  assert.equal(response.ok, true, JSON.stringify(body));
  assert.equal(body.result.saved, true);
  assert.equal(body.result.age, 26);

  const [profile, userDocument, username] = await Promise.all([
    getDocument(`profiles/${user.localId}`, user.idToken),
    getDocument(`users/${user.localId}`, user.idToken),
    getDocument(`usernames/${payload.profile.username}`, user.idToken),
  ]);
  assert.equal(profile.response.ok, true, JSON.stringify(profile.body));
  assert.equal(profile.body.fields.age.integerValue, "26");
  assert.equal(profile.body.fields.isComplete.booleanValue, true);
  assert.equal(profile.body.fields.username.stringValue, payload.profile.username);
  assert.equal(userDocument.response.ok, true, JSON.stringify(userDocument.body));
  assert.equal(userDocument.body.fields.draft.mapValue.fields.age.integerValue, "26");
  assert.equal(username.body.fields.uid.stringValue, user.localId);
});

test("saveMyProfile rejects underage accounts without writing a profile", async () => {
  const user = await createUser();
  const payload = validPayload("Jan 1, 2010");
  const { response, body } = await callSaveProfile(user, payload);
  assert.equal(response.status, 400, JSON.stringify(body));
  assert.equal(body.error.status, "INVALID_ARGUMENT");

  const profile = await getDocument(`profiles/${user.localId}`, user.idToken);
  assert.equal(profile.response.status, 404);
});

test("saveMyProfile prevents another account from claiming an existing username", async () => {
  const firstUser = await createUser();
  const secondUser = await createUser();
  const sharedUsername = `u${firstUser.localId.replace(/[^a-z0-9]/gi, "").toLowerCase()}`;
  const payload = validPayload("Jan 1, 2000", sharedUsername);
  const firstSave = await callSaveProfile(firstUser, payload);
  assert.equal(firstSave.response.ok, true, JSON.stringify(firstSave.body));

  const secondPayload = validPayload("Jan 1, 2000", sharedUsername);
  secondPayload.profile.fullName = "Bob Example";
  secondPayload.profile.fullNameLower = "bob example";
  secondPayload.privateProfile.draft.fullName = "Bob Example";
  const secondSave = await callSaveProfile(secondUser, secondPayload);
  assert.equal(secondSave.response.status, 409, JSON.stringify(secondSave.body));
  assert.equal(secondSave.body.error.status, "ALREADY_EXISTS");
});
