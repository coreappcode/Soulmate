package com.example.data

object ChatPolicies {
    const val MAX_MESSAGE_LENGTH = 2_000
    const val MESSAGE_PAGE_SIZE = 50

    fun isMessageLengthValid(text: String): Boolean = text.length <= MAX_MESSAGE_LENGTH

    fun isNotificationForUser(currentUid: String?, recipientId: String?): Boolean =
        currentUid != null && recipientId == currentUid

    fun shouldAutoScrollToLatest(firstVisibleItemIndex: Int): Boolean =
        firstVisibleItemIndex <= 1
}
