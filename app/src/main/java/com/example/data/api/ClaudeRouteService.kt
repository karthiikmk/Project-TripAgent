package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SafeRoutePlan
import com.example.data.routing.HighwayCorridorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ClaudeRouteService(
    private val geminiService: GeminiRouteService = GeminiRouteService()
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateSafeRoute(
        origin: String,
        destination: String,
        routeNotes: String = ""
    ): Result<SafeRoutePlan> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.CLAUDE_API_KEY
        } catch (e: Exception) {
            null
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_CLAUDE_API_KEY") {
            Log.d("ClaudeRouteService", "No Claude API key found. Delegating to HighwayCorridorEngine")
            return@withContext Result.success(HighwayCorridorEngine.resolveSafeRoute(origin, destination))
        }

        val prompt = buildClaudeUserPrompt(origin, destination, routeNotes)
        val systemPrompt = buildClaudeSystemPrompt()

        try {
            val url = "https://api.anthropic.com/v1/messages"

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            val requestJson = JSONObject().apply {
                put("model", "claude-3-5-haiku-20241022")
                put("max_tokens", 1500)
                put("system", systemPrompt)
                put("messages", messagesArray)
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("ClaudeRouteService", "Claude API returned code ${response.code}, falling back to deterministic engine")
                return@withContext Result.success(HighwayCorridorEngine.resolveSafeRoute(origin, destination))
            }

            val responseBody = response.body?.string().orEmpty()
            val text = parseClaudeResponse(responseBody)

            if (text.isBlank()) {
                return@withContext Result.success(HighwayCorridorEngine.resolveSafeRoute(origin, destination))
            }

            val parsedPlan = geminiService.parseFormattedText(text, origin, destination)
            Result.success(parsedPlan)
        } catch (e: Exception) {
            Log.e("ClaudeRouteService", "Claude call failed", e)
            Result.success(HighwayCorridorEngine.resolveSafeRoute(origin, destination))
        }
    }

    private fun parseClaudeResponse(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val contentArray = root.optJSONArray("content")
            val firstBlock = contentArray?.optJSONObject(0)
            firstBlock?.optString("text").orEmpty()
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildClaudeSystemPrompt(): String {
        return """
You plan car routes for a Hyundai i20 N Line with an active Bluelink subscription, anywhere in India.
The goal is the safest good road, not the shortest. Distance and duration do not matter. A longer divided highway is better than a shorter village road, ghat shortcut, or city cut.

Safest path order:
1. Access-controlled expressway or bypass, with median and no village crossings.
2. Divided four-lane or six-lane national highway, with service road through towns.
3. City ring road or bypass only if divided and access-controlled.
4. Undivided two-lane NH only if no divided road exists.
5. State highway only for the last join, and only if wide and paved.
6. Reject village roads, market roads, inner-city cuts, and unmarked ghat shortcuts.

Pin methodology:
- Plan the road first. Every segment must be an expressway, national highway, or divided bypass. Name the number.
- A pin is only a lock. It is a pass-through. The driver does not stop and does not get out.
- The pin must be a Google Maps place that sits on that road: a toll plaza, fuel station, or named bridge only.
- Reject a village name, area name, or locality. Reject words Junction, Exit, and Pass-through.

Link:
- Form: https://www.google.com/maps/dir/START/STOP/STOP/END/
- Plus signs between words. Nothing after the last slash. No api=1, no coordinates, no waypoints.
- Add the road to every stop.

Print strictly in this order and format:
🗺️ Route
- [filled Google Maps URL]
- Check: preview stays on the named expressway, NH, or divided bypass
- If no directions: search the destination → Directions → set the start → Add stop for each pin
- [Start] → [NH or expressway number]
- [Stop 1 name] [Road number] → pass-through, pay and continue
- [Stop 2 name] [Road number] → pass-through, pay and continue
- [Destination]
- ✅ Road numbers used: [numbers]. Name any non-NH bit in this line only.

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
        """.trimIndent()
    }

    private fun buildClaudeUserPrompt(origin: String, destination: String, notes: String): String {
        return """
Plan the safe highway trip from:
Start: $origin
Destination: $destination
${if (notes.isNotBlank()) "Additional note: $notes" else ""}
        """.trimIndent()
    }
}
