import { createHash } from "node:crypto";

export function storageObjectPathFromDownloadUrl(
  downloadUrl: string,
  bucketName: string,
  conversationId: string
): string | undefined {
  let parsed: URL;
  try {
    parsed = new URL(downloadUrl);
  } catch {
    return undefined;
  }
  if (parsed.hostname !== "firebasestorage.googleapis.com") return undefined;
  const match = /^\/v0\/b\/([^/]+)\/o\/(.+)$/.exec(parsed.pathname);
  if (!match) return undefined;

  let urlBucket: string;
  let objectPath: string;
  try {
    urlBucket = decodeURIComponent(match[1]);
    objectPath = decodeURIComponent(match[2]);
  } catch {
    return undefined;
  }
  if (urlBucket !== bucketName || !objectPath.startsWith(`conversations/${conversationId}/`)) {
    return undefined;
  }
  return objectPath;
}

export function firebaseDownloadUrl(bucketName: string, objectPath: string, token: string): string {
  return `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodeURIComponent(objectPath)}?alt=media&token=${token}`;
}

export function chatAttachmentDownloadToken(tokenSeed: string, objectPath: string): string {
  return createHash("sha256")
    .update(`${tokenSeed}:${objectPath}`)
    .digest("hex");
}
