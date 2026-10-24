const assert = require("node:assert/strict");
const { test } = require("node:test");
const {
  shouldNotifyChatMessage,
  shouldRetryChatNotificationFailure,
} = require("../lib/chatNotificationPolicy.js");

test("chat notifications require global messages enabled", () => {
  assert.equal(shouldNotifyChatMessage({
    pushMessagesEnabled: false,
    conversationMuted: false,
    hasBlock: false,
  }), false);
});

test("muted or blocked conversations do not send notifications", () => {
  assert.equal(shouldNotifyChatMessage({
    pushMessagesEnabled: true,
    conversationMuted: true,
    hasBlock: false,
  }), false);
  assert.equal(shouldNotifyChatMessage({
    pushMessagesEnabled: true,
    conversationMuted: false,
    hasBlock: true,
  }), false);
});

test("enabled and unmuted unblocked conversations send notifications", () => {
  assert.equal(shouldNotifyChatMessage({
    pushMessagesEnabled: true,
    conversationMuted: false,
    hasBlock: false,
  }), true);
});

test("temporary FCM failures are retried but invalid tokens are not", () => {
  assert.equal(shouldRetryChatNotificationFailure("messaging/server-unavailable"), true);
  assert.equal(shouldRetryChatNotificationFailure("messaging/internal-error"), true);
  assert.equal(shouldRetryChatNotificationFailure("messaging/invalid-registration-token"), false);
  assert.equal(shouldRetryChatNotificationFailure(undefined), false);
});
