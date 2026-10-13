package com.example.data

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
    val city: String = "Mumbai",
    val verified: Boolean,
    val interests: List<String>,
    val lookingFor: List<String>,
    val height: String = "5'10\"",
    val job: String = "Designer",
    val education: String = "Graduate",
    val zodiac: String = "Leo",
    val isOnline: Boolean = true,
    val isNew: Boolean = false
)

data class Country(
    val name: String,
    val flag: String,
    val dialCode: String
)

data class UserProfileState(
    val selectedCountry: Country = Country("India", "🇮🇳", "+91"),
    val phoneNumber: String = "9876543210",
    val otpCode: String = "",
    val isPhoneVerified: Boolean = true,
    val fullName: String = "Anya Sharma",
    val dateOfBirth: String = "14/08/1999",
    val age: Int = 25,
    val gender: String = "Woman",
    val username: String = "anya_sharma",
    val photos: List<String> = listOf(
        "https://picsum.photos/seed/anya1/600/800",
        "https://picsum.photos/seed/anya2/600/800",
        "https://picsum.photos/seed/anya3/600/800"
    ),
    val selfieCapturedUrl: String? = null,
    val isFaceVerified: Boolean = true,
    val bio: String = "Coffee enthusiast, indie music lover, and UI designer exploring cozy cafes. Always ready for impromptu weekend getaways and good conversation.",
    val selectedInterests: List<String> = listOf("Coffee", "Music", "Design", "Travel", "Photography"),
    val selectedLookingFor: List<String> = listOf("Relationship"),
    val city: String = "Mumbai",
    val email: String = "anya.sharma@example.com",
    val height: String = "5'6\"",
    val job: String = "Product Designer",
    val education: String = "NID Ahmedabad",
    val zodiac: String = "Leo",
    val locationPermissionGranted: Boolean = true,
    val notificationsPermissionGranted: Boolean = true
) {
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
    val status: MessageStatus = MessageStatus.READ,
    val imageUrl: String? = null
)

data class ChatConversation(
    val id: String,
    val user: User,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val messages: List<ChatMessage> = emptyList()
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
