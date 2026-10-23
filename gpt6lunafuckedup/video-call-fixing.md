# Chat-thread video call fix

The video-call button in `ChatThreadScreen` is still a placeholder: it displays
“Video calls are not available yet.” The call flow is already wired from
`ChatInfoScreen` through `DashboardScreen.openCall`.

To fix the chat-thread entry point:

1. Add an `onVideoCall` callback to `ChatThreadScreen`.
2. Replace the placeholder toast with that callback.
3. Wire it in `DashboardScreen` to `openCall(activeConv.user, "video")`.
4. Verify that tapping the thread button starts the existing video-call flow,
   including its permission, connection, and error handling.
