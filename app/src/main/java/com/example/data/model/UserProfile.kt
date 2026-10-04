package com.example.data.model

data class GoogleUserProfile(
    val email: String = "mkkarthiik@gmail.com",
    val displayName: String = "Karthiik",
    val photoUrl: String = "",
    val isLoggedIn: Boolean = true
)

data class CarDetails(
    val makeModel: String = "Hyundai i20 N Line",
    val variant: String = "N8 1.0 Turbo GDi DCT",
    val registrationNumber: String = "TN 07 CM 4521",
    val tyreSpecification: String = "195/55 R16 Low-Profile Alloys",
    val recommendedPsi: String = "33-35 PSI Cold",
    val hasConnectedCarBluelink: Boolean = true,
    val fastagProvider: String = "Active FASTag (ICICI Bank)",
    val fuelType: String = "Turbo Petrol (95 Octane)"
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
