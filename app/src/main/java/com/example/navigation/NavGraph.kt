package com.example.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.SoulmateViewModel
import com.example.ui.screens.auth.OtpVerifyScreen
import com.example.ui.screens.auth.PhoneEntryScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.onboarding.CountryPickerScreen
import com.example.ui.screens.onboarding.LandingScreen
import com.example.ui.screens.profile_setup.AboutYouScreen
import com.example.ui.screens.profile_setup.AddPhotoScreen
import com.example.ui.screens.profile_setup.BasicInfoScreen
import com.example.ui.screens.profile_setup.DatePickerScreen
import com.example.ui.screens.profile_setup.FaceCaptureScreen
import com.example.ui.screens.profile_setup.PermissionsScreen
import com.example.ui.screens.profile_setup.PhotosSetupScreen
import com.example.ui.screens.profile_setup.UsernameScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.utils.findActivity

@Composable
fun SoulmateNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: SoulmateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val activity = LocalContext.current.findActivity()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setAppInForeground(true)
                Lifecycle.Event.ON_STOP -> viewModel.setAppInForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        viewModel.setAppInForeground(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setAppInForeground(false)
        }
    }

    LaunchedEffect(uiState.dataError) {
        val dashboardVisible = navController.currentDestination?.route in
            setOf(Routes.DASHBOARD, Routes.DASHBOARD_PROFILE)
        if (!dashboardVisible) {
            uiState.dataError?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(phoneAuthState.codeSentVersion) {
        if (
            phoneAuthState.codeSentVersion > 0 &&
            navController.currentDestination?.route == Routes.PHONE_ENTRY
        ) {
            navController.navigate(Routes.OTP_VERIFY)
        }
    }

    LaunchedEffect(
        uiState.isPhoneVerified,
        uiState.isProfileLoading,
        uiState.isOnboardingComplete
    ) {
        val currentRoute = navController.currentDestination?.route
        if (
            uiState.isPhoneVerified &&
            !uiState.isProfileLoading &&
            (currentRoute == Routes.SPLASH ||
                currentRoute == Routes.PHONE_ENTRY ||
                currentRoute == Routes.OTP_VERIFY)
        ) {
            val destination = if (uiState.isOnboardingComplete) Routes.DASHBOARD else Routes.BASIC_INFO
            navController.navigate(destination) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    if (!uiState.isProfileLoading) {
                        val destination = if (uiState.isPhoneVerified) {
                            if (uiState.isOnboardingComplete) Routes.DASHBOARD else Routes.BASIC_INFO
                        } else {
                            Routes.LANDING
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Landing Screen
        composable(Routes.LANDING) {
            LandingScreen(
                onGetStarted = {
                    navController.navigate(Routes.PHONE_ENTRY)
                }
            )
        }

        // 3. Country Picker Screen
        composable(Routes.COUNTRY_PICKER) {
            CountryPickerScreen(
                selectedCountry = uiState.selectedCountry,
                onCountrySelected = { country ->
                    viewModel.selectCountry(country)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Phone Entry Screen
        composable(Routes.PHONE_ENTRY) {
            PhoneEntryScreen(
                country = uiState.selectedCountry,
                phoneNumber = uiState.phoneNumber,
                isLoading = phoneAuthState.isSendingCode,
                errorMessage = phoneAuthState.errorMessage,
                onPhoneNumberChange = { viewModel.updatePhoneNumber(it) },
                onCountryTap = { navController.navigate(Routes.COUNTRY_PICKER) },
                onSendCode = {
                    val fullPhoneNumber =
                        uiState.selectedCountry.dialCode + uiState.phoneNumber.filter(Char::isDigit)
                    if (activity != null) {
                        viewModel.sendPhoneVerificationCode(activity, fullPhoneNumber)
                    } else {
                        viewModel.setPhoneAuthError("Unable to start phone verification right now.")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. OTP Verification Screen
        composable(Routes.OTP_VERIFY) {
            OtpVerifyScreen(
                countryDialCode = uiState.selectedCountry.dialCode,
                phoneNumber = uiState.phoneNumber,
                otp = uiState.otpCode,
                isVerifying = phoneAuthState.isVerifying,
                isSendingCode = phoneAuthState.isSendingCode,
                errorMessage = phoneAuthState.errorMessage,
                codeSentVersion = phoneAuthState.codeSentVersion,
                onOtpChange = { viewModel.updateOtp(it) },
                onVerify = { viewModel.verifyPhoneCode(it) },
                onResendCode = {
                    val fullPhoneNumber =
                        uiState.selectedCountry.dialCode + uiState.phoneNumber.filter(Char::isDigit)
                    if (activity != null) {
                        viewModel.sendPhoneVerificationCode(
                            activity,
                            fullPhoneNumber,
                            forceResending = true
                        )
                    } else {
                        viewModel.setPhoneAuthError("Unable to resend the verification code right now.")
                    }
                },
                onChangeNumber = {
                    viewModel.resetPhoneAuth()
                    navController.popBackStack()
                },
                onBack = {
                    viewModel.resetPhoneAuth()
                    navController.popBackStack()
                }
            )
        }

        // 6. Basic Info Screen
        composable(Routes.BASIC_INFO) {
            BasicInfoScreen(
                fullName = uiState.fullName,
                dateOfBirth = uiState.dateOfBirth,
                gender = uiState.gender,
                onFullNameChange = { viewModel.updateFullName(it) },
                onDateOfBirthChange = { viewModel.updateDateOfBirth(it) },
                onOpenDatePicker = { navController.navigate(Routes.DATE_PICKER) },
                onGenderSelect = { viewModel.updateGender(it) },
                onContinue = { navController.navigate(Routes.USERNAME) },
                onBack = { navController.popBackStack() }
            )
        }

        // 6b. Date Picker Screen
        composable(Routes.DATE_PICKER) {
            DatePickerScreen(
                currentDate = uiState.dateOfBirth,
                onDateConfirmed = { date ->
                    viewModel.updateDateOfBirth(date)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Username Screen
        composable(Routes.USERNAME) {
            UsernameScreen(
                username = uiState.username,
                onUsernameChange = { viewModel.updateUsername(it) },
                onCheckAvailability = { viewModel.isUsernameAvailable(it) },
                onContinue = { navController.navigate(Routes.PHOTOS) },
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Photos Setup Screen (Navigates directly to About You, face verification is moved to Profile)
        composable(Routes.PHOTOS) {
            PhotosSetupScreen(
                photos = uiState.photos,
                onAddPhotoClick = { navController.navigate(Routes.ADD_PHOTO) },
                onRemovePhoto = { viewModel.removePhoto(it) },
                onSetMainPhoto = { viewModel.setMainPhoto(it) },
                isUploading = uiState.isUploadingPhoto,
                errorMessage = uiState.dataError,
                onContinue = { navController.navigate(Routes.ABOUT_YOU) },
                onBack = { navController.popBackStack() }
            )
        }

        // 8b. Add Photo Screen
        composable(Routes.ADD_PHOTO) {
            AddPhotoScreen(
                onPhotoAdded = { photoUrl ->
                    viewModel.addPhoto(photoUrl)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Selfie capture stores the image for review; it does not claim identity verification.
        composable(Routes.FACE_CAPTURE) {
            FaceCaptureScreen(
                onFaceCaptured = { selfieUrl ->
                    viewModel.setCapturedSelfie(selfieUrl)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 12. About You Screen
        composable(Routes.ABOUT_YOU) {
            AboutYouScreen(
                bio = uiState.bio,
                selectedInterests = uiState.selectedInterests,
                selectedLookingFor = uiState.selectedLookingFor,
                onBioChange = { viewModel.updateBio(it) },
                onToggleInterest = { viewModel.toggleInterest(it) },
                onToggleLookingFor = { viewModel.toggleLookingFor(it) },
                onContinue = { navController.navigate(Routes.PERMISSIONS) },
                onBack = { navController.popBackStack() }
            )
        }

        // 13. Permissions Screen
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                locationAllowed = uiState.locationPermissionGranted,
                notificationsAllowed = uiState.notificationsPermissionGranted,
                onAllowLocation = { granted -> viewModel.setLocationPermission(granted) },
                onAllowNotifications = { granted -> viewModel.setNotificationsPermission(granted) },
                isSaving = uiState.isSavingProfile,
                errorMessage = uiState.dataError,
                onEnterSoulmate = {
                    viewModel.completeOnboarding {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onSkip = {
                    viewModel.completeOnboarding {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 14. Dashboard Shell
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                initialTab = Routes.DASHBOARD_EXPLORE,
                viewModel = viewModel,
                onNavigateToFaceVerify = {
                    navController.navigate(Routes.FACE_CAPTURE)
                },
                onNavigateToDatePicker = {
                    navController.navigate(Routes.DATE_PICKER)
                },
                onNavigateToCountryPicker = {
                    navController.navigate(Routes.COUNTRY_PICKER)
                },
                onLogout = {
                    viewModel.signOut()
                    navController.navigate(Routes.LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 14b. Dashboard Profile Destination
        composable(Routes.DASHBOARD_PROFILE) {
            DashboardScreen(
                initialTab = Routes.DASHBOARD_PROFILE,
                viewModel = viewModel,
                onNavigateToFaceVerify = {
                    navController.navigate(Routes.FACE_CAPTURE)
                },
                onNavigateToDatePicker = {
                    navController.navigate(Routes.DATE_PICKER)
                },
                onNavigateToCountryPicker = {
                    navController.navigate(Routes.COUNTRY_PICKER)
                },
                onLogout = {
                    viewModel.signOut()
                    navController.navigate(Routes.LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 15. System Screens
        composable(Routes.SYSTEM_NO_INTERNET) {
            com.example.ui.screens.system.NoInternetScreen(
                onRetry = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYSTEM_LOCATION_REQUIRED) {
            com.example.ui.screens.system.LocationRequiredScreen(
                onEnableLocation = { granted ->
                    viewModel.setLocationPermission(granted)
                    if (granted) navController.popBackStack()
                },
                onContinueCountryOnly = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYSTEM_LOADING) {
            com.example.ui.screens.system.LoadingScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYSTEM_ERROR) {
            com.example.ui.screens.system.ErrorScreen(
                onRetry = { navController.popBackStack() },
                onGoHome = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYSTEM_MAINTENANCE) {
            com.example.ui.screens.system.MaintenanceScreen(
                onTryAgain = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
