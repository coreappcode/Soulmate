const assert = require("node:assert/strict");
const { test } = require("node:test");
const { validateProfilePayload } = require("../lib/profileValidation.js");

const now = new Date("2026-10-24T12:00:00.000Z");

function validPayload() {
  const profile = {
    fullName: "Alice Example",
    fullNameLower: "alice example",
    username: "alice_example",
    usernameLower: "alice_example",
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
    dateOfBirth: "Jan 1, 2000",
    age: 26,
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
  const privateProfile = {
    phoneNumber: "+911234567890",
    email: "alice@example.com",
    countryName: "India",
    countryDialCode: "+91",
    countryFlag: "🇮🇳",
    selfieCapturedUrl: null,
    locationPermissionGranted: false,
    notificationsPermissionGranted: false,
    draft,
  };
  return { profile, privateProfile };
}

test("profile payload validates and derives age from the private birth date", () => {
  const { profile, privateProfile } = validPayload();
  privateProfile.draft.age = 1;
  const validated = validateProfilePayload(profile, privateProfile, now);
  assert.equal(validated.age, 26);
  assert.equal(validated.draft.age, 26);
});

test("profile payload normalizes client text while keeping private and public values aligned", () => {
  const { profile, privateProfile } = validPayload();
  privateProfile.draft.fullName = ` ${profile.fullName} `;
  privateProfile.draft.bio = ` ${profile.bio} `;
  privateProfile.draft.city = ` ${profile.city} `;
  const validated = validateProfilePayload(profile, privateProfile, now);
  assert.equal(validated.draft.fullName, profile.fullName);
  assert.equal(validated.draft.bio, profile.bio);
  assert.equal(validated.draft.city, profile.city);
});

test("profile payload rejects underage, future, and malformed birth dates", () => {
  for (const dateOfBirth of ["Jan 1, 2010", "Jan 1, 2099", "Feb 30, 2000", "Jan 1, 2000 trailing"]) {
    const { profile, privateProfile } = validPayload();
    privateProfile.draft.dateOfBirth = dateOfBirth;
    assert.throws(
      () => validateProfilePayload(profile, privateProfile, now),
      /Date of birth/
    );
  }
});

test("profile payload rejects mismatched private/public values and invalid constraints", () => {
  const mismatched = validPayload();
  mismatched.privateProfile.draft.fullName = "Different Name";
  assert.throws(
    () => validateProfilePayload(mismatched.profile, mismatched.privateProfile, now),
    /do not match/
  );

  const invalidBio = validPayload();
  invalidBio.profile.bio = "";
  assert.throws(
    () => validateProfilePayload(invalidBio.profile, invalidBio.privateProfile, now),
    /fields are invalid/
  );
});
