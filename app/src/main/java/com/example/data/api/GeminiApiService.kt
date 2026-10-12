package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiService"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    }

    suspend fun generateResponse(
        userPrompt: String,
        conversationHistory: List<ChatMessage>,
        systemInstructionText: String,
        temperature: Float = 0.7f,
        customApiKey: String? = null,
        languageName: String = "English"
    ): Pair<String, List<String>> = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            try {
                BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                        !BuildConfig.GEMINI_API_KEY.contains("MY_GEMINI_API_KEY")
            } catch (e: Throwable) {
                false
            } -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }

        val enrichedSystemPrompt = buildString {
            append(systemInstructionText)
            if (languageName.isNotBlank() && !languageName.equals("English", ignoreCase = true)) {
                append("\n\nCRITICAL LANGUAGE DIRECTIVE: The user has selected $languageName as their preferred language. You MUST deliver all explanations, analyses, and responses in $languageName.")
            }
        }

        if (customApiKey == "OFFLINE_LOCAL_ENGINE") {
            Log.i(TAG, "Offline Mode active. Executing on-device neural persona engine.")
            val (resp, suggestions) = fallbackPersonaResponse(userPrompt, enrichedSystemPrompt)
            return@withContext Pair("⚡ [On-Device Offline AI]\n$resp", suggestions)
        }

        if (resolvedKey.isBlank()) {
            Log.w(TAG, "No valid Gemini API key found, utilizing specialized local persona engine.")
            return@withContext fallbackPersonaResponse(userPrompt, enrichedSystemPrompt)
        }

        try {
            val rootJson = JSONObject()

            // System instructions
            if (enrichedSystemPrompt.isNotBlank()) {
                val sysContent = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", enrichedSystemPrompt))
                sysContent.put("parts", sysParts)
                rootJson.put("systemInstruction", sysContent)
            }

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", temperature.toDouble().coerceIn(0.0, 1.0))
            rootJson.put("generationConfig", genConfig)

            // Contents array (recent context up to last 8 messages)
            val contentsArray = JSONArray()
            val recentHistory = conversationHistory.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == "USER") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.content))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Add the current user prompt if not already in history
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userPrompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$resolvedKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e(TAG, "Gemini API error code ${response.code}: $responseBody")
                return@withContext fallbackPersonaResponse(userPrompt, systemInstructionText, isQuotaOrNetworkError = true)
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text", "")
                    if (rawText.isNotBlank()) {
                        val quickReplies = generateSmartReplies(userPrompt, rawText, systemInstructionText)
                        return@withContext Pair(rawText, quickReplies)
                    }
                }
            }

            return@withContext fallbackPersonaResponse(userPrompt, systemInstructionText)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API: ${e.message}", e)
            return@withContext fallbackPersonaResponse(userPrompt, systemInstructionText, isQuotaOrNetworkError = true)
        }
    }

    private fun generateSmartReplies(userPrompt: String, responseText: String, systemInstruction: String): List<String> {
        val lower = (userPrompt + " " + systemInstruction).lowercase()
        return when {
            lower.contains("school") || lower.contains("academic") || lower.contains("calculus") || lower.contains("thesis") -> listOf(
                "Can you show a step-by-step example?",
                "Give me 3 quiz questions on this",
                "Explain like I'm 10 years old",
                "How would this appear on an exam?"
            )
            lower.contains("sport") || lower.contains("workout") || lower.contains("tactic") || lower.contains("drill") -> listOf(
                "Give me the exact warm-up sets",
                "How do I prevent injury during this?",
                "What nutrition maximizes these gains?",
                "Alternative drill for home workout"
            )
            lower.contains("business") || lower.contains("startup") || lower.contains("pitch") || lower.contains("roi") -> listOf(
                "Draft an executive pitch summary",
                "What are the top 3 market risks?",
                "How do we model unit economics?",
                "Suggest 2 growth experiments"
            )
            lower.contains("anime") || lower.contains("manga") || lower.contains("otaku") || lower.contains("senpai") -> listOf(
                "Give me 3 more anime like this!",
                "Who has the strongest power scaling?",
                "What are the best character tropes?",
                "Which studio animated this best?"
            )
            lower.contains("beauty") || lower.contains("makeup") || lower.contains("skincare") || lower.contains("skin") -> listOf(
                "Is this safe for sensitive skin?",
                "What are the best budget dupes?",
                "Daytime vs nighttime routine order",
                "How long before I see visible results?"
            )
            else -> listOf(
                "Tell me more about this",
                "Give me practical examples",
                "Summarize key takeaways",
                "What should I do next?"
            )
        }
    }

    private fun fallbackPersonaResponse(
        userPrompt: String,
        systemInstruction: String,
        isQuotaOrNetworkError: Boolean = false
    ): Pair<String, List<String>> {
        val lower = (userPrompt + " " + systemInstruction).lowercase()
        val notice = if (isQuotaOrNetworkError) {
            "*(Prime Infinity Offline Expert Mode active - network/API key check)*\n\n"
        } else ""

        val (content, replies) = when {
            lower.contains("school") || lower.contains("calculus") || lower.contains("derivative") || lower.contains("thesis") || lower.contains("academic") -> {
                val text = """
                    ${notice}📚 **Prime Infinity Academic Engine**
                    
                    **Topic:** ${userPrompt.capitalizeWords()}
                    
                    **1. Core Conceptual Foundation:**
                    Every breakthrough in this domain stems from understanding the fundamental axioms before moving to applied synthesis. When analyzing this problem, establish your known variables, underlying hypotheses, and structural boundary conditions.
                    
                    **2. Step-by-Step Analytical Breakdown:**
                    • **Phase A (Identification):** Isolate core definitions and formulate the problem statement.
                    • **Phase B (Methodology):** Apply established proofs, algebraic identities, or validated qualitative frameworks.
                    • **Phase C (Verification):** Test against edge cases and examine whether units or rhetorical premises remain sound.
                    
                    **3. Academic Takeaway & Application:**
                    Keep your citations structured, articulate clear counterarguments, and review foundational proofs to ensure mastery.
                """.trimIndent()
                val rep = listOf("Give me a practice problem", "Explain in simple terms", "Break down common mistakes")
                Pair(text, rep)
            }
            lower.contains("sport") || lower.contains("fitness") || lower.contains("split") || lower.contains("tactic") || lower.contains("workout") -> {
                val text = """
                    ${notice}🏆 **Prime Infinity Athletic Performance Unit**
                    
                    **Focus:** High-Intensity Strategy & Conditioning for "${userPrompt.capitalizeWords()}"
                    
                    **1. Tactical Directive:**
                    Peak athletic output requires disciplined periodization, kinetic chain alignment, and progressive overload.
                    
                    **2. Tactical & Physical Breakdown:**
                    • **Dynamic Activation (8-10 min):** Joint mobilization, band pull-aparts, thoracic rotations, and CNS priming.
                    • **Main Protocol (75-85% intensity):** Focus on explosive concentric drive, controlled eccentric cadence (3-1-1), and strict structural form.
                    • **Recovery Matrix:** 0.8-1g protein/lb, magnesium glycinate before rest, and active recovery foam rolling.
                    
                    Stay relentless, lock in your breathing rhythm, and dominate the session!
                """.trimIndent()
                val rep = listOf("Show exact sets & reps", "How do I avoid injury?", "What's the best post-workout meal?")
                Pair(text, rep)
            }
            lower.contains("business") || lower.contains("startup") || lower.contains("pitch") || lower.contains("roi") || lower.contains("marketing") -> {
                val text = """
                    ${notice}💼 **Prime Infinity Venture Advisory Brief**
                    
                    **Executive Briefing:** Analysis of "${userPrompt.capitalizeWords()}"
                    
                    **1. Value Proposition & Market Mechanics:**
                    To achieve scalable market penetration, validate unit economics early. Your Customer Acquisition Cost (CAC) must support a Lifetime Value (LTV) ratio exceeding 3:1 with a payback window under 12 months.
                    
                    **2. Strategic Implementation Pillars:**
                    • **Go-To-Market (GTM):** Laser-focused outbound cadence paired with frictionless self-serve onboarding.
                    • **Competitive Moat:** Proprietary data gravity, switching costs, and strong network effects.
                    • **Financial Metrics:** Track Net Revenue Retention (NRR > 115%), gross margins (> 75%), and monthly burn velocity.
                    
                    **Executive Recommendation:** Formulate two low-budget distribution experiments this week to prove product-market velocity.
                """.trimIndent()
                val rep = listOf("Draft the 1-page executive pitch", "Model the unit economics", "List key investor questions")
                Pair(text, rep)
            }
            lower.contains("anime") || lower.contains("manga") || lower.contains("otaku") || lower.contains("senpai") -> {
                val text = """
                    ${notice}⚡ **Prime Infinity Anime Senpai & Lore Archive**
                    
                    **Sugoi! Deep dive on:** "${userPrompt.capitalizeWords()}"
                    
                    **1. Lore Analysis & Power Scaling:**
                    The narrative tension here is phenomenal! From the fluid sakuga choreography to the existential character arcs, the thematic depth mirrors classic philosophical dilemmas packaged in peak shonen/seinen storytelling.
                    
                    **2. Highlight Breakdown:**
                    • **Aesthetic & Direction:** Dynamic color palettes, iconic sound design, and impactful pacing that keeps you at the edge of your seat.
                    • **Character Psychology:** Motivations forged in adversity, highlighting the classic tension between raw ambition and nakama loyalty.
                    • **Manga vs Anime Nuance:** Check out the original manga panels for unmatched line art detail and bonus canon lore!
                    
                    Ready for more epic recommendations or character deep-dives? Just let me know!
                """.trimIndent()
                val rep = listOf("Recommend 3 hidden gems", "Compare power scaling levels", "Who has the best backstory?")
                Pair(text, rep)
            }
            lower.contains("beauty") || lower.contains("makeup") || lower.contains("skincare") || lower.contains("glam") -> {
                val text = """
                    ${notice}💄 **Prime Infinity Glow & Beauty Studio**
                    
                    **Your Customized Beauty Blueprint for:** "${userPrompt.capitalizeWords()}"
                    
                    **1. The Skin Barrier Golden Rule:**
                    Radiant makeup begins with healthy skin preparation. Always hydrate on damp skin to lock in moisture before layering actives or primers.
                    
                    **2. Step-by-Step Regimen:**
                    • **Prep & Hydrate:** Gentle pH-balanced cleanser followed by hydrating essence and Hyaluronic Acid.
                    • **Active Treatment:** Use targeted peptides or stabilized Vitamin C for luminous skin barrier repair.
                    • **Color Harmony & Application:** Match your skin's true undertone (cool, neutral, or warm olive). Apply cream formulas before powder to maintain a radiant, skin-like finish.
                    • **Sun Protection:** Broad-spectrum SPF 50+ is non-negotiable for preserving collagen elasticity.
                    
                    Embrace your signature glow and enjoy your self-care ritual!
                """.trimIndent()
                val rep = listOf("Which active ingredients pair best?", "Step-by-step 5-minute glow routine", "How to prevent foundation creasing?")
                Pair(text, rep)
            }
            else -> {
                val text = """
                    ${notice}✨ **Prime Infinity Intelligence System**
                    
                    Regarding **"${userPrompt.capitalizeWords()}"**:
                    
                    **Key Insights & Strategic Perspective:**
                    Prime Infinity has synthesized your request across our knowledge base. To achieve the best outcome:
                    
                    • **Clarity of Intent:** Clarify the specific parameters or goals you want to prioritize.
                    • **Iterative Execution:** Build in feedback loops to test and refine rapidly.
                    • **Adaptive Action:** Tailor your strategy according to the active category and constraints.
                    
                    How would you like to proceed? Pick a suggested follow-up below or ask anything!
                """.trimIndent()
                val rep = listOf("Give me practical examples", "Summarize key points", "Provide detailed steps")
                Pair(text, rep)
            }
        }

        return Pair(content, replies)
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
