package com.example.projectwork.utils

import android.util.Log
import com.example.projectwork.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.io.IOException

/**
 * Data models for OpenAI API
 */
data class OpenAIChoice(
    val index: Int,
    val message: OpenAIMessage,
    val finish_reason: String
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val id: String,
    val choices: List<OpenAIChoice>,
    val created: Long,
    val model: String,
    val usage: OpenAIUsage
)

data class OpenAIUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// Recipe data model
data class Recipe(
    val name: String,
    val ingredients: List<String>
)

/**
 * Helper class to interact with OpenAI API
 */
object OpenAIHelper {
    private const val TAG = "OpenAIHelper"
    private const val MODEL = "gpt-3.5-turbo"
    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private const val API_KEY = BuildConfig.OPENAI_API_KEY

    // Create OkHttpClient with timeout configuration
    private val client by lazy {
        try {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create OkHttpClient: ${e.message}", e)
            null
        }
    }

    /**
     * Tests if the OpenAI API is connected and working
     * @return Pair<Boolean, String> - Boolean indicates if connection was successful, String contains status message
     */
    suspend fun testAPIConnection(): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                if (client == null) {
                    return@withContext Pair(false, "HTTP client not initialized")
                }

                // Simple query to test connection
                val testQuery = "Hello"
                val systemPrompt = "You are a helpful cooking assistant. Just respond with 'Connection successful'."
                
                // Create JSON request
                val jsonRequest = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", testQuery)
                        })
                    })
                    put("max_tokens", 10)
                    put("temperature", 0.1)
                }

                // Build HTTP request
                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                try {
                    val response = client?.newCall(request)?.execute()
                    if (response?.isSuccessful == true) {
                        Log.d(TAG, "API connection test successful")
                        return@withContext Pair(true, "Connected to OpenAI API")
                    } else {
                        val errorMessage = response?.body?.string() ?: "Unknown error"
                        Log.w(TAG, "API connection test failed: $errorMessage")
                        return@withContext Pair(false, "Failed to connect to OpenAI API: $errorMessage")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "API connection test failed: ${e.message}", e)
                    return@withContext Pair(false, "Failed to connect to OpenAI API: ${e.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "API connection test failed: ${e.message}", e)
                return@withContext Pair(false, "Failed to set up HTTP client: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Makes a request to the OpenAI API for recipe information
     * @param query The user's recipe query
     * @return OpenAIResponse with recipe information
     */
    suspend fun getRecipeInfo(query: String): OpenAIResponse {
        return withContext(Dispatchers.IO) {
            if (client == null) {
                Log.w(TAG, "HTTP client not initialized, using simulated response")
                return@withContext createSimulatedResponse(query)
            }
            
            try {
                Log.d(TAG, "Starting API request for query: $query")

                // Create JSON request for chat completion
                val jsonRequest = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are a helpful cooking assistant. Provide concise recipe details with ingredients and instructions.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", query)
                        })
                    })
                    put("max_tokens", 500)
                    put("temperature", 0.7)
                }

                // Build HTTP request
                val request = Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                // Execute request
                try {
                    val response = client?.newCall(request)?.execute()
                    
                    if (response?.isSuccessful == true) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            // Parse JSON response
                            val jsonResponse = JSONObject(responseBody)
                            val id = jsonResponse.getString("id")
                            val created = jsonResponse.getLong("created")
                            val model = jsonResponse.getString("model")
                            
                            // Parse usage
                            val jsonUsage = jsonResponse.getJSONObject("usage")
                            val usage = OpenAIUsage(
                                prompt_tokens = jsonUsage.getInt("prompt_tokens"),
                                completion_tokens = jsonUsage.getInt("completion_tokens"),
                                total_tokens = jsonUsage.getInt("total_tokens")
                            )
                            
                            // Parse choices
                            val jsonChoices = jsonResponse.getJSONArray("choices")
                            val choices = mutableListOf<OpenAIChoice>()
                            for (i in 0 until jsonChoices.length()) {
                                val choice = jsonChoices.getJSONObject(i)
                                val index = choice.getInt("index")
                                val finishReason = choice.getString("finish_reason")
                                
                                // Parse message
                                val jsonMessage = choice.getJSONObject("message")
                                val role = jsonMessage.getString("role")
                                val content = jsonMessage.getString("content")
                                
                                choices.add(
                                    OpenAIChoice(
                                        index = index,
                                        message = OpenAIMessage(role, content),
                                        finish_reason = finishReason
                                    )
                                )
                            }
                            
                            Log.d(TAG, "Successfully received API response")
                            return@withContext OpenAIResponse(
                                id = id,
                                choices = choices,
                                created = created,
                                model = model,
                                usage = usage
                            )
                        }
                    }
                    
                    // If we reached here, something went wrong
                    Log.w(TAG, "API request failed: ${response?.message}")
                    return@withContext createSimulatedResponse(query)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "API request failed: ${e.message}", e)
                    return@withContext createSimulatedResponse(query)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up API request: ${e.message}", e)
                return@withContext createSimulatedResponse(query)
            }
        }
    }

    /**
     * Create a simulated response for testing or when API is unavailable
     */
    private fun createSimulatedResponse(query: String): OpenAIResponse {
        Log.d(TAG, "Creating simulated response for query: $query")

        val lowercaseQuery = query.lowercase()

        val content = when {
            lowercaseQuery.contains("pasta") || lowercaseQuery.contains("spaghetti") -> """
                # Spaghetti with Tomato Sauce

                A simple Italian classic that everyone loves.

                ## Ingredients
                - 1 pound (450g) spaghetti
                - 2 tablespoons olive oil
                - 1 onion, finely chopped
                - 3 cloves garlic, minced
                - 28 oz (800g) canned crushed tomatoes
                - 1 teaspoon dried oregano
                - 1 teaspoon dried basil
                - Salt and pepper to taste
                - Grated Parmesan cheese for serving
                - Fresh basil leaves (optional)

                ## Instructions
                1. Bring a large pot of salted water to a boil and cook spaghetti according to package directions.
                2. Meanwhile, heat olive oil in a large saucepan over medium heat.
                3. Add onion and cook until softened, about 5 minutes.
                4. Add garlic and cook for 30 seconds until fragrant.
                5. Add crushed tomatoes, oregano, basil, salt, and pepper.
                6. Simmer for 15-20 minutes, stirring occasionally.
                7. Drain pasta and serve topped with sauce, grated Parmesan, and fresh basil if desired.
            """.trimIndent()

            lowercaseQuery.contains("pizza") -> """
                # Homemade Pizza

                Make delicious pizza at home with this simple recipe.

                ## Ingredients
                - 2¼ teaspoons active dry yeast
                - 1 teaspoon sugar
                - 1 cup warm water
                - 3 cups all-purpose flour
                - 2 tablespoons olive oil
                - 1 teaspoon salt
                - 1 cup pizza sauce
                - 2 cups shredded mozzarella cheese
                - Toppings of your choice (pepperoni, mushrooms, bell peppers, etc.)

                ## Instructions
                1. In a large bowl, dissolve yeast and sugar in warm water. Let sit for 10 minutes.
                2. Stir in flour, salt, and olive oil. Mix until a soft dough forms.
                3. Knead on a floured surface for 5-7 minutes until smooth and elastic.
                4. Place in a greased bowl, cover, and let rise for 1 hour.
                5. Preheat oven to 450°F (230°C).
                6. Punch down dough and roll out on a floured surface.
                7. Transfer to a pizza pan or baking sheet.
                8. Spread sauce, sprinkle cheese, and add toppings.
                9. Bake for 12-15 minutes until crust is golden and cheese is bubbly.
            """.trimIndent()

            lowercaseQuery.contains("chicken") -> """
                # Simple Baked Chicken

                A versatile and easy chicken recipe that's perfect for weeknight dinners.

                ## Ingredients
                - 4 chicken breasts
                - 2 tablespoons olive oil
                - 2 teaspoons paprika
                - 1 teaspoon garlic powder
                - 1 teaspoon dried oregano
                - 1 teaspoon salt
                - ½ teaspoon black pepper
                - 1 lemon, sliced (optional)

                ## Instructions
                1. Preheat oven to 375°F (190°C).
                2. In a small bowl, mix paprika, garlic powder, oregano, salt, and pepper.
                3. Brush chicken with olive oil and sprinkle with spice mixture.
                4. Place chicken in a baking dish. Add lemon slices if desired.
                5. Bake for 25-30 minutes until internal temperature reaches 165°F (74°C).
                6. Let rest for 5 minutes before serving.
            """.trimIndent()

            else -> """
                I can help with many recipes! Here are some popular options:

                ## Popular Recipes
                - Pasta dishes (spaghetti, carbonara, lasagna)
                - Pizza (homemade dough and various toppings)
                - Chicken recipes (baked, grilled, or in various sauces)
                - Soups and stews
                - Desserts like chocolate cake or apple pie

                Just ask for a specific recipe, and I'll provide detailed ingredients and instructions!
            """.trimIndent()
        }

        // Create a simulated response
        val timestamp = System.currentTimeMillis()
        return OpenAIResponse(
            id = "simulated-${timestamp}",
            choices = listOf(
                OpenAIChoice(
                    index = 0,
                    message = OpenAIMessage("assistant", content),
                    finish_reason = "stop"
                )
            ),
            created = timestamp / 1000,
            model = "simulated-model",
            usage = OpenAIUsage(
                prompt_tokens = query.length + 50,
                completion_tokens = content.length,
                total_tokens = query.length + content.length + 50
            )
        )
    }

    /**
     * Extract ingredient list from response
     * @param response The full response from AI
     * @return List of ingredients or null if not found
     */
    fun extractIngredients(response: String): List<String>? {
        try {
            // Look for ingredients section in the response
            val ingredientSection = response.split("##").find { section ->
                section.trim().lowercase().startsWith("ingredients")
            }

            // Extract bullet points if ingredient section found
            return ingredientSection?.lines()
                ?.filter { line -> line.trim().startsWith("-") }
                ?.map { line -> line.substringAfter("-").trim() }
                ?.filter { it.isNotEmpty() }

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting ingredients: ${e.message}", e)
            return null
        }
    }
}