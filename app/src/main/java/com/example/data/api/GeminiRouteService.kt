package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.RouteParking
import com.example.data.model.RouteStop
import com.example.data.model.RouteTips
import com.example.data.model.SafeRoutePlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class GeminiRouteService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateSafeRoute(
        origin: String,
        destination: String,
        routeNotes: String = "",
        algorithmPrompt: String = ""
    ): Result<SafeRoutePlan> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = if (algorithmPrompt.isNotBlank()) {
            buildString {
                append(algorithmPrompt).append("\n\n")
                append("Plan the safe highway trip from:\n")
                append("Start: ").append(origin).append("\n")
                append("Destination: ").append(destination).append("\n")
                if (routeNotes.isNotBlank()) append("Additional notes: ").append(routeNotes).append("\n")
            }
        } else {
            buildPrompt(origin, destination, routeNotes)
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Use smart offline route builder
            Log.d("GeminiRouteService", "Using built-in safe routing engine (no Gemini API key)")
            return@withContext Result.success(buildOfflineSafeRoute(origin, destination))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("topP", 0.8)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiRouteService", "API error ${response.code}, falling back to built-in generator")
                return@withContext Result.success(buildOfflineSafeRoute(origin, destination))
            }

            val responseBody = response.body?.string().orEmpty()
            val parsedPlan = parseGeminiResponse(responseBody, origin, destination)
            Result.success(parsedPlan)
        } catch (e: Exception) {
            Log.e("GeminiRouteService", "Gemini call failed", e)
            Result.success(buildOfflineSafeRoute(origin, destination))
        }
    }

    private fun buildPrompt(origin: String, destination: String, notes: String): String {
        return """
You plan car routes for a Hyundai i20 N Line with an active Bluelink subscription, anywhere in India.
The goal is the safest good road, not the shortest. Distance and duration do not matter. A longer divided highway is better than a shorter village road, ghat shortcut, or city cut.

## Safest path road rating table:
- ★★★★★: Access-controlled expressway, open end to end, median, no village crossings. NE-series, state expressways, greenfield corridors. (Penalty: 1.0)
- ★★★★★: Access-controlled city ring road or bypass with a median. (Penalty: 1.1)
- ★★★★☆: Divided four-lane or six-lane road, median barrier, service road or flyover through every town. (Penalty: 1.2)
- ★★★☆☆: Divided road where some towns are crossed at grade. (Penalty: 2.0)
- ★★☆☆☆: Two-lane NH with paved shoulders. (Penalty: 5.0)
- ★☆☆☆☆: Undivided two-lane NH, only if no divided road exists. (Penalty: 10.0)
- ⚠️: Undivided state highway, last join only. (Penalty: 50.0)
- ⛔: Village road, market road, inner-city cut, unmarked ghat shortcut, expressway stretch not yet open. (REJECT)

How to rank:
1. List two or three real corridors between start and destination: the expressway, the divided NH, and the shortcut Maps offers. Split each into segments with road number and rating.
2. Reject any corridor with a ⛔ segment outside the exempt legs.
3. The corridor with the highest route rating wins, whatever the extra distance.
4. Tie breakers: fewest km at that weakest rating, fewest hazard points, lowest sum of km × penalty, more toll plazas.

Pin methodology:
- Plan the road first. Every segment must be an expressway, national highway, or divided bypass. Name the number.
- A pin is only a lock. It is a pass-through. The driver does not stop and does not get out.
- Every toll plaza on the planned road is a pin by default. It spans both carriageways and cannot force a U-turn.
- Every change of road number gets a pin just after the change, on the new road.
- Place the pin just after the point where Maps would turn off, on the planned road. Not before it.
- Reject a village name, area name, or locality. Reject words Junction, Exit, and Pass-through.

Link:
- Form: https://www.google.com/maps/dir/START/STOP/STOP/END/
- Plus signs between words. Nothing after the last slash. No api=1, no coordinates, no waypoints.
- Add the road to every stop.

Print strictly in this order and format:
🚗 Trip details
- Distance: about [N] km on the chosen route.
- Drive time: about [N] h without stops.
- Path: [start] → [road number via towns] → [road number] → [destination].

🗺️ Route
- [filled Google Maps URL]
- Check: preview stays on the named expressway, NH, or divided bypass
- If no directions: search the destination → Directions → set the start → Add stop for each pin
- [Start] → [NH or expressway number]
- [Stop 1 name] [Road number] → pass-through, pay and continue
- [Stop 2 name] [Road number] → pass-through, pay and continue
- [Destination]
- ✅ Road numbers used: [numbers].

🏁 Route rating
- Rating: [stars of the weakest segment]
- Selected: [corridor as road numbers and via towns]
- Weakest: [road number, stretch, km, and why]
- Rejected: [other corridors, stars, and why]

📝 Tips
- Best time: [best departure window]
- Avoid: [stretches, hours, or diversions to avoid]
- Local note: [local driving or temple/visit practice]
- Corridor note: [surface, patrol, 1033 availability]

🅿️ Parking
- Best: [lot name], [type], [https://www.google.com/maps/search/...]
- Backup: [lot name], [type], [https://www.google.com/maps/search/...]

🆘 Emergency
- Car, breakdown, flat tyre, battery, or no fuel: RSA button, middle button on the rear-view mirror
- Car, accident or injury: SOS button, left button on the rear-view mirror
- Hyundai tow or fuel: 1800 102 4645
- Any emergency: 112
- Police: 100
- Ambulance: 108
- Fire: 101
- Highway blocked or crash: 1033

Plan the trip from:
Start: $origin
Destination: $destination
${if (notes.isNotBlank()) "Additional notes: $notes" else ""}
        """.trimIndent()
    }

    private fun parseGeminiResponse(rawJson: String, origin: String, destination: String): SafeRoutePlan {
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

            if (text.isBlank()) {
                return buildOfflineSafeRoute(origin, destination)
            }

            parseFormattedText(text, origin, destination)
        } catch (e: Exception) {
            Log.e("GeminiRouteService", "Parse error", e)
            buildOfflineSafeRoute(origin, destination)
        }
    }

    fun parseFormattedText(text: String, origin: String, destination: String): SafeRoutePlan {
        val lines = text.lines().map { it.trim() }

        var mapsUrl = ""
        val stops = mutableListOf<RouteStop>()
        var verifyLine = ""
        var bestTime = "Early morning (05:00 AM - 06:00 AM) to clear city exit before peak traffic."
        var avoid = "Avoid night driving on undivided sections and unverified ghat shortcuts."
        var localNote = "FASTag mandatory at all toll plazas. Stay on main carriageway."
        var corridorNote = "Divided highway with regular NHAI 1033 patrolling and emergency assistance."

        var bestLotName = "Primary Tourist & Visitor Parking"
        var bestLotType = "Public Paved"
        var bestLotMap = "https://www.google.com/maps/search/${encodeForMaps(destination)}+Parking/"

        var backupLotName = "Secondary Commercial Parking"
        var backupLotType = "Commercial"
        var backupLotMap = "https://www.google.com/maps/search/${encodeForMaps(destination)}+Car+Parking/"

        lines.forEach { line ->
            when {
                line.startsWith("- https://www.google.com/maps/dir/") || line.startsWith("https://www.google.com/maps/dir/") -> {
                    mapsUrl = line.removePrefix("- ").trim()
                }
                line.startsWith("- ✅") || line.startsWith("✅") -> {
                    verifyLine = line.removePrefix("- ").trim()
                }
                line.contains("→ pass-through", ignoreCase = true) -> {
                    val cleaned = line.removePrefix("- ").trim()
                    val parts = cleaned.split("→ pass-through")
                    val placeAndRoad = parts.firstOrNull()?.trim().orEmpty()
                    stops.add(RouteStop(placeName = placeAndRoad, roadNumber = "", isPassThrough = true))
                }
                line.startsWith("- Best time:") -> {
                    bestTime = line.substringAfter("- Best time:").trim()
                }
                line.startsWith("- Avoid:") -> {
                    avoid = line.substringAfter("- Avoid:").trim()
                }
                line.startsWith("- Local note:") -> {
                    localNote = line.substringAfter("- Local note:").trim()
                }
                line.startsWith("- Corridor note:") -> {
                    corridorNote = line.substringAfter("- Corridor note:").trim()
                }
                line.startsWith("- Best:") -> {
                    val content = line.substringAfter("- Best:").trim()
                    val tokens = content.split(",").map { it.trim() }
                    if (tokens.isNotEmpty()) bestLotName = tokens[0]
                    if (tokens.size > 1) bestLotType = tokens[1]
                    if (tokens.size > 2) bestLotMap = tokens[2]
                }
                line.startsWith("- Backup:") -> {
                    val content = line.substringAfter("- Backup:").trim()
                    val tokens = content.split(",").map { it.trim() }
                    if (tokens.isNotEmpty()) backupLotName = tokens[0]
                    if (tokens.size > 1) backupLotType = tokens[1]
                    if (tokens.size > 2) backupLotMap = tokens[2]
                }
            }
        }

        if (mapsUrl.isBlank()) {
            mapsUrl = generateMapsDirUrl(origin, destination, stops.map { it.placeName })
        }

        if (verifyLine.isBlank()) {
            verifyLine = "✅ Road numbers used: Divided National Highway / Expressway corridor. Final approach leaves NH to destination."
        }

        return SafeRoutePlan(
            id = "custom-${System.currentTimeMillis()}",
            title = "$origin to $destination",
            origin = origin,
            destination = destination,
            distanceApprox = "Audited Highway Corridor",
            primaryRoads = listOf("Divided NH / Expressway"),
            mapsUrl = mapsUrl,
            stops = stops,
            verifyLine = verifyLine,
            tips = RouteTips(bestTime, avoid, localNote, corridorNote),
            parking = RouteParking(bestLotName, bestLotType, bestLotMap, backupLotName, backupLotType, backupLotMap),
            isCurated = false
        )
    }

    private fun generateMapsDirUrl(start: String, end: String, stops: List<String>): String {
        val sb = StringBuilder("https://www.google.com/maps/dir/")
        sb.append(encodeForMaps(start)).append("/")
        stops.forEach { stop ->
            if (stop.isNotBlank()) {
                sb.append(encodeForMaps(stop)).append("/")
            }
        }
        sb.append(encodeForMaps(end)).append("/")
        return sb.toString()
    }

    private fun encodeForMaps(input: String): String {
        return input.trim()
            .replace(",", "")
            .replace(".", "")
            .replace("&", "and")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString("+")
    }

    fun buildOfflineSafeRoute(origin: String, destination: String): SafeRoutePlan {
        return com.example.data.routing.HighwayCorridorEngine.resolveSafeRoute(origin, destination)
    }
}
