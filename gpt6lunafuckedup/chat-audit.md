# End-to-end chat audit

**Scope:** Chat screens and actions, ViewModel, Firebase repository, Firestore and
Storage rules, Cloud Functions, and message notifications.

**Fix status:** Code-level fixes were made for all numbered findings by
**GPT-6 LUNA**. Backend changes are not deployed yet, and no physical-device
workflow was tested. Finding 2 has an explicit limitation for previously
issued Firebase Storage download URLs.

## High priority

### 1. Chat notification content can leak across account switches — FIXED BY GPT-6 LUNA

A callable now unregisters the current user's device token during sign-out.
Message notification payloads include the recipient ID, and the Android
messaging service discards notifications that do not match the signed-in user.
The recipient check protects users if remote token cleanup fails.

Evidence: [MainActivity.kt](../app/src/main/java/com/example/MainActivity.kt),
[CallPushTokenStore.kt](../app/src/main/java/com/example/calls/CallPushTokenStore.kt),
[SoulmateViewModel.kt](../app/src/main/java/com/example/ui/SoulmateViewModel.kt),
[index.ts](../functions/src/index.ts),
[CallMessagingService.kt](../app/src/main/java/com/example/calls/CallMessagingService.kt).

### 2. Blocking does not stop chat messages or attachment access — FIXED BY GPT-6 LUNA

Firestore rules now deny message creation, message reads, and message/unread
updates when either participant has blocked the other. Storage SDK reads and
writes are likewise denied for blocked pairs. Previously issued Firebase
Storage download URLs are bearer links and remain usable by anyone who already
has the URL; token migration or revocation is still needed to remove that
legacy limitation.

Evidence: [FirebaseRepository.kt](../app/src/main/java/com/example/data/FirebaseRepository.kt),
[firestore.rules](../firestore.rules),
[storage.rules](../storage.rules),
[index.ts](../functions/src/index.ts).

## Medium priority

### 3. Either participant can delete the other person's messages and files — FIXED BY GPT-6 LUNA

Firestore now restricts message deletion to the sender. Storage writes and
deletes now require the file name to belong to the authenticated user's
conversation-upload prefix.

Evidence: [firestore.rules](../firestore.rules),
[storage.rules](../storage.rules).

### 4. Audio and document attachments are not usable in the chat thread — FIXED BY GPT-6 LUNA

The thread now renders audio and document attachments as openable controls and
routes them to a compatible app. The microphone control opens the real
attachment picker instead of showing a fake recording toast.

Evidence: [SendAttachmentScreen.kt](../app/src/main/java/com/example/ui/screens/chats/SendAttachmentScreen.kt),
[FirebaseRepository.kt](../app/src/main/java/com/example/data/FirebaseRepository.kt),
[ChatThreadScreen.kt](../app/src/main/java/com/example/ui/screens/chats/ChatThreadScreen.kt).

### 5. The chat-thread video-call button is still a placeholder — FIXED BY GPT-6 LUNA

The chat-thread video button now opens the same existing video-call flow used by
Chat Info.

Evidence: [ChatThreadScreen.kt](../app/src/main/java/com/example/ui/screens/chats/ChatThreadScreen.kt),
[DashboardScreen.kt](../app/src/main/java/com/example/ui/screens/dashboard/DashboardScreen.kt).

### 6. Send failures can discard text or leave orphaned uploads — FIXED BY GPT-6 LUNA

Text and attachment captions now enforce the 2,000-character limit before
upload/send. The text draft remains until a send succeeds. Attachment sends
stay on screen on failure, and an uploaded object is deleted if its message
write fails; cleanup failures are surfaced.

Evidence: [ChatThreadScreen.kt](../app/src/main/java/com/example/ui/screens/chats/ChatThreadScreen.kt),
[SendAttachmentScreen.kt](../app/src/main/java/com/example/ui/screens/chats/SendAttachmentScreen.kt),
[SoulmateViewModel.kt](../app/src/main/java/com/example/ui/SoulmateViewModel.kt),
[FirebaseRepository.kt](../app/src/main/java/com/example/data/FirebaseRepository.kt),
[firestore.rules](../firestore.rules).

### 7. Message notifications do not navigate to the chat and may be delayed — FIXED BY GPT-6 LUNA

Tapping a message notification now carries the conversation ID through
`MainActivity` and opens the matching thread. Message pushes use high priority
so Android can deliver the user-visible notification promptly.

Evidence: [ChatNotificationManager.kt](../app/src/main/java/com/example/calls/ChatNotificationManager.kt),
[index.ts](../functions/src/index.ts).

### 8. Back navigation from chat sub-screens exits the conversation — FIXED BY GPT-6 LUNA

Back from Chat Info, Options, Report, or attachment selection now returns to
the conversation thread. Attachment selection returns to the thread only after
a successful send; failure retains the selection for retry.

Evidence: [DashboardScreen.kt](../app/src/main/java/com/example/ui/screens/dashboard/DashboardScreen.kt).

### 9. Every thread loads all messages and jumps to the latest on any new message — FIXED BY GPT-6 LUNA

The live listener now loads a bounded recent page, older messages load on
demand, and incoming messages only auto-scroll when the reader is already near
the latest message.

Evidence: [FirebaseRepository.kt](../app/src/main/java/com/example/data/FirebaseRepository.kt),
[ChatThreadScreen.kt](../app/src/main/java/com/example/ui/screens/chats/ChatThreadScreen.kt).

### 10. A compatibility rule allows changing a conversation preview without sending a message — FIXED BY GPT-6 LUNA

The legacy preview-only update rule was removed. Current message updates must
be backed by a newly created message document and transaction rules.

Evidence: [firestore.rules](../firestore.rules).

## Lower priority

### 11. Chat Info's “Shared Photos & Media” is not shared chat media — FIXED BY GPT-6 LUNA

Chat Info now queries and displays the conversation's actual shared
attachments. It shows an empty state when no attachments have been shared
instead of showing profile photos or unavailable-feature placeholders.

Evidence: [ChatInfoScreen.kt](../app/src/main/java/com/example/ui/screens/chats/ChatInfoScreen.kt).

### 12. No chat-specific automated test coverage was found — FIXED BY GPT-6 LUNA

Added Android tests for message length, notification-recipient validation, and
scroll behavior, plus Functions tests for notification mute/block/global
settings policy.

Evidence: [ExampleUnitTest.kt](../app/src/test/java/com/example/ExampleUnitTest.kt),
[ExampleRobolectricTest.kt](../app/src/test/java/com/example/ExampleRobolectricTest.kt).

## Validation and remaining rollout

Validation passed:

- `gradle :app:testDebugUnitTest --rerun-tasks`
- `cd functions && npm test` (TypeScript build and three notification policy tests)
- Firestore and Storage emulator requests returned expected `403` for unauthenticated reads.
- Firestore index JSON validation and `git diff --check`

The live message listener, read receipts/unread counts, typing indicators, and
per-chat mute behavior remain wired in source. Firestore rules, Storage rules,
indexes, and Functions changes still need deployment to affect the production
Firebase project. Authenticated rules behavior and the full call, notification,
attachment, and navigation flows still need device/emulator integration
testing. Previously issued Storage download URLs remain bearer links as noted
under finding 2.
