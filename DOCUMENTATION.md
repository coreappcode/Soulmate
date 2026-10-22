# Soulmate Android App — Complete Technical & UI Documentation

---

## 1. Executive Summary & Architecture

- **Application Name:** Soulmate
- **Kotlin Namespace:** `com.example`
- **Android Application ID:** `com.soulmate.android`
- **Architecture:** Single-Activity MVVM (Model-View-ViewModel) built 100% in Kotlin & Jetpack Compose
- **Design System:** Custom Material Design 3 (M3) "Aurora Warm" Design System with Edge-to-Edge system insets
- **Header & Navbar Theme:** Tinder-Model Iconic Pink (`#FD3A73`) for all Top App Bar headers and Bottom Navigation Bar tabs
- **Navigation Architecture:** Dual-layer navigation system:
  1. Jetpack Navigation Compose (`NavHost` / `NavController`) with slide + cross-fade page transitions for top-level routes (Splash, Landing, Onboarding, Dashboard, System fallback screens).
  2. In-Scaffold reactive sub-screen state machine (`ActiveSubScreen`) within `DashboardScreen` for instant tab switching, smooth child-screen transitions, and automatic back-stack popping via Jetpack `BackHandler`.
- **State Management:** `SoulmateViewModel` driving Kotlin Coroutines `MutableStateFlow` and immutable reactive UI state representations

### Firebase Backend Connections and Work Completed

- **Firebase project:** `soulmate-e285e`; the Android app registered for this package is `com.soulmate.android`. Firebase configuration is checked in at the repository root and in `app/google-services.json`; both copies must remain synchronized.
- **Authentication:** Firebase Phone Authentication sends and verifies SMS OTPs. The verified Firebase credential signs in the user; changing an existing account's phone number also requires OTP verification. Authentication errors are shown in the UI rather than replaced with a mock success state.
- **Firestore:** `FirebaseRepository` is the app's data boundary. Private profile data and onboarding drafts are stored under `users/{uid}`; public discovery profiles under `profiles/{uid}`; normalized username claims under `usernames/{username}`; user preferences under `users/{uid}/settings`; blocks under `users/{uid}/blocks`. Swipes, matches, friend requests, friendships, conversations, and conversation messages use their corresponding top-level collections, with messages nested under each conversation.
- **Profile privacy:** Public profiles omit age when the owner disables age visibility. Discovery, friend, profile, and chat displays use the privacy-aware age labels. The other privacy toggles are stored but are not enforced because online presence, distance visibility, non-friend messaging controls, and read receipts are not implemented.
- **Firebase Storage:** Profile images are uploaded under `users/{uid}/profile/`, private selfie uploads under `users/{uid}/verification/`, and chat attachments under `conversations/{conversationId}/`. Uploads are initiated only after selection/capture through Android system pickers and camera contracts; profile images are cropped before upload.
- **Backend deployment:** `firebase.json` points to `firestore.rules`, `firestore.indexes.json`, and `storage.rules`. Firestore rules and indexes and Storage rules were deployed to `soulmate-e285e` on October 22, 2026. Redeploy them whenever these checked-in deployment files change.
- **Mock-flow cleanup:** Removed seeded mock data and simulated identity-verification completion. A captured selfie is stored privately and does not grant a verified badge. Billing, account deletion/deactivation, data export, live presence, precise-distance matching, push/email delivery, and moderation are not backed by configured services and are presented as unavailable or limited.
- **Device integrations:** Onboarding uses actual Android runtime permission results. Photo selection uses the Android Photo Picker; camera capture uses the system camera with a FileProvider; profile cropping is performed before upload.
- **Setup and validation notes:** Enable Phone sign-in in Firebase Authentication and register SHA-1/SHA-256 fingerprints for the app's signing certificates before production phone-auth testing. No signing fingerprints were registered during this work. An Android build could not be verified in the development environment because no Android SDK is configured.

---

## 2. Colour Palette & Visual Design System

The application uses the **Aurora Warm** aesthetic with high-visibility, brand-defining **Tinder Pink** for all structural navigation components (headers and bottom navigation bars).

| Token Name | Hex Code | Visual Swatch Role | Exact Usage in App |
| :--- | :--- | :--- | :--- |
| **TinderPink** | `#FFFD3A73` | Iconic Tinder Pink | **Primary brand color for Top App Bar header & Bottom Nav Bar** |
| **HeaderBackground** | `#FFFD3A73` | Iconic Tinder Pink | **Top app bars across Explore, Chats, Friends, Settings, and Profile** |
| **BottomNavBackground** | `#FFFD3A73` | Iconic Tinder Pink | **Dashboard bottom navigation bar background across all tabs** |
| **ActiveTabLabel** | `#FFFFFFFF` | Crisp White | Active tab label and active icon on Tinder Pink nav bar |
| **InactiveTabColor** | `#FFFFD1DC` | Soft Pink-White | Inactive tab label and inactive icon on Tinder Pink nav bar |
| **RosePrimary** | `#FFFF6B9D` | Warm Rose Pink | Aurora gradient start, like buttons, heart icons |
| **OrchidPrimary** | `#FFC86DD7` | Radiant Orchid Purple | Aurora gradient middle, active tab pill accents |
| **PeriwinklePrimary** | `#FF6E8EFB` | Soft Periwinkle Blue | Aurora gradient end, verified badges, action pills |
| **AppBackground** | `#FFFFF7F2` | Warm Cream | Canvas background across screens (never pure white/black) |
| **CardBackground** | `#FFFFFFFF` | Pure White | Elevated content cards, message bubbles, swipe cards |
| **ElevatedSurface** | `#FFFDF2F8` | Soft Blush | Chips, secondary pill backgrounds, pill buttons |
| **DarkAccent** | `#FF2A1B3D` | Deep Plum | Primary contrasting elements, bold highlights |
| **HairlineDivider** | `#FFF0E6EC` | Subtle Lilac Line | 1dp dividers and borders between list items |
| **TextPrimary** | `#FF1A1423` | Dark Plum-Black | Headings, titles, primary body text (never pure black) |
| **TextSecondary** | `#FF6B5B7A` | Muted Plum-Gray | Subtitles, helper text, timestamps, secondary labels |
| **TextMuted** | `#FFA99BB5` | Lavender Gray | Placeholders, inactive indicators, countdown timers |
| **BorderBlack** | `#FF000000` | Solid Black | 2dp high-visibility input & card borders during onboarding |
| **BorderSubtle** | `#FFE8D5E5` | Muted Lilac | Subtle card borders |
| **SemanticSuccess** | `#FF34D399` | Mint Green | Online indicator, verification checkmark success |
| **SemanticError** | `#FFF87171` | Coral Red | Validation warnings, delete account, report/block actions |
| **SemanticWarning** | `#FFFBBF24` | Warm Amber | Match expiry timers, pending requests |

### Gradients
- **`AuroraGradient`**: Linear diagonal sweep from `RosePrimary` (`#FFFF6B9D`) → `OrchidPrimary` (`#FFC86DD7`) → `PeriwinklePrimary` (`#FF6E8EFB`).
- **`AuroraHorizontalGradient`**: Linear horizontal sweep used on primary action buttons (`GradientButton`) and active indicators.

---

## 3. Core Reusable UI Components, Buttons & Icons

### Navigation Components (`com.example.ui.components.NavComponents.kt`)
1. **`SoulTopBar`**
   - **Background:** `HeaderBackground` (`#FD3A73` Tinder Pink)
   - **Content Color:** Crisp White (`#FFFFFF`)
   - **Height:** 56.dp + `statusBarsPadding()`
   - **Features:** Optional back navigation arrow (`Icons.AutoMirrored.Filled.ArrowBack`), bold title in `PoppinsFontFamily`, flexible `actions` RowScope slot (e.g. Search and Filter buttons on Explore).
   - **Bottom Edge:** 1dp semi-transparent hairline divider (`Color.White.copy(alpha = 0.25f)`).
2. **`SoulBottomNav`**
   - **Background:** `BottomNavBackground` (`#FD3A73` Tinder Pink)
   - **Elevation & Shadow:** 8.dp elevation with brand-tinted shadow
   - **Height:** 68.dp + `navigationBarsPadding()`
   - **Top Edge:** 1dp semi-transparent hairline divider (`Color.White.copy(alpha = 0.25f)`).
   - **Tabs (5 items):**
     1. **Explore:** `AutoAwesome` icon (Filled when active, Outlined when inactive)
     2. **Chats:** `ChatBubble` icon with unread badge counter (`"3"`)
     3. **Friends:** `People` icon with pending request badge counter (`"2"`)
     4. **Settings:** `Settings` icon
     5. **Profile:** `Person` icon
   - **Active State:** White icon + White label + scale animation
   - **Inactive State:** `InactiveTabColor` (`#FFD1DC`) icon + label

### Buttons (`com.example.ui.components.Buttons.kt`)
1. **`GradientButton`**
   - **Style:** Full-width rounded rectangle (18dp corner radius) filled with `AuroraHorizontalGradient`.
   - **Behavior:** Material ripple, press-down micro-scale animation (`0.97f`), circular progress indicator when `loading = true`.
2. **`SoulOutlinedButton`**
   - **Style:** Outlined border (1.5dp `OrchidPrimary`), transparent background, orchid text.
   - **Behavior:** Press-down micro-scale animation, optional leading icon parameter.
3. **Card & Circular Action Buttons**
   - Circular action buttons (Pass "X", Superlike "Star", Like "Heart", Rewind "Replay") with tactile elevation.

### Input Components (`com.example.ui.components.Inputs.kt`)
1. **`SoulTextField`**: Clean container with high-contrast border, label in `PoppinsFontFamily`, leading/trailing icon slots, and error feedback.
2. **`OtpInput`**: 6 individual styled digit cells supporting auto-advance, backspace handling, and paste.
3. **`SoulChip`**: Selectable pill chip with gradient fill when active.

---

## 4. Complete Screen Directory & Navigations (All 56 Screens)

The application consists of **56 complete UI screens** organized into 9 logical functional domains.

---

### Group 1: Splash & Authentication

#### 1. Splash Screen
- **Route:** `splash` (`Routes.SPLASH`)
- **File:** `ui/screens/splash/SplashScreen.kt`
- **Description:** Branded entry screen with pulsing Aurora heart logo and animated title typography.
- **Navigations:**
  - Automatic 2000ms delay timer → navigates to `Routes.LANDING` (popping `splash` off backstack).
- **Buttons / Actions:** None (automatic timer).
- **Icons:** Animated Vector Heart / Aurora Icon.

#### 2. Landing Screen
- **Route:** `landing` (`Routes.LANDING`)
- **File:** `ui/screens/onboarding/LandingScreen.kt`
- **Description:** Full-screen immersive hero image background (`img_landing_hero.jpg`), clean design with zero unwanted white containers and zero text clutter. Features only the elevated "Get Started" action button positioned up from the bottom.
- **Navigations:**
  - "Get Started" button tap → navigates to `Routes.PHONE_ENTRY`.
- **Buttons:** `GradientButton` ("Get Started").
- **Visuals:** Edge-to-edge full-bleed background image with subtle bottom gradient scrim for button contrast.

#### 3. Phone Entry Screen
- **Route:** `phone_entry` (`Routes.PHONE_ENTRY`)
- **File:** `ui/screens/auth/PhoneEntryScreen.kt`
- **Description:** International phone number input with country-code selection and Firebase Phone Auth SMS verification.
- **Navigations:**
  - Back Button → returns to `Routes.LANDING`.
  - Country Code selector tap → navigates to `Routes.COUNTRY_PICKER`.
  - "Send Code" → requests a Firebase verification SMS and opens `Routes.OTP_VERIFY` only after Firebase sends the code.
- **Buttons:** Country selector pill, `GradientButton` ("Send Code").
- **Icons:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Phone`.

#### 4. Country Picker Screen
- **Route:** `country_picker` (`Routes.COUNTRY_PICKER`)
- **File:** `ui/screens/onboarding/CountryPickerScreen.kt`
- **Description:** Searchable list of 200+ world countries with emoji flags, country names, and dial codes.
- **Navigations:**
  - Back Button → returns to calling screen (`Routes.PHONE_ENTRY` or `PhoneNumberScreen`).
  - Country item tap → updates `selectedCountry` in `SoulmateViewModel` and pops back.
- **Buttons:** Close button (`IconButton`), country list item rows.
- **Icons:** `Icons.Default.Search`, `Icons.Default.Close`.

#### 5. OTP Verification Screen
- **Route:** `otp_verify` (`Routes.OTP_VERIFY`)
- **File:** `ui/screens/auth/OtpVerifyScreen.kt`
- **Description:** Firebase-backed 6-digit SMS code entry with resend countdown, verification errors, and a dedicated Verify button.
- **Navigations:**
  - Back Button → returns to `Routes.PHONE_ENTRY`.
  - "Verify" button (or automatic 6-digit completion) → signs in with the Firebase phone credential and navigates to `Routes.BASIC_INFO` only after successful verification.
  - "Change phone number" → returns to `Routes.PHONE_ENTRY`.
- **Buttons:** `GradientButton` ("Verify"), TextButton ("Resend in 0:XXs").
- **Icons:** `Icons.AutoMirrored.Filled.ArrowBack`, `Icons.Default.Lock`, `Icons.Default.Check`.

---

### Group 2: Profile Setup Flow (Onboarding Wizard)

#### 6. Basic Info Screen
- **Route:** `basic_info` (`Routes.BASIC_INFO`)
- **File:** `ui/screens/profile_setup/BasicInfoScreen.kt`
- **Description:** Step 1 of Onboarding. Collects Full Name, Date of Birth (with age 18+ requirement enforcement), and Gender selection.
- **Navigations:**
  - Back Button → returns to `Routes.OTP_VERIFY`.
  - Date of Birth field tap → triggers native Android `DatePickerDialog` (with fallback to `Routes.DATE_PICKER`).
  - "Continue" → validates required inputs and navigates to `Routes.USERNAME`.
- **Buttons:** Gender cards (`Man`, `Woman`, `Non-binary`), `GradientButton` ("Continue").
- **Icons:** `Icons.Default.CalendarMonth`, `Icons.Default.Male`, `Icons.Default.Female`, `Icons.Default.Transgender`.

#### 7. Date Picker Screen (Standalone Fallback)
- **Route:** `date_picker` (`Routes.DATE_PICKER`)
- **File:** `ui/screens/profile_setup/DatePickerScreen.kt`
- **Description:** Standalone date picker destination with calendar grid, month/year navigation, and age calculation.
- **Navigations:**
  - Back Button / "Confirm Date" → sets `dateOfBirth` in ViewModel and returns.
- **Buttons:** `GradientButton` ("Confirm Date").
- **Icons:** `Icons.Default.CalendarToday`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 8. Username Selection Screen
- **Route:** `username` (`Routes.USERNAME`)
- **File:** `ui/screens/profile_setup/UsernameScreen.kt`
- **Description:** Step 2 of Onboarding. Unique `@handle` selection with live validation (length, alphanumeric characters, availability).
- **Navigations:**
  - Back Button → returns to `Routes.BASIC_INFO`.
  - "Continue" → updates `username` in ViewModel and navigates to `Routes.PHOTOS`.
- **Buttons:** `GradientButton` ("Claim Username").
- **Icons:** `Icons.Default.AlternateEmail`, `Icons.Default.CheckCircle`.

#### 9. Photos Setup Screen
- **Route:** `photos` (`Routes.PHOTOS`)
- **File:** `ui/screens/profile_setup/PhotosSetupScreen.kt`
- **Description:** Step 3 of Onboarding. 6-slot photo grid requiring at least 2 photos to continue. Allows setting main photo and removing photos.
- **Navigations:**
  - Back Button → returns to `Routes.USERNAME`.
  - Empty Slot tap → navigates to `Routes.ADD_PHOTO`.
  - Empty slot → opens the photo source screen; selected photos are cropped and uploaded to Firebase Storage.
  - "Continue" (enabled when >= 2 photos) → navigates to `Routes.ABOUT_YOU`.
- **Buttons:** `SoulOutlinedButton` ("Choose from Gallery"), `GradientButton` ("Continue"), Remove ("X") icon buttons.
- **Icons:** `Icons.Default.Add`, `Icons.Default.PhotoLibrary`, `Icons.Default.Delete`, `Icons.Default.Star`.

#### 10. Add Photo Screen
- **Route:** `add_photo` (`Routes.ADD_PHOTO`)
- **File:** `ui/screens/profile_setup/AddPhotoScreen.kt`
- **Description:** Gallery or system-camera capture followed by a native 3:4 cropper and Firebase Storage upload.
- **Navigations:**
  - Back Button → returns to `Routes.PHOTOS`.
  - "Choose from Gallery" card → launches real Android Photo Picker (`PickVisualMedia`).
  - "Take Photo" card → launches device Camera (`TakePicture`) using the app's FileProvider URI.
- **Buttons:** Source cards (`PhotoSourceCard`).
- **Icons:** `Icons.Default.PhotoLibrary`, `Icons.Default.CameraAlt`.

#### 11. Face Capture Screen
- **Route:** `face_capture` (`Routes.FACE_CAPTURE`)
- **File:** `ui/screens/profile_setup/FaceCaptureScreen.kt`
- **Description:** Selfie upload screen with camera and Android Photo Picker. A selfie is stored privately; it does not verify identity.
- **Navigations:**
  - Back Button → returns to calling screen (`PhotosSetupScreen` or `DashboardProfile`).
  - "Take Selfie (Camera)" → launches native camera.
  - "Choose from Gallery" → launches real photo picker.
  - "Use This" → uploads the selfie to the user's private Firebase Storage folder.
  - "Retake" → resets photo capture prompt.
- **Buttons:** `GradientButton` ("Take Selfie (Camera)"), `SoulOutlinedButton` ("Choose from Gallery"), `GradientButton` ("Use This"), `SoulOutlinedButton` ("Retake").
- **Icons:** `Icons.Default.CameraAlt`, `Icons.Default.Person`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 12. About You Screen
- **Route:** `about_you` (`Routes.ABOUT_YOU`)
- **File:** `ui/screens/profile_setup/AboutYouScreen.kt`
- **Description:** Step 4 of Onboarding. Bio input (up to 500 characters), interest chip selector (up to 10 interests), and single-selection "Who are you looking for? *" (Men, Women, Non-binary, All).
- **Navigations:**
  - Back Button → returns to `Routes.PHOTOS`.
  - "Continue" → validates required fields and navigates to `Routes.PERMISSIONS`.
- **Buttons:** Interest chips (`SoulChip`), `LookingForCard` selection cards, `GradientButton` ("Continue").
- **Icons:** `Icons.Default.CheckCircle`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 13. Permissions Screen
- **Route:** `permissions` (`Routes.PERMISSIONS`)
- **File:** `ui/screens/profile_setup/PermissionsScreen.kt`
- **Description:** Final onboarding step requesting Location and Notification permissions via real Android Activity Result contracts (`RequestMultiplePermissions` for Location, `RequestPermission` for `POST_NOTIFICATIONS`).
- **Navigations:**
  - Back Button → returns to `Routes.ABOUT_YOU`.
  - Location Card tap → triggers native Android location permission dialog.
  - Notifications Card tap → triggers native Android notification permission dialog.
  - "Enter Soulmate" / "Skip for now" → validates and saves the complete profile to Firebase before navigating to `Routes.DASHBOARD`.
- **Buttons:** `PermissionCard` (Location), `PermissionCard` (Notifications), `GradientButton` ("Enter Soulmate"), TextButton ("Skip for now").
- **Icons:** `Icons.Default.LocationOn`, `Icons.Default.Notifications`, `Icons.Default.Check`.

---

### Group 3: Main Dashboard Container

#### 14. Dashboard Shell Screen
- **Route:** `dashboard` (`Routes.DASHBOARD`)
- **File:** `ui/screens/dashboard/DashboardScreen.kt`
- **Description:** Main app container featuring a Tinder Pink (`#FD3A73`) bottom navigation bar hosting 5 primary tabs:
  1. **Explore:** `Routes.DASHBOARD_EXPLORE`
  2. **Chats:** `Routes.DASHBOARD_CHATS`
  3. **Friends:** `Routes.DASHBOARD_FRIENDS`
  4. **Settings:** `Routes.DASHBOARD_SETTINGS`
  5. **Profile:** `Routes.DASHBOARD_PROFILE`
- **Navigations:** Tab switching updates active sub-screen within scaffold. Top app bar is styled in Tinder Pink (`#FD3A73`).
- **Icons:** `Icons.Default.AutoAwesome`, `Icons.Default.ChatBubble`, `Icons.Default.People`, `Icons.Default.Settings`, `Icons.Default.Person`.

---

### Group 4: Explore & Matchmaking Sub-Screens

#### 17. Explore Screen
- **Route:** `dashboard/explore` (`Routes.DASHBOARD_EXPLORE`)
- **File:** `ui/screens/explore/ExploreScreen.kt`
- **Description:** Core card swipe deck with segment switcher (Nearby / Country / Global). Swipe left (pass), swipe right (like), superlike (star), and rewind.
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with white title "Explore", search icon, and filter icon.
- **Navigations:**
  - Filter Icon in header → opens `FiltersScreen` (`ActiveSubScreen.Filters`).
  - Search Icon in header → opens `UsernameSearchScreen` (`ActiveSubScreen.UsernameSearch`).
  - Card Body tap → opens `FullProfileScreen` (`ActiveSubScreen.ExploreProfileDetail`).
  - Right Swipe / Like on matched user → opens `ItsAMatchScreen` (`ActiveSubScreen.ItsAMatch`).
- **Buttons:** Pass Button ("X"), Superlike Button (Star), Like Button (Heart), Rewind Button (Replay).
- **Icons:** `Icons.Default.Close`, `Icons.Default.Star`, `Icons.Default.Favorite`, `Icons.Default.FilterList`, `Icons.Default.Search`, `Icons.Default.Replay`.

#### 18. Filters Screen
- **Route:** `explore/filters` (`Routes.EXPLORE_FILTERS`)
- **File:** `ui/screens/explore/FiltersScreen.kt`
- **Description:** Discovery preferences: Age Range slider (18-60), Maximum Distance slider (1-100 km), Interested In selector, Online Only and Verified Only toggles.
- **Navigations:**
  - Back Button / "Apply Filters" → applies filter state in ViewModel and returns to Explore deck.
  - "Reset" button → resets filters to defaults.
- **Buttons:** `GradientButton` ("Apply Filters"), TextButton ("Reset").
- **Icons:** `Icons.Default.Tune`, `Icons.Default.Check`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 19. Full Profile Screen (Explore Detail)
- **Route:** `explore/profile_detail` (`Routes.EXPLORE_PROFILE_DETAIL`)
- **File:** `ui/screens/explore/FullProfileScreen.kt`
- **Description:** Comprehensive profile view with photo carousel, verified badge, bio, lifestyle tags, astrological sign, job, education, and distance.
- **Navigations:**
  - Back Button → returns to Explore deck.
  - Like FAB → likes user and returns to deck (or triggers match).
  - Pass FAB → passes user and returns.
  - "Send Friend Request" → sends request and displays confirmation.
- **Buttons:** FABs (Like / Pass / Friend Request).
- **Icons:** `Icons.Default.Favorite`, `Icons.Default.Close`, `Icons.Default.PersonAdd`, `Icons.Default.Verified`.

#### 20. It's A Match Screen
- **Route:** `explore/its_a_match` (`Routes.ITS_A_MATCH`)
- **File:** `ui/screens/explore/ItsAMatchScreen.kt`
- **Description:** Celebration screen shown when two users like each other. Features dual floating avatar circles and celebratory graphics.
- **Navigations:**
  - "Send a Message" → opens `ChatThreadScreen` with matched user.
  - "Keep Swiping" → returns to Explore deck.
- **Buttons:** `GradientButton` ("Send a Message"), `SoulOutlinedButton` ("Keep Swiping").
- **Icons:** `Icons.Default.Favorite`, `Icons.Default.Chat`.

#### 21. Username Search Screen
- **Route:** `explore/search` (`Routes.EXPLORE_SEARCH`)
- **File:** `ui/screens/explore/UsernameSearchScreen.kt`
- **Description:** Live search bar to discover profiles by handle (e.g. `@maya`).
- **Navigations:**
  - Back Button → returns to Explore deck.
  - Result row tap → opens `FullProfileScreen` for that user.
- **Buttons:** Clear search button, Result row tap target.
- **Icons:** `Icons.Default.Search`, `Icons.Default.Close`, `Icons.AutoMirrored.Filled.ArrowBack`.

---

### Group 5: Chats & Messaging Sub-Screens

#### 22. Chat List Screen
- **Route:** `dashboard/chats` (`Routes.DASHBOARD_CHATS`)
- **File:** `ui/screens/chats/ChatListScreen.kt`
- **Description:** Inbox with New Matches horizontal carousel and Conversation list showing last message, unread badges, and verified status.
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with white title "Chats" and new chat action icon.
- **Navigations:**
  - Tapping a conversation → opens `ChatThreadScreen`.
  - Long press on conversation → opens `ChatOptionsScreen`.
- **Buttons:** Match avatar circles, conversation card rows.
- **Icons:** `Icons.Default.CheckCircle`, `Icons.Default.Lock`, `Icons.Default.Add`.

#### 23. Chat Thread Screen
- **Route:** `chat/thread` (`Routes.CHAT_THREAD`)
- **File:** `ui/screens/chats/ChatThreadScreen.kt`
- **Description:** Real-time messaging screen with message bubbles, status ticks (sent, delivered, read), attachment button, and text field.
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with partner's avatar, name, and options button.
- **Navigations:**
  - Back Button → returns to Chat List.
  - Header avatar/name tap → opens `ChatInfoScreen`.
  - More options (3 vertical dots) → opens `ChatOptionsScreen`.
  - Attachment (+) button → opens `SendAttachmentScreen`.
- **Buttons:** Send button (`IconButton`), Attachment button, Options menu button.
- **Icons:** `Icons.AutoMirrored.Filled.Send`, `Icons.Default.AddPhotoAlternate`, `Icons.Default.MoreVert`, `Icons.Default.Verified`.

#### 25. Chat Options Screen
- **Route:** `chat/options` (`Routes.CHAT_OPTIONS`)
- **File:** `ui/screens/chats/ChatOptionsScreen.kt`
- **Description:** Action sheet with options: View Profile, Shared Media, Mute Notifications, Clear Chat, Block User, Report User, Delete Chat.
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "View Profile" → opens `FullProfileScreen`.
  - "Shared Media & Files" → opens `ChatInfoScreen`.
  - "Block User" / "Report User" → blocks user in ViewModel and returns to Chat List.
- **Buttons:** Action list items.
- **Icons:** `Icons.Default.Person`, `Icons.Default.NotificationsOff`, `Icons.Default.Block`, `Icons.Default.Report`, `Icons.Default.Delete`.

#### 26. Chat Info Screen
- **Route:** `chat/info` (`Routes.CHAT_INFO`)
- **File:** `ui/screens/chats/ChatInfoScreen.kt`
- **Description:** Contact card showing user photo, bio, shared media gallery, hairline dividers, and safety actions (Block / Report).
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "View Full Profile" → opens `FullProfileScreen`.
- **Buttons:** `SoulOutlinedButton` ("View Full Profile"), Block/Mute buttons.
- **Icons:** `Icons.Default.PermMedia`, `Icons.Default.Lock`, `Icons.Default.Block`.

#### 27. Blocked Users Screen
- **Route:** `chat/blocked_users` (`Routes.BLOCKED_USERS`)
- **File:** `ui/screens/chats/BlockedUsersScreen.kt`
- **Description:** List of currently blocked accounts with option to unblock.
- **Navigations:**
  - Back Button → returns to previous screen.
  - "Unblock" button → removes user from blocked list in ViewModel.
- **Buttons:** `SoulOutlinedButton` ("Unblock").
- **Icons:** `Icons.Default.Block`, `Icons.AutoMirrored.Filled.ArrowBack`.

#### 28. Send Attachment Screen
- **Route:** `chat/attachment` (`Routes.CHAT_ATTACHMENT`)
- **File:** `ui/screens/chats/SendAttachmentScreen.kt`
- **Description:** Media attachment composer. Uses Android Photo Picker, system camera (`TakePicture`), audio picker, and document picker; attachments upload to Firebase Storage.
- **Navigations:**
  - Back Button → returns to Chat Thread.
  - "Photo Gallery" → launches real Android Photo Picker (`PickVisualMedia`).
  - "Take Instant Photo" → launches native device camera (`TakePicture`).
  - Audio and Document options use Android system pickers.
  - "Send Attachment" → uploads the selected file and saves the message to Firestore.
- **Buttons:** `AttachmentOptionCard` (Gallery / Camera / Audio / Document), media-specific send button.
- **Icons:** `Icons.Default.PhotoLibrary`, `Icons.Default.CameraAlt`, `Icons.Default.Headphones`, `Icons.Default.Description`, `Icons.AutoMirrored.Filled.Send`.

---

### Group 6: Friends & Social Sub-Screens

#### 29. Friends Screen
- **Route:** `dashboard/friends` (`Routes.DASHBOARD_FRIENDS`)
- **File:** `ui/screens/friends/FriendsScreen.kt`
- **Description:** Friends hub with tabs: "My Friends" (grid of established connections) and "Requests" (received and sent invitations).
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with white title "Friends" and "+" icon to discover new friends.
- **Navigations:**
  - Header "+" icon → opens `FindFriendsScreen`.
  - Friend card tap → opens `FriendProfileScreen`.
  - Request card tap → opens `RequestDetailScreen`.
- **Buttons:** Tab pills, "Accept" / "Decline" buttons on request cards.
- **Icons:** `Icons.Default.PersonAdd`, `Icons.Default.Check`, `Icons.Default.Close`.

#### 30. Friend Request Detail Screen
- **Route:** `friends/request_detail` (`Routes.FRIEND_REQUEST_DETAIL`)
- **File:** `ui/screens/friends/RequestDetailScreen.kt`
- **Description:** Full-screen inspection of a received friend request with sender's full bio, photos, and mutual interests.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Accept Request" → moves user to Friends list and returns.
  - "Decline Request" → removes request and returns.
- **Buttons:** `GradientButton` ("Accept Request"), `SoulOutlinedButton` ("Decline").
- **Icons:** `Icons.Default.CheckCircle`, `Icons.Default.Cancel`.

#### 31. Friend Profile Screen
- **Route:** `friends/friend_profile` (`Routes.FRIEND_PROFILE`)
- **File:** `ui/screens/friends/FriendProfileScreen.kt`
- **Description:** Friend's full profile displaying connection anniversary, common interests, and direct message shortcut.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Message" button → opens `ChatThreadScreen` with this friend.
  - "Remove Friend" → confirms removal and returns.
- **Buttons:** `GradientButton` ("Send Message"), `SoulOutlinedButton` ("Remove Friend").
- **Icons:** `Icons.Default.Chat`, `Icons.Default.PersonRemove`.

#### 32. Find Friends Screen
- **Route:** `friends/find_friends` (`Routes.FIND_FRIENDS`)
- **File:** `ui/screens/friends/FindFriendsScreen.kt`
- **Description:** Discovery list of people nearby with "Send Request" buttons.
- **Navigations:**
  - Back Button → returns to Friends screen.
  - "Send Request" tap → sends request and updates button to "Requested".
  - Profile avatar tap → opens `FullProfileScreen`.
- **Buttons:** Request buttons on user items.
- **Icons:** `Icons.Default.Search`, `Icons.Default.PersonAdd`.

---

### Group 7: Settings & Account Management Sub-Screens

#### 33. Settings Screen (Hub)
- **Route:** `dashboard/settings` (`Routes.DASHBOARD_SETTINGS`)
- **File:** `ui/screens/settings/SettingsScreen.kt`
- **Description:** Categorized settings menu: Account, Matchmaking, Verification, Privacy, Notifications, Pro Plans, Terms, Community Rules, Logout, Delete Account.
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with white title "Settings".
- **Navigations:** Menu rows navigate to each respective sub-screen.
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
- **Description:** Displays user's verification badge status (Active Verified vs. Unverified) with security benefits breakdown.
- **Navigations:**
  - Back Button → returns to Settings Hub.
  - "Re-Verify Face" / "Verify Now" → navigates to `FaceCaptureScreen`.
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
- **Navigations:** Back Button → returns to Settings Hub.
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
  - Phone Row → opens `PhoneNumberScreen`.
  - Username Row → opens `EditUsernameScreen`.
  - Email Row → opens `EmailScreen`.
  - Logout Row → opens `LogoutConfirmationScreen`.
  - Delete Account Row → opens `DeleteAccountScreen`.
- **Icons:** `Icons.Default.Phone`, `Icons.Default.AlternateEmail`, `Icons.Default.Email`, `Icons.Default.ExitToApp`.

#### 42. Logout Confirmation Screen
- **Route:** `settings/logout_confirm` (`Routes.SETTINGS_LOGOUT_CONFIRM`)
- **File:** `ui/screens/settings/LogoutConfirmationScreen.kt`
- **Description:** Confirmation modal to safely sign out of current device.
- **Navigations:**
  - "Cancel" → returns to Account Settings.
  - "Log Out" → clears active session and navigates to `Routes.LANDING`.
- **Buttons:** `GradientButton` ("Log Out"), `SoulOutlinedButton` ("Cancel").
- **Icons:** `Icons.Default.ExitToApp`, `Icons.Default.Warning`.

#### 43. Delete Account Screen
- **Route:** `settings/delete_account` (`Routes.SETTINGS_DELETE_ACCOUNT`)
- **File:** `ui/screens/settings/DeleteAccountScreen.kt`
- **Description:** States that account deletion is unavailable in this build; the screen does not modify or delete account data.
- **Navigations:**
  - "Close" → returns to Account Settings.
- **Buttons:** `SoulOutlinedButton` ("Close").

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
- **Top App Bar:** Tinder Pink (`#FD3A73`) background with white title "Profile" and settings shortcut icon.
- **Navigations:**
  - "Edit Photos" → opens `EditPhotosScreen`.
  - "Edit Basic Info" → opens `EditBasicInfoScreen`.
  - "Edit Username" → opens `EditUsernameScreen`.
  - "Edit About Me" → opens `EditAboutScreen`.
  - "Preview" → opens `PreviewProfileScreen`.
- **Buttons:** Edit buttons, Preview Button (`SoulOutlinedButton`).
- **Icons:** `Icons.Default.Edit`, `Icons.Default.PhotoCamera`, `Icons.Default.Visibility`, `Icons.Default.Verified`.

#### 48. Edit Photos Screen
- **Route:** `profile/edit_photos` (`Routes.PROFILE_EDIT_PHOTOS`)
- **File:** `ui/screens/profile/EditPhotosScreen.kt`
- **Description:** Manage up to six profile photos. Photo Picker selections use the Android 3:4 cropper before upload; existing photos can be reordered or removed.
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
- **Description:** Reusable shimmer skeleton loading state (`ShimmerBox`) used during network transitions.
- **Navigations:** Back Button → pops back.
- **Icons:** Shimmer gradients.

#### 53. Error Screen
- **Route:** `system/error` (`Routes.SYSTEM_ERROR`)
- **File:** `ui/screens/system/ErrorScreen.kt`
- **Description:** Graceful error recovery screen with error illustration and "Try Again" / "Go Home" actions.
- **Navigations:**
  - "Try Again" → executes retry callback and pops back.
  - "Go Home" → navigates to `Routes.DASHBOARD`.
- **Buttons:** `GradientButton` ("Try Again"), `SoulOutlinedButton` ("Go to Dashboard").
- **Icons:** `Icons.Default.ErrorOutline`, `Icons.Default.Refresh`.

#### 54. No Internet Screen
- **Route:** `system/no_internet` (`Routes.SYSTEM_NO_INTERNET`)
- **File:** `ui/screens/system/NoInternetScreen.kt`
- **Description:** Offline state display informing users that an active network connection is required.
- **Navigations:** "Check Connection" / "Try Again" → tests connectivity and resumes.
- **Buttons:** `GradientButton` ("Check Connection").
- **Icons:** `Icons.Default.WifiOff`, `Icons.Default.Refresh`.

#### 55. Location Required Screen
- **Route:** `system/location_required` (`Routes.SYSTEM_LOCATION_REQUIRED`)
- **File:** `ui/screens/system/LocationRequiredScreen.kt`
- **Description:** Full-screen rationale explaining why location is required to calculate accurate proximity for soulmate matches.
- **Navigations:**
  - "Enable Location" → launches permission prompt or returns.
  - "Browse Country-Wide" → falls back to country-wide discovery without GPS.
- **Buttons:** `GradientButton` ("Enable Location"), `SoulOutlinedButton` ("Browse Country-Wide").
- **Icons:** `Icons.Default.LocationOff`, `Icons.Default.NearMe`.

#### 56. Maintenance Screen
- **Route:** `system/maintenance` (`Routes.SYSTEM_MAINTENANCE`)
- **File:** `ui/screens/system/MaintenanceScreen.kt`
- **Description:** Routine maintenance screen informing users about platform algorithm enhancements and connection security updates.
- **Navigations:**
  - "Try Again" → triggers refresh and pops back.
  - Back Button → pops back.
- **Buttons:** `GradientButton` ("Try Again").
- **Icons:** `Icons.Default.Build`, `Icons.AutoMirrored.Filled.ArrowBack`.

---

## 5. Navigation Summary & Graph Flow

```
[SplashScreen] (2s timer)
       │
       ▼
[LandingScreen] (Full-screen background image, Get Started button)
       │
       ▼
[PhoneEntryScreen] ◄───► [CountryPickerScreen]
       │
       ▼
[OtpVerifyScreen] (6-digit code, Resend timer, Verify button)
       │
       ▼
[BasicInfoScreen] ◄───► [DatePickerDialog / Standalone DatePickerScreen]
       │
       ▼
[UsernameScreen]
       │
       ▼
[PhotosSetupScreen] ◄───► [AddPhotoScreen] (Real Android PhotoPicker & Camera)
       │
       ▼
[AboutYouScreen] (Bio, Interests, Who are you looking for?)
       │
       ▼
[PermissionsScreen] (Real Android System Permissions: Location & Notifications)
       │
       ▼
╔═══════════════════════════════════════════════════════════════════════════════════════════╗
║                      DASHBOARD HUB (Header & Nav Bar: Tinder Pink #FD3A73)                ║
╠═══════════════════════════════════════════════════════════════════════════════════════════╣
║ 1. Explore Tab:                                                                           ║
║    ExploreScreen ──► FiltersScreen                                                        ║
║                  ──► FullProfileScreen                                                    ║
║                  ──► ItsAMatchScreen ──► ChatThreadScreen                                 ║
║                  ──► UsernameSearchScreen                                                 ║
║                                                                                           ║
║ 2. Chats Tab:                                                                             ║
║    ChatListScreen ──► ChatThreadScreen ──► ChatOptionsScreen                              ║
║                                        ──► ChatInfoScreen                                 ║
║                                        ──► SendAttachmentScreen (Real Media Picker/Camera)║
║                   ──► BlockedUsersScreen                                                  ║
║                                                                                           ║
║ 3. Friends Tab:                                                                           ║
║    FriendsScreen ──► FindFriendsScreen                                                    ║
║                  ──► RequestDetailScreen                                                  ║
║                  ──► FriendProfileScreen ──► ChatThreadScreen                             ║
║                                                                                           ║
║ 4. Settings Tab:                                                                          ║
║    SettingsScreen ──► MatchmakingScreen                                                   ║
║                   ──► VerificationStatusScreen ──► FaceCaptureScreen                      ║
║                   ──► PrivacySettingsScreen                                               ║
║                   ──► NotificationSettingsScreen                                          ║
║                   ──► ProPlansScreen                                                      ║
║                   ──► TermsScreen                                                         ║
║                   ──► CommunityRulesScreen                                                ║
║                   ──► AccountSettingsScreen ──► PhoneNumberScreen                         ║
║                                             ──► EditUsernameScreen                        ║
║                                             ──► EmailScreen                               ║
║                                             ──► LogoutConfirmationScreen ──► LandingScreen║
║                   ──► DeleteAccountScreen (unavailable; no data is removed)              ║
║                                                                                           ║
║ 5. Profile Tab:                                                                           ║
║    MyProfileScreen ──► EditPhotosScreen (Real PhotoPicker & 3:4 Crop)                     ║
║                    ──► EditBasicInfoScreen                                                ║
║                    ──► EditUsernameScreen                                                 ║
║                    ──► EditAboutScreen                                                    ║
║                    ──► PreviewProfileScreen                                               ║
║                    ──► FaceCaptureScreen (private selfie upload; not identity verification)║
╚═══════════════════════════════════════════════════════════════════════════════════════════╝
```

---

## 6. Real Android OS/SDK Implementations vs. External Services

> **Media selection and capture use Android system contracts.** Profile photos are cropped before upload; media is persisted in Firebase Storage. A stored selfie does not count as identity verification.

### Real Android Integrations (Fully Implemented)

1. **Android Photo Picker (`ActivityResultContracts.PickVisualMedia`):**
   - **Where:** `AddPhotoScreen.kt`, `PhotosSetupScreen.kt`, `FaceCaptureScreen.kt`, `EditPhotosScreen.kt`, `SendAttachmentScreen.kt`.
   - **How it works:** Directly leverages the Android system photo picker via `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())`. Invokes the OS-level modal allowing users to select genuine photos from device storage, Google Photos, or cloud drives.
   - **Rendering:** Loaded via `coil.compose.AsyncImage` which natively supports `content://` and `file://` local URIs with automatic decoding, downsampling, and caching.
2. **Camera Snap & Selfie Capture (`ActivityResultContracts.TakePicture`):**
   - **Where:** `AddPhotoScreen.kt`, `FaceCaptureScreen.kt`, `SendAttachmentScreen.kt`.
   - **How it works:** Launches the Android camera app with a FileProvider URI; the captured image is cropped where applicable and uploaded to Firebase Storage.
3. **Android Runtime OS Permissions (`ActivityResultContracts.RequestMultiplePermissions` & `RequestPermission`):**
   - **Where:** `PermissionsScreen.kt`, declared in `AndroidManifest.xml`.
   - **How it works:** Tapping the Location or Notifications card triggers the genuine Android system permission dialog (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `POST_NOTIFICATIONS` on Android 13+).
4. **Interactive 3:4 Profile Photo Cropping (CanHub Android Image Cropper):**
   - **Where:** `AddPhotoScreen.kt` and `EditPhotosScreen.kt`.
   - **How it works:** The Android cropper returns a real cropped JPEG URI before profile-photo upload.
5. **Native Android Date Picker (`android.app.DatePickerDialog`):**
   - **Where:** `BasicInfoScreen.kt`, `DatePickerScreen.kt`.
   - **How it works:** Launches the native Android OS DatePickerDialog attached to the host activity context, with age calculation and 18+ validation.
6. **Edge-to-Edge System Window Insets:**
   - **Where:** `MainActivity.kt` and all screens.
   - **How it works:** Zero-clipping edge-to-edge support with `statusBarsPadding()`, `navigationBarsPadding()`, and `imePadding()` ensuring seamless integration on gesture-nav and 3-button devices.
7. **Tinder Pink Header & Navigation Bar Color System:**
   - **Where:** `NavComponents.kt`, `Color.kt`, all dashboard screens and sub-screens.
   - **How it works:** Tinder Pink (`#FD3A73`) top bar header, Tinder Pink (`#FD3A73`) bottom nav bar, Warm Cream (`#FFF7F2`) body canvas. Pure white (`#FFFFFF`) is strictly disallowed on headers and nav bars.

---

### Client-Side State vs. External Server Dependencies

The app uses Firebase Authentication, Cloud Firestore, and Firebase Storage for account and app data. The project is `soulmate-e285e`; the Firestore database and Storage bucket are in `asia-south1`. Firestore rules/indexes and Storage rules were deployed on October 22, 2026. Re-deploy after changes to the checked-in rules or indexes and before releasing.

| Feature / UI Component | Screen(s) Involved | Current Implementation (Client-Side Behavior) | External Infrastructure Requirement |
| :--- | :--- | :--- | :--- |
| **Media Picking, Cropping & Camera** | Profile, selfie, and chat attachment screens | Android Photo Picker, `TakePicture`, CanHub cropper, and Firebase Storage uploads. | Storage rules are deployed; re-deploy after rule changes. |
| **OS System Permissions** | `PermissionsScreen.kt` | **100% Real Android SDK** (`RequestMultiplePermissions`). | None required (uses native OS). |
| **Date Selection** | `BasicInfoScreen.kt` | **100% Real Android SDK** (`DatePickerDialog`). | None required (uses native OS). |
| **SMS OTP Verification** | `PhoneEntryScreen.kt`, `OtpVerifyScreen.kt`, `PhoneNumberScreen.kt` | Firebase Phone Auth sends/resends the SMS, signs the user in with the verified credential, and updates an existing account phone only after OTP verification. | Enable Phone sign-in in Firebase Authentication; register the app's SHA-1/SHA-256 fingerprints in Firebase for app verification. Firebase test phone numbers can be used during development. |
| **Profile and username claims** | Onboarding and profile screens | Draft/private data and public profiles are stored in Firestore; username claims are transactional. | Firestore rules and indexes are deployed; re-deploy after changes. |
| **Peer-to-peer chat** | `ChatThreadScreen.kt`, `SendAttachmentScreen.kt` | Messages and conversations use Firestore; supported attachments upload to Storage. | Firestore/Storage rules and indexes are deployed; re-deploy after changes. |
| **Friends and matchmaking** | Explore and Friends screens | Swipes, matches, friend requests, friendships, blocks, and settings use Firestore. | Location-distance ranking, live presence, and moderation are not configured. |
| **Identity verification** | `FaceCaptureScreen.kt`, `VerificationStatusScreen.kt` | A selfie can be stored privately; the UI never marks it verified. | A real identity-review provider is not configured. |
| **In-App Billing** | `ProPlansScreen.kt` | Purchases and restore are explicitly unavailable; no subscription is activated. | Google Play Billing and Play Console products are not configured. |
| **Account deletion** | `DeleteAccountScreen.kt` | The screen states that deletion is unavailable and makes no account changes. | A secure deletion backend is not configured. |

Before testing SMS sign-in, confirm **Phone** is enabled under Firebase Console → Authentication → Sign-in method, and add the app's SHA-1 and SHA-256 signing-certificate fingerprints to the Android app registration. These fingerprints were not registered during this work. The Android Firebase config must be available as `app/google-services.json`. Use Firebase Authentication test phone numbers during development to avoid sending real SMS messages.

---

## 7. TestTags & Accessibility Directory

Every interactive element includes explicit `Modifier.testTag` identifiers adhering to snake_case naming conventions:

- **Auth & Onboarding:**
  - `phone_input`, `country_picker_trigger`, `send_code_button`
  - `otp_input_row`, `otp_cell_0` to `otp_cell_5`, `verify_otp_button`, `resend_otp_button`
  - `name_input`, `dob_input`, `gender_man`, `gender_woman`, `gender_nonbinary`
  - `username_input`, `photos_grid`, `add_photo_slot_0` to `slot_5`
  - `face_capture_camera_button`, `face_capture_gallery_button`
  - `bio_input`, `looking_for_men`, `looking_for_women`, `looking_for_everyone`
  - `permission_location_card`, `permission_notifications_card`, `enter_soulmate_button`
- **Dashboard & Explore:**
  - `dashboard_screen`, `explore_screen`, `explore_deck`
  - `explore_search_icon`, `explore_filter_icon`
  - `action_pass_button`, `action_like_button`, `action_superlike_button`, `action_rewind_button`
  - `nav_tab_explore`, `nav_tab_chats`, `nav_tab_friends`, `nav_tab_settings`, `nav_tab_profile`
- **Chats & Messaging:**
  - `chat_list`, `chat_thread_list`, `chat_input_field`, `chat_send_button`, `chat_attachment_button`
  - `chat_options_button`, `chat_info_avatar`
- **Friends & Social:**
  - `friends_tab_list`, `friend_requests_list`, `find_friends_button`
- **Settings:**
  - `settings_account_row`, `settings_matchmaking_row`, `settings_verification_row`
  - `settings_privacy_row`, `settings_notifications_row`, `settings_pro_plans_row`
  - `settings_logout_button`, `settings_delete_account_button`

---
*Document updated on October 14, 2026 for Soulmate Android Applet.*
