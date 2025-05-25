package com.example.projectwork.utils

import java.util.UUID

/**
 * Data class representing a parsed recipe
 */
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>
)

/**
 * Data class representing a recipe ingredient
 */
data class Ingredient(
    val name: String,
    val quantity: String,
    val unit: String? = null
) {
    override fun toString(): String {
        return if (unit != null) {
            "$quantity $unit $name"
        } else {
            "$quantity $name"
        }
    }
}

/**
 * Utility class for parsing recipes from text
 */
object RecipeParser {
    
    /**
     * Parses a recipe from text
     * 
     * @param text The text to parse
     * @return A Recipe object if parsing is successful, null otherwise
     */
    fun parseRecipe(text: String): Recipe? {
        // Check if text has enough content to be a recipe
        if (text.length < 50) return null
        
        try {
            // Extract title
            val title = extractTitle(text)
            
            // Extract ingredients section
            val ingredientsText = extractSection(text, "ingredients", "instructions")
            
            // Extract instructions section
            val instructionsText = extractSection(text, "instructions", null)
            
            // Parse ingredients
            val ingredients = parseIngredients(ingredientsText ?: "")
            
            // Parse instructions
            val instructions = parseInstructions(instructionsText ?: "")
            
            // Return recipe if we have at least a title and some ingredients
            return if (title != null && ingredients.isNotEmpty()) {
                Recipe(
                    title = title.trim(),
                    ingredients = ingredients,
                    instructions = instructions
                )
            } else null
        } catch (e: Exception) {
            // If any error occurs during parsing, return null
            return null
        }
    }
    
    /**
     * Extracts the title from recipe text
     */
    private fun extractTitle(text: String): String? {
        // Try to find a title at the beginning of the text
        val lines = text.split("\n")
        for (line in lines.take(5)) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && 
                !trimmed.startsWith("#") && 
                !trimmed.startsWith("-") &&
                !trimmed.startsWith("*")) {
                return trimmed
            }
        }
        
        // Try to find a title with markdown heading notation
        val titleRegex = """#\s+(.+)""".toRegex()
        val matchResult = titleRegex.find(text)
        return matchResult?.groupValues?.get(1)
    }
    
    /**
     * Extracts a section from recipe text between two markers
     */
    private fun extractSection(text: String, sectionName: String, nextSectionName: String?): String? {
        val lowercaseText = text.lowercase()
        
        // Look for section headings (with variations in formatting)
        val sectionIndicators = listOf(
            """#+\s+$sectionName""",
            """$sectionName:""",
            """$sectionName\s*:""",
            """$sectionName\s*\n"""
        )
        
        // Find the start of the section
        var startIndex = -1
        for (indicator in sectionIndicators) {
            val regex = indicator.toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(lowercaseText)
            if (match != null) {
                startIndex = match.range.last + 1
                break
            }
        }
        
        if (startIndex == -1) return null
        
        // Find the end of the section (start of the next section)
        var endIndex = text.length
        if (nextSectionName != null) {
            val nextSectionIndicators = listOf(
                """#+\s+$nextSectionName""",
                """$nextSectionName:""",
                """$nextSectionName\s*:""",
                """$nextSectionName\s*\n"""
            )
            
            for (indicator in nextSectionIndicators) {
                val regex = indicator.toRegex(RegexOption.IGNORE_CASE)
                val match = regex.find(lowercaseText, startIndex)
                if (match != null) {
                    endIndex = match.range.first
                    break
                }
            }
        }
        
        return text.substring(startIndex, endIndex).trim()
    }
    
    /**
     * Parses ingredients from text
     */
    private fun parseIngredients(text: String): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val lines = text.split("\n")
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#')) continue
            
            // Remove markdown list markers
            var ingredientText = trimmed
                .replace(Regex("^-\\s*"), "")
                .replace(Regex("^\\*\\s*"), "")
                .trim()
            
            if (ingredientText.isEmpty()) continue
            
            // Try to parse ingredient with quantity and unit
            val quantityUnitRegex = """^([\d\/\.\s]+)\s*(cup|cups|tablespoon|tablespoons|tbsp|teaspoon|teaspoons|tsp|gram|grams|g|kilogram|kilograms|kg|pound|pounds|lb|lbs|ounce|ounces|oz|ml|liter|liters|l|pinch|pinches|clove|cloves|can|cans|package|packages|pkg|slice|slices)s?\s+of\s+(.+)$""".toRegex(RegexOption.IGNORE_CASE)
            var match = quantityUnitRegex.find(ingredientText)
            
            if (match != null) {
                val quantity = match.groupValues[1].trim()
                val unit = match.groupValues[2].trim()
                val name = match.groupValues[3].trim()
                ingredients.add(Ingredient(name, quantity, unit))
                continue
            }
            
            // Try simpler pattern without "of"
            val simpleQuantityUnitRegex = """^([\d\/\.\s]+)\s*(cup|cups|tablespoon|tablespoons|tbsp|teaspoon|teaspoons|tsp|gram|grams|g|kilogram|kilograms|kg|pound|pounds|lb|lbs|ounce|ounces|oz|ml|liter|liters|l|pinch|pinches|clove|cloves|can|cans|package|packages|pkg|slice|slices)s?\s+(.+)$""".toRegex(RegexOption.IGNORE_CASE)
            match = simpleQuantityUnitRegex.find(ingredientText)
            
            if (match != null) {
                val quantity = match.groupValues[1].trim()
                val unit = match.groupValues[2].trim()
                val name = match.groupValues[3].trim()
                ingredients.add(Ingredient(name, quantity, unit))
                continue
            }
            
            // Try to match just a quantity
            val quantityRegex = """^([\d\/\.\s]+)\s+(.+)$""".toRegex()
            match = quantityRegex.find(ingredientText)
            
            if (match != null) {
                val quantity = match.groupValues[1].trim()
                val name = match.groupValues[2].trim()
                ingredients.add(Ingredient(name, quantity))
                continue
            }
            
            // If no pattern matches, just add the whole line as an ingredient
            ingredients.add(Ingredient(ingredientText, "1"))
        }
        
        return ingredients
    }
    
    /**
     * Parses instructions from text
     */
    private fun parseInstructions(text: String): List<String> {
        val instructions = mutableListOf<String>()
        val lines = text.split("\n")
        
        val currentStep = StringBuilder()
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#')) {
                if (currentStep.isNotEmpty()) {
                    instructions.add(currentStep.toString().trim())
                    currentStep.clear()
                }
                continue
            }
            
            // Check for numbered step
            val numberedStep = Regex("""^\d+\.\s+(.+)$""").find(trimmed)
            if (numberedStep != null) {
                if (currentStep.isNotEmpty()) {
                    instructions.add(currentStep.toString().trim())
                    currentStep.clear()
                }
                currentStep.append(numberedStep.groupValues[1])
                continue
            }
            
            // Check for bullet point
            val bulletPoint = Regex("""^[-*]\s+(.+)$""").find(trimmed)
            if (bulletPoint != null) {
                if (currentStep.isNotEmpty()) {
                    instructions.add(currentStep.toString().trim())
                    currentStep.clear()
                }
                currentStep.append(bulletPoint.groupValues[1])
                continue
            }
            
            // If it's continuing text for the current step
            if (currentStep.isNotEmpty()) {
                currentStep.append(" ").append(trimmed)
            } else {
                currentStep.append(trimmed)
            }
        }
        
        if (currentStep.isNotEmpty()) {
            instructions.add(currentStep.toString().trim())
        }
        
        return instructions
    }
} 