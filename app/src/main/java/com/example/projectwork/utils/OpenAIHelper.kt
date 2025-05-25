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

// Enhanced Recipe data model
data class SimpleRecipe(
    val name: String,
    val ingredients: List<String>
)

// Chat message data model
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Enhanced chatbot response
data class ChatbotResponse(
    val message: String,
    val suggestedIngredients: List<String> = emptyList(),
    val suggestedRecipes: List<SimpleRecipe> = emptyList(),
    val actionType: ChatActionType = ChatActionType.GENERAL_RESPONSE
)

enum class ChatActionType {
    GENERAL_RESPONSE,
    INGREDIENT_SUGGESTION,
    RECIPE_SUGGESTION,
    SHOPPING_LIST_CREATION,
    PRICE_ESTIMATION,
    NUTRITION_INFO
}

/**
 * Helper class to interact with OpenAI API
 */
object OpenAIHelper {
    private const val TAG = "OpenAIHelper"
    private const val MODEL = "gpt-3.5-turbo"
    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private const val API_KEY = BuildConfig.OPENAI_API_KEY

    // Create OkHttpClient with timeout configuration
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Tests if the OpenAI API is connected and working
     * @return Pair<Boolean, String> - Boolean indicates if connection was successful, String contains status message
     */
    suspend fun testAPIConnection(): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
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
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Log.d(TAG, "API connection test successful")
                        return@withContext Pair(true, "Connected to OpenAI API")
                    } else {
                        val errorMessage = response.body?.string() ?: "Unknown error"
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
     * Enhanced chatbot that can handle various grocery and recipe-related queries
     */
    suspend fun getChatbotResponse(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        existingIngredients: List<String> = emptyList()
    ): Result<ChatbotResponse> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = createEnhancedSystemPrompt(existingIngredients)
            val messages = buildConversationMessages(systemPrompt, conversationHistory, userMessage)
            
            val requestBody = JSONObject().apply {
                put("model", MODEL)
                put("messages", messages)
                put("max_tokens", 500)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val messageContent = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        
                        val chatbotResponse = parseEnhancedResponse(messageContent, userMessage)
                        Result.success(chatbotResponse)
                    } else {
                        Result.failure(Exception("No response from AI"))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "API Error: ${response.code} - $errorBody")
                Result.failure(Exception("API Error: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chatbot response: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get recipe suggestions based on available ingredients
     */
    suspend fun getRecipeFromIngredients(ingredients: List<String>): Result<List<SimpleRecipe>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Based on these ingredients: ${ingredients.joinToString(", ")}, 
                suggest 3 simple recipes I can make. For each recipe, provide:
                1. Recipe name
                2. Complete ingredient list (including what I might need to buy)
                
                Format your response as JSON:
                {
                  "recipes": [
                    {
                      "name": "Recipe Name",
                      "ingredients": ["ingredient1", "ingredient2", ...]
                    }
                  ]
                }
            """.trimIndent()

            val result = getRecipeInfo(prompt)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    val recipes = parseRecipeListFromResponse(response)
                    Result.success(recipes)
                } else {
                    Result.failure(Exception("No recipe data received"))
                }
            } else {
                result.map { emptyList<SimpleRecipe>() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recipes from ingredients: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get ingredient suggestions for a specific cuisine or dietary preference
     */
    suspend fun getIngredientSuggestions(
        cuisine: String = "",
        dietaryRestrictions: String = "",
        mealType: String = ""
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Suggest 10-15 essential ingredients for ")
                if (cuisine.isNotEmpty()) append("$cuisine cuisine ")
                if (mealType.isNotEmpty()) append("$mealType meals ")
                if (dietaryRestrictions.isNotEmpty()) append("with $dietaryRestrictions dietary restrictions ")
                append(". Focus on versatile ingredients that can be used in multiple dishes.")
                append(" Return only a comma-separated list of ingredients.")
            }

            val result = getRecipeInfo(prompt)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    val ingredients = response.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    Result.success(ingredients)
                } else {
                    Result.failure(Exception("No ingredient suggestions received"))
                }
            } else {
                result.map { emptyList<String>() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ingredient suggestions: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get nutritional information for ingredients
     */
    suspend fun getNutritionalInfo(ingredients: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Provide brief nutritional information for these ingredients: ${ingredients.joinToString(", ")}.
                Include key nutrients, health benefits, and any dietary considerations.
                Keep it concise and practical for grocery shopping decisions.
            """.trimIndent()

            getRecipeInfo(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting nutritional info: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Estimate prices for ingredients (mock implementation - in real app, integrate with price APIs)
     */
    suspend fun estimateIngredientPrices(
        ingredients: List<String>,
        location: String = "US"
    ): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            // Mock price estimation - in a real app, you'd integrate with grocery APIs
            val priceEstimates = ingredients.associateWith { ingredient ->
                when {
                    ingredient.contains("meat", ignoreCase = true) -> kotlin.random.Random.nextDouble(5.0, 15.0)
                    ingredient.contains("vegetable", ignoreCase = true) -> kotlin.random.Random.nextDouble(1.0, 4.0)
                    ingredient.contains("fruit", ignoreCase = true) -> kotlin.random.Random.nextDouble(2.0, 6.0)
                    ingredient.contains("dairy", ignoreCase = true) -> kotlin.random.Random.nextDouble(2.0, 8.0)
                    ingredient.contains("grain", ignoreCase = true) -> kotlin.random.Random.nextDouble(1.0, 5.0)
                    else -> kotlin.random.Random.nextDouble(1.0, 10.0)
                }
            }
            Result.success(priceEstimates)
        } catch (e: Exception) {
            Log.e(TAG, "Error estimating prices: ${e.message}", e)
            Result.failure(e)
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
                    val response = client.newCall(request).execute()
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            // Parse JSON response
                            return@withContext parseOpenAIResponse(responseBody)
                        }
                    }
                    
                    // If we reached here, something went wrong
                    Log.w(TAG, "API request failed: ${response.message}")
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
     * Alias for getRecipeFromOpenAI for backward compatibility
     */
    suspend fun getRecipeInfo(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("model", MODEL)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 500)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        Result.success(content)
                    } else {
                        Result.failure(Exception("No response from AI"))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "API Error: ${response.code} - $errorBody")
                Result.failure(Exception("API Error: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenAI API: ${e.message}", e)
            Result.failure(e)
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
     * Extract ingredients from recipe text
     */
    fun extractIngredients(recipeText: String): List<String>? {
        try {
            // Try to extract the ingredients section
            val ingredientsRegex = """(?i)(?:##?\s*ingredients|\bingredients\s*:)[\s\S]*?(?=##?\s*|\b\w+\s*:|\Z)""".toRegex()
            val match = ingredientsRegex.find(recipeText)
            
            if (match != null) {
                val ingredientsSection = match.value
                
                // Extract individual ingredients (looking for bullet points or numbered list items)
                val ingredientItemsRegex = """(?:^|\n)\s*(?:[-*•]|\d+\.)\s*(.+)""".toRegex()
                val ingredients = ingredientItemsRegex.findAll(ingredientsSection)
                    .map { it.groupValues[1].trim() }
                    .filter { it.isNotEmpty() }
                    .toList()
                
                return ingredients
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting ingredients: ${e.message}", e)
            return null
        }
    }

    // Helper methods
    private fun createEnhancedSystemPrompt(existingIngredients: List<String>): String {
        return """
            You are a helpful grocery and recipe assistant. You can help users with:
            1. Recipe suggestions based on ingredients they have
            2. Ingredient recommendations for specific cuisines or dietary needs
            3. Creating shopping lists from recipes
            4. Nutritional information about ingredients
            5. General cooking and grocery shopping advice
            
            Current ingredients the user has: ${existingIngredients.joinToString(", ")}
            
            Always be practical, concise, and helpful. When suggesting recipes, consider what ingredients 
            the user already has. When recommending ingredients, focus on versatile items that can be 
            used in multiple dishes.
        """.trimIndent()
    }

    private fun buildConversationMessages(
        systemPrompt: String,
        history: List<ChatMessage>,
        currentMessage: String
    ): JSONArray {
        return JSONArray().apply {
            // Add system message
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            
            // Add conversation history (last 5 messages to stay within token limits)
            history.takeLast(5).forEach { message ->
                put(JSONObject().apply {
                    put("role", message.role)
                    put("content", message.content)
                })
            }
            
            // Add current user message
            put(JSONObject().apply {
                put("role", "user")
                put("content", currentMessage)
            })
        }
    }

    private fun parseEnhancedResponse(content: String, userMessage: String): ChatbotResponse {
        // Determine action type based on user message and response content
        val actionType = when {
            userMessage.contains("recipe", ignoreCase = true) -> ChatActionType.RECIPE_SUGGESTION
            userMessage.contains("ingredient", ignoreCase = true) -> ChatActionType.INGREDIENT_SUGGESTION
            userMessage.contains("nutrition", ignoreCase = true) -> ChatActionType.NUTRITION_INFO
            userMessage.contains("price", ignoreCase = true) -> ChatActionType.PRICE_ESTIMATION
            userMessage.contains("shopping", ignoreCase = true) -> ChatActionType.SHOPPING_LIST_CREATION
            else -> ChatActionType.GENERAL_RESPONSE
        }

        // Extract ingredients and recipes from response if present
        val ingredients = extractIngredientsFromText(content)
        val recipes = extractRecipesFromText(content)

        return ChatbotResponse(
            message = content,
            suggestedIngredients = ingredients,
            suggestedRecipes = recipes,
            actionType = actionType
        )
    }

    private fun extractIngredientsFromText(text: String): List<String> {
        // Simple extraction - look for common ingredient patterns
        val ingredientPattern = Regex("""(?:ingredients?|need|buy|get):\s*([^\n]+)""", RegexOption.IGNORE_CASE)
        val matches = ingredientPattern.findAll(text)
        
        return matches.flatMap { match ->
            match.groupValues[1].split(",", ";", "and")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it.length > 2 }
        }.toList()
    }

    private fun extractRecipesFromText(text: String): List<SimpleRecipe> {
        // Simple extraction - look for recipe patterns
        val recipes = mutableListOf<SimpleRecipe>()
        val lines = text.split("\n")
        
        var currentRecipe: String? = null
        val currentIngredients = mutableListOf<String>()
        
        for (line in lines) {
            when {
                line.contains("recipe", ignoreCase = true) && line.contains(":") -> {
                    // Save previous recipe if exists
                    if (currentRecipe != null && currentIngredients.isNotEmpty()) {
                        recipes.add(SimpleRecipe(currentRecipe, currentIngredients.toList()))
                        currentIngredients.clear()
                    }
                    currentRecipe = line.substringAfter(":").trim()
                }
                line.trim().startsWith("-") || line.trim().startsWith("•") -> {
                    currentIngredients.add(line.trim().removePrefix("-").removePrefix("•").trim())
                }
            }
        }
        
        // Add last recipe
        if (currentRecipe != null && currentIngredients.isNotEmpty()) {
            recipes.add(SimpleRecipe(currentRecipe, currentIngredients.toList()))
        }
        
        return recipes
    }

    private fun parseRecipeListFromResponse(response: String): List<SimpleRecipe> {
        return try {
            val jsonResponse = JSONObject(response)
            val recipesArray = jsonResponse.getJSONArray("recipes")
            
            (0 until recipesArray.length()).map { i ->
                val recipeObj = recipesArray.getJSONObject(i)
                val name = recipeObj.getString("name")
                val ingredientsArray = recipeObj.getJSONArray("ingredients")
                val ingredients = (0 until ingredientsArray.length()).map { j ->
                    ingredientsArray.getString(j)
                }
                SimpleRecipe(name, ingredients)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing recipe list: ${e.message}", e)
            // Fallback to text parsing
            extractRecipesFromText(response)
        }
    }
}