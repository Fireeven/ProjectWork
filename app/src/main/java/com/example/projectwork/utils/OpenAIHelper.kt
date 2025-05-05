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
    suspend fun getRecipeFromOpenAI(query: String): OpenAIResponse {
        return withContext(Dispatchers.IO) {
            if (client == null) {
                Log.w(TAG, "HTTP client not initialized, using simulated response")
                return@withContext createSimulatedRecipeResponse(query)
            }
            
            try {
                Log.d(TAG, "Starting API request for recipe query: $query")

                // Create JSON request for chat completion
                val jsonRequest = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", """
                                You are a helpful cooking assistant. 
                                Provide a detailed recipe based on the user's query. 
                                Format your response with clear sections: 
                                
                                # Recipe Title
                                
                                ## Ingredients
                                - List ingredients with quantities and units
                                - One ingredient per line
                                
                                ## Instructions
                                1. First step
                                2. Second step
                                
                                Be precise with ingredients and steps.
                            """.trimIndent())
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Give me a recipe for $query")
                        })
                    })
                    put("max_tokens", 1000)
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
                            return@withContext parseOpenAIResponse(responseBody)
                        }
                    }
                    
                    // If we reached here, something went wrong
                    Log.w(TAG, "API request failed: ${response?.message}")
                    return@withContext createSimulatedRecipeResponse(query)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "API request failed: ${e.message}", e)
                    return@withContext createSimulatedRecipeResponse(query)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up API request: ${e.message}", e)
                return@withContext createSimulatedRecipeResponse(query)
            }
        }
    }

    /**
     * Create a simulated recipe response for testing or when API is unavailable
     */
    private fun createSimulatedRecipeResponse(query: String): OpenAIResponse {
        Log.d(TAG, "Creating simulated recipe response for query: $query")

        val lowercaseQuery = query.lowercase()

        val content = when {
            lowercaseQuery.contains("pasta") || lowercaseQuery.contains("spaghetti") -> """
                # Spaghetti with Tomato Sauce

                ## Ingredients
                - 1 pound spaghetti
                - 2 tablespoons olive oil
                - 1 onion, finely chopped
                - 3 cloves garlic, minced
                - 28 oz canned crushed tomatoes
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
                # Lemon Garlic Roasted Chicken

                ## Ingredients
                - 1 whole chicken (about 4-5 pounds)
                - 3 tablespoons olive oil
                - 4 cloves garlic, minced
                - 2 lemons
                - 1 tablespoon fresh rosemary, chopped
                - 1 tablespoon fresh thyme, chopped
                - 1 teaspoon salt
                - 1/2 teaspoon black pepper
                - 1 onion, quartered
                - 2 carrots, chopped

                ## Instructions
                1. Preheat oven to 425°F (220°C).
                2. Pat chicken dry with paper towels.
                3. In a small bowl, mix olive oil, garlic, zest from 1 lemon, rosemary, thyme, salt, and pepper.
                4. Cut one lemon into quarters and place inside the chicken cavity along with the onion.
                5. Rub the olive oil mixture all over the chicken.
                6. Place the chicken in a roasting pan and scatter chopped carrots around it.
                7. Roast for 1 hour and 20 minutes, or until juices run clear.
                8. Let rest for 10-15 minutes before carving.
                9. Squeeze juice from the remaining lemon over the chicken before serving.
            """.trimIndent()

            lowercaseQuery.contains("cake") || lowercaseQuery.contains("dessert") -> """
                # Chocolate Cake

                ## Ingredients
                - 2 cups all-purpose flour
                - 2 cups sugar
                - 3/4 cup unsweetened cocoa powder
                - 2 teaspoons baking soda
                - 1 teaspoon baking powder
                - 1 teaspoon salt
                - 2 eggs
                - 1 cup buttermilk
                - 1/2 cup vegetable oil
                - 2 teaspoons vanilla extract
                - 1 cup hot coffee

                ## Instructions
                1. Preheat oven to 350°F (175°C).
                2. Grease and flour two 9-inch round cake pans.
                3. In a large bowl, combine flour, sugar, cocoa, baking soda, baking powder, and salt.
                4. Add eggs, buttermilk, oil, and vanilla; beat for 2 minutes.
                5. Stir in hot coffee (batter will be thin).
                6. Pour batter into prepared pans.
                7. Bake for 30-35 minutes, or until a toothpick inserted comes out clean.
                8. Cool in pans for 10 minutes, then remove to wire racks to cool completely.
                9. Frost with your favorite chocolate frosting.
            """.trimIndent()

            else -> """
                # Simple Vegetable Stir Fry

                ## Ingredients
                - 2 tablespoons vegetable oil
                - 1 onion, sliced
                - 2 bell peppers, sliced
                - 2 carrots, julienned
                - 1 broccoli head, cut into florets
                - 2 cloves garlic, minced
                - 1 tablespoon ginger, grated
                - 1/4 cup soy sauce
                - 1 tablespoon honey
                - 1 tablespoon cornstarch
                - 2 tablespoons water
                - Sesame seeds for garnish

                ## Instructions
                1. Heat oil in a large wok or frying pan over high heat.
                2. Add onion and stir-fry for 1 minute.
                3. Add bell peppers, carrots, and broccoli. Stir-fry for 3-4 minutes.
                4. Add garlic and ginger, stir-fry for 30 seconds until fragrant.
                5. In a small bowl, mix soy sauce, honey, cornstarch, and water.
                6. Pour sauce over vegetables and stir to coat.
                7. Cook for 2-3 minutes until sauce thickens and vegetables are tender-crisp.
                8. Garnish with sesame seeds and serve over rice or noodles.
            """.trimIndent()
        }

        return createSimulatedResponseWithContent(content)
    }

    /**
     * Helper function to create a simulated response with given content
     */
    private fun createSimulatedResponseWithContent(content: String): OpenAIResponse {
        return OpenAIResponse(
            id = "simulated-id-${System.currentTimeMillis()}",
            choices = listOf(
                OpenAIChoice(
                    index = 0,
                    message = OpenAIMessage(
                        role = "assistant",
                        content = content
                    ),
                    finish_reason = "stop"
                )
            ),
            created = System.currentTimeMillis() / 1000,
            model = MODEL,
            usage = OpenAIUsage(
                prompt_tokens = 100,
                completion_tokens = content.length / 4,
                total_tokens = 100 + content.length / 4
            )
        )
    }
    
    /**
     * Parse OpenAI API response JSON
     */
    private fun parseOpenAIResponse(responseBody: String): OpenAIResponse {
        try {
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
            
            Log.d(TAG, "Successfully parsed API response")
            return OpenAIResponse(
                id = id,
                choices = choices,
                created = created,
                model = model,
                usage = usage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API response: ${e.message}", e)
            throw e
        }
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