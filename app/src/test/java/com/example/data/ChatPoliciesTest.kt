package com.example.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPoliciesTest {
    @Test
    fun messageLengthMatchesFirestoreLimit() {
        assertTrue(ChatPolicies.isMessageLengthValid("x".repeat(ChatPolicies.MAX_MESSAGE_LENGTH)))
        assertFalse(ChatPolicies.isMessageLengthValid("x".repeat(ChatPolicies.MAX_MESSAGE_LENGTH + 1)))
    }

    @Test
    fun messageNotificationMustBelongToSignedInUser() {
        assertTrue(ChatPolicies.isNotificationForUser("recipient", "recipient"))
        assertFalse(ChatPolicies.isNotificationForUser("other-user", "recipient"))
        assertFalse(ChatPolicies.isNotificationForUser(null, "recipient"))
    }

    @Test
    fun autoScrollOnlyWhenAlreadyNearLatestMessage() {
        assertTrue(ChatPolicies.shouldAutoScrollToLatest(0))
        assertTrue(ChatPolicies.shouldAutoScrollToLatest(1))
        assertFalse(ChatPolicies.shouldAutoScrollToLatest(2))
    }
}
