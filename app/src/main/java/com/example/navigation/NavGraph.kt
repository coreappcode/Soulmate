package com.example.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.ui.screens.profile_setup.VerificationProcessingScreen
import com.example.ui.screens.profile_setup.VerificationSuccessScreen
import com.example.ui.screens.splash.SplashScreen

@Composable
fun SoulmateNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: SoulmateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    navController.navigate(Routes.LANDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
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
                onPhoneNumberChange = { viewModel.updatePhoneNumber(it) },
                onCountryTap = { navController.navigate(Routes.COUNTRY_PICKER) },
                onSendCode = { navController.navigate(Routes.OTP_VERIFY) },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. OTP Verification Screen
        composable(Routes.OTP_VERIFY) {
            OtpVerifyScreen(
                countryDialCode = uiState.selectedCountry.dialCode,
                phoneNumber = uiState.phoneNumber,
                otp = uiState.otpCode,
                onOtpChange = { viewModel.updateOtp(it) },
                onVerifySuccess = {
                    viewModel.setPhoneVerified(true)
                    navController.navigate(Routes.BASIC_INFO)
                },
                onChangeNumber = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
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
                onContinue = { navController.navigate(Routes.PHOTOS) },
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Photos Setup Screen (Navigates directly to About You, face verification is moved to Profile)
        composable(Routes.PHOTOS) {
            PhotosSetupScreen(
                photos = uiState.photos,
                onAddPhotoClick = { navController.navigate(Routes.ADD_PHOTO) },
                onPhotoSelected = { viewModel.addPhoto(it) },
                onRemovePhoto = { viewModel.removePhoto(it) },
                onSetMainPhoto = { viewModel.setMainPhoto(it) },
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

        // 9. Face Capture Screen (Accessible from Profile page)
        composable(Routes.FACE_CAPTURE) {
            FaceCaptureScreen(
                onFaceCaptured = { selfieUrl ->
                    viewModel.setCapturedSelfie(selfieUrl)
                    navController.navigate(Routes.VERIFY_PROCESSING)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 10. Verification Processing Screen
        composable(Routes.VERIFY_PROCESSING) {
            VerificationProcessingScreen(
                onVerificationComplete = {
                    viewModel.setFaceVerified(true)
                    navController.navigate(Routes.VERIFY_SUCCESS)
                }
            )
        }

        // 11. Verification Success Screen (Returns to Profile page)
        composable(Routes.VERIFY_SUCCESS) {
            VerificationSuccessScreen(
                onContinue = {
                    navController.navigate(Routes.DASHBOARD_PROFILE) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                }
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
                onAllowLocation = { viewModel.setLocationPermission(true) },
                onAllowNotifications = { viewModel.setNotificationsPermission(true) },
                onEnterSoulmate = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LANDING) { inclusive = true }
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
                    navController.navigate(Routes.LANDING) {
                        popUpTo(Routes.LANDING) { inclusive = true }
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
                    navController.navigate(Routes.LANDING) {
                        popUpTo(Routes.LANDING) { inclusive = true }
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
                onEnableLocation = {
                    viewModel.setLocationPermission(true)
                    navController.popBackStack()
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
