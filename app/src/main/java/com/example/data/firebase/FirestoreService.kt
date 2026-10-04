package com.example.data.firebase

import android.util.Log
import com.example.data.model.RouteEmergency
import com.example.data.model.RouteParking
import com.example.data.model.RouteStop
import com.example.data.model.RouteTips
import com.example.data.model.SafeRoutePlan
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class CloudParkingPin(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val locationType: String = "Secure Covered Parking",
    val mapLink: String = "",
    val verifiedBy: String = "",
    val carSuitability: String = "Hyundai i20 N Line / Sedans / SUVs",
    val timestamp: Long = System.currentTimeMillis()
)

object FirestoreService {
    private const val TAG = "FirestoreService"

    private val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not yet initialized: ${e.message}")
            null
        }

    fun isConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveRouteToCloud(userEmail: String, plan: SafeRoutePlan): Boolean {
        val db = firestore ?: return false
        val cleanEmail = userEmail.ifBlank { "anonymous" }.replace(".", "_").replace("@", "_at_")
        return try {
            val routeDoc = hashMapOf(
                "id" to plan.id,
                "title" to plan.title,
                "origin" to plan.origin,
                "destination" to plan.destination,
                "distanceApprox" to plan.distanceApprox,
                "primaryRoads" to plan.primaryRoads,
                "mapsUrl" to plan.mapsUrl,
                "verifyLine" to plan.verifyLine,
                "bestTime" to plan.tips.bestTime,
                "avoid" to plan.tips.avoid,
                "corridorNote" to plan.tips.corridorNote,
                "bestLotName" to plan.parking.bestLotName,
                "bestLotMapLink" to plan.parking.bestLotMapLink,
                "backupLotName" to plan.parking.backupLotName,
                "backupLotMapLink" to plan.parking.backupLotMapLink,
                "hyundaiRsaPhone" to plan.emergency.hyundaiRsaPhone,
                "nhaiEmergency" to plan.emergency.nhaiHighwayEmergency,
                "userEmail" to userEmail,
                "carModel" to plan.carModel,
                "updatedAt" to System.currentTimeMillis()
            )

            withTimeoutOrNull(4000) {
                db.collection("users")
                    .document(cleanEmail)
                    .collection("route_history")
                    .document(plan.id.ifBlank { "route_${System.currentTimeMillis()}" })
                    .set(routeDoc, SetOptions.merge())
                    .await()
            } != null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save route to Firestore: ${e.message}")
            false
        }
    }

    suspend fun fetchCloudRoutes(userEmail: String): List<SafeRoutePlan> {
        val db = firestore ?: return emptyList()
        val cleanEmail = userEmail.ifBlank { "anonymous" }.replace(".", "_").replace("@", "_at_")
        return try {
            val result = withTimeoutOrNull(4000) {
                db.collection("users")
                    .document(cleanEmail)
                    .collection("route_history")
                    .get()
                    .await()
            } ?: return emptyList()

            result.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val origin = doc.getString("origin") ?: ""
                val dest = doc.getString("destination") ?: ""
                @Suppress("UNCHECKED_CAST")
                val roads = doc.get("primaryRoads") as? List<String> ?: listOf("NH Highway")
                val mapsUrl = doc.getString("mapsUrl") ?: ""
                val verifyLine = doc.getString("verifyLine") ?: "Audited Highway Corridor"

                SafeRoutePlan(
                    id = id,
                    title = title,
                    origin = origin,
                    destination = dest,
                    distanceApprox = doc.getString("distanceApprox") ?: "300 km",
                    primaryRoads = roads,
                    mapsUrl = mapsUrl,
                    stops = listOf(
                        RouteStop(
                            placeName = "Toll Corridor",
                            roadNumber = roads.firstOrNull() ?: "NH",
                            isPassThrough = true,
                            note = "→ Verified Cloud Sync"
                        )
                    ),
                    verifyLine = verifyLine,
                    tips = RouteTips(
                        bestTime = doc.getString("bestTime") ?: "Daylight hours (06:00 - 18:00)",
                        avoid = doc.getString("avoid") ?: "Avoid unlit interior bypasses after dark",
                        localNote = "Synced from Cloud Firestore",
                        corridorNote = doc.getString("corridorNote") ?: "Divided multi-lane highway"
                    ),
                    parking = RouteParking(
                        bestLotName = doc.getString("bestLotName") ?: "Secure Verified Parking",
                        bestLotType = "Covered / Security Guard",
                        bestLotMapLink = doc.getString("bestLotMapLink") ?: "",
                        backupLotName = doc.getString("backupLotName") ?: "Commercial Mall Basement",
                        backupLotType = "Multi-level Covered",
                        backupLotMapLink = doc.getString("backupLotMapLink") ?: ""
                    ),
                    emergency = RouteEmergency(
                        hyundaiRsaPhone = doc.getString("hyundaiRsaPhone") ?: "1800 102 4645",
                        nhaiHighwayEmergency = doc.getString("nhaiEmergency") ?: "1033"
                    ),
                    isCurated = false,
                    userEmail = userEmail,
                    carModel = doc.getString("carModel") ?: "Hyundai i20 N Line"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch routes from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveParkingPin(pin: CloudParkingPin): Boolean {
        val db = firestore ?: return false
        return try {
            val docId = pin.id.ifBlank { "pin_${System.currentTimeMillis()}" }
            withTimeoutOrNull(4000) {
                db.collection("parking_pins")
                    .document(docId)
                    .set(pin, SetOptions.merge())
                    .await()
            } != null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save parking pin: ${e.message}")
            false
        }
    }

    suspend fun fetchRoutingPrompt(): String? {
        val db = firestore ?: return null
        return try {
            withTimeoutOrNull(4000) {
                val doc = db.collection("config")
                    .document("routing")
                    .get()
                    .await()
                doc.getString("prompt")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch routing prompt from Firestore: ${e.message}")
            null
        }
    }
}
