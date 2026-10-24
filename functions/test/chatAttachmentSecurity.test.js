const assert = require("node:assert/strict");
const { test } = require("node:test");
const {
  chatAttachmentDownloadToken,
  firebaseDownloadUrl,
  storageObjectPathFromDownloadUrl,
} = require("../lib/chatAttachmentSecurity.js");

test("Firebase Storage URLs are scoped to the expected bucket and conversation", () => {
  const path = "conversations/alice_bob/alice_photo.jpg";
  const url = firebaseDownloadUrl("soulmate.appspot.com", path, "old-token");

  assert.equal(
    storageObjectPathFromDownloadUrl(url, "soulmate.appspot.com", "alice_bob"),
    path
  );
  assert.equal(
    storageObjectPathFromDownloadUrl(url, "other.appspot.com", "alice_bob"),
    undefined
  );
  assert.equal(
    storageObjectPathFromDownloadUrl(url, "soulmate.appspot.com", "alice_eve"),
    undefined
  );
});

test("non-Firebase and malformed attachment URLs are not treated as Storage objects", () => {
  assert.equal(
    storageObjectPathFromDownloadUrl("https://example.com/photo.jpg", "bucket", "pair"),
    undefined
  );
  assert.equal(
    storageObjectPathFromDownloadUrl("not a URL", "bucket", "pair"),
    undefined
  );
});

test("block-state tokens are deterministic per private seed and object path", () => {
  const first = chatAttachmentDownloadToken("secret-seed", "conversations/pair/a.jpg");
  assert.equal(first, chatAttachmentDownloadToken("secret-seed", "conversations/pair/a.jpg"));
  assert.notEqual(first, chatAttachmentDownloadToken("new-seed", "conversations/pair/a.jpg"));
});
