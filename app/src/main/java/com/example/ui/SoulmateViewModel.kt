package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.data.Country
import com.example.data.FriendRequestItem
import com.example.data.MessageStatus
import com.example.data.MockData
import com.example.data.SentFriendRequest
import com.example.data.User
import com.example.data.UserProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class ExploreFilterState(
    val ageRange: ClosedFloatingPointRange<Float> = 18f..35f,
    val maxDistance: Float = 25f,
    val interestedIn: String = "All",
    val selectedInterests: List<String> = emptyList(),
    val onlineOnly: Boolean = false,
    val verifiedOnly: Boolean = false
)

data class MatchmakingState(
    val showMe: String = "Everyone",
    val ageRange: ClosedFloatingPointRange<Float> = 20f..32f,
    val distance: Float = 30f,
    val countryToggle: Boolean = false,
    val globalToggle: Boolean = false
)

data class PrivacySettingsState(
    val showOnlineStatus: Boolean = true,
    val showDistance: Boolean = true,
    val showAge: Boolean = true,
    val allowNonFriendsMessaging: Boolean = false,
    val sendReadReceipts: Boolean = true
)

data class NotificationSettingsState(
    val pushMatches: Boolean = true,
    val pushMessages: Boolean = true,
    val emailSummaries: Boolean = false,
    val soundAndVibration: Boolean = true,
    val marketingPromotions: Boolean = false
)

class SoulmateViewModel : ViewModel() {

    // User Profile State
    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    // Explore Deck State
    private val _exploreDeck = MutableStateFlow(MockData.users)
    val exploreDeck: StateFlow<List<User>> = _exploreDeck.asStateFlow()

    private val _swipedHistory = MutableStateFlow<List<User>>(emptyList())
    val swipedHistory: StateFlow<List<User>> = _swipedHistory.asStateFlow()

    private val _exploreSegment = MutableStateFlow(0) // 0: Nearby, 1: Country, 2: Global
    val exploreSegment: StateFlow<Int> = _exploreSegment.asStateFlow()

    private val _filterState = MutableStateFlow(ExploreFilterState())
    val filterState: StateFlow<ExploreFilterState> = _filterState.asStateFlow()

    private val _matchedUser = MutableStateFlow<User?>(null)
    val matchedUser: StateFlow<User?> = _matchedUser.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<User?>(null)
    val selectedUserProfile: StateFlow<User?> = _selectedUserProfile.asStateFlow()

    // Chats State
    private val _conversations = MutableStateFlow(MockData.conversations)
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _activeConversation = MutableStateFlow<ChatConversation?>(MockData.conversations.firstOrNull())
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    private val _blockedUsers = MutableStateFlow(MockData.blockedUsers)
    val blockedUsers: StateFlow<List<User>> = _blockedUsers.asStateFlow()

    // Friends State
    private val _friendsList = MutableStateFlow(MockData.users.take(6))
    val friendsList: StateFlow<List<User>> = _friendsList.asStateFlow()

    private val _friendRequests = MutableStateFlow(MockData.friendRequests)
    val friendRequests: StateFlow<List<FriendRequestItem>> = _friendRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow(MockData.sentRequests)
    val sentRequests: StateFlow<List<SentFriendRequest>> = _sentRequests.asStateFlow()

    private val _selectedFriendRequest = MutableStateFlow<FriendRequestItem?>(MockData.friendRequests.firstOrNull())
    val selectedFriendRequest: StateFlow<FriendRequestItem?> = _selectedFriendRequest.asStateFlow()

    // Settings State
    private val _matchmaking = MutableStateFlow(MatchmakingState())
    val matchmaking: StateFlow<MatchmakingState> = _matchmaking.asStateFlow()

    private val _privacySettings = MutableStateFlow(PrivacySettingsState())
    val privacySettings: StateFlow<PrivacySettingsState> = _privacySettings.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettingsState())
    val notificationSettings: StateFlow<NotificationSettingsState> = _notificationSettings.asStateFlow()

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    // --- Onboarding & Profile Setup Actions ---

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

    fun updateBio(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun updateHeight(height: String) {
        _uiState.update { it.copy(height = height) }
    }

    fun updateJob(job: String) {
        _uiState.update { it.copy(job = job) }
    }

    fun updateEducation(education: String) {
        _uiState.update { it.copy(education = education) }
    }

    fun updateZodiac(zodiac: String) {
        _uiState.update { it.copy(zodiac = zodiac) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
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

    fun reorderPhotos(fromIndex: Int, toIndex: Int) {
        _uiState.update {
            if (fromIndex in it.photos.indices && toIndex in it.photos.indices) {
                val mutable = it.photos.toMutableList()
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
                it.copy(photos = mutable)
            } else it
        }
    }

    fun setCapturedSelfie(url: String) {
        _uiState.update { it.copy(selfieCapturedUrl = url) }
    }

    fun setFaceVerified(verified: Boolean) {
        _uiState.update { it.copy(isFaceVerified = verified) }
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
            it.copy(selectedLookingFor = listOf(option))
        }
    }

    fun setLocationPermission(granted: Boolean) {
        _uiState.update { it.copy(locationPermissionGranted = granted) }
    }

    fun setNotificationsPermission(granted: Boolean) {
        _uiState.update { it.copy(notificationsPermissionGranted = granted) }
    }

    // --- Explore Actions ---

    fun setExploreSegment(index: Int) {
        _exploreSegment.value = index
    }

    fun swipePass(user: User) {
        _swipedHistory.update { listOf(user) + it }
        _exploreDeck.update { it.filter { item -> item.id != user.id } }
    }

    /**
     * Swiping right / Like:
     * 30% chance triggers It's A Match!
     */
    fun swipeLike(user: User, onMatch: (User) -> Unit) {
        _swipedHistory.update { listOf(user) + it }
        _exploreDeck.update { it.filter { item -> item.id != user.id } }

        val isMatch = Random.nextFloat() < 0.30f
        if (isMatch) {
            _matchedUser.value = user
            onMatch(user)
        }
    }

    fun rewind(): Boolean {
        val history = _swipedHistory.value
        if (history.isNotEmpty()) {
            val restored = history.first()
            _swipedHistory.value = history.drop(1)
            _exploreDeck.update { listOf(restored) + it }
            return true
        }
        return false
    }

    fun resetDeck() {
        _exploreDeck.value = MockData.users
        _swipedHistory.value = emptyList()
    }

    fun updateFilters(newFilters: ExploreFilterState) {
        _filterState.value = newFilters
    }

    fun setSelectedUserProfile(user: User?) {
        _selectedUserProfile.value = user
    }

    // --- Chats Actions ---

    fun selectConversation(conversation: ChatConversation) {
        _activeConversation.value = conversation
    }

    fun openConversationForUser(user: User): ChatConversation {
        val existing = _conversations.value.firstOrNull { it.user.id == user.id }
        if (existing != null) {
            _activeConversation.value = existing
            return existing
        }
        val newConv = ChatConversation(
            id = "conv_${user.id}",
            user = user,
            lastMessage = "Started a new conversation",
            timestamp = "Just now",
            unreadCount = 0,
            isTyping = false,
            messages = emptyList()
        )
        _conversations.update { listOf(newConv) + it }
        _activeConversation.value = newConv
        return newConv
    }

    fun sendMessage(text: String, imageUrl: String? = null) {
        val active = _activeConversation.value ?: return
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            text = text,
            isOwn = true,
            timestamp = "Just now",
            status = MessageStatus.SENT,
            imageUrl = imageUrl
        )
        val updatedMessages = active.messages + newMsg
        val updatedConv = active.copy(
            lastMessage = if (imageUrl != null && text.isBlank()) "Sent a photo" else text,
            timestamp = "Just now",
            messages = updatedMessages
        )
        _activeConversation.value = updatedConv
        _conversations.update { list ->
            list.map { if (it.id == updatedConv.id) updatedConv else it }
        }
    }

    fun clearActiveChat() {
        val active = _activeConversation.value ?: return
        val cleared = active.copy(messages = emptyList(), lastMessage = "Chat cleared")
        _activeConversation.value = cleared
        _conversations.update { list ->
            list.map { if (it.id == active.id) cleared else it }
        }
    }

    fun deleteActiveChat() {
        val active = _activeConversation.value ?: return
        _conversations.update { list -> list.filter { it.id != active.id } }
        _activeConversation.value = null
    }

    fun blockUser(user: User) {
        _blockedUsers.update { it + user }
        _conversations.update { list -> list.filter { it.user.id != user.id } }
        _friendsList.update { list -> list.filter { it.id != user.id } }
        _exploreDeck.update { list -> list.filter { it.id != user.id } }
    }

    fun unblockUser(user: User) {
        _blockedUsers.update { it.filter { it.id != user.id } }
    }

    // --- Friends Actions ---

    fun setSelectedFriendRequest(item: FriendRequestItem?) {
        _selectedFriendRequest.value = item
    }

    fun acceptFriendRequest(item: FriendRequestItem) {
        _friendRequests.update { it.filter { req -> req.id != item.id } }
        _friendsList.update { listOf(item.user) + it }
    }

    fun declineFriendRequest(item: FriendRequestItem) {
        _friendRequests.update { it.filter { req -> req.id != item.id } }
    }

    fun cancelSentRequest(sent: SentFriendRequest) {
        _sentRequests.update { it.filter { req -> req.id != sent.id } }
    }

    fun sendFriendRequest(user: User) {
        val sent = SentFriendRequest(
            id = "sent_${user.id}",
            user = user,
            status = "Pending",
            timestamp = "Just now"
        )
        _sentRequests.update { listOf(sent) + it }
    }

    fun removeFriend(user: User) {
        _friendsList.update { it.filter { f -> f.id != user.id } }
    }

    // --- Settings Actions ---

    fun updateMatchmaking(newState: MatchmakingState) {
        _matchmaking.value = newState
    }

    fun updatePrivacySettings(newState: PrivacySettingsState) {
        _privacySettings.value = newState
    }

    fun updateNotificationSettings(newState: NotificationSettingsState) {
        _notificationSettings.value = newState
    }

    fun setProUser(pro: Boolean) {
        _isProUser.value = pro
    }
}
