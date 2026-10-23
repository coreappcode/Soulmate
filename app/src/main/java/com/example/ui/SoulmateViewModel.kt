package com.example.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.data.ChatPolicies
import com.example.data.ConversationSummary
import com.example.data.Country
import com.example.data.FriendRequestItem
import com.example.data.FirebaseDashboardData
import com.example.data.FirebaseListenerRegistration
import com.example.data.FirebasePresenceRepository
import com.example.data.FirebaseRepository
import com.example.data.MessageStatus
import com.example.data.SentFriendRequest
import com.example.data.User
import com.example.data.UserProfileState
import com.example.data.CountryCatalog
import com.example.data.UsernameTakenException
import com.example.calls.CallPushTokenStore
import com.example.utils.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import java.util.Locale

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

    data class PhoneAuthUiState(
        val isSendingCode: Boolean = false,
        val isVerifying: Boolean = false,
        val isPhoneUpdate: Boolean = false,
        val isPhoneUpdateComplete: Boolean = false,
        val errorMessage: String? = null,
        val codeSentVersion: Int = 0
    )

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository(firebaseAuth)
    private val presenceRepository = FirebasePresenceRepository(firebaseAuth)
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var dataLoadJob: Job? = null
    private var profileSaveJob: Job? = null
    private var loadedUid: String? = null
    private var appInForeground = false
    private var privacySettingsLoaded = false
    private var presenceListening = false
    private var onlineUserIds = emptySet<String>()
    private var presenceSyncJob: Job? = null
    private var conversationSummaryListener: FirebaseListenerRegistration? = null
    private var activeConversationListener: FirebaseListenerRegistration? = null
    private var conversationRefreshJob: Job? = null
    private var typingRefreshJob: Job? = null
    private var typingExpiryJob: Job? = null
    private var typingConversationId: String? = null
    private var typingGeneration = 0L
    private var activeTypingExpiryMillis: Long? = null
    private var lastReadRequestAt: Timestamp? = null
    private var unreadClearRequested = false
    private var olderMessages = emptyList<ChatMessage>()
    private var hasOlderMessagesAvailable = false
    private val summaryTypingExpiries = mutableMapOf<String, Long>()
    private val summaryTypingExpiryJobs = mutableMapOf<String, Job>()
    private val _phoneAuthState = MutableStateFlow(PhoneAuthUiState())
    val phoneAuthState: StateFlow<PhoneAuthUiState> = _phoneAuthState.asStateFlow()
    private var pendingPhoneNumberForProfile: String? = null

    // User Profile State
    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()

    private val _usernameSearchResults = MutableStateFlow<List<User>>(emptyList())
    val usernameSearchResults: StateFlow<List<User>> = _usernameSearchResults.asStateFlow()

    // Explore Deck State
    private val _exploreDeck = MutableStateFlow(emptyList<User>())
    val exploreDeck: StateFlow<List<User>> = _exploreDeck.asStateFlow()
    private val _availableProfiles = MutableStateFlow(emptyList<User>())

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
    private val _conversations = MutableStateFlow(emptyList<ChatConversation>())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    private val _blockedUsers = MutableStateFlow(emptyList<User>())
    val blockedUsers: StateFlow<List<User>> = _blockedUsers.asStateFlow()

    // Friends State
    private val _friendsList = MutableStateFlow(emptyList<User>())
    val friendsList: StateFlow<List<User>> = _friendsList.asStateFlow()

    private val _friendRequests = MutableStateFlow(emptyList<FriendRequestItem>())
    val friendRequests: StateFlow<List<FriendRequestItem>> = _friendRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow(emptyList<SentFriendRequest>())
    val sentRequests: StateFlow<List<SentFriendRequest>> = _sentRequests.asStateFlow()

    private val _selectedFriendRequest = MutableStateFlow<FriendRequestItem?>(null)
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

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user == null) {
            stopConversationSummaryListener()
            stopActiveConversationObservation(clearActive = true, publishTypingStop = false)
            loadedUid = null
            presenceRepository.stopObserving()
            presenceListening = false
            privacySettingsLoaded = false
            onlineUserIds = emptySet()
            clearRemoteState()
        } else if (loadedUid != user.uid) {
            stopConversationSummaryListener()
            stopActiveConversationObservation(clearActive = true, publishTypingStop = false)
            presenceRepository.stopObserving()
            presenceListening = false
            privacySettingsLoaded = false
            onlineUserIds = emptySet()
            loadedUid = user.uid
            loadSignedInData(user.uid)
        }
    }

    private fun applyCurrentFilters() {
        val profile = _uiState.value
        val filters = _filterState.value
        val matchmaking = _matchmaking.value
        val blockedIds = _blockedUsers.value.map { it.id }.toSet()
        val nearbyCountry = profile.selectedCountry.name
        val nearbyCity = profile.city.trim()
        val viewerGender = profile.gender

        _exploreDeck.value = _availableProfiles.value.filter { user ->
            val sameCountry = user.country.equals(nearbyCountry, ignoreCase = true)
            val sameCity = nearbyCity.isNotBlank() &&
                user.city.equals(nearbyCity, ignoreCase = true)
            val segmentMatches = when (_exploreSegment.value) {
                0 -> if (nearbyCity.isNotBlank()) sameCity else sameCountry
                1 -> sameCountry
                else -> true
            }
            val matchmakingMatches = matchmaking.globalToggle ||
                !matchmaking.countryToggle || sameCountry
            val viewerLooksForUser = profile.selectedLookingFor.isEmpty() ||
                profile.selectedLookingFor.any { matchesGender(it, user.gender) }
            val userLooksForViewer = user.lookingFor.isEmpty() ||
                user.lookingFor.any { matchesGender(it, viewerGender) }

            user.id !in blockedIds &&
                (!user.ageVisible ||
                    (user.age.toFloat() in filters.ageRange &&
                        user.age.toFloat() in matchmaking.ageRange)) &&
                matchesGender(filters.interestedIn, user.gender) &&
                matchesGender(matchmaking.showMe, user.gender) &&
                (filters.selectedInterests.isEmpty() ||
                    user.interests.any { interest ->
                        filters.selectedInterests.any { it.equals(interest, ignoreCase = true) }
                    }) &&
                viewerLooksForUser &&
                userLooksForViewer &&
                (!filters.onlineOnly || user.isOnline) &&
                (!filters.verifiedOnly || user.verified) &&
                segmentMatches &&
                matchmakingMatches
        }
    }

    private fun matchesGender(preference: String, gender: String): Boolean {
        if (preference == "All" || preference == "Everyone") return true
        val normalized = gender.trim().lowercase(Locale.ROOT)
        return when (preference.lowercase(Locale.ROOT)) {
            "women", "woman" -> normalized in setOf("woman", "women", "female")
            "men", "man" -> normalized in setOf("man", "men", "male")
            else -> normalized == preference.trim().lowercase(Locale.ROOT)
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        dataLoadJob?.cancel()
        profileSaveJob?.cancel()
        presenceSyncJob?.cancel()
        presenceRepository.stopObserving()
        stopConversationSummaryListener()
        stopActiveConversationObservation(clearActive = true, publishTypingStop = false)
        super.onCleared()
    }

    fun setAppInForeground(isForeground: Boolean) {
        if (appInForeground == isForeground) return
        appInForeground = isForeground
        if (isForeground) {
            startPresenceIfReady()
            if (loadedUid != null && !_uiState.value.isProfileLoading) {
                startConversationSummaryListener()
                _activeConversation.value?.let(::observeActiveConversation)
            }
        } else {
            stopConversationSummaryListener()
            presenceRepository.stopObserving()
            presenceListening = false
            onlineUserIds = emptySet()
            applyPresenceToUsers()
            syncPresencePublication()
            stopActiveConversationObservation(clearActive = false)
        }
    }

    private fun startPresenceIfReady() {
        if (!appInForeground || !privacySettingsLoaded || firebaseAuth.currentUser == null) return
        if (!presenceListening) {
            presenceListening = true
            presenceRepository.startObserving(
                onOnlineUsersChanged = { ids ->
                    onlineUserIds = ids
                    applyPresenceToUsers()
                },
                onError = ::reportDataError
            )
        }
        syncPresencePublication()
    }

    private fun syncPresencePublication() {
        presenceSyncJob?.cancel()
        val shouldPublish = appInForeground &&
            privacySettingsLoaded &&
            _privacySettings.value.showOnlineStatus
        presenceSyncJob = viewModelScope.launch {
            try {
                presenceRepository.setPublishingEnabled(shouldPublish)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    private fun startConversationSummaryListener() {
        if (!appInForeground || conversationSummaryListener != null) return
        val uid = firebaseAuth.currentUser?.uid ?: return
        conversationSummaryListener = repository.observeConversationSummaries(
            onUpdate = { summaries ->
                if (firebaseAuth.currentUser?.uid == uid) applyConversationSummaries(summaries)
            },
            onError = { exception ->
                if (firebaseAuth.currentUser?.uid == uid) reportDataError(exception)
            }
        )
    }

    private fun stopConversationSummaryListener() {
        conversationSummaryListener?.remove()
        conversationSummaryListener = null
        conversationRefreshJob?.cancel()
        conversationRefreshJob = null
        summaryTypingExpiryJobs.values.forEach(Job::cancel)
        summaryTypingExpiryJobs.clear()
        summaryTypingExpiries.clear()
    }

    private fun applyConversationSummaries(summaries: List<ConversationSummary>) {
        val blockedIds = _blockedUsers.value.map { it.id }.toSet()
        val visibleSummaries = summaries.filterNot { it.userId in blockedIds }
        val currentById = _conversations.value.associateBy { it.id }
        val hasNewConversation = visibleSummaries.any { it.id !in currentById }
        if (hasNewConversation && conversationRefreshJob?.isActive != true) {
            conversationRefreshJob = viewModelScope.launch {
                try {
                    refreshDashboardData()
                } catch (exception: Exception) {
                    reportDataError(exception)
                }
            }
        }

        _conversations.value = visibleSummaries.mapNotNull { summary ->
            currentById[summary.id]?.copy(
                lastMessage = summary.lastMessage,
                timestamp = summary.timestamp,
                unreadCount = summary.unreadCount,
                isTyping = summary.isTyping,
                notificationsMuted = summary.notificationsMuted
            )
        }
        _activeConversation.update { active ->
            val summary = visibleSummaries.firstOrNull { it.id == active?.id }
            if (active == null || summary == null) active else active.copy(
                lastMessage = summary.lastMessage,
                timestamp = summary.timestamp,
                unreadCount = summary.unreadCount,
                isTyping = summary.isTyping,
                notificationsMuted = summary.notificationsMuted
            )
        }

        visibleSummaries.forEach { summary ->
            if (summaryTypingExpiries[summary.id] == summary.typingExpiresAtMillis) return@forEach
            summaryTypingExpiryJobs.remove(summary.id)?.cancel()
            val expiresAt = summary.typingExpiresAtMillis
            if (expiresAt == null) {
                summaryTypingExpiries.remove(summary.id)
            } else {
                summaryTypingExpiries[summary.id] = expiresAt
                summaryTypingExpiryJobs[summary.id] = viewModelScope.launch {
                    delay((expiresAt - System.currentTimeMillis()).coerceAtLeast(0L))
                    if (summaryTypingExpiries[summary.id] == expiresAt) {
                        summaryTypingExpiries.remove(summary.id)
                        summaryTypingExpiryJobs.remove(summary.id)
                        _conversations.update { conversations ->
                            conversations.map {
                                if (it.id == summary.id) it.copy(isTyping = false) else it
                            }
                        }
                        _activeConversation.update {
                            if (it?.id == summary.id) it.copy(isTyping = false) else it
                        }
                    }
                }
            }
        }
    }

    private fun observeActiveConversation(conversation: ChatConversation) {
        if (typingConversationId != null && typingConversationId != conversation.id) {
            stopTypingStatus()
        }
        activeConversationListener?.remove()
        activeConversationListener = null
        typingExpiryJob?.cancel()
        activeTypingExpiryMillis = null
        lastReadRequestAt = null
        unreadClearRequested = false
        activeConversationListener = repository.observeConversation(
            conversationId = conversation.id,
            onUpdate = { liveState ->
                if (_activeConversation.value?.id != conversation.id) return@observeConversation
                if (olderMessages.isEmpty()) {
                    hasOlderMessagesAvailable = liveState.hasOlderMessages
                }
                val messages = mergeChatMessages(olderMessages, liveState.messages)
                _activeConversation.update { active ->
                    if (active?.id != conversation.id) active else active.copy(
                        messages = messages,
                        hasOlderMessages = hasOlderMessagesAvailable,
                        unreadCount = liveState.unreadCount,
                        isTyping = liveState.isTyping
                    )
                }
                _conversations.update { conversations ->
                    conversations.map {
                        if (it.id == conversation.id) it.copy(
                            messages = messages,
                            hasOlderMessages = hasOlderMessagesAvailable,
                            unreadCount = liveState.unreadCount,
                            isTyping = liveState.isTyping
                        ) else it
                    }
                }

                if (activeTypingExpiryMillis != liveState.typingExpiresAtMillis) {
                    activeTypingExpiryMillis = liveState.typingExpiresAtMillis
                    typingExpiryJob?.cancel()
                    val expiry = liveState.typingExpiresAtMillis
                    if (expiry != null) {
                        typingExpiryJob = viewModelScope.launch {
                            delay((expiry - System.currentTimeMillis()).coerceAtLeast(0L))
                            if (
                                _activeConversation.value?.id == conversation.id &&
                                activeTypingExpiryMillis == expiry
                            ) {
                                activeTypingExpiryMillis = null
                                _activeConversation.update {
                                    if (it?.id == conversation.id) it.copy(isTyping = false) else it
                                }
                                _conversations.update { conversations ->
                                    conversations.map {
                                        if (it.id == conversation.id) it.copy(isTyping = false) else it
                                    }
                                }
                            }
                        }
                    }
                }

                val newestIncoming = liveState.latestIncomingMessageAt
                if (
                    appInForeground &&
                    newestIncoming != null &&
                    lastReadRequestAt?.let { newestIncoming > it } != false
                ) {
                    lastReadRequestAt = newestIncoming
                    viewModelScope.launch {
                        try {
                            repository.markConversationRead(
                                conversation.id,
                                newestIncoming,
                                sendReadReceipt = _privacySettings.value.sendReadReceipts
                            )
                        } catch (exception: Exception) {
                            if (lastReadRequestAt == newestIncoming) lastReadRequestAt = null
                            reportDataError(exception)
                        }
                    }
                } else if (
                    appInForeground &&
                    liveState.unreadCount > 0 &&
                    !unreadClearRequested
                ) {
                    unreadClearRequested = true
                    viewModelScope.launch {
                        try {
                            repository.markConversationRead(
                                conversation.id,
                                null,
                                sendReadReceipt = _privacySettings.value.sendReadReceipts
                            )
                        } catch (exception: Exception) {
                            unreadClearRequested = false
                            reportDataError(exception)
                        }
                    }
                }
            },
            onError = ::reportDataError
        )
    }

    private fun stopActiveConversationObservation(
        clearActive: Boolean = true,
        publishTypingStop: Boolean = true
    ) {
        activeConversationListener?.remove()
        activeConversationListener = null
        typingExpiryJob?.cancel()
        typingExpiryJob = null
        activeTypingExpiryMillis = null
        if (publishTypingStop) {
            stopTypingStatus()
        } else {
            typingConversationId = null
            typingGeneration++
            typingRefreshJob?.cancel()
            typingRefreshJob = null
        }
        if (clearActive) _activeConversation.value = null
    }

    fun updateChatTyping(isTyping: Boolean) {
        val conversationId = _activeConversation.value?.id ?: return
        if (!isTyping) {
            if (typingConversationId == conversationId) stopTypingStatus()
            return
        }
        if (typingConversationId == conversationId && typingRefreshJob?.isActive == true) return
        stopTypingStatus()
        typingGeneration++
        val generation = typingGeneration
        typingConversationId = conversationId
        typingRefreshJob = viewModelScope.launch {
            try {
                while (
                    typingConversationId == conversationId &&
                    typingGeneration == generation
                ) {
                    repository.setTypingStatus(conversationId, true)
                    delay(3_000L)
                }
            } catch (exception: Exception) {
                if (typingConversationId == conversationId) {
                    typingConversationId = null
                    typingGeneration++
                }
                reportDataError(exception)
            }
        }
    }

    private fun stopTypingStatus() {
        val conversationId = typingConversationId ?: return
        typingConversationId = null
        typingGeneration++
        typingRefreshJob = null
        viewModelScope.launch {
            try {
                repository.setTypingStatus(conversationId, false)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    private suspend fun stopTypingAndWait() {
        val conversationId = typingConversationId ?: return
        typingConversationId = null
        typingGeneration++
        val writer = typingRefreshJob
        typingRefreshJob = null
        writer?.cancelAndJoin()
        repository.setTypingStatus(conversationId, false)
    }

    private fun withPresence(user: User): User =
        user.copy(isOnline = user.showOnlineStatus && user.id in onlineUserIds)

    private fun applyPresenceToUsers() {
        _availableProfiles.update { users -> users.map(::withPresence) }
        _friendsList.update { users -> users.map(::withPresence) }
        _blockedUsers.update { users -> users.map(::withPresence) }
        _conversations.update { conversations ->
            conversations.map { it.copy(user = withPresence(it.user)) }
        }
        _friendRequests.update { requests ->
            requests.map { it.copy(user = withPresence(it.user)) }
        }
        _sentRequests.update { requests ->
            requests.map { it.copy(user = withPresence(it.user)) }
        }
        _swipedHistory.update { users -> users.map(::withPresence) }
        _usernameSearchResults.update { users -> users.map(::withPresence) }
        _selectedUserProfile.update { it?.let(::withPresence) }
        _matchedUser.update { it?.let(::withPresence) }
        _selectedFriendRequest.update { it?.copy(user = withPresence(it.user)) }
        _activeConversation.update { it?.copy(user = withPresence(it.user)) }
        applyCurrentFilters()
    }

    private fun loadSignedInData(uid: String) {
        if (loadedUid == uid && _uiState.value.isProfileLoading) return
        loadedUid = uid
        dataLoadJob?.cancel()
        _uiState.update {
            it.copy(
                isPhoneVerified = firebaseAuth.currentUser?.phoneNumber != null,
                isProfileLoading = true,
                dataError = null
            )
        }
        dataLoadJob = viewModelScope.launch {
            try {
                val authPhone = firebaseAuth.currentUser?.phoneNumber.orEmpty()
                val profile = repository.loadProfile(uid) ?: run {
                    val country = CountryCatalog.allCountries
                        .filter { authPhone.startsWith(it.dialCode) }
                        .maxByOrNull { it.dialCode.length }
                        ?: CountryCatalog.allCountries.first()
                    UserProfileState(
                        selectedCountry = country,
                        phoneNumber = authPhone.removePrefix(country.dialCode)
                    )
                }
                if (!profile.isOnboardingComplete) {
                    repository.saveDraft(profile)
                }
                _uiState.value = profile.copy(
                    isPhoneVerified = firebaseAuth.currentUser?.phoneNumber != null,
                    isProfileLoading = false
                )
                refreshDashboardData()
                applyStoredSettings(repository.loadAllSettings())
                privacySettingsLoaded = true
                startPresenceIfReady()
                if (appInForeground) startConversationSummaryListener()
                applyCurrentFilters()
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                _uiState.update {
                    it.copy(
                        isProfileLoading = false,
                        dataError = exception.localizedMessage
                            ?: "Unable to load your account data. Please try again."
                    )
                }
            }
        }
    }

    private suspend fun refreshDashboardData() {
        val data = repository.loadDashboard()
        applyDashboardData(data)
    }

    private fun applyDashboardData(data: FirebaseDashboardData) {
        _exploreDeck.value = data.exploreDeck.map(::withPresence)
        _availableProfiles.value = data.exploreDeck.map(::withPresence)
        val blockedIds = data.blockedUsers.map { it.id }.toSet()
        val existingConversations = _conversations.value.associateBy { it.id }
        _conversations.value = data.conversations
            .filterNot { it.user.id in blockedIds }
            .map { conversation ->
                val existing = existingConversations[conversation.id]
                conversation.copy(
                    user = withPresence(conversation.user),
                    messages = existing?.messages.orEmpty(),
                    sharedAttachments = existing?.sharedAttachments.orEmpty(),
                    isTyping = existing?.isTyping ?: conversation.isTyping,
                    notificationsMuted = existing?.notificationsMuted
                        ?: conversation.notificationsMuted,
                    hasOlderMessages = existing?.hasOlderMessages
                        ?: conversation.hasOlderMessages,
                    isLoadingOlderMessages = existing?.isLoadingOlderMessages ?: false
                )
            }
        _friendsList.value = data.friends.filterNot { it.id in blockedIds }.map(::withPresence)
        _friendRequests.value = data.friendRequests.map { it.copy(user = withPresence(it.user)) }
        _sentRequests.value = data.sentRequests.map { it.copy(user = withPresence(it.user)) }
        _blockedUsers.value = data.blockedUsers.map(::withPresence)
        applyCurrentFilters()
    }

    private fun clearRemoteState() {
        dataLoadJob?.cancel()
        profileSaveJob?.cancel()
        stopConversationSummaryListener()
        stopActiveConversationObservation(clearActive = true, publishTypingStop = false)
        _uiState.value = UserProfileState()
        _exploreDeck.value = emptyList()
        _swipedHistory.value = emptyList()
        _conversations.value = emptyList()
        _activeConversation.value = null
        _blockedUsers.value = emptyList()
        _friendsList.value = emptyList()
        _friendRequests.value = emptyList()
        _sentRequests.value = emptyList()
        _selectedFriendRequest.value = null
        _usernameSearchResults.value = emptyList()
        _selectedUserProfile.value = null
        _matchedUser.value = null
        _matchmaking.value = MatchmakingState()
        _filterState.value = ExploreFilterState()
        _privacySettings.value = PrivacySettingsState()
        _notificationSettings.value = NotificationSettingsState()
        _availableProfiles.value = emptyList()
        _isProUser.value = false
        privacySettingsLoaded = false
    }

    private fun reportDataError(exception: Exception) {
        if (exception is CancellationException) throw exception
        _uiState.update {
            it.copy(
                isSavingProfile = false,
                isUploadingPhoto = false,
                dataError = exception.localizedMessage
                    ?: "The request could not be completed. Please try again."
            )
        }
    }

    fun clearDataError() {
        _uiState.update { it.copy(dataError = null) }
    }

    private fun scheduleProfilePersistence() {
        if (firebaseAuth.currentUser == null) return
        val profileSnapshot = _uiState.value
        profileSaveJob?.cancel()
        profileSaveJob = viewModelScope.launch {
            delay(500)
            try {
                if (profileSnapshot.isOnboardingComplete) {
                    repository.saveProfile(profileSnapshot, complete = true)
                } else {
                    repository.saveDraft(profileSnapshot)
                }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    // --- Onboarding & Profile Setup Actions ---

    fun selectCountry(country: Country) {
        _uiState.update { it.copy(selectedCountry = country) }
        scheduleProfilePersistence()
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
        scheduleProfilePersistence()
    }

    fun updateOtp(otp: String) {
        _uiState.update { it.copy(otpCode = otp) }
        _phoneAuthState.update { it.copy(errorMessage = null) }
    }

    fun setPhoneAuthError(message: String) {
        _phoneAuthState.update {
            it.copy(isSendingCode = false, errorMessage = message)
        }
    }

    fun resetPhoneAuth() {
        verificationId = null
        resendToken = null
        pendingPhoneNumberForProfile = null
        _uiState.update { it.copy(otpCode = "") }
        _phoneAuthState.value = PhoneAuthUiState()
    }

    fun signOut(context: Context) {
        val uid = firebaseAuth.currentUser?.uid
        presenceSyncJob?.cancel()
        presenceRepository.stopObserving()
        presenceListening = false
        viewModelScope.launch {
            try {
                stopTypingAndWait()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
            stopConversationSummaryListener()
            stopActiveConversationObservation(clearActive = true, publishTypingStop = false)
            if (uid != null) {
                try {
                    CallPushTokenStore.unregister(context.applicationContext, uid)
                } catch (exception: Exception) {
                    reportDataError(exception)
                }
            }
            try {
                presenceRepository.setPublishingEnabled(false)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
            firebaseAuth.signOut()
            resetPhoneAuth()
            _uiState.update { it.copy(isPhoneVerified = false, phoneNumber = "") }
        }
    }

    fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String,
        forceResending: Boolean = false,
        updateExistingPhone: Boolean = false,
        phoneNumberForProfile: String? = null
    ) {
        if (!phoneNumber.matches(Regex("^\\+[1-9]\\d{7,14}$"))) {
            setPhoneAuthError("Enter a valid phone number with country code.")
            return
        }

        if (updateExistingPhone && firebaseAuth.currentUser == null) {
            setPhoneAuthError("Sign in again before updating your phone number.")
            return
        }

        pendingPhoneNumberForProfile = phoneNumberForProfile
        if (forceResending) {
            _phoneAuthState.update {
                it.copy(isSendingCode = true, errorMessage = null)
            }
        } else {
            verificationId = null
            resendToken = null
            _uiState.update { it.copy(otpCode = "") }
            _phoneAuthState.value = PhoneAuthUiState(
                isSendingCode = true,
                isPhoneUpdate = updateExistingPhone
            )
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                completePhoneCredential(credential, updateExistingPhone)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                _phoneAuthState.update {
                    it.copy(
                        isSendingCode = false,
                        isVerifying = false,
                        errorMessage = exception.localizedMessage
                            ?: "Unable to send a verification code. Please try again."
                    )
                }
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = newVerificationId
                resendToken = token
                _uiState.update { it.copy(otpCode = "") }
                _phoneAuthState.update {
                    it.copy(
                        isSendingCode = false,
                        errorMessage = null,
                        isPhoneUpdate = updateExistingPhone,
                        codeSentVersion = it.codeSentVersion + 1
                    )
                }
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResending) {
            resendToken?.let(optionsBuilder::setForceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyPhoneCode(code: String) {
        if (_phoneAuthState.value.isVerifying) return

        val currentVerificationId = verificationId
        if (currentVerificationId.isNullOrBlank()) {
            _phoneAuthState.update {
                it.copy(errorMessage = "Request a verification code before continuing.")
            }
            return
        }

        if (code.length != 6 || !code.all(Char::isDigit)) {
            _phoneAuthState.update {
                it.copy(errorMessage = "Enter the 6-digit code sent to your phone.")
            }
            return
        }

        _phoneAuthState.update {
            it.copy(isVerifying = true, errorMessage = null)
        }
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, code)
        completePhoneCredential(credential, _phoneAuthState.value.isPhoneUpdate)
    }

    private fun completePhoneCredential(
        credential: PhoneAuthCredential,
        updateExistingPhone: Boolean
    ) {
        _phoneAuthState.update {
            it.copy(isVerifying = true, errorMessage = null)
        }
        if (updateExistingPhone) {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _phoneAuthState.update {
                    it.copy(
                        isVerifying = false,
                        errorMessage = "Sign in again before updating your phone number."
                    )
                }
                return
            }
            currentUser.updatePhoneNumber(credential).addOnCompleteListener { task ->
                onPhoneCredentialResult(
                    task.isSuccessful,
                    task.exception,
                    updateExistingPhone = true
                )
            }
        } else {
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                onPhoneCredentialResult(
                    task.isSuccessful,
                    task.exception,
                    updateExistingPhone = false
                )
            }
        }
    }

    private fun onPhoneCredentialResult(
        isSuccessful: Boolean,
        exception: Exception?,
        updateExistingPhone: Boolean
    ) {
        if (isSuccessful) {
            _uiState.update {
                it.copy(
                    isPhoneVerified = true,
                    isProfileLoading = !updateExistingPhone,
                    phoneNumber = if (updateExistingPhone) {
                        pendingPhoneNumberForProfile ?: it.phoneNumber
                    } else {
                        it.phoneNumber
                    }
                )
            }
            _phoneAuthState.update {
                it.copy(
                    isSendingCode = false,
                    isVerifying = false,
                    isPhoneUpdateComplete = updateExistingPhone,
                    errorMessage = null
                )
            }
            pendingPhoneNumberForProfile = null
            if (!updateExistingPhone) {
                firebaseAuth.currentUser?.uid?.let(::loadSignedInData)
            } else {
                scheduleProfilePersistence()
            }
        } else {
            _phoneAuthState.update {
                it.copy(
                    isSendingCode = false,
                    isVerifying = false,
                    errorMessage = exception?.localizedMessage
                        ?: "The verification code was rejected. Check it and try again."
                )
            }
        }
    }

    private fun updateProfile(transform: (UserProfileState) -> UserProfileState) {
        _uiState.update { transform(it).copy(dataError = null) }
        scheduleProfilePersistence()
    }

    suspend fun persistProfileEdit(
        transform: (UserProfileState) -> UserProfileState
    ): Boolean {
        if (_uiState.value.isSavingProfile) return false
        if (firebaseAuth.currentUser == null) {
            reportDataError(IllegalStateException("You must be signed in to save profile changes."))
            return false
        }

        profileSaveJob?.cancel()
        profileSaveJob = null
        val editedProfile = transform(_uiState.value)
        _uiState.update { it.copy(isSavingProfile = true, dataError = null) }

        return try {
            if (editedProfile.isOnboardingComplete) {
                repository.saveProfile(editedProfile, complete = true)
            } else {
                repository.saveDraft(editedProfile)
            }
            _uiState.update { current ->
                transform(current).copy(isSavingProfile = false, dataError = null)
            }
            true
        } catch (exception: Exception) {
            reportDataError(exception)
            false
        }
    }

    private fun applyStoredSettings(settings: Map<String, Map<String, Any?>>) {
        fun number(document: String, field: String, default: Float): Float =
            (settings[document]?.get(field) as? Number)?.toFloat() ?: default
        fun bool(document: String, field: String, default: Boolean): Boolean =
            settings[document]?.get(field) as? Boolean ?: default
        fun text(document: String, field: String, default: String): String =
            settings[document]?.get(field) as? String ?: default

        _matchmaking.value = MatchmakingState(
            showMe = text("matchmaking", "showMe", "Everyone"),
            ageRange = number("matchmaking", "ageMin", 20f)..number("matchmaking", "ageMax", 32f),
            distance = number("matchmaking", "distance", 30f),
            countryToggle = bool("matchmaking", "countryToggle", false),
            globalToggle = bool("matchmaking", "globalToggle", false)
        )
        _privacySettings.value = PrivacySettingsState(
            showOnlineStatus = bool("privacy", "showOnlineStatus", true),
            showDistance = bool("privacy", "showDistance", true),
            showAge = bool("privacy", "showAge", true),
            allowNonFriendsMessaging = bool("privacy", "allowNonFriendsMessaging", false),
            sendReadReceipts = bool("privacy", "sendReadReceipts", true)
        )
        _notificationSettings.value = NotificationSettingsState(
            pushMatches = bool("notifications", "pushMatches", true),
            pushMessages = bool("notifications", "pushMessages", true),
            emailSummaries = bool("notifications", "emailSummaries", false),
            soundAndVibration = bool("notifications", "soundAndVibration", true),
            marketingPromotions = bool("notifications", "marketingPromotions", false)
        )
        fun strings(document: String, field: String): List<String> =
            (settings[document]?.get(field) as? List<*>)?.filterIsInstance<String>().orEmpty()
        _filterState.value = ExploreFilterState(
            ageRange = number("filters", "ageMin", 18f)..number("filters", "ageMax", 35f),
            maxDistance = number("filters", "maxDistance", 25f),
            interestedIn = text("filters", "interestedIn", "All"),
            selectedInterests = strings("filters", "selectedInterests"),
            onlineOnly = bool("filters", "onlineOnly", false),
            verifiedOnly = bool("filters", "verifiedOnly", false)
        )
    }

    fun updateFullName(name: String) = updateProfile { it.copy(fullName = name) }

    fun updateDateOfBirth(dob: String) = updateProfile {
        it.copy(dateOfBirth = dob, age = calculateAge(dob))
    }

    fun updateGender(gender: String) = updateProfile { it.copy(gender = gender) }

    fun updateUsername(username: String) = updateProfile { it.copy(username = username) }

    suspend fun isUsernameAvailable(username: String): Boolean =
        repository.isUsernameAvailable(username)

    private fun calculateAge(dateOfBirth: String): Int =
        Validators.ageFromDateOfBirth(dateOfBirth) ?: 0

    fun completeOnboarding(onComplete: () -> Unit) {
        val profile = _uiState.value
        val isValid = profile.isPhoneVerified &&
            Validators.isValidName(profile.fullName) &&
            Validators.isValidAdultDateOfBirth(profile.dateOfBirth) &&
            profile.gender.isNotBlank() &&
            Validators.isValidUsername(profile.username) &&
            profile.photos.size in 2..6 &&
            profile.bio.trim().length in 10..500 &&
            profile.selectedInterests.size in 1..10 &&
            profile.selectedLookingFor.size == 1
        if (!isValid) {
            _uiState.update {
                it.copy(dataError = "Complete each required profile field before continuing.")
            }
            return
        }
        if (_uiState.value.isSavingProfile) return

        profileSaveJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, dataError = null) }
            try {
                repository.saveProfile(profile, complete = true)
                _uiState.update {
                    it.copy(isSavingProfile = false, isOnboardingComplete = true)
                }
                refreshDashboardData()
                onComplete()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun updateBio(bio: String) = updateProfile { it.copy(bio = bio) }
    fun updateAboutPreferences(
        bio: String,
        interests: List<String>,
        lookingFor: List<String>
    ) = updateProfile {
        it.copy(
            bio = bio,
            selectedInterests = interests,
            selectedLookingFor = lookingFor
        )
    }

    fun updateCity(city: String) = updateProfile { it.copy(city = city) }
    fun updateHeight(height: String) = updateProfile { it.copy(height = height) }
    fun updateJob(job: String) = updateProfile { it.copy(job = job) }
    fun updateEducation(education: String) = updateProfile { it.copy(education = education) }
    fun updateZodiac(zodiac: String) = updateProfile { it.copy(zodiac = zodiac) }
    fun updateEmail(email: String) = updateProfile { it.copy(email = email) }

    fun addPhoto(uriString: String) {
        val currentProfile = _uiState.value
        if (currentProfile.isUploadingPhoto) return
        if (currentProfile.photos.size >= 6) {
            _uiState.update { it.copy(dataError = "A profile can contain at most six photos.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, dataError = null) }
            try {
                val photoUrl = repository.uploadProfilePhoto(Uri.parse(uriString))
                _uiState.update { current ->
                    current.copy(
                        photos = (current.photos + photoUrl).distinct().take(6),
                        isUploadingPhoto = false
                    )
                }
                scheduleProfilePersistence()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun replacePhoto(index: Int, uriString: String) {
        val originalPhoto = _uiState.value.photos.getOrNull(index) ?: return
        if (_uiState.value.isUploadingPhoto) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, dataError = null) }
            var uploadedPhoto: String? = null
            try {
                uploadedPhoto = repository.uploadProfilePhoto(Uri.parse(uriString))
                val latestProfile = _uiState.value
                val photos = latestProfile.photos.toMutableList()
                if (index !in photos.indices) {
                    throw IllegalStateException("The selected photo is no longer available.")
                }
                photos[index] = uploadedPhoto
                val updatedProfile = latestProfile.copy(
                    photos = photos,
                    isUploadingPhoto = false
                )
                if (updatedProfile.isOnboardingComplete) {
                    repository.saveProfile(updatedProfile, complete = true)
                } else {
                    repository.saveDraft(updatedProfile)
                }
                _uiState.value = updatedProfile
            } catch (exception: Exception) {
                uploadedPhoto?.let { photoUrl ->
                    try {
                        repository.deleteProfilePhoto(photoUrl)
                    } catch (cleanupException: Exception) {
                        reportDataError(cleanupException)
                    }
                }
                _uiState.update { it.copy(isUploadingPhoto = false) }
                reportDataError(exception)
                return@launch
            }

            try {
                repository.deleteProfilePhoto(originalPhoto)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun removePhoto(index: Int) {
        val photo = _uiState.value.photos.getOrNull(index) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(dataError = null) }
            try {
                repository.deleteProfilePhoto(photo)
                _uiState.update {
                    it.copy(photos = it.photos.toMutableList().apply { removeAt(index) })
                }
                scheduleProfilePersistence()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun setMainPhoto(index: Int) {
        updateProfile {
            if (index !in it.photos.indices || index == 0) it
            else it.copy(photos = it.photos.toMutableList().apply {
                add(0, removeAt(index))
            })
        }
    }

    fun reorderPhotos(fromIndex: Int, toIndex: Int) {
        updateProfile {
            if (fromIndex !in it.photos.indices || toIndex !in it.photos.indices) it
            else it.copy(photos = it.photos.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            })
        }
    }

    fun setCapturedSelfie(uriString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, dataError = null) }
            try {
                val url = repository.uploadVerificationSelfie(Uri.parse(uriString))
                _uiState.update {
                    it.copy(
                        selfieCapturedUrl = url,
                        isUploadingPhoto = false
                    )
                }
                scheduleProfilePersistence()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun toggleInterest(interest: String) = updateProfile {
        val updated = if (interest in it.selectedInterests) {
            it.selectedInterests - interest
        } else if (it.selectedInterests.size < 10) {
            it.selectedInterests + interest
        } else {
            it.selectedInterests
        }
        it.copy(selectedInterests = updated)
    }

    fun toggleLookingFor(option: String) = updateProfile {
        it.copy(selectedLookingFor = listOf(option))
    }

    fun setLocationPermission(granted: Boolean) =
        updateProfile { it.copy(locationPermissionGranted = granted) }

    fun setNotificationsPermission(granted: Boolean) =
        updateProfile { it.copy(notificationsPermissionGranted = granted) }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                _usernameSearchResults.value = repository.searchProfiles(query).map(::withPresence)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun setExploreSegment(index: Int) {
        _exploreSegment.value = index.coerceIn(0, 2)
        applyCurrentFilters()
    }

    fun swipePass(user: User) {
        viewModelScope.launch {
            try {
                repository.recordSwipe(user, liked = false)
                _swipedHistory.update { listOf(user) + it }
                _availableProfiles.update { profiles -> profiles.filterNot { it.id == user.id } }
                _exploreDeck.update { it.filterNot { candidate -> candidate.id == user.id } }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun swipeLike(user: User, onMatch: (User) -> Unit) {
        viewModelScope.launch {
            try {
                val isMatch = repository.recordSwipe(user, liked = true)
                _swipedHistory.update { listOf(user) + it }
                _availableProfiles.update { profiles -> profiles.filterNot { it.id == user.id } }
                _exploreDeck.update { it.filterNot { candidate -> candidate.id == user.id } }
                if (isMatch) {
                    _matchedUser.value = user
                    refreshDashboardData()
                    onMatch(user)
                }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun rewind(): Boolean {
        val lastSwiped = _swipedHistory.value.firstOrNull() ?: return false
        viewModelScope.launch {
            try {
                repository.undoSwipe(lastSwiped.id)
                _swipedHistory.update { it.drop(1) }
                _availableProfiles.update { profiles ->
                    (listOf(lastSwiped) + profiles).distinctBy { it.id }
                }
                applyCurrentFilters()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
        return true
    }

    fun resetDeck() {
        viewModelScope.launch {
            try {
                _exploreDeck.value = repository.loadDashboard().exploreDeck
                _availableProfiles.value = _exploreDeck.value
                _swipedHistory.value = emptyList()
                applyCurrentFilters()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun updateFilters(newFilters: ExploreFilterState) {
        _filterState.value = newFilters
        applyCurrentFilters()
        persistSettings(
            "filters",
            mapOf(
                "ageMin" to newFilters.ageRange.start,
                "ageMax" to newFilters.ageRange.endInclusive,
                "maxDistance" to newFilters.maxDistance,
                "interestedIn" to newFilters.interestedIn,
                "selectedInterests" to newFilters.selectedInterests,
                "onlineOnly" to newFilters.onlineOnly,
                "verifiedOnly" to newFilters.verifiedOnly
            )
        )
    }

    fun setSelectedUserProfile(user: User?) {
        _selectedUserProfile.value = user?.let(::withPresence)
    }

    fun selectConversation(conversation: ChatConversation) {
        if (_activeConversation.value?.id != conversation.id) {
            olderMessages = emptyList()
            hasOlderMessagesAvailable = false
        }
        _activeConversation.value = conversation.copy(user = withPresence(conversation.user))
        observeActiveConversation(conversation)
    }

    fun loadOlderChatMessages(conversationId: String) {
        val active = _activeConversation.value ?: return
        if (
            active.id != conversationId ||
            active.isLoadingOlderMessages ||
            !active.hasOlderMessages
        ) return
        val beforeMessageId = active.messages.firstOrNull()?.id ?: return
        updateConversation(conversationId) { it.copy(isLoadingOlderMessages = true) }
        viewModelScope.launch {
            try {
                val page = repository.loadOlderMessages(conversationId, beforeMessageId)
                if (_activeConversation.value?.id != conversationId) return@launch
                olderMessages = mergeChatMessages(olderMessages, page.messages)
                hasOlderMessagesAvailable = page.hasMore
                updateConversation(conversationId) {
                    it.copy(
                        messages = mergeChatMessages(olderMessages, it.messages),
                        hasOlderMessages = page.hasMore
                    )
                }
            } catch (exception: Exception) {
                reportDataError(exception)
            } finally {
                updateConversation(conversationId) {
                    it.copy(isLoadingOlderMessages = false)
                }
            }
        }
    }

    fun loadConversationAttachments(conversationId: String) {
        viewModelScope.launch {
            try {
                val attachments = repository.loadConversationAttachments(conversationId)
                updateConversation(conversationId) { it.copy(sharedAttachments = attachments) }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    private fun updateConversation(
        conversationId: String,
        transform: (ChatConversation) -> ChatConversation
    ) {
        _activeConversation.update { active ->
            if (active?.id == conversationId) transform(active) else active
        }
        _conversations.update { items ->
            items.map { if (it.id == conversationId) transform(it) else it }
        }
    }

    private fun mergeChatMessages(
        older: List<ChatMessage>,
        newer: List<ChatMessage>
    ): List<ChatMessage> = (older + newer)
        .distinctBy { it.id }
        .sortedWith(
            compareBy<ChatMessage> { it.createdAt?.seconds ?: Long.MIN_VALUE }
                .thenBy { it.createdAt?.nanoseconds ?: 0 }
                .thenBy { it.id }
        )

    fun prepareConversationActions(conversation: ChatConversation) {
        stopActiveConversationObservation(clearActive = false)
        _activeConversation.value = conversation.copy(user = withPresence(conversation.user))
    }

    fun closeActiveConversation() {
        stopActiveConversationObservation()
    }

    fun setConversationNotificationsMuted(conversationId: String, muted: Boolean) {
        viewModelScope.launch {
            try {
                repository.setConversationNotificationsMuted(conversationId, muted)
                _conversations.update { conversations ->
                    conversations.map {
                        if (it.id == conversationId) it.copy(notificationsMuted = muted) else it
                    }
                }
                _activeConversation.update {
                    if (it?.id == conversationId) it.copy(notificationsMuted = muted) else it
                }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun openConversationForUser(user: User, onReady: (ChatConversation) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val conversation = repository.ensureConversation(user.id)
                _activeConversation.value = conversation
                _conversations.update { current ->
                    listOf(conversation) + current.filterNot { it.id == conversation.id }
                }
                selectConversation(conversation)
                onReady(conversation)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun openConversationById(
        conversationId: String,
        onComplete: (ChatConversation?) -> Unit
    ) {
        viewModelScope.launch {
            val conversation = try {
                repository.openConversation(conversationId).also { opened ->
                    _activeConversation.value = opened
                    _conversations.update { current ->
                        listOf(opened) + current.filterNot { it.id == opened.id }
                    }
                    selectConversation(opened)
                }
            } catch (exception: Exception) {
                reportDataError(exception)
                null
            }
            onComplete(conversation)
        }
    }

    suspend fun sendMessage(
        text: String,
        attachmentUri: String? = null,
        mimeType: String? = null
    ): Boolean {
        val conversation = _activeConversation.value ?: return false
        if (!ChatPolicies.isMessageLengthValid(text)) {
            reportDataError(IllegalArgumentException(
                "Messages must be ${ChatPolicies.MAX_MESSAGE_LENGTH} characters or fewer."
            ))
            return false
        }
        var uploadedAttachmentUrl: String? = null
        return try {
            val contentType = mimeType ?: if (attachmentUri != null) "image/jpeg" else null
            val attachmentUrl = attachmentUri?.let { value ->
                val uri = Uri.parse(value)
                if (uri.scheme == "content" || uri.scheme == "file") {
                    repository.uploadChatAttachment(
                        conversation.id,
                        uri,
                        contentType ?: "application/octet-stream"
                    ).also { uploadedAttachmentUrl = it }
                } else {
                    value
                }
            }
            val attachmentType = when {
                contentType == null -> null
                contentType.startsWith("image/") -> "image"
                contentType.startsWith("audio/") -> "audio"
                else -> "document"
            }
            try {
                repository.sendMessage(
                    conversation.id,
                    text,
                    attachmentUrl,
                    attachmentType
                )
            } catch (sendException: Exception) {
                if (uploadedAttachmentUrl != null) {
                    try {
                        repository.deleteChatAttachment(uploadedAttachmentUrl.orEmpty())
                    } catch (cleanupException: Exception) {
                        throw IllegalStateException(
                            "The message failed and its uploaded attachment could not be removed.",
                            cleanupException
                        ).also { it.addSuppressed(sendException) }
                    }
                }
                throw sendException
            }
            true
        } catch (exception: Exception) {
            reportDataError(exception)
            false
        }
    }

    fun clearActiveChat() {
        val active = _activeConversation.value ?: return
        viewModelScope.launch {
            try {
                repository.clearConversation(active.id)
                olderMessages = emptyList()
                hasOlderMessagesAvailable = false
                repository.markConversationRead(
                    active.id,
                    null,
                    sendReadReceipt = _privacySettings.value.sendReadReceipts
                )
                val cleared = active.copy(messages = emptyList(), lastMessage = "", unreadCount = 0)
                _activeConversation.value = cleared
                _conversations.update { items ->
                    items.map { if (it.id == cleared.id) cleared else it }
                }
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun deleteActiveChat() {
        val active = _activeConversation.value ?: return
        viewModelScope.launch {
            try {
                repository.archiveConversation(active.id)
                _conversations.update { items -> items.filterNot { it.id == active.id } }
                stopActiveConversationObservation()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun blockUser(user: User) {
        viewModelScope.launch {
            try {
                repository.blockUser(user.id)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun unblockUser(user: User) {
        viewModelScope.launch {
            try {
                repository.unblockUser(user.id)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    suspend fun submitUserReport(targetId: String, reason: String, details: String): Boolean {
        return try {
            repository.submitUserReport(targetId, reason, details)
            true
        } catch (exception: Exception) {
            reportDataError(exception)
            false
        }
    }

    fun setSelectedFriendRequest(item: FriendRequestItem?) {
        _selectedFriendRequest.value = item
    }

    fun acceptFriendRequest(item: FriendRequestItem) = respondToFriendRequest(item, accept = true)
    fun declineFriendRequest(item: FriendRequestItem) = respondToFriendRequest(item, accept = false)

    private fun respondToFriendRequest(item: FriendRequestItem, accept: Boolean) {
        viewModelScope.launch {
            try {
                repository.respondToFriendRequest(item.id, accept)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun cancelSentRequest(sent: SentFriendRequest) {
        viewModelScope.launch {
            try {
                repository.cancelFriendRequest(sent.id)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun sendFriendRequest(user: User) {
        viewModelScope.launch {
            try {
                repository.sendFriendRequest(user)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun removeFriend(user: User) {
        viewModelScope.launch {
            try {
                repository.removeFriend(user.id)
                refreshDashboardData()
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun updateMatchmaking(newState: MatchmakingState) {
        _matchmaking.value = newState
        applyCurrentFilters()
        persistSettings(
            "matchmaking",
            mapOf(
                "showMe" to newState.showMe,
                "ageMin" to newState.ageRange.start,
                "ageMax" to newState.ageRange.endInclusive,
                "distance" to newState.distance,
                "countryToggle" to newState.countryToggle,
                "globalToggle" to newState.globalToggle
            )
        )
    }

    fun updatePrivacySettings(newState: PrivacySettingsState) {
        val previousSettings = _privacySettings.value
        _privacySettings.value = newState
        syncPresencePublication()
        if (newState.sendReadReceipts && !previousSettings.sendReadReceipts) {
            val active = _activeConversation.value
            val newestIncoming = active?.messages.orEmpty()
                .asSequence()
                .filterNot { it.isOwn }
                .mapNotNull { it.createdAt }
                .maxOrNull()
            if (active != null && appInForeground) {
                viewModelScope.launch {
                    try {
                        repository.markConversationRead(
                            active.id,
                            newestIncoming,
                            sendReadReceipt = true
                        )
                    } catch (exception: Exception) {
                        reportDataError(exception)
                    }
                }
            }
        }
        viewModelScope.launch {
            try {
                repository.savePrivacySettings(
                    mapOf(
                        "showOnlineStatus" to newState.showOnlineStatus,
                        "showDistance" to newState.showDistance,
                        "showAge" to newState.showAge,
                        "allowNonFriendsMessaging" to newState.allowNonFriendsMessaging,
                        "sendReadReceipts" to newState.sendReadReceipts
                    ),
                    showAge = newState.showAge,
                    age = _uiState.value.age
                )
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }

    fun updateNotificationSettings(newState: NotificationSettingsState) {
        _notificationSettings.value = newState
        persistSettings(
            "notifications",
            mapOf(
                "pushMatches" to newState.pushMatches,
                "pushMessages" to newState.pushMessages,
                "emailSummaries" to newState.emailSummaries,
                "soundAndVibration" to newState.soundAndVibration,
                "marketingPromotions" to newState.marketingPromotions
            )
        )
    }

    private fun persistSettings(name: String, values: Map<String, Any>) {
        viewModelScope.launch {
            try {
                repository.saveSettings(name, values)
            } catch (exception: Exception) {
                reportDataError(exception)
            }
        }
    }
}
