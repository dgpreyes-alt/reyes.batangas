package com.example.core.data

import android.util.Log
import com.example.core.model.ItineraryTimeSlots
import com.example.core.model.TripItineraryItem
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class GeneratedItineraryPlan(
    val tripSummary: String,
    val stops: List<TripItineraryItem>
)

class BatangasAiRepository {
    private val conciergeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are BatangasConnect AI, a warm, knowledgeable smart tourism guide specializing exclusively in San Juan, Batangas, Philippines.
                    Always ground advice in authentic San Juan, Batangas locations and culture:
                    - Beaches & Marine: Laiya (Brgy. Laiya Aplaya & Laiya Ibabao), Hugom Marine Sanctuary, Biga/Imelda coves.
                    - Eco & Mountains: Mount Daguldol (register at Brgy. Hugom hall with local guides), Subukin Mangrove Boardwalk.
                    - Heritage & Crafts: San Juan Nepomuceno Parish Church & Poblacion ancestral houses, Brgy. Pinagbayanan traditional red-clay palayok pottery.
                    - Food & Culture: Kapeng Barako (Liberica), artisanal coconut lambanog, authentic thick-noodle Batangas lomi, suman sa lihiya, and San Juan Public Market pasalubong.
                    - Practical logistics: Routes via Lipa City or Batangas City, ALPS/JAC/DLTB buses + San Juan jeepneys/tricycles, eco-fees, and responsible coastal tourism etiquette.
                    Keep answers concise, practical, structured with clear bullet points, and under 220 words unless asked for more detail.
                    """.trimIndent()
                )
            }
        )
    }

    private val itinerarySchema = Schema.obj(
        properties = mapOf(
            "tripSummary" to Schema.string(
                description = "Short, inspiring 1-2 sentence overview of the San Juan, Batangas itinerary"
            ),
            "stops" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Specific attraction or activity in San Juan, Batangas"),
                        "location" to Schema.string(description = "Barangay or specific landmark name in San Juan, Batangas"),
                        "dayNumber" to Schema.integer(description = "Day number starting at 1"),
                        "timeSlot" to Schema.enumeration(
                            values = ItineraryTimeSlots.all,
                            description = "Time of day slot"
                        ),
                        "notes" to Schema.string(description = "Practical local tip, budget note, or activity detail")
                    )
                ),
                description = "Ordered list of itinerary stops in San Juan, Batangas"
            )
        )
    )

    private val itineraryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = itinerarySchema
            }
        )
    }

    fun askConciergeStream(question: String): Flow<String> = flow {
        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                conciergeModel.generateContentStream(question).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI guide returned an empty response. Please try again.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI concierge stream failed", e)
            throw e
        }
    }

    suspend fun generateSanJuanItinerary(
        days: Int,
        travelerStyle: String,
        groupContext: String
    ): GeneratedItineraryPlan {
        val prompt = """
            Create a realistic $days-day tourism itinerary specifically in San Juan, Batangas, Philippines.
            Traveler style: $travelerStyle
            Group & preferences: $groupContext
            Include 3 to ${days.coerceIn(1, 3) * 3} authentic stops across real San Juan barangays such as Laiya Aplaya, Hugom (Mt. Daguldol / Coral Cove), Subukin Mangrove, Poblacion (Nepomuceno Parish / Lomi eateries), Pinagbayanan Pottery Village, or Buhay na Sapa (Barako & Lambanog).
            Keep titles under 80 characters, locations under 80 characters, and notes practical and under 160 characters.
        """.trimIndent()

        val response = withTimeout(60_000L) {
            itineraryModel.generateContent(prompt)
        }
        val jsonText = response.text
            ?: throw IllegalStateException("Gemini returned an empty itinerary response.")

        val root = JSONObject(jsonText)
        val summary = root.optString(
            "tripSummary",
            "Custom San Juan, Batangas itinerary tailored for $travelerStyle."
        )
        val stopsArray = root.getJSONArray("stops")
        val stops = (0 until stopsArray.length()).map { index ->
            val obj = stopsArray.getJSONObject(index)
            val rawSlot = obj.optString("timeSlot", "Morning")
            val validSlot = if (rawSlot in ItineraryTimeSlots.all) rawSlot else "Morning"
            TripItineraryItem(
                title = obj.getString("title").take(120),
                location = obj.getString("location").take(120),
                dayNumber = obj.optInt("dayNumber", 1).coerceIn(1, 14),
                timeSlot = validSlot,
                notes = obj.optString("notes", "").take(400),
                visited = false,
                source = "ai_planner"
            )
        }
        return GeneratedItineraryPlan(tripSummary = summary, stops = stops)
    }

    private companion object {
        const val TAG = "BatangasAiRepository"
    }
}
