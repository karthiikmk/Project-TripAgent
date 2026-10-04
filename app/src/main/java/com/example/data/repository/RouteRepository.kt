package com.example.data.repository

import com.example.data.api.ClaudeRouteService
import com.example.data.api.GeminiRouteService
import com.example.data.curated.CuratedRoutes
import com.example.data.local.RouteDao
import com.example.data.local.RouteEntity
import com.example.data.model.RouteParking
import com.example.data.model.RouteStop
import com.example.data.model.RouteTips
import com.example.data.model.SafeRoutePlan
import com.example.data.routing.HighwayCorridorEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class RoutingEngineMode(val label: String) {
    AUTO("Auto (AI / Offline)"),
    DETERMINISTIC("Highway Engine (Offline / Zero AI)"),
    GEMINI("Google Gemini"),
    CLAUDE("Anthropic Claude")
}

class RouteRepository(
    private val routeDao: RouteDao,
    private val geminiService: GeminiRouteService = GeminiRouteService(),
    private val claudeService: ClaudeRouteService = ClaudeRouteService()
) {
    val savedRoutes: Flow<List<SafeRoutePlan>> = routeDao.getAllSavedRoutes().map { entities ->
        entities.map { it.toSafeRoutePlan() }
    }

    val favoriteRoutes: Flow<List<SafeRoutePlan>> = routeDao.getFavoriteRoutes().map { entities ->
        entities.map { it.toSafeRoutePlan() }
    }

    fun getSavedRoutesForUser(email: String): Flow<List<SafeRoutePlan>> =
        routeDao.getSavedRoutesForUser(email).map { entities ->
            entities.map { it.toSafeRoutePlan() }
        }

    fun getCuratedRoutes(): List<SafeRoutePlan> = CuratedRoutes.routes

    fun getCuratedRouteById(id: String): SafeRoutePlan? {
        return CuratedRoutes.routes.find { it.id == id }
    }

    suspend fun getSavedRouteById(id: Long): SafeRoutePlan? {
        return routeDao.getRouteById(id)?.toSafeRoutePlan()
    }

    suspend fun saveRoute(plan: SafeRoutePlan, userEmail: String = "", carModel: String = ""): Long {
        val updatedPlan = plan.copy(
            userEmail = if (userEmail.isNotBlank()) userEmail else plan.userEmail,
            carModel = if (carModel.isNotBlank()) carModel else plan.carModel
        )
        val id = routeDao.insertRoute(updatedPlan.toEntity())
        try {
            com.example.data.firebase.FirestoreService.saveRouteToCloud(updatedPlan.userEmail, updatedPlan)
        } catch (_: Exception) {
            // Graceful fallback to offline local Room storage
        }
        return id
    }

    suspend fun syncRoutesWithCloud(userEmail: String) {
        if (userEmail.isBlank()) return
        try {
            val cloudRoutes = com.example.data.firebase.FirestoreService.fetchCloudRoutes(userEmail)
            cloudRoutes.forEach { cloudPlan ->
                routeDao.insertRoute(cloudPlan.toEntity())
            }
        } catch (_: Exception) {
            // Graceful offline fallback
        }
    }

    suspend fun deleteRoute(id: Long) {
        routeDao.deleteRouteById(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        routeDao.updateFavorite(id, isFavorite)
    }

    suspend fun planSafeRoute(
        origin: String,
        destination: String,
        notes: String = "",
        mode: RoutingEngineMode = RoutingEngineMode.AUTO,
        algorithmPrompt: String = ""
    ): Result<SafeRoutePlan> {
        return when (mode) {
            RoutingEngineMode.DETERMINISTIC -> {
                Result.success(HighwayCorridorEngine.resolveSafeRoute(origin, destination))
            }
            RoutingEngineMode.CLAUDE -> {
                claudeService.generateSafeRoute(origin, destination, notes, algorithmPrompt)
            }
            RoutingEngineMode.GEMINI -> {
                geminiService.generateSafeRoute(origin, destination, notes, algorithmPrompt)
            }
            RoutingEngineMode.AUTO -> {
                geminiService.generateSafeRoute(origin, destination, notes, algorithmPrompt)
            }
        }
    }

    private fun RouteEntity.toSafeRoutePlan(): SafeRoutePlan {
        return SafeRoutePlan(
            id = id.toString(),
            title = title,
            origin = origin,
            destination = destination,
            distanceApprox = distanceApprox,
            primaryRoads = primaryRoads.split(",").map { it.trim() },
            mapsUrl = mapsUrl,
            stops = emptyList(),
            verifyLine = verifyLine,
            tips = RouteTips(
                bestTime = bestTime,
                avoid = avoid,
                localNote = localNote,
                corridorNote = corridorNote
            ),
            parking = RouteParking(
                bestLotName = bestLotName,
                bestLotType = bestLotType,
                bestLotMapLink = bestLotMapLink,
                backupLotName = backupLotName,
                backupLotType = backupLotType,
                backupLotMapLink = backupLotMapLink
            ),
            isCurated = false,
            userEmail = userEmail,
            carModel = carModel
        )
    }

    private fun SafeRoutePlan.toEntity(): RouteEntity {
        return RouteEntity(
            id = id.toLongOrNull() ?: 0L,
            title = title,
            origin = origin,
            destination = destination,
            distanceApprox = distanceApprox,
            mapsUrl = mapsUrl,
            primaryRoads = primaryRoads.joinToString(", "),
            verifyLine = verifyLine,
            bestTime = tips.bestTime,
            avoid = tips.avoid,
            localNote = tips.localNote,
            corridorNote = tips.corridorNote,
            bestLotName = parking.bestLotName,
            bestLotType = parking.bestLotType,
            bestLotMapLink = parking.bestLotMapLink,
            backupLotName = parking.backupLotName,
            backupLotType = parking.backupLotType,
            backupLotMapLink = parking.backupLotMapLink,
            formattedOutput = toFormattedPromptOutput(),
            isFavorite = false,
            userEmail = userEmail,
            carModel = carModel
        )
    }
}
