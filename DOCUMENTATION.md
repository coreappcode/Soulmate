# Soulmate Android App — Complete Technical & UI Documentation

---

## 1. Executive Summary & Architecture

- **Application Name:** Soulmate
- **Package Identifier:** `com.example`
- **Application ID:** `com.aistudio.datingapp.rvyb`
- **Architecture:** Single-Activity MVVM (Model-View-ViewModel) built 100% in Kotlin & Jetpack Compose
- **Design System:** Custom Material Design 3 (M3) "Aurora Warm" Design System with Edge-to-Edge system insets
- **Navigation:** Navigation Compose (`NavHost` / `NavController`) with slide + cross-fade page transitions
- **State Management:** `SoulmateViewModel` driving Kotlin Coroutines `MutableStateFlow` and immutable UI state representations

---

## 2. Colour Palette & Visual Design Pattern

The application uses an **Aurora Warm** aesthetic. Pure blacks (#000000) are reserved for sharp high-contrast form borders and buttons, while backgrounds use warm cream and blush tones.

| Token Name | Hex Code | Visual Swatch Role | Usage in App |
| :--- | :--- | :--- | :--- |
| **RosePrimary** | `#FFFF6B9D` | Warm Rose Pink | Aurora gradient start, like buttons, hearts |
| **OrchidPrimary** | `#FFC86DD7` | Radiant Orchid Purple | Aurora gradient middle, active tabs, main accents |
| **PeriwinklePrimary** | `#FF6E8EFB` | Soft Periwinkle Blue | Aurora gradient end, verified badges, action pills |
| **AppBackground** | `#FFFFF7F2` | Warm Cream | Canvas background across all screens |
| **CardBackground** | `#FFFFFFFF` | Pure White | Elevated content cards, message bubbles |
| **ElevatedSurface** | `#FFFDF2F8` | Soft Blush | Chips, secondary pill backgrounds |
| **DarkAccent** | `#FF2A1B3D` | Deep Plum | Primary contrasting elements |
| **HeaderBackground** | `#FFFBE9F1` | Soft Rose Blush | Top app bars, header banners |
| **BottomNavBackground** | `#FFF3E8FF` | Soft Lavender | Dashboard bottom navigation bar |
| **HairlineDivider** | `#FFF0E6EC` | Subtle Lilac Line | 1dp dividers between list items |
| **TextPrimary** | `#FF1A1423` | Dark Plum-Black | Headings, titles, primary body text |
| **TextSecondary** | `#FF6B5B7A` | Muted Plum-Gray | Subtitles, helper text, timestamps |
| **TextMuted** | `#FFA99BB5` | Lavender Gray | Placeholders, inactive icons |
| **BorderBlack** | `#FF000000` | Solid Black | 2dp high-visibility input & card borders |
| **SemanticSuccess** | `#FF34D399` | Mint Green | Online indicator, verification success |
| **SemanticError** | `#FFF87171` | Coral Red | Validation warnings, delete account, blocks |
| **SemanticWarning** | `#FFFBBF24` | Warm Amber | Match expiry, pending status |

### Gradients
- **`AuroraGradient`**: Linear diagonal sweep from `RosePrimary` → `OrchidPrimary` → `PeriwinklePrimary`.
- **`AuroraHorizontalGradient`**: Linear horizontal sweep used on primary action buttons (`GradientButton`) and active indicators.

---

## 3. Core Reusable UI Components, Buttons & Icons

### Buttons
1. **`GradientButton`** (`com.example.ui.components.Buttons.kt`)
   - **Style:** Full-width pill or rounded rectangle (18dp corner radius) filled with `AuroraHorizontalGradient`.
   - **Behavior:** Material ripple, press-down scale animation (`0.97f`), circular progress indicator when `loading = true`.
2. **`SoulOutlinedButton`** (`com.example.ui.components.Buttons.kt`)
   - **Style:** Outlined border (1.5dp `OrchidPrimary`), transparent background, orchid text.
   - **Behavior:** Press-down micro-scale animation, optional leading icon parameter.
3. **`FloatingActionButton` (FABs) & Icon Buttons**
   - Circular card buttons with 1dp border and brand shadow for Explore swipe actions (Pass, Superlike, Like).

### Input Components
1. **`SoulTextField`** (`com.example.ui.components.Inputs.kt`)
   - Clean white container with solid 2dp black border (`BorderBlack`), label in `PoppinsFontFamily`, optional leading and trailing icon slots, error message display.
2. **`SoulChip`** (`com.example.ui.components.Cards.kt`)
   - Selectable interest pill. Selected state uses `AuroraHorizontalGradient` with white text and black border; unselected state uses white background with black border.
3. **`ProgressDots`** (`com.example.ui.components.Navigation.kt`)
   - 5-dot sequential step indicator for the profile setup wizard.

### Primary Icons Used
- **Navigation:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Close`, `Icons.Default.ChevronRight`
- **Dashboard Tabs:** `Icons.Default.Explore`, `Icons.Default.Chat`, `Icons.Default.People`, `Icons.Default.Settings`, `Icons.Default.Person`
- **Photo & Camera:** `Icons.Default.PhotoLibrary`, `Icons.Default.CameraAlt`, `Icons.Default.Add`, `Icons.Default.Crop`
- **Actions & Status:** `Icons.Default.Favorite`, `Icons.Default.Star`, `Icons.Default.CheckCircle`, `Icons.Default.Verified`, `Icons.Default.Search`, `Icons.Default.FilterList`
- **Profile & Gender:** `Icons.Default.Male`, `Icons.Default.Female`, `Icons.Default.Transgender`, `Icons.Default.CalendarMonth`, `Icons.Default.LocationOn`

---

## 4. Complete Screen Directory & Navigations (All 55 Screens)

Below is the complete inventory of all 55 screens within the application, including route strings, trigger points, incoming/outgoing destinations, and action buttons.

---

### Group 1: Splash & Authentication

#### 1. Splash Screen
- **Route:** `splash` (`Routes.SPLASH`)
- **File:** `ui/screens/splash/SplashScreen.kt`
- **Description:** Warm branded landing screen with pulsing Aurora heart logo and animated title.
- **Navigations:**
  - Automatically transitions after 2000ms delay to `Routes.LANDING`.
- **Buttons / Actions:** None (automatic timer).
- **Icons:** Custom animated vector Heart / Aurora Icon.

#### 2. Landing Screen
- **Route:** `landing` (`Routes.LANDING`)
- **File:** `ui/screens/onboarding/LandingScreen.kt`
- **Description:** Welcoming hero screen displaying the app value proposition.
- **Navigations:**
  - "Get Started" / "Continue with Phone" → navigates to `Routes.PHONE_ENTRY`.
  - Terms of Service link → navigates to `Routes.SETTINGS_TERMS`.
- **Buttons:** `GradientButton` ("Get Started").
- **Icons:** `Icons.Default.Favorite`, `Icons.Default.Security`.

#### 3. Phone Entry Screen
- **Route:** `phone_entry` (`Routes.PHONE_ENTRY`)
- **File:** `ui/screens/auth/PhoneEntryScreen.kt`
- **Description:** International phone number input with Country Code selector trigger.
- **Navigations:**
  - Back Button → navigates back to `Routes.LANDING`.
  - Country Code selector tap → navigates to `Routes.COUNTRY_PICKER`.
  - "Send Code" → saves phone in ViewModel and navigates to `Routes.OTP_VERIFY`.
- **Buttons:** Country selector button, `GradientButton` ("Send Code").
- **Icons:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Phone`.

#### 4. Country Picker Screen
- **Route:** `country_picker` (`Routes.COUNTRY_PICKER`)
- **File:** `ui/screens/onboarding/CountryPickerScreen.kt`
- **Description:** Searchable list of 200+ world countries with flags and dial codes.
- **Navigations:**
  - Back Button → returns to `Routes.PHONE_ENTRY`.
  - Country item tap → selects country in `SoulmateViewModel` and pops back to `Routes.PHONE_ENTRY`.
- **Buttons:** Close button (`IconButton`), Country row tap target.
- **Icons:** `Icons.Default.Search`, `Icons.Default.Close`.

#### 5. OTP Verification Screen
- **Route:** `otp_verify` (`Routes.OTP_VERIFY`)
- **File:** `ui/screens/auth/OtpVerifyScreen.kt`
- **Description:** 6-digit PIN input with countdown timer and resend code action.
- **Navigations:**
  - Back Button → returns to `Routes.PHONE_ENTRY`.
  - "Verify" (or automatic 6-digit completion) → navigates to `Routes.BASIC_INFO`.
  - "Resend Code" → resets 30-second timer.
- **Buttons:** `GradientButton` ("Verify & Continue"), TextButton ("Resend Code").
- **Icons:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Lock`.

---

### Group 2: Profile Setup Flow (Onboarding Wizard)

#### 6. Basic Info Screen
- **Route:** `basic_info` (`Routes.BASIC_INFO`)
- **File:** `ui/screens/profile_setup/BasicInfoScreen.kt`
- **Description:** Step 1 of 5. Collects Full Name, Date of Birth (with age 18+ enforcement), and Gender.
- **Navigations:**
  - Back Button → returns to `Routes.OTP_VERIFY`.
  - Date of Birth field tap → triggers native Android `DatePickerDialog` (with M3 DatePicker fallback).
  - "Continue" → validates required fields and navigates to `Routes.USERNAME`.
- **Buttons:** Gender selection cards (`Man`, `Woman`, `Non-binary`), `GradientButton` ("Continue").
- **Icons:** `Icons.Default.CalendarMonth`, `Icons.Default.Male`, `Icons.Default.Female`, `Icons.Default.Transgender`.

#### 7. Date Picker Screen (Fallback Standalone)
- **Route:** `date_picker` (`Routes.DATE_PICKER`)
- **File:** `ui/screens/profile_setup/DatePickerScreen.kt`
- **Description:** Standalone date picker destination with calendar grid and age validation.
- **Navigations:**
  - Back Button / "Confirm" → returns to `Routes.BASIC_INFO` with formatted date string.
- **Buttons:** `GradientButton` ("Confirm Date").
- **Icons:** `Icons.Default.CalendarToday`.

#### 8. Username Selection Screen
- **Route:** `username` (`Routes.USERNAME`)
- **File:** `ui/screens/profile_setup/UsernameScreen.kt`
- **Description:** Step 2 of 5. Unique `@handle` selection with real-time format checking (3-20 chars, alphanumeric).
- **Navigations:**
  - Back Button → returns to `Routes.BASIC_INFO`.
  - "Continue" → sets username in ViewModel and navigates to `Routes.PHOTOS`.
- **Buttons:** `GradientButton` ("Claim Username").
- **Icons:** `Icons.Default.AlternateEmail`, `Icons.Default.CheckCircle`.

#### 9. Photos Setup Screen
- **Route:** `photos` (`Routes.PHOTOS`)
- **File:** `ui/screens/profile_setup/PhotosSetupScreen.kt`
- **Description:** Step 3 of 5. 6-slot photo grid requiring at least 2 photos to continue. Allows setting main photo and reordering.
- **Navigations:**
  - Back Button → returns to `Routes.USERNAME`.
  - Empty Slot tap → navigates to `Routes.ADD_PHOTO`.
  - "Choose from Gallery" button → launches real Android PhotoPicker (`ActivityResultContracts.PickVisualMedia`).
  - "Continue" (enabled when >= 2 photos) → navigates to `Routes.FACE_CAPTURE`.
- **Buttons:** `SoulOutlinedButton` ("Choose from Gallery"), `GradientButton` ("Continue"), Remove Photo ("X") icon buttons.
- **Icons:** `Icons.Default.Add`, `Icons.Default.PhotoLibrary`, `Icons.Default.Delete`, `Icons.Default.Star`.

#### 10. Add Photo Screen
- **Route:** `add_photo` (`Routes.ADD_PHOTO`)
- **File:** `ui/screens/profile_setup/AddPhotoScreen.kt`
- **Description:** Modal source chooser offering Gallery or Camera capture.
- **Navigations:**
  - Back Button → returns to `Routes.PHOTOS`.
  - "Choose from Gallery" card → launches real Android PhotoPicker (`PickVisualMedia`). On selection, returns photo URI and pops back.
  - "Take Photo" card → launches device Camera (`TakePicturePreview`). On capture, writes bitmap to cache and pops back.
- **Buttons:** `PhotoSourceCard` (Gallery), `PhotoSourceCard` (Camera).
- **Icons:** `Icons.Default.PhotoLibrary`, `Icons.Default.CameraAlt`.

#### 11. Face Capture Screen
- **Route:** `face_capture` (`Routes.FACE_CAPTURE`)
- **File:** `ui/screens/profile_setup/FaceCaptureScreen.kt`
- **Description:** Step 4 of 5. Selfie verification screen with face alignment oval guide. Directly integrates the real Android OS camera (`ActivityResultContracts.TakePicturePreview`) to snap a live selfie, plus an Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) fallback to choose a selfie directly from device media. Captured images are compressed and stored to app cache and rendered inside the circular guide.
- **Navigations:**
  - Back Button → returns to `Routes.PHOTOS`.
  - "Take Selfie (Camera)" → launches native device camera for real photo capture.
  - "Choose from Gallery" → launches real Android photo picker modal (`PickVisualMedia`).
  - "Use This" → saves captured image URI in ViewModel and navigates to `Routes.VERIFY_PROCESSING`.
  - "Retake" → discards current image and resets camera capture prompt.
- **Buttons:** `GradientButton` ("Take Selfie (Camera)"), `SoulOutlinedButton` ("Choose from Gallery"), `GradientButton` ("Use This"), `SoulOutlinedButton` ("Retake").
- **Icons:** `Icons.Default.Person`, `Icons.Default.CameraAlt`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 12. Verification Processing Screen
- **Route:** `verify_processing` (`Routes.VERIFY_PROCESSING`)
- **File:** `ui/screens/profile_setup/VerificationProcessingScreen.kt`
- **Description:** Automated biometric analysis animation with 3 checklist items (Face Detected, Liveness Confirmed, Profile Match).
- **Navigations:**
  - Automatically transitions after completion to `Routes.VERIFY_SUCCESS`.
- **Buttons:** None (automated pipeline).
- **Icons:** `Icons.Default.Autorenew`, `Icons.Default.CheckCircle`.

#### 13. Verification Success Screen
- **Route:** `verify_success` (`Routes.VERIFY_SUCCESS`)
- **File:** `ui/screens/profile_setup/VerificationSuccessScreen.kt`
- **Description:** Celebration screen showcasing the blue verified checkmark badge.
- **Navigations:**
  - "Continue Setup" → navigates to `Routes.ABOUT_YOU`.
- **Buttons:** `GradientButton` ("Continue Setup").
- **Icons:** `Icons.Default.Verified`, `Icons.Default.Check`.

#### 14. About You Screen
- **Route:** `about_you` (`Routes.ABOUT_YOU`)
- **File:** `ui/screens/profile_setup/AboutYouScreen.kt`
- **Description:** Step 5 of 5. Bio input (min 10 chars, max 500), interest chip selector (up to 10), and single-selection "Who are you looking for? *" (Men, Women, Non-binary, All).
- **Navigations:**
  - Back Button → returns to `Routes.VERIFY_SUCCESS`.
  - "Continue" → validates required fields (marked with `*`) and navigates to `Routes.PERMISSIONS`.
- **Buttons:** Interest chips (`SoulChip`), `LookingForCard` (single selection), `GradientButton` ("Continue").
- **Icons:** `Icons.Default.CheckCircle`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 15. Permissions Screen
- **Route:** `permissions` (`Routes.PERMISSIONS`)
- **File:** `ui/screens/profile_setup/PermissionsScreen.kt`
- **Description:** Final onboarding step requesting Location and Notification permissions. Tapping each permission card triggers the real Android OS permission request dialogs using Jetpack ActivityResult contracts (`ActivityResultContracts.RequestMultiplePermissions` for `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`, and `ActivityResultContracts.RequestPermission` for `POST_NOTIFICATIONS`).
- **Navigations:**
  - Back Button → returns to `Routes.ABOUT_YOU`.
  - Location Card Tap → launches native Android Location permission prompt (`ACCESS_FINE_LOCATION`).
  - Notifications Card Tap → launches native Android Notification permission prompt (`POST_NOTIFICATIONS` on Android 13+).
  - "Enter Soulmate" / "Skip for now" → triggers celebration confetti animation and navigates to `Routes.DASHBOARD` (clearing entire onboarding backstack).
- **Buttons:** `PermissionCard` (Location), `PermissionCard` (Notifications), `GradientButton` ("Enter Soulmate"), `SoulTextButton` ("Skip for now").
- **Icons:** `Icons.Default.LocationOn`, `Icons.Default.Notifications`, `Icons.Default.Check`.

---

### Group 3: Main Dashboard Container

#### 16. Dashboard Screen
- **Route:** `dashboard` (`Routes.DASHBOARD`)
- **File:** `ui/screens/dashboard/DashboardScreen.kt`
- **Description:** Main app container featuring a custom bottom navigation bar hosting 5 primary tabs:
  1. **Explore:** `Routes.DASHBOARD_EXPLORE`
  2. **Chats:** `Routes.DASHBOARD_CHATS`
  3. **Friends:** `Routes.DASHBOARD_FRIENDS`
  4. **Settings:** `Routes.DASHBOARD_SETTINGS`
  5. **Profile:** `Routes.DASHBOARD_PROFILE`
- **Navigations:** Tab switching updates the active sub-screen within the scaffold.
- **Icons:** `Icons.Default.Explore`, `Icons.Default.Chat`, `Icons.Default.People`, `Icons.Default.Settings`, `Icons.Default.Person`.

---

### Group 4: Explore & Matchmaking Sub-Screens

#### 17. Explore Screen
- **Route:** `dashboard/explore` (`Routes.DASHBOARD_EXPLORE`)
- **File:** `ui/screens/explore/ExploreScreen.kt`
- **Description:** Core card swipe deck with segment switcher (Nearby / Country / Global). Drag cards left (pass) or right (like).
- **Navigations:**
  - Filter Icon in header → navigates to `Routes.EXPLORE_FILTERS`.
  - Search Icon in header → navigates to `Routes.EXPLORE_SEARCH`.
  - Card Body tap → navigates to `Routes.EXPLORE_PROFILE_DETAIL` for the active user.
  - Right Swipe / Like on matched user → opens `Routes.ITS_A_MATCH`.
- **Buttons:** Pass Button ("X"), Superlike Button (Star), Like Button (Heart), Rewind Button.
- **Icons:** `Icons.Default.Close`, `Icons.Default.Star`, `Icons.Default.Favorite`, `Icons.Default.FilterList`, `Icons.Default.Search`.

#### 18. Filters Screen
- **Route:** `explore/filters` (`Routes.EXPLORE_FILTERS`)
- **File:** `ui/screens/explore/FiltersScreen.kt`
- **Description:** Discovery preferences: Age Range slider (18-60), Maximum Distance slider (1-100 km), Interested In selector, Online Only and Verified Only toggles.
- **Navigations:**
  - Back Button / "Apply Filters" → applies filter state in ViewModel and pops back to Explore.
  - "Reset" button → resets filters to defaults.
- **Buttons:** `GradientButton` ("Apply Filters"), TextButton ("Reset").
- **Icons:** `Icons.Default.Tune`, `Icons.Default.Check`.

#### 19. Full Profile Screen (Explore Detail)
- **Route:** `explore/profile_detail` (`Routes.EXPLORE_PROFILE_DETAIL`)
- **File:** `ui/screens/explore/FullProfileScreen.kt`
- **Description:** Comprehensive profile view with photo carousel, verified badge, bio, lifestyle tags, astrological sign, job, education, and distance.
- **Navigations:**
  - Back Button → returns to Explore deck.
  - Like FAB → likes user and pops back (or triggers match).
  - Pass FAB → passes user and pops back.
  - "Send Friend Request" → sends request and shows confirmation toast.
- **Buttons:** Floating Action Buttons (Like / Pass / Friend Request).
- **Icons:** `Icons.Default.Favorite`, `Icons.Default.Close`, `Icons.Default.PersonAdd`, `Icons.Default.Verified`.

#### 20. It's A Match Screen
- **Route:** `explore/its_a_match` (`Routes.ITS_A_MATCH`)
- **File:** `ui/screens/explore/ItsAMatchScreen.kt`
- **Description:** Celebration screen shown when two users like each other. Features dual floating avatar circles and confetti animation.
- **Navigations:**
  - "Send a Message" → navigates directly to `Routes.CHAT_THREAD` with the matched user.
  - "Keep Swiping" → pops back to Explore deck.
- **Buttons:** `GradientButton` ("Send a Message"), `SoulOutlinedButton` ("Keep Swiping").
- **Icons:** `Icons.Default.Favorite`, `Icons.Default.Chat`.

#### 21. Username Search Screen
- **Route:** `explore/search` (`Routes.EXPLORE_SEARCH`)
- **File:** `ui/screens/explore/UsernameSearchScreen.kt`
- **Description:** Live search bar to discover profiles by handle (e.g., `@maya`).
- **Navigations:**
  - Back Button → returns to Explore deck.
  - Result row tap → sets selected user and navigates to `Routes.EXPLORE_PROFILE_DETAIL`.
- **Buttons:** Clear search button, Result row tap targets.
- **Icons:** `Icons.Default.Search`, `Icons.Default.Close`, `Icons.AutoMirrored.Filled.ArrowBack`.

---

### Group 5: Chats & Messaging Sub-Screens

#### 22. Chat List Screen
- **Route:** `dashboard/chats` (`Routes.DASHBOARD_CHATS`)
- **File:** `ui/screens/chats/ChatListScreen.kt`
- **Description:** Inbox with New Matches horizontal carousel and Conversation list showing last message, unread badges, and verified status.
- **Navigations:**
  - Tapping an unverified match → navigates to `Routes.CHAT_VERIFY_TO_UNLOCK`.
  - Tapping an active conversation → sets active thread and navigates to `Routes.CHAT_THREAD`.
- **Buttons:** Match avatar circles, Conversation item cards.
- **Icons:** `Icons.Default.CheckCircle`, `Icons.Default.Lock`.

#### 23. Verify To Unlock Screen
- **Route:** `chat/verify_to_unlock` (`Routes.CHAT_VERIFY_TO_UNLOCK`)
- **File:** `ui/screens/chats/VerifyToUnlockScreen.kt`
- **Description:** Safety gate informing users that face verification is required before chatting with verified members.
- **Navigations:**
  - Back Button → returns to Chat List.
  - "Verify Now" → navigates to `Routes.FACE_CAPTURE`.
- **Buttons:** `GradientButton` ("Verify Now").
- **Icons:** `Icons.Default.Shield`, `Icons.Default.Verified`.

#### 24. Chat Thread Screen
- **Route:** `chat/thread` (`Routes.CHAT_THREAD`)
- **File:** `ui/screens/chats/ChatThreadScreen.kt`
- **Description:** Real-time messaging screen with message bubbles, status ticks (sent, delivered, read), attachment button, and text field.
- **Navigations:**
  - Back Button → returns to Chat List.
  - Header avatar / name tap → navigates to `Routes.CHAT_INFO`.
  - More options (3 vertical dots) → navigates to `Routes.CHAT_OPTIONS`.
  - Attachment (+) button → navigates to `Routes.CHAT_ATTACHMENT`.
- **Buttons:** Send button (`IconButton`), Attachment button, Options menu button.
- **Icons:** `Icons.AutoMirrored.Filled.Send`, `Icons.Default.AddPhotoAlternate`, `Icons.Default.MoreVert`, `Icons.Default.Verified`.

#### 25. Chat Options Screen
- **Route:** `chat/options` (`Routes.CHAT_OPTIONS`)
- **File:** `ui/screens/chats/ChatOptionsScreen.kt`
- **Description:** Action sheet with options: View Profile, Mute Notifications, Clear Chat, Block User, Report User.
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "View Profile" → navigates to `Routes.EXPLORE_PROFILE_DETAIL`.
  - "Block User" → adds user to blocked list and pops back to Chat List.
- **Buttons:** Action list items.
- **Icons:** `Icons.Default.Person`, `Icons.Default.NotificationsOff`, `Icons.Default.Block`, `Icons.Default.Report`.

#### 26. Chat Info Screen
- **Route:** `chat/info` (`Routes.CHAT_INFO`)
- **File:** `ui/screens/chats/ChatInfoScreen.kt`
- **Description:** Contact card showing user photo, bio, shared media gallery, and safety settings.
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "View Full Profile" → navigates to `Routes.EXPLORE_PROFILE_DETAIL`.
- **Buttons:** `SoulOutlinedButton` ("View Full Profile"), Block/Mute buttons.
- **Icons:** `Icons.Default.PermMedia`, `Icons.Default.Lock`.

#### 27. Blocked Users Screen
- **Route:** `chat/blocked_users` (`Routes.BLOCKED_USERS`)
- **File:** `ui/screens/chats/BlockedUsersScreen.kt`
- **Description:** List of currently blocked accounts with option to unblock.
- **Navigations:**
  - Back Button → returns to previous screen.
  - "Unblock" button → removes from blocked list in ViewModel.
- **Buttons:** `SoulOutlinedButton` ("Unblock").
- **Icons:** `Icons.Default.Block`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 28. Send Attachment Screen
- **Route:** `chat/attachment` (`Routes.CHAT_ATTACHMENT`)
- **File:** `ui/screens/chats/SendAttachmentScreen.kt`
- **Description:** Media attachment composer. Integrates real Android PhotoPicker (`ActivityResultContracts.PickVisualMedia`) for device gallery media and real Camera capture (`ActivityResultContracts.TakePicturePreview`) for snapping instant photos, provides preview with caption input, and attaches to conversation.
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "Photo Gallery" → launches real Android Photo Picker (`PickVisualMedia`).
  - "Take Instant Photo" → launches native device camera (`TakePicturePreview`).
  - "Send Attachment" → appends image message to Chat Thread and pops back.
- **Buttons:** `AttachmentOptionCard` (Gallery / Camera / Audio / Document), `GradientButton` ("Send Photo").
- **Icons:** `Icons.Default.PhotoLibrary`, `Icons.Default.CameraAlt`, `Icons.Default.Headphones`, `Icons.Default.Description`, `Icons.AutoMirrored.Filled.Send`.

---

### Group 6: Friends & Social Sub-Screens

#### 29. Friends Screen
- **Route:** `dashboard/friends` (`Routes.DASHBOARD_FRIENDS`)
- **File:** `ui/screens/friends/FriendsScreen.kt`
- **Description:** Friends hub with tabs: "My Friends" (grid of established connections) and "Requests" (received and sent invitations).
- **Navigations:**
  - Header "+" icon → navigates to `Routes.FIND_FRIENDS`.
  - Friend card tap → navigates to `Routes.FRIEND_PROFILE`.
  - Request card tap → navigates to `Routes.FRIEND_REQUEST_DETAIL`.
- **Buttons:** Tab selector pills, "Accept" / "Decline" buttons on request cards.
- **Icons:** `Icons.Default.PersonAdd`, `Icons.Default.Check`, `Icons.Default.Close`.

#### 30. Friend Request Detail Screen
- **Route:** `friends/request_detail` (`Routes.FRIEND_REQUEST_DETAIL`)
- **File:** `ui/screens/friends/RequestDetailScreen.kt`
- **Description:** Full-screen inspection of a received friend request with sender's full bio, photos, and mutual interests.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Accept Request" → moves user to Friends list and pops back.
  - "Decline Request" → removes request and pops back.
- **Buttons:** `GradientButton` ("Accept Request"), `SoulOutlinedButton` ("Decline").
- **Icons:** `Icons.Default.CheckCircle`, `Icons.Default.Cancel`.

#### 31. Friend Profile Screen
- **Route:** `friends/friend_profile` (`Routes.FRIEND_PROFILE`)
- **File:** `ui/screens/friends/FriendProfileScreen.kt`
- **Description:** Friend's full profile displaying connection anniversary, common interests, and direct message shortcut.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Message" button → opens `Routes.CHAT_THREAD` with this friend.
  - "Remove Friend" → confirms removal and pops back.
- **Buttons:** `GradientButton` ("Send Message"), `SoulOutlinedButton` ("Remove Friend").
- **Icons:** `Icons.Default.Chat`, `Icons.Default.PersonRemove`.

#### 32. Find Friends Screen
- **Route:** `friends/find_friends` (`Routes.FIND_FRIENDS`)
- **File:** `ui/screens/friends/FindFriendsScreen.kt`
- **Description:** Discovery list of people nearby with "Send Request" buttons.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Send Request" tap → sends request and updates button to "Requested".
- **Buttons:** Request buttons on user items.
- **Icons:** `Icons.Default.Search`, `Icons.Default.PersonAdd`.

---

### Group 7: Settings & Account Management Sub-Screens

#### 33. Settings Screen (Hub)
- **Route:** `dashboard/settings` (`Routes.DASHBOARD_SETTINGS`)
- **File:** `ui/screens/settings/SettingsScreen.kt`
- **Description:** Categorized settings menu: Account, Matchmaking, Verification, Privacy, Notifications, Pro Plans, Terms, Community Rules, Logout, Delete Account.
- **Navigations:**
  - Menu items navigate to their respective sub-screens (detailed below).
- **Icons:** `Icons.Default.Tune`, `Icons.Default.VerifiedUser`, `Icons.Default.Lock`, `Icons.Default.Notifications`, `Icons.Default.Star`, `Icons.Default.ExitToApp`, `Icons.Default.Delete`.

#### 34. Matchmaking Settings Screen
- **Route:** `settings/matchmaking` (`Routes.SETTINGS_MATCHMAKING`)
- **File:** `ui/screens/settings/MatchmakingScreen.kt`
- **Description:** "Show Me" orientation filter, Age range slider, Distance radius slider, and Global Match toggle.
- **Navigations:** Back Button → returns to Settings Hub.
- **Buttons:** Filter pills, Sliders.
- **Icons:** `Icons.Default.Tune`, `Icons.Default.Public`.

#### 35. Verification Status Screen
- **Route:** `settings/verification` (`Routes.SETTINGS_VERIFICATION`)
- **File:** `ui/screens/settings/VerificationStatusScreen.kt`
- **Description:** Displays user's verification badge status (Active Verified vs. Unverified) with security benefits explanation.
- **Navigations:**
  - Back Button → returns to Settings Hub.
  - "Re-Verify Face" → navigates to `Routes.FACE_CAPTURE`.
- **Buttons:** `GradientButton` ("Re-Verify Face").
- **Icons:** `Icons.Default.Verified`, `Icons.Default.Security`.

#### 36. Privacy Settings Screen
- **Route:** `settings/privacy` (`Routes.SETTINGS_PRIVACY`)
- **File:** `ui/screens/settings/PrivacySettingsScreen.kt`
- **Description:** Privacy toggles: Show Online Status, Show Distance, Show Age, Allow Non-Friends Messaging, Send Read Receipts.
- **Navigations:** Back Button → returns to Settings Hub.
- **Buttons:** Toggle switches.
- **Icons:** `Icons.Default.Visibility`, `Icons.Default.Lock`.

#### 37. Notification Settings Screen
- **Route:** `settings/notifications` (`Routes.SETTINGS_NOTIFICATIONS`)
- **File:** `ui/screens/settings/NotificationSettingsScreen.kt`
- **Description:** Notification toggles: New Matches, Messages, Email Summaries, Sound & Vibration, Marketing Promotions.
- **Navigations:** Back Button → returns to Settings Hub.
- **Buttons:** Toggle switches.
- **Icons:** `Icons.Default.Notifications`, `Icons.Default.VolumeUp`.

#### 38. Pro Plans Screen
- **Route:** `settings/pro_plans` (`Routes.SETTINGS_PRO_PLANS`)
- **File:** `ui/screens/settings/ProPlansScreen.kt`
- **Description:** Subscription tier comparison (Soulmate Free vs. Soulmate Gold / Platinum): Unlimited Likes, Rewinds, Global Passport, 5 Superlikes/week.
- **Navigations:**
  - Back Button → returns to Settings Hub.
  - "Upgrade to Gold" → activates Pro state in ViewModel (`isProUser = true`) and displays celebration banner.
- **Buttons:** Tier selector cards, `GradientButton` ("Upgrade to Soulmate Gold").
- **Icons:** `Icons.Default.Star`, `Icons.Default.CheckCircle`, `Icons.Default.Bolt`.

#### 39. Terms Screen
- **Route:** `settings/terms` (`Routes.SETTINGS_TERMS`)
- **File:** `ui/screens/settings/TermsScreen.kt`
- **Description:** Comprehensive Terms of Service, Privacy Policy, and Community Guidelines.
- **Navigations:** Back Button → returns to Settings Hub (or Landing Screen).
- **Icons:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Gavel`.

#### 40. Community Rules Screen
- **Route:** `settings/community_rules` (`Routes.SETTINGS_COMMUNITY_RULES`)
- **File:** `ui/screens/settings/TermsScreen.kt`
- **Description:** Community standards covering respect, safety, anti-harassment, and photo policy.
- **Navigations:** Back Button → returns to Settings Hub.
- **Icons:** `Icons.Default.Security`, `Icons.Default.Favorite`.

#### 41. Account Settings Screen
- **Route:** `settings/account` (`Routes.SETTINGS_ACCOUNT`)
- **File:** `ui/screens/settings/AccountSettingsScreen.kt`
- **Description:** Account overview displaying linked Phone, Username, and Email.
- **Navigations:**
  - Phone Row → navigates to `Routes.SETTINGS_PHONE`.
  - Username Row → navigates to `Routes.SETTINGS_USERNAME`.
  - Email Row → navigates to `Routes.SETTINGS_EMAIL`.
  - Logout Row → navigates to `Routes.SETTINGS_LOGOUT_CONFIRM`.
  - Delete Account Row → navigates to `Routes.SETTINGS_DELETE_ACCOUNT`.
- **Icons:** `Icons.Default.Phone`, `Icons.Default.AlternateEmail`, `Icons.Default.Email`, `Icons.Default.ExitToApp`.

#### 42. Logout Confirmation Screen
- **Route:** `settings/logout_confirm` (`Routes.SETTINGS_LOGOUT_CONFIRM`)
- **File:** `ui/screens/settings/LogoutConfirmationScreen.kt`
- **Description:** Confirmation modal to safely sign out of the current device.
- **Navigations:**
  - "Cancel" → pops back to Account Settings.
  - "Log Out" → clears active session and navigates to `Routes.LANDING`.
- **Buttons:** `GradientButton` ("Log Out"), `SoulOutlinedButton` ("Cancel").
- **Icons:** `Icons.Default.ExitToApp`, `Icons.Default.Warning`.

#### 43. Delete Account Screen
- **Route:** `settings/delete_account` (`Routes.SETTINGS_DELETE_ACCOUNT`)
- **File:** `ui/screens/settings/DeleteAccountScreen.kt`
- **Description:** Irreversible account deletion flow with reason selector and "DELETE" text confirmation input.
- **Navigations:**
  - "Keep My Account" → pops back to Account Settings.
  - "Permanently Delete" (enabled only when user types "DELETE") → wipes state and navigates to `Routes.LANDING`.
- **Buttons:** Red Delete Button, `SoulOutlinedButton` ("Keep My Account").
- **Icons:** `Icons.Default.DeleteForever`, `Icons.Default.Warning`.

#### 44. Phone Number Edit Screen
- **Route:** `settings/phone` (`Routes.SETTINGS_PHONE`)
- **File:** `ui/screens/settings/PhoneNumberScreen.kt`
- **Description:** Change registered phone number with country selector and SMS verification prompt.
- **Navigations:** Back Button / "Save Phone" → updates phone in ViewModel and returns to Account Settings.
- **Buttons:** `GradientButton` ("Save Phone Number").
- **Icons:** `Icons.Default.Phone`.

#### 45. Edit Username Screen
- **Route:** `settings/username` (`Routes.SETTINGS_USERNAME`)
- **File:** `ui/screens/settings/EditUsernameScreen.kt`
- **Description:** Change `@handle` from Settings with validation.
- **Navigations:** Back Button / "Save Username" → updates username in ViewModel and returns.
- **Buttons:** `GradientButton` ("Save Username").
- **Icons:** `Icons.Default.AlternateEmail`.

#### 46. Email Screen
- **Route:** `settings/email` (`Routes.SETTINGS_EMAIL`)
- **File:** `ui/screens/settings/EmailScreen.kt`
- **Description:** Add or update email address for account recovery.
- **Navigations:** Back Button / "Save Email" → updates email in ViewModel and returns.
- **Buttons:** `GradientButton` ("Save Email").
- **Icons:** `Icons.Default.Email`.

---

### Group 8: Profile & Editing Sub-Screens

#### 47. My Profile Screen
- **Route:** `dashboard/profile` (`Routes.DASHBOARD_PROFILE`)
- **File:** `ui/screens/profile/MyProfileScreen.kt`
- **Description:** User profile overview showing profile photo, completion progress bar (e.g. 85%), quick-action buttons, bio preview, and interest chips.
- **Navigations:**
  - "Edit Photos" → navigates to `Routes.PROFILE_EDIT_PHOTOS`.
  - "Edit Basic Info" → navigates to `Routes.PROFILE_EDIT_BASIC`.
  - "Edit Username" → navigates to `Routes.PROFILE_EDIT_USERNAME`.
  - "Edit About Me" → navigates to `Routes.PROFILE_EDIT_ABOUT`.
  - "Preview" → navigates to `Routes.PROFILE_PREVIEW`.
- **Buttons:** Edit buttons, Preview Button (`SoulOutlinedButton`).
- **Icons:** `Icons.Default.Edit`, `Icons.Default.PhotoCamera`, `Icons.Default.Visibility`, `Icons.Default.Verified`.

#### 48. Edit Photos Screen
- **Route:** `profile/edit_photos` (`Routes.PROFILE_EDIT_PHOTOS`)
- **File:** `ui/screens/profile/EditPhotosScreen.kt`
- **Description:** Manage 6 profile photos. Tapping empty slot launches real Android PhotoPicker (`PickVisualMedia`). Tapping an existing photo sets it as Main. Includes built-in Photo Crop & Adjustment view.
- **Navigations:**
  - Back Button / "Save Changes" → persists photo ordering in ViewModel and returns to My Profile.
  - Photo Picker → opens real system gallery via `PickVisualMedia`.
- **Buttons:** `GradientButton` ("Save Changes"), Crop Adjustment controls (Rotate, Confirm Crop, Cancel).
- **Icons:** `Icons.Default.PhotoLibrary`, `Icons.Default.Crop`, `Icons.Default.Star`, `Icons.Default.Delete`.

#### 49. Edit Basic Info Screen
- **Route:** `profile/edit_basic` (`Routes.PROFILE_EDIT_BASIC`)
- **File:** `ui/screens/profile/EditBasicInfoScreen.kt`
- **Description:** Edit Full Name, Date of Birth, Gender, Height, Job Title, Education, and City.
- **Navigations:** Back Button / "Save" → updates basic info in ViewModel and returns to My Profile.
- **Buttons:** `GradientButton` ("Save Changes").
- **Icons:** `Icons.Default.Person`, `Icons.Default.Work`, `Icons.Default.School`, `Icons.Default.LocationCity`.

#### 50. Edit About Me Screen
- **Route:** `profile/edit_about` (`Routes.PROFILE_EDIT_ABOUT`)
- **File:** `ui/screens/profile/EditAboutScreen.kt`
- **Description:** Edit Bio text (with 500-char counter), toggle interests (up to 10), and modify "Who are you looking for? *".
- **Navigations:** Back Button / "Save" → updates bio and interests in ViewModel and returns.
- **Buttons:** `GradientButton` ("Save Changes"), `SoulChip` interest selectors, `LookingForCard` options.
- **Icons:** `Icons.Default.FormatQuote`, `Icons.Default.Interests`.

#### 51. Preview Profile Screen
- **Route:** `profile/preview` (`Routes.PROFILE_PREVIEW`)
- **File:** `ui/screens/profile/PreviewProfileScreen.kt`
- **Description:** "Preview as others see you" simulation, rendering user's exact profile card as it appears in the Explore swipe deck.
- **Navigations:** Back Button → returns to My Profile.
- **Buttons:** Floating close button, Photo carousel indicators.
- **Icons:** `Icons.Default.Close`, `Icons.Default.Verified`.

---

### Group 9: System & Fallback Screens

#### 52. Loading Screen
- **Route:** `system/loading` (`Routes.SYSTEM_LOADING`)
- **File:** `ui/screens/system/LoadingScreen.kt`
- **Description:** Reusable shimmer skeleton loading state (`ShimmerBox`) used during network simulation and cold transitions.
- **Icons:** Shimmer gradients.

#### 53. Error Screen
- **Route:** `system/error` (`Routes.SYSTEM_ERROR`)
- **File:** `ui/screens/system/ErrorScreen.kt`
- **Description:** Graceful error recovery screen with error illustration and "Try Again" action.
- **Navigations:** "Try Again" → executes retry callback or returns to `Routes.DASHBOARD`.
- **Buttons:** `GradientButton` ("Try Again").
- **Icons:** `Icons.Default.ErrorOutline`, `Icons.Default.Refresh`.

#### 54. No Internet Screen
- **Route:** `system/no_internet` (`Routes.SYSTEM_NO_INTERNET`)
- **File:** `ui/screens/system/NoInternetScreen.kt`
- **Description:** Offline state display informing users that an active network connection is required.
- **Navigations:** "Check Connection" → tests connectivity and resumes applet.
- **Buttons:** `GradientButton` ("Check Connection").
- **Icons:** `Icons.Default.WifiOff`, `Icons.Default.Refresh`.

#### 55. Location Required Screen
- **Route:** `system/location_required` (`Routes.SYSTEM_LOCATION_REQUIRED`)
- **File:** `ui/screens/system/LocationRequiredScreen.kt`
- **Description:** Full-screen rationale explaining why location is required to calculate accurate proximity for soulmate matches.
- **Navigations:**
  - "Enable Location" → launches permission prompt or navigates back to Explore.
  - "Browse Country-Wide" → falls back to country-wide discovery without GPS.
- **Buttons:** `GradientButton` ("Enable Location"), `SoulOutlinedButton` ("Browse Country-Wide").
- **Icons:** `Icons.Default.LocationOff`, `Icons.Default.NearMe`.

---

## 5. Exhaustive Breakdown of Real Android OS/SDK Implementations vs. External Services

This section explicitly documents which parts of the application utilize real native Android OS / SDK capabilities versus client-side state / external services.

> **CRITICAL ARCHITECTURAL GUARANTEE — ZERO DUMMY MEDIA PICKERS:**
> There are **NO dummy gallery image pickers or dummy camera pickers anywhere in this application**. Every screen where an image or photo can be selected or captured utilizes genuine Android Jetpack Activity Result contracts (`ActivityResultContracts.PickVisualMedia`, `ActivityResultContracts.TakePicturePreview`). Device storage, photo gallery, and hardware camera are directly interfaced with zero mock UI pickers.

### Real Android Integrations (Fully Implemented)

1. **Android Photo Picker (`ActivityResultContracts.PickVisualMedia`):**
   - **Where:** `AddPhotoScreen.kt`, `PhotosSetupScreen.kt`, `FaceCaptureScreen.kt`, `EditPhotosScreen.kt`, `SendAttachmentScreen.kt`.
   - **How it works:** Directly leverages the Android system photo picker via `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())`. Invokes the OS-level modal allowing users to select genuine photos from device storage, Google Photos, or cloud drives.
   - **Rendering:** Loaded via `coil.compose.AsyncImage` which natively supports `content://` and `file://` local URIs with automatic decoding, downsampling, and caching.
2. **Camera Snap & Selfie Capture (`ActivityResultContracts.TakePicturePreview`):**
   - **Where:** `AddPhotoScreen.kt`, `FaceCaptureScreen.kt`, `SendAttachmentScreen.kt`.
   - **How it works:** Launches the native device camera activity, captures a real hardware bitmap, compresses it as JPEG into `context.cacheDir`, and returns a persistent local file URI.
3. **Android Runtime OS Permissions (`ActivityResultContracts.RequestMultiplePermissions` & `RequestPermission`):**
   - **Where:** `PermissionsScreen.kt`, declared in `AndroidManifest.xml`.
   - **How it works:** Tapping the Location or Notifications card triggers the genuine Android system permission dialog (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `POST_NOTIFICATIONS` on Android 13+).
4. **Interactive 3:4 Profile Photo Cropping (`EditPhotosScreen.kt`):**
   - **Where:** `EditPhotosScreen.kt` (`PhotoCropAdjustmentView`).
   - **How it works:** Dedicated 3:4 aspect-ratio framing view with dedicated "Crop" button on every profile photo slot, allowing users to reframe and preview cropped portraits.
5. **Native Android Date Picker (`android.app.DatePickerDialog`):**
   - **Where:** `BasicInfoScreen.kt`, `DatePickerScreen.kt`.
   - **How it works:** Launches the native Android OS DatePickerDialog attached to the host activity context, with age calculation and 18+ validation.
6. **Edge-to-Edge System Window Insets:**
   - **Where:** `MainActivity.kt` and all screens.
   - **How it works:** Zero-clipping edge-to-edge support with `statusBarsPadding()`, `navigationBarsPadding()`, and `imePadding()` ensuring seamless integration on gesture-nav and 3-button devices.
7. **Complete Header & Navigation Bar Color System:**
   - **Where:** `NavComponents.kt`, `Color.kt`, all dashboard screens and sub-screens.
   - **How it works:** Rose Blush (`#FBE9F1`) top bar header, Lavender Mist (`#F3E8FF`) bottom nav bar, Warm Cream (`#FFF7F2`) body canvas. Pure white (`#FFFFFF`) is strictly disallowed on headers and nav bars.

---

### Client-Side State vs. External Server Dependencies

The app is built as a complete, fully functional self-contained client. Below is the exact technical reality regarding external cloud/server dependencies:

| Feature / UI Component | Screen(s) Involved | Current Implementation (Client-Side Behavior) | External Infrastructure Requirement |
| :--- | :--- | :--- | :--- |
| **Media Picking & Camera** | All photo screens | **100% Real Android SDK** (`PickVisualMedia` + `TakePicturePreview`). | None required (uses native OS). |
| **OS System Permissions** | `PermissionsScreen.kt` | **100% Real Android SDK** (`RequestMultiplePermissions`). | None required (uses native OS). |
| **Date Selection** | `BasicInfoScreen.kt` | **100% Real Android SDK** (`DatePickerDialog`). | None required (uses native OS). |
| **SMS OTP Verification** | `PhoneEntryScreen.kt`, `OtpVerifyScreen.kt` | Accepts valid 6-digit PIN input with 30s countdown timer and resend trigger. | External paid SMS gateway (e.g. Twilio or Firebase Phone Auth) to deliver physical cellular SMS messages. |
| **Verification Analysis Pipeline** | `VerificationProcessingScreen.kt` | 3-step automated biometric analysis animation (Face Detected, Liveness Confirmed, Profile Match) that transitions to verified state. | Cloud server biometrics endpoint or on-device ML Kit Face Mesh detection. |
| **Peer-to-Peer Chat Sync** | `ChatThreadScreen.kt` | Real-time interactive message sending, typing status, and photo attachments stored in ViewModel reactive state. | Cloud WebSocket server or Firebase Firestore for remote two-way peer synchronization. |
| **Matchmaking Swipe Deck** | `ExploreScreen.kt` | Interactive swipe physics with swipe-left (pass), swipe-right (like), superlike, filter controls, and match celebration. | Remote database (Firebase Firestore / Cloud SQL) with geospatial coordinates. |
| **In-App Billing** | `ProPlansScreen.kt` | Interactive plan selector (1 Month / 6 Months / 12 Months) updating user's Gold tier status immediately in state. | Google Play Billing Library (`com.android.billingclient:billing`) and Google Play Console merchant account. |

---

## 6. Navigation Summary & Graph Flow

```
[SplashScreen] (2s timer)
       │
       ▼
[LandingScreen]
       │
       ▼
[PhoneEntryScreen] ◄───► [CountryPickerScreen]
       │
       ▼
[OtpVerifyScreen]
       │
       ▼
[BasicInfoScreen] ◄───► [DatePickerDialog]
       │
       ▼
[UsernameScreen]
       │
       ▼
[PhotosSetupScreen] ◄───► [AddPhotoScreen] (Real Android PhotoPicker & Camera)
       │
       ▼
[FaceCaptureScreen] (Real Android Camera & PhotoPicker)
       │
       ▼
[VerificationProcessingScreen] (Biometric Analysis Pipeline)
       │
       ▼
[VerificationSuccessScreen]
       │
       ▼
[AboutYouScreen] (Bio *, Interests *, Who are you looking for? *)
       │
       ▼
[PermissionsScreen] (Real Android System Permissions)
       │
       ▼
╔═════════════════════════════════════════════════════════════════════════╗
║                            DASHBOARD HUB                                ║
╠═════════════════════════════════════════════════════════════════════════╣
║ 1. Explore Tab:                                                         ║
║    ExploreScreen ──► FiltersScreen                                      ║
║                  ──► FullProfileScreen                                  ║
║                  ──► ItsAMatchScreen ──► ChatThreadScreen               ║
║                  ──► UsernameSearchScreen                               ║
║                                                                         ║
║ 2. Chats Tab:                                                           ║
║    ChatListScreen ──► VerifyToUnlockScreen                              ║
║                   ──► ChatThreadScreen ──► ChatOptionsScreen            ║
║                                        ──► ChatInfoScreen               ║
║                                        ──► SendAttachmentScreen (Picker)║
║                   ──► BlockedUsersScreen                                ║
║                                                                         ║
║ 3. Friends Tab:                                                         ║
║    FriendsScreen ──► FindFriendsScreen                                  ║
║                  ──► RequestDetailScreen                                ║
║                  ──► FriendProfileScreen ──► ChatThreadScreen           ║
║                                                                         ║
║ 4. Settings Tab:                                                        ║
║    SettingsScreen ──► MatchmakingScreen                                 ║
║                   ──► VerificationStatusScreen                          ║
║                   ──► PrivacySettingsScreen                             ║
║                   ──► NotificationSettingsScreen                        ║
║                   ──► ProPlansScreen                                    ║
║                   ──► TermsScreen                                       ║
║                   ──► AccountSettingsScreen ──► PhoneNumberScreen       ║
║                                             ──► EditUsernameScreen      ║
║                                             ──► EmailScreen             ║
║                                             ──► LogoutConfirmScreen     ║
║                                             ──► DeleteAccountScreen     ║
║                                                                         ║
║ 5. Profile Tab:                                                         ║
║    MyProfileScreen ──► EditPhotosScreen (Real PhotoPicker & Crop)       ║
║                    ──► EditBasicInfoScreen                              ║
║                    ──► EditUsernameScreen                               ║
║                    ──► EditAboutScreen                                  ║
║                    ──► PreviewProfileScreen                             ║
╚═════════════════════════════════════════════════════════════════════════╝
```

---
*Document generated on October 14, 2026 for Soulmate Android Applet.*
