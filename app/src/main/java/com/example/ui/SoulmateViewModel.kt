package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Country
import com.example.data.MockData
import com.example.data.UserProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SoulmateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        UserProfileState(
            selectedCountry = MockData.popularCountries.first(),
            phoneNumber = "",
            fullName = "",
            dateOfBirth = "",
            gender = "",
            username = "",
            photos = listOf(
                "https://picsum.photos/seed/self1/400/600",
                "https://picsum.photos/seed/self2/400/600"
            ),
            bio = "Always ready for an impromptu adventure and a good flat white.",
            selectedInterests = listOf("Music", "Travel", "Coffee"),
            selectedLookingFor = listOf("Women")
        )
    )
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    fun selectCountry(country: Country) {
        _uiState.update { it.copy(selectedCountry = country) }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun updateOtp(otp: String) {
        _uiState.update { it.copy(otpCode = otp) }
    }

    fun setPhoneVerified(verified: Boolean) {
        _uiState.update { it.copy(isPhoneVerified = verified) }
    }

    fun updateFullName(name: String) {
        _uiState.update { it.copy(fullName = name) }
    }

    fun updateDateOfBirth(dob: String) {
        _uiState.update { it.copy(dateOfBirth = dob) }
    }

    fun updateGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun addPhoto(url: String) {
        _uiState.update {
            if (it.photos.size < 6) {
                it.copy(photos = it.photos + url)
            } else {
                it
            }
        }
    }

    fun removePhoto(index: Int) {
        _uiState.update {
            if (index in it.photos.indices) {
                val updated = it.photos.toMutableList().apply { removeAt(index) }
                it.copy(photos = updated)
            } else {
                it
            }
        }
    }

    fun setMainPhoto(index: Int) {
        _uiState.update {
            if (index in it.photos.indices && index != 0) {
                val mutable = it.photos.toMutableList()
                val selected = mutable.removeAt(index)
                mutable.add(0, selected)
                it.copy(photos = mutable)
            } else {
                it
            }
        }
    }

    fun setCapturedSelfie(url: String) {
        _uiState.update { it.copy(selfieCapturedUrl = url) }
    }

    fun setFaceVerified(verified: Boolean) {
        _uiState.update { it.copy(isFaceVerified = verified) }
    }

    fun updateBio(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun toggleInterest(interest: String) {
        _uiState.update {
            val current = it.selectedInterests
            val updated = if (current.contains(interest)) {
                current - interest
            } else {
                if (current.size < 10) current + interest else current
            }
            it.copy(selectedInterests = updated)
        }
    }

    fun toggleLookingFor(option: String) {
        _uiState.update {
            // Only can select one option, not more
            it.copy(selectedLookingFor = listOf(option))
        }
    }

    fun setLocationPermission(granted: Boolean) {
        _uiState.update { it.copy(locationPermissionGranted = granted) }
    }

    fun setNotificationsPermission(granted: Boolean) {
        _uiState.update { it.copy(notificationsPermissionGranted = granted) }
    }
}
