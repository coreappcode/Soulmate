package com.example.ui.screens.dashboard

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.ChatConversation
import com.example.data.FriendRequestItem
import com.example.data.User
import com.example.data.UserProfileState
import com.example.navigation.Routes
import com.example.ui.SoulmateViewModel
import com.example.ui.components.SoulBottomNav
import com.example.ui.screens.chats.BlockedUsersScreen
import com.example.ui.screens.chats.ChatInfoScreen
import com.example.ui.screens.chats.ChatListScreen
import com.example.ui.screens.chats.ChatOptionsScreen
import com.example.ui.screens.chats.ChatThreadScreen
import com.example.ui.screens.chats.SendAttachmentScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.explore.FiltersScreen
import com.example.ui.screens.explore.FullProfileScreen
import com.example.ui.screens.explore.ItsAMatchScreen
import com.example.ui.screens.explore.UsernameSearchScreen
import com.example.ui.screens.friends.FindFriendsScreen
import com.example.ui.screens.friends.FriendProfileScreen
import com.example.ui.screens.friends.FriendsScreen
import com.example.ui.screens.friends.RequestDetailScreen
import com.example.ui.screens.profile.EditAboutScreen
import com.example.ui.screens.profile.EditBasicInfoScreen
import com.example.ui.screens.profile.EditPhotosScreen
import com.example.ui.screens.profile.MyProfileScreen
import com.example.ui.screens.profile.PreviewProfileScreen
import com.example.ui.screens.settings.AccountSettingsScreen
import com.example.ui.screens.settings.CommunityRulesScreen
import com.example.ui.screens.settings.DeleteAccountScreen
import com.example.ui.screens.settings.EditUsernameScreen
import com.example.ui.screens.settings.EmailScreen
import com.example.ui.screens.settings.LogoutConfirmationScreen
import com.example.ui.screens.settings.MatchmakingScreen
import com.example.ui.screens.settings.NotificationSettingsScreen
import com.example.ui.screens.settings.PhoneNumberScreen
import com.example.ui.screens.settings.PrivacySettingsScreen
import com.example.ui.screens.settings.ProPlansScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.TermsScreen
import com.example.ui.screens.settings.VerificationStatusScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.HeaderBackground
import com.example.utils.findActivity

sealed interface ActiveSubScreen {
    data object None : ActiveSubScreen

    // Explore sub-screens
    data object Filters : ActiveSubScreen
    data object UsernameSearch : ActiveSubScreen
    data class ExploreProfileDetail(val user: User) : ActiveSubScreen
    data class ItsAMatch(val user: User) : ActiveSubScreen

    // Chat sub-screens
    data class ChatThread(val conversation: ChatConversation) : ActiveSubScreen
    data class ChatOptions(val user: User) : ActiveSubScreen
    data class ChatInfo(val user: User) : ActiveSubScreen
    data object ChatAttachment : ActiveSubScreen
    data object BlockedUsers : ActiveSubScreen

    // Friends sub-screens
    data class RequestDetail(val request: FriendRequestItem) : ActiveSubScreen
    data class FriendProfile(val user: User) : ActiveSubScreen
    data object FindFriends : ActiveSubScreen

    // Settings sub-screens
    data object Matchmaking : ActiveSubScreen
    data object VerificationStatus : ActiveSubScreen
    data object Privacy : ActiveSubScreen
    data object Notifications : ActiveSubScreen
    data object ProPlans : ActiveSubScreen
    data object Terms : ActiveSubScreen
    data object CommunityRules : ActiveSubScreen
    data object Account : ActiveSubScreen
    data object PhoneNumber : ActiveSubScreen
    data object EditUsername : ActiveSubScreen
    data object Email : ActiveSubScreen
    data object LogoutConfirm : ActiveSubScreen
    data object DeleteAccount : ActiveSubScreen

    // Profile sub-screens
    data object EditPhotos : ActiveSubScreen
    data object EditBasicInfo : ActiveSubScreen
    data object EditAbout : ActiveSubScreen
    data object PreviewProfile : ActiveSubScreen
}

@Composable
fun DashboardScreen(
    initialTab: String = Routes.DASHBOARD_EXPLORE,
    viewModel: SoulmateViewModel,
    onNavigateToFaceVerify: () -> Unit,
    onNavigateToDatePicker: () -> Unit,
    onNavigateToCountryPicker: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var activeSubScreen by remember { mutableStateOf<ActiveSubScreen>(ActiveSubScreen.None) }

    val userProfile by viewModel.uiState.collectAsState()
    val exploreDeck by viewModel.exploreDeck.collectAsState()
    val exploreSegment by viewModel.exploreSegment.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val friendsList by viewModel.friendsList.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val matchmakingSettings by viewModel.matchmaking.collectAsState()
    val privacySettings by viewModel.privacySettings.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val usernameSearchResults by viewModel.usernameSearchResults.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val context = LocalContext.current
    val phoneAuthActivity = context.findActivity()
    val sentRequestIds = remember(sentRequests) { sentRequests.map { it.user.id }.toSet() }

    LaunchedEffect(userProfile.dataError) {
        userProfile.dataError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearDataError()
        }
    }

    // Handle back button for any active sub-screen
    BackHandler(enabled = activeSubScreen !is ActiveSubScreen.None) {
        activeSubScreen = ActiveSubScreen.None
    }

    Scaffold(
        bottomBar = {
            // Only show bottom navigation when on the top-level of a dashboard tab
            if (activeSubScreen is ActiveSubScreen.None) {
                SoulBottomNav(
                    currentRoute = currentTab,
                    onNavigate = { newRoute ->
                        currentTab = newRoute
                        activeSubScreen = ActiveSubScreen.None
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = HeaderBackground,
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val sub = activeSubScreen) {
                // EXPLORE SUB-SCREENS
                is ActiveSubScreen.Filters -> {
                    FiltersScreen(
                        currentFilters = filterState,
                        onApply = {
                            viewModel.updateFilters(it)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.UsernameSearch -> {
                    UsernameSearchScreen(
                        results = usernameSearchResults,
                        sentRequestIds = sentRequestIds,
                        onSearchQuery = viewModel::searchUsers,
                        onUserClick = { user ->
                            activeSubScreen = ActiveSubScreen.ExploreProfileDetail(user)
                        },
                        onAddFriend = { user ->
                            viewModel.sendFriendRequest(user)
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.ExploreProfileDetail -> {
                    FullProfileScreen(
                        user = sub.user,
                        onBack = { activeSubScreen = ActiveSubScreen.None },
                        onPass = {
                            viewModel.swipePass(sub.user)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onConnect = {
                            viewModel.swipeLike(sub.user) { matched ->
                                activeSubScreen = ActiveSubScreen.ItsAMatch(matched)
                            }
                            if (activeSubScreen !is ActiveSubScreen.ItsAMatch) {
                                activeSubScreen = ActiveSubScreen.None
                            }
                        }
                    )
                }

                is ActiveSubScreen.ItsAMatch -> {
                    ItsAMatchScreen(
                        matchedUser = sub.user,
                        myPhotoUrl = userProfile.photos.firstOrNull() ?: "",
                        onSendMessage = {
                            viewModel.openConversationForUser(sub.user) { conversation ->
                                currentTab = Routes.DASHBOARD_CHATS
                                activeSubScreen = ActiveSubScreen.ChatThread(conversation)
                            }
                        },
                        onKeepSwiping = {
                            activeSubScreen = ActiveSubScreen.None
                        }
                    )
                }

                // CHATS SUB-SCREENS
                is ActiveSubScreen.ChatThread -> {
                    val activeConv = conversations.firstOrNull { it.id == sub.conversation.id } ?: sub.conversation
                    ChatThreadScreen(
                        conversation = activeConv,
                        onSendMessage = { text ->
                            viewModel.sendMessage(text)
                        },
                        onOpenAttachment = {
                            activeSubScreen = ActiveSubScreen.ChatAttachment
                        },
                        onOpenInfo = {
                            activeSubScreen = ActiveSubScreen.ChatInfo(activeConv.user)
                        },
                        onOpenOptions = {
                            activeSubScreen = ActiveSubScreen.ChatOptions(activeConv.user)
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.ChatOptions -> {
                    ChatOptionsScreen(
                        user = sub.user,
                        onViewProfile = {
                            activeSubScreen = ActiveSubScreen.ExploreProfileDetail(sub.user)
                        },
                        onMediaAndFiles = {
                            activeSubScreen = ActiveSubScreen.ChatInfo(sub.user)
                        },
                        onMute = {
                            Toast.makeText(context, "Chat muting is not available yet.", Toast.LENGTH_LONG).show()
                        },
                        onClearChat = {
                            viewModel.clearActiveChat()
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBlockUser = {
                            viewModel.blockUser(sub.user)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onReportUser = {
                            Toast.makeText(context, "Reporting is not configured. This user was not blocked.", Toast.LENGTH_LONG).show()
                        },
                        onDeleteChat = {
                            viewModel.deleteActiveChat()
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.ChatInfo -> {
                    ChatInfoScreen(
                        user = sub.user,
                        onCallClick = {
                            Toast.makeText(context, "Audio calls are not available yet.", Toast.LENGTH_LONG).show()
                        },
                        onVideoClick = {
                            Toast.makeText(context, "Video calls are not available yet.", Toast.LENGTH_LONG).show()
                        },
                        onMuteClick = {
                            Toast.makeText(context, "Chat muting is not available yet.", Toast.LENGTH_LONG).show()
                        },
                        onBlockClick = {
                            viewModel.blockUser(sub.user)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onReportClick = {
                            viewModel.blockUser(sub.user)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.ChatAttachment -> {
                    SendAttachmentScreen(
                        onSendAttachment = { uri, caption, mimeType ->
                            viewModel.sendMessage(caption, uri.toString(), mimeType)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.BlockedUsers -> {
                    BlockedUsersScreen(
                        blockedUsers = blockedUsers,
                        onUnblock = { user -> viewModel.unblockUser(user) },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                // FRIENDS SUB-SCREENS
                is ActiveSubScreen.RequestDetail -> {
                    RequestDetailScreen(
                        request = sub.request,
                        onAccept = {
                            viewModel.acceptFriendRequest(sub.request)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onDecline = {
                            viewModel.declineFriendRequest(sub.request)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.FriendProfile -> {
                    FriendProfileScreen(
                        user = sub.user,
                        onRemoveFriend = {
                            viewModel.removeFriend(sub.user)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onSendMessage = {
                            viewModel.openConversationForUser(sub.user) { conversation ->
                                currentTab = Routes.DASHBOARD_CHATS
                                activeSubScreen = ActiveSubScreen.ChatThread(conversation)
                            }
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.FindFriends -> {
                    FindFriendsScreen(
                        candidates = exploreDeck,
                        sentRequestIds = sentRequestIds,
                        onConnect = { user ->
                            viewModel.sendFriendRequest(user)
                        },
                        onOpenProfile = { user ->
                            activeSubScreen = ActiveSubScreen.ExploreProfileDetail(user)
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                // SETTINGS SUB-SCREENS
                is ActiveSubScreen.Matchmaking -> {
                    MatchmakingScreen(
                        currentSettings = matchmakingSettings,
                        onSave = {
                            viewModel.updateMatchmaking(it)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.VerificationStatus -> {
                    VerificationStatusScreen(
                        hasUploadedSelfie = !userProfile.selfieCapturedUrl.isNullOrBlank(),
                        onVerifyNow = onNavigateToFaceVerify,
                        onReverify = onNavigateToFaceVerify,
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.Privacy -> {
                    PrivacySettingsScreen(
                        currentSettings = privacySettings,
                        onSave = {
                            viewModel.updatePrivacySettings(it)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.Notifications -> {
                    NotificationSettingsScreen(
                        currentSettings = notificationSettings,
                        onSave = {
                            viewModel.updateNotificationSettings(it)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.ProPlans -> {
                    ProPlansScreen(
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.Terms -> {
                    TermsScreen(onBack = { activeSubScreen = ActiveSubScreen.None })
                }

                is ActiveSubScreen.CommunityRules -> {
                    CommunityRulesScreen(onBack = { activeSubScreen = ActiveSubScreen.None })
                }

                is ActiveSubScreen.Account -> {
                    AccountSettingsScreen(
                        userProfile = userProfile,
                        onChangePhone = { activeSubScreen = ActiveSubScreen.PhoneNumber },
                        onChangeUsername = { activeSubScreen = ActiveSubScreen.EditUsername },
                        onChangeEmail = { activeSubScreen = ActiveSubScreen.Email },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.PhoneNumber -> {
                    PhoneNumberScreen(
                        currentCountry = userProfile.selectedCountry,
                        currentPhone = userProfile.phoneNumber,
                        otp = userProfile.otpCode,
                        isSendingCode = phoneAuthState.isSendingCode,
                        isVerifying = phoneAuthState.isVerifying,
                        isPhoneUpdate = phoneAuthState.isPhoneUpdate,
                        isPhoneUpdateComplete = phoneAuthState.isPhoneUpdateComplete,
                        codeSentVersion = phoneAuthState.codeSentVersion,
                        errorMessage = phoneAuthState.errorMessage,
                        onOtpChange = { viewModel.updateOtp(it) },
                        onRequestCode = { newPhone ->
                            val fullPhoneNumber =
                                userProfile.selectedCountry.dialCode +
                                    newPhone.filter(Char::isDigit)
                            if (phoneAuthActivity != null) {
                                viewModel.sendPhoneVerificationCode(
                                    phoneAuthActivity,
                                    fullPhoneNumber,
                                    updateExistingPhone = true,
                                    phoneNumberForProfile = newPhone
                                )
                            } else {
                                viewModel.setPhoneAuthError(
                                    "Unable to start phone verification right now."
                                )
                            }
                        },
                        onVerifyCode = { viewModel.verifyPhoneCode(it) },
                        onResendCode = { newPhone ->
                            val fullPhoneNumber =
                                userProfile.selectedCountry.dialCode +
                                    newPhone.filter(Char::isDigit)
                            if (phoneAuthActivity != null) {
                                viewModel.sendPhoneVerificationCode(
                                    phoneAuthActivity,
                                    fullPhoneNumber,
                                    forceResending = true,
                                    updateExistingPhone = true,
                                    phoneNumberForProfile = newPhone
                                )
                            } else {
                                viewModel.setPhoneAuthError(
                                    "Unable to resend the verification code right now."
                                )
                            }
                        },
                        onChangeNumber = { viewModel.resetPhoneAuth() },
                        onPhoneUpdated = {
                            viewModel.resetPhoneAuth()
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onOpenCountryPicker = onNavigateToCountryPicker,
                        onBack = {
                            viewModel.resetPhoneAuth()
                            activeSubScreen = ActiveSubScreen.None
                        }
                    )
                }

                is ActiveSubScreen.EditUsername -> {
                    EditUsernameScreen(
                        currentUsername = userProfile.username,
                        onCheckAvailability = { viewModel.isUsernameAvailable(it) },
                        onSave = { newUsername ->
                            viewModel.updateUsername(newUsername)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.Email -> {
                    EmailScreen(
                        currentEmail = userProfile.email,
                        onSave = { newEmail ->
                            viewModel.updateEmail(newEmail)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.LogoutConfirm -> {
                    LogoutConfirmationScreen(
                        onConfirmLogout = onLogout,
                        onCancel = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.DeleteAccount -> {
                    DeleteAccountScreen(
                        onCancel = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                // PROFILE SUB-SCREENS
                is ActiveSubScreen.EditPhotos -> {
                    EditPhotosScreen(
                        photos = userProfile.photos,
                        onAddPhoto = { viewModel.addPhoto(it) },
                        onReplacePhoto = { index, uri -> viewModel.replacePhoto(index, uri) },
                        onRemovePhoto = { viewModel.removePhoto(it) },
                        onSetMainPhoto = { viewModel.setMainPhoto(it) },
                        onSave = { activeSubScreen = ActiveSubScreen.None },
                        onBack = { activeSubScreen = ActiveSubScreen.None },
                        isUploading = userProfile.isUploadingPhoto,
                        errorMessage = userProfile.dataError
                    )
                }

                is ActiveSubScreen.EditBasicInfo -> {
                    EditBasicInfoScreen(
                        currentName = userProfile.fullName,
                        currentDob = userProfile.dateOfBirth,
                        currentGender = userProfile.gender,
                        currentCity = userProfile.city,
                        onSave = { name, dob, gender, city ->
                            viewModel.updateFullName(name)
                            viewModel.updateDateOfBirth(dob)
                            viewModel.updateGender(gender)
                            viewModel.updateCity(city)
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onOpenDatePicker = onNavigateToDatePicker,
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.EditAbout -> {
                    EditAboutScreen(
                        currentBio = userProfile.bio,
                        currentInterests = userProfile.selectedInterests,
                        currentLookingFor = userProfile.selectedLookingFor,
                        onSave = { bio, interests, lookingFor ->
                            viewModel.updateBio(bio)
                            interests.forEach { viewModel.toggleInterest(it) }
                            lookingFor.forEach { viewModel.toggleLookingFor(it) }
                            activeSubScreen = ActiveSubScreen.None
                        },
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                is ActiveSubScreen.PreviewProfile -> {
                    PreviewProfileScreen(
                        userProfile = userProfile,
                        onBack = { activeSubScreen = ActiveSubScreen.None }
                    )
                }

                // MAIN DASHBOARD TABS
                is ActiveSubScreen.None -> {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(200),
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            Routes.DASHBOARD_EXPLORE -> {
                                ExploreScreen(
                                    candidates = exploreDeck,
                                    selectedSegment = exploreSegment,
                                    onSegmentChange = { viewModel.setExploreSegment(it) },
                                    onLike = { user ->
                                        viewModel.swipeLike(user) { matched ->
                                            activeSubScreen = ActiveSubScreen.ItsAMatch(matched)
                                        }
                                    },
                                    onPass = { user -> viewModel.swipePass(user) },
                                    onRewind = { viewModel.rewind() },
                                    onCardClick = { user ->
                                        activeSubScreen = ActiveSubScreen.ExploreProfileDetail(user)
                                    },
                                    onOpenFilters = {
                                        activeSubScreen = ActiveSubScreen.Filters
                                    },
                                    onOpenSearch = {
                                        activeSubScreen = ActiveSubScreen.UsernameSearch
                                    }
                                )
                            }

                            Routes.DASHBOARD_CHATS -> {
                                ChatListScreen(
                                    conversations = conversations,
                                    onSelectConversation = { conv ->
                                        viewModel.selectConversation(conv)
                                        activeSubScreen = ActiveSubScreen.ChatThread(conv)
                                    },
                                    onLongPressConversation = { conv ->
                                        activeSubScreen = ActiveSubScreen.ChatOptions(conv.user)
                                    },
                                    onNewChatClick = {
                                        activeSubScreen = ActiveSubScreen.FindFriends
                                    },
                                    onGoToExplore = {
                                        currentTab = Routes.DASHBOARD_EXPLORE
                                    }
                                )
                            }

                            Routes.DASHBOARD_FRIENDS -> {
                                FriendsScreen(
                                    friendsList = friendsList,
                                    friendRequests = friendRequests,
                                    sentRequests = sentRequests,
                                    onAcceptRequest = { req -> viewModel.acceptFriendRequest(req) },
                                    onDeclineRequest = { req -> viewModel.declineFriendRequest(req) },
                                    onCancelSentRequest = { sent -> viewModel.cancelSentRequest(sent) },
                                    onOpenRequestDetail = { req ->
                                        activeSubScreen = ActiveSubScreen.RequestDetail(req)
                                    },
                                    onOpenFriendProfile = { friend ->
                                        activeSubScreen = ActiveSubScreen.FriendProfile(friend)
                                    },
                                    onOpenChat = { friend ->
                                        viewModel.openConversationForUser(friend) { conversation ->
                                            currentTab = Routes.DASHBOARD_CHATS
                                            activeSubScreen = ActiveSubScreen.ChatThread(conversation)
                                        }
                                    },
                                    onOpenFindFriends = {
                                        activeSubScreen = ActiveSubScreen.FindFriends
                                    }
                                )
                            }

                            Routes.DASHBOARD_SETTINGS -> {
                                SettingsScreen(
                                    userProfile = userProfile,
                                    notificationSettings = notificationSettings,
                                    onTogglePush = { checked ->
                                        viewModel.updateNotificationSettings(notificationSettings.copy(pushMatches = checked))
                                    },
                                    onToggleEmail = { checked ->
                                        viewModel.updateNotificationSettings(notificationSettings.copy(emailSummaries = checked))
                                    },
                                    onNavigateToEditProfile = {
                                        activeSubScreen = ActiveSubScreen.EditBasicInfo
                                    },
                                    onNavigateToVerificationStatus = {
                                        activeSubScreen = ActiveSubScreen.VerificationStatus
                                    },
                                    onNavigateToPhoneNumber = {
                                        activeSubScreen = ActiveSubScreen.PhoneNumber
                                    },
                                    onNavigateToMatchmaking = {
                                        activeSubScreen = ActiveSubScreen.Matchmaking
                                    },
                                    onNavigateToLocation = {
                                        activeSubScreen = ActiveSubScreen.Matchmaking
                                    },
                                    onNavigateToPrivacy = {
                                        activeSubScreen = ActiveSubScreen.Privacy
                                    },
                                    onNavigateToBlockedUsers = {
                                        activeSubScreen = ActiveSubScreen.BlockedUsers
                                    },
                                    onNavigateToProPlans = {
                                        activeSubScreen = ActiveSubScreen.ProPlans
                                    },
                                    onNavigateToTerms = {
                                        activeSubScreen = ActiveSubScreen.Terms
                                    },
                                    onNavigateToCommunityRules = {
                                        activeSubScreen = ActiveSubScreen.CommunityRules
                                    },
                                    onNavigateToAccount = {
                                        activeSubScreen = ActiveSubScreen.Account
                                    },
                                    onNavigateToLogoutConfirm = {
                                        activeSubScreen = ActiveSubScreen.LogoutConfirm
                                    },
                                    onNavigateToDeleteAccount = {
                                        activeSubScreen = ActiveSubScreen.DeleteAccount
                                    }
                                )
                            }

                            Routes.DASHBOARD_PROFILE -> {
                                MyProfileScreen(
                                    userProfile = userProfile,
                                    onNavigateToSettings = {
                                        currentTab = Routes.DASHBOARD_SETTINGS
                                    },
                                    onNavigateToEditPhotos = {
                                        activeSubScreen = ActiveSubScreen.EditPhotos
                                    },
                                    onNavigateToEditBasicInfo = {
                                        activeSubScreen = ActiveSubScreen.EditBasicInfo
                                    },
                                    onNavigateToEditAbout = {
                                        activeSubScreen = ActiveSubScreen.EditAbout
                                    },
                                    onNavigateToPreview = {
                                        activeSubScreen = ActiveSubScreen.PreviewProfile
                                    }
                                )
                            }

                            else -> {
                                ExploreScreen(
                                    candidates = exploreDeck,
                                    selectedSegment = exploreSegment,
                                    onSegmentChange = { viewModel.setExploreSegment(it) },
                                    onLike = { user ->
                                        viewModel.swipeLike(user) { matched ->
                                            activeSubScreen = ActiveSubScreen.ItsAMatch(matched)
                                        }
                                    },
                                    onPass = { user -> viewModel.swipePass(user) },
                                    onRewind = { viewModel.rewind() },
                                    onCardClick = { user ->
                                        activeSubScreen = ActiveSubScreen.ExploreProfileDetail(user)
                                    },
                                    onOpenFilters = { activeSubScreen = ActiveSubScreen.Filters },
                                    onOpenSearch = { activeSubScreen = ActiveSubScreen.UsernameSearch }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
