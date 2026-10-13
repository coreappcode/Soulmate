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
    val verified: Boolean,
    val interests: List<String>,
    val lookingFor: List<String>
)

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
    val gender: String = "",
    val username: String = "",
    val photos: List<String> = emptyList(),
    val selfieCapturedUrl: String? = null,
    val isFaceVerified: Boolean = false,
    val bio: String = "",
    val selectedInterests: List<String> = emptyList(),
    val selectedLookingFor: List<String> = emptyList(),
    val locationPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false
)
