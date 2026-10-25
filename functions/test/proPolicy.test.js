const assert = require("node:assert/strict");
const { test } = require("node:test");
const {
  canStartCall,
  canRecordSwipe,
  FREE_DAILY_SWIPE_LIMIT,
  hasActiveProSubscription,
  remainingFreeSwipes,
  utcDayKey,
} = require("../lib/proPolicy.js");

test("Pro access accepts active, canceled-until-expiry, and grace subscriptions", () => {
  const expiry = 10_000;
  for (const state of [
    "SUBSCRIPTION_STATE_ACTIVE",
    "SUBSCRIPTION_STATE_CANCELED",
    "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
  ]) {
    assert.equal(hasActiveProSubscription(true, state, expiry, 9_999), true);
  }
});

test("Pro access rejects paused, expired, and unverified subscriptions", () => {
  assert.equal(hasActiveProSubscription(true, "SUBSCRIPTION_STATE_PAUSED", 10_000, 1), false);
  assert.equal(hasActiveProSubscription(true, "SUBSCRIPTION_STATE_ACTIVE", 1_000, 1_000), false);
  assert.equal(hasActiveProSubscription(false, "SUBSCRIPTION_STATE_ACTIVE", 10_000, 1), false);
});

test("free users may swipe 20 profiles per day and cannot exceed the limit", () => {
  assert.equal(FREE_DAILY_SWIPE_LIMIT, 20);
  assert.equal(canRecordSwipe(false, 19, false), true);
  assert.equal(canRecordSwipe(false, 20, false), false);
  assert.equal(remainingFreeSwipes(false, 20), 0);
});

test("Pro users are unlimited and existing swipe retries do not consume another slot", () => {
  assert.equal(canRecordSwipe(true, 20_000, false), true);
  assert.equal(canRecordSwipe(false, 20, true), true);
  assert.equal(remainingFreeSwipes(true, 20_000), null);
});

test("audio and video calls require active Pro status for both participants", () => {
  assert.equal(canStartCall(true, true), true);
  assert.equal(canStartCall(true, false), false);
  assert.equal(canStartCall(false, true), false);
  assert.equal(canStartCall(false, false), false);
});

test("daily swipe window changes at midnight UTC", () => {
  const beforeMidnight = Date.parse("2026-10-24T23:59:59.999Z");
  const midnight = Date.parse("2026-10-25T00:00:00.000Z");
  assert.equal(utcDayKey(beforeMidnight), "2026-10-24");
  assert.equal(utcDayKey(midnight), "2026-10-25");
});
