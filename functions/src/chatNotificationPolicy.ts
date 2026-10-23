export type ChatNotificationPolicyInput = {
  pushMessagesEnabled: boolean;
  conversationMuted: boolean;
  hasBlock: boolean;
};

export function shouldNotifyChatMessage(input: ChatNotificationPolicyInput): boolean {
  return input.pushMessagesEnabled && !input.conversationMuted && !input.hasBlock;
}
