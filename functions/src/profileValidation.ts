export class InvalidProfileError extends Error {}

type RecordValue = Record<string, unknown>;

const profileFields = [
  "fullName",
  "fullNameLower",
  "username",
  "usernameLower",
  "gender",
  "bio",
  "photos",
  "interests",
  "lookingFor",
  "countryName",
  "city",
  "height",
  "job",
  "education",
  "zodiac",
] as const;

const draftFields = [
  "fullName",
  "dateOfBirth",
  "age",
  "gender",
  "username",
  "photos",
  "bio",
  "interests",
  "lookingFor",
  "city",
  "height",
  "job",
  "education",
  "zodiac",
] as const;

const privateFields = [
  "phoneNumber",
  "email",
  "countryName",
  "countryDialCode",
  "countryFlag",
  "selfieCapturedUrl",
  "locationPermissionGranted",
  "notificationsPermissionGranted",
  "draft",
] as const;

function isRecord(value: unknown): value is RecordValue {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function hasExactKeys(value: RecordValue, fields: readonly string[]): boolean {
  const keys = Object.keys(value);
  return keys.length === fields.length && fields.every((field) => field in value);
}

function isText(value: unknown, min: number, max: number): value is string {
  return typeof value === "string" && value.trim().length >= min && value.length <= max;
}

function isStringList(value: unknown, min: number, max: number): value is string[] {
  return Array.isArray(value) &&
    value.length >= min &&
    value.length <= max &&
    value.every((item) => typeof item === "string" && item.trim().length > 0) &&
    new Set(value).size === value.length;
}

function ageFromDateOfBirth(value: unknown, now: Date): number | undefined {
  if (typeof value !== "string") return undefined;
  const match = /^([A-Z][a-z]{2}) (\d{1,2}), (\d{4})$/.exec(value);
  if (!match) return undefined;
  const month = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"].indexOf(match[1]);
  if (month < 0) return undefined;
  const day = Number(match[2]);
  const year = Number(match[3]);
  const birth = new Date(Date.UTC(year, month, day));
  if (birth.getUTCFullYear() !== year ||
      birth.getUTCMonth() !== month ||
      birth.getUTCDate() !== day ||
      birth.getTime() > now.getTime()) return undefined;

  let age = now.getUTCFullYear() - year;
  if (
    now.getUTCMonth() < month ||
    (now.getUTCMonth() === month && now.getUTCDate() < day)
  ) {
    age -= 1;
  }
  return age >= 18 && age <= 120 ? age : undefined;
}

export function validateProfilePayload(
  profileValue: unknown,
  privateValue: unknown,
  now: Date = new Date()
): { age: number; username: string; draft: RecordValue; privateProfile: RecordValue } {
  if (!isRecord(profileValue) || !isRecord(privateValue)) {
    throw new InvalidProfileError("Profile data is missing or malformed.");
  }
  if (!hasExactKeys(profileValue, profileFields) ||
      !hasExactKeys(privateValue, privateFields) ||
      !isRecord(privateValue.draft) ||
      !hasExactKeys(privateValue.draft, draftFields)
  ) {
    throw new InvalidProfileError("Profile data contains missing or unsupported fields.");
  }

  const profile = profileValue;
  const privateProfile = privateValue;
  const draft = privateProfile.draft as RecordValue;
  const age = ageFromDateOfBirth(draft.dateOfBirth, now);
  if (!age) throw new InvalidProfileError("Date of birth must be valid and indicate an age from 18 to 120.");

  const username = profile.username;
  if (
    !isText(profile.fullName, 2, 50) ||
    profile.fullNameLower !== (profile.fullName as string).trim().toLowerCase() ||
    typeof username !== "string" ||
    !/^[a-z0-9_]{5,45}$/.test(username) ||
    profile.usernameLower !== username ||
    !["Woman", "Man", "Non-binary"].includes(String(profile.gender)) ||
    !isText(profile.bio, 10, 500) ||
    !isStringList(profile.photos, 2, 6) ||
    !isStringList(profile.interests, 1, 10) ||
    !isStringList(profile.lookingFor, 1, 1) ||
    !["Men", "Women", "Non-binary", "All"].includes(String(profile.lookingFor[0])) ||
    !isText(profile.countryName, 1, 100) ||
    !isText(profile.city, 0, 100) ||
    !isText(profile.height, 0, 80) ||
    !isText(profile.job, 0, 80) ||
    !isText(profile.education, 0, 80) ||
    !isText(profile.zodiac, 0, 80)
  ) {
    throw new InvalidProfileError("One or more profile fields are invalid.");
  }

  const expectedDraft: RecordValue = {
    fullName: profile.fullName,
    dateOfBirth: draft.dateOfBirth,
    age,
    gender: profile.gender,
    username,
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
  if (draftFields.some((field) =>
    field !== "age" &&
    (["fullName", "bio", "city", "height", "job", "education", "zodiac"].includes(field)
      ? typeof draft[field] !== "string" ||
        (draft[field] as string).trim() !== expectedDraft[field]
      : JSON.stringify(draft[field]) !== JSON.stringify(expectedDraft[field]))
  )) {
    throw new InvalidProfileError("Private and public profile fields do not match.");
  }

  if (
    (privateProfile.phoneNumber !== null && typeof privateProfile.phoneNumber !== "string") ||
    typeof privateProfile.email !== "string" ||
    (privateProfile.email !== "" &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(privateProfile.email)) ||
    privateProfile.email.length > 254 ||
    privateProfile.countryName !== profile.countryName ||
    !isText(privateProfile.countryDialCode, 1, 8) ||
    !isText(privateProfile.countryFlag, 1, 16) ||
    (privateProfile.selfieCapturedUrl !== null &&
      typeof privateProfile.selfieCapturedUrl !== "string") ||
    typeof privateProfile.locationPermissionGranted !== "boolean" ||
    typeof privateProfile.notificationsPermissionGranted !== "boolean"
  ) {
    throw new InvalidProfileError("Private profile fields are invalid.");
  }

  const normalizedDraft = { ...expectedDraft };
  return {
    age,
    username,
    draft: normalizedDraft,
    privateProfile: { ...privateProfile, draft: normalizedDraft },
  };
}
