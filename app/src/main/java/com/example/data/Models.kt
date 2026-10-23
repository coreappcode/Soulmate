package com.example.data

import com.google.firebase.Timestamp

data class User(
    val id: String,
    val name: String,
    val username: String,
    val age: Int,
    val gender: String,
    val bio: String,
    val photos: List<String>,
    val distance: String,
    val country: String,
    val city: String = "",
    val verified: Boolean,
    val interests: List<String>,
    val lookingFor: List<String>,
    val height: String = "",
    val job: String = "",
    val education: String = "",
    val zodiac: String = "",
    val isOnline: Boolean = false,
    val showOnlineStatus: Boolean = true,
    val isNew: Boolean = false,
    val ageVisible: Boolean = true
) {
    val nameWithAge: String
        get() = if (ageVisible && age > 0) "$name, $age" else name

    val ageLabel: String
        get() = if (ageVisible && age > 0) "$age yrs" else ""
}

data class Country(
    val name: String,
    val flag: String,
    val dialCode: String
)

data class UserProfileState(
    val selectedCountry: Country = Country("India", "🇮🇳", "+91"),
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isPhoneVerified: Boolean = false,
    val fullName: String = "",
    val dateOfBirth: String = "",
    val age: Int = 0,
    val gender: String = "",
    val username: String = "",
    val photos: List<String> = emptyList(),
    val selfieCapturedUrl: String? = null,
    val isFaceVerified: Boolean = false,
    val bio: String = "",
    val selectedInterests: List<String> = emptyList(),
    val selectedLookingFor: List<String> = emptyList(),
    val city: String = "",
    val email: String = "",
    val height: String = "",
    val job: String = "",
    val education: String = "",
    val zodiac: String = "",
    val locationPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val isProfileLoading: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val dataError: String? = null
) {
    val formattedPhoneNumber: String
        get() = if (phoneNumber.isBlank()) {
            ""
        } else if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "${selectedCountry.dialCode} $phoneNumber"
        }

    val profileStrength: Float
        get() {
            var filled = 0f
            val total = 10f
            if (fullName.isNotBlank()) filled += 1f
            if (dateOfBirth.isNotBlank()) filled += 1f
            if (gender.isNotBlank()) filled += 1f
            if (username.isNotBlank()) filled += 1f
            if (photos.isNotEmpty()) filled += (photos.size.coerceAtMost(3) / 3f) * 2f
            if (bio.isNotBlank()) filled += 1f
            if (selectedInterests.size >= 3) filled += 1f
            if (selectedLookingFor.isNotEmpty()) filled += 1f
            if (isFaceVerified) filled += 1f
            return (filled / total).coerceIn(0f, 1f)
        }
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isOwn: Boolean,
    val timestamp: String,
    val status: MessageStatus = MessageStatus.SENT,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null,
    val createdAt: Timestamp? = null
)

data class ChatConversation(
    val id: String,
    val user: User,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val notificationsMuted: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val sharedAttachments: List<ChatMessage> = emptyList(),
    val hasOlderMessages: Boolean = false,
    val isLoadingOlderMessages: Boolean = false
)

data class FriendRequestItem(
    val id: String,
    val user: User,
    val mutualInterests: Int,
    val timestamp: String
)

data class SentFriendRequest(
    val id: String,
    val user: User,
    val status: String = "Pending",
    val timestamp: String
)
