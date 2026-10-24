export type ChatNotificationPolicyInput = {
  pushMessagesEnabled: boolean;
  conversationMuted: boolean;
  hasBlock: boolean;
};

export function shouldNotifyChatMessage(input: ChatNotificationPolicyInput): boolean {
  return input.pushMessagesEnabled && !input.conversationMuted && !input.hasBlock;
}

export function shouldRetryChatNotificationFailure(errorCode: string | undefined): boolean {
  return errorCode === "messaging/server-unavailable" ||
    errorCode === "messaging/internal-error" ||
    errorCode === "messaging/message-rate-exceeded" ||
    errorCode === "messaging/quota-exceeded";
}
