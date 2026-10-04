package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import com.example.data.model.CarDetails
import com.example.data.model.GoogleUserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("safe_route_user_session", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<GoogleUserProfile> = _userProfile.asStateFlow()

    private val _carDetails = MutableStateFlow(loadCarDetails())
    val carDetails: StateFlow<CarDetails> = _carDetails.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun loadUserProfile(): GoogleUserProfile {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, true) // default logged in with Google account
        val email = prefs.getString(KEY_EMAIL, "mkkarthiik@gmail.com") ?: "mkkarthiik@gmail.com"
        val name = prefs.getString(KEY_DISPLAY_NAME, "Karthiik") ?: "Karthiik"
        return GoogleUserProfile(
            email = email,
            displayName = name,
            photoUrl = "",
            isLoggedIn = isLoggedIn
        )
    }

    private fun loadCarDetails(): CarDetails {
        return CarDetails(
            makeModel = prefs.getString(KEY_CAR_MAKE, "Hyundai i20 N Line") ?: "Hyundai i20 N Line",
            variant = prefs.getString(KEY_CAR_VARIANT, "N8 1.0 Turbo GDi DCT") ?: "N8 1.0 Turbo GDi DCT",
            registrationNumber = prefs.getString(KEY_CAR_REG, "TN 07 CM 4521") ?: "TN 07 CM 4521",
            tyreSpecification = prefs.getString(KEY_CAR_TYRE, "195/55 R16 Low-Profile Alloys") ?: "195/55 R16 Low-Profile Alloys",
            recommendedPsi = prefs.getString(KEY_CAR_PSI, "33-35 PSI Cold") ?: "33-35 PSI Cold",
            hasConnectedCarBluelink = prefs.getBoolean(KEY_CAR_BLUELINK, true),
            fastagProvider = prefs.getString(KEY_CAR_FASTAG, "Active FASTag (ICICI Bank)") ?: "Active FASTag (ICICI Bank)",
            fuelType = prefs.getString(KEY_CAR_FUEL, "Turbo Petrol (95 Octane)") ?: "Turbo Petrol (95 Octane)"
        )
    }

    private fun loadThemeMode(): AppThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name
        return try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.LIGHT
        }
    }

    fun signInWithGoogle(email: String, displayName: String, carDetails: CarDetails? = null) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()

        _userProfile.value = GoogleUserProfile(
            email = email,
            displayName = displayName,
            isLoggedIn = true
        )

        if (carDetails != null) {
            updateCarDetails(carDetails)
        }
    }

    fun signOut() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
        _userProfile.value = _userProfile.value.copy(isLoggedIn = false)
    }

    fun updateCarDetails(details: CarDetails) {
        prefs.edit()
            .putString(KEY_CAR_MAKE, details.makeModel)
            .putString(KEY_CAR_VARIANT, details.variant)
            .putString(KEY_CAR_REG, details.registrationNumber)
            .putString(KEY_CAR_TYRE, details.tyreSpecification)
            .putString(KEY_CAR_PSI, details.recommendedPsi)
            .putBoolean(KEY_CAR_BLUELINK, details.hasConnectedCarBluelink)
            .putString(KEY_CAR_FASTAG, details.fastagProvider)
            .putString(KEY_CAR_FUEL, details.fuelType)
            .apply()

        _carDetails.value = details
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_DISPLAY_NAME = "user_display_name"

        private const val KEY_CAR_MAKE = "car_make"
        private const val KEY_CAR_VARIANT = "car_variant"
        private const val KEY_CAR_REG = "car_reg"
        private const val KEY_CAR_TYRE = "car_tyre"
        private const val KEY_CAR_PSI = "car_psi"
        private const val KEY_CAR_BLUELINK = "car_bluelink"
        private const val KEY_CAR_FASTAG = "car_fastag"
        private const val KEY_CAR_FUEL = "car_fuel"

        private const val KEY_THEME_MODE = "theme_mode"
    }
}
