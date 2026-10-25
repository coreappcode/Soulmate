export const FREE_DAILY_SWIPE_LIMIT = 20;

const PRO_STATES = new Set([
  "SUBSCRIPTION_STATE_ACTIVE",
  "SUBSCRIPTION_STATE_CANCELED",
  "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
]);

export function hasActiveProSubscription(
  isPro: boolean,
  subscriptionState: string | undefined,
  expiresAtMillis: number,
  now: number,
): boolean {
  return isPro &&
    PRO_STATES.has(subscriptionState ?? "") &&
    Number.isFinite(expiresAtMillis) &&
    expiresAtMillis > now;
}

export function canRecordSwipe(
  isPro: boolean,
  swipesUsedToday: number,
  swipeAlreadyExists: boolean,
): boolean {
  return isPro || swipeAlreadyExists || swipesUsedToday < FREE_DAILY_SWIPE_LIMIT;
}

export function remainingFreeSwipes(isPro: boolean, swipesUsedToday: number): number | null {
  return isPro ? null : Math.max(0, FREE_DAILY_SWIPE_LIMIT - swipesUsedToday);
}

export function canStartCall(callerIsPro: boolean, calleeIsPro: boolean): boolean {
  return callerIsPro && calleeIsPro;
}

export function utcDayKey(now: number): string {
  return new Date(now).toISOString().slice(0, 10);
}
