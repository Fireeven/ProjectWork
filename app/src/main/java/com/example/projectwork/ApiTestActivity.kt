package com.example.projectwork

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectwork.ui.theme.ProjectWorkTheme
import com.example.projectwork.utils.OpenAIHelper
import kotlinx.coroutines.launch

class ApiTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectWorkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiConnectionTest()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConnectionTest() {
    val coroutineScope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var testResponse by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            "OpenAI API Connection Test",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
            Text("Testing connection...")
        } else {
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            // Test the API connection
                            testResult = OpenAIHelper.testAPIConnection()
                            
                            // If connected, try to get a test response
                            if (testResult?.first == true) {
                                try {
                                    val response = OpenAIHelper.getRecipeInfo("Give me a very simple pasta recipe")
                                    testResponse = response.choices.firstOrNull()?.message?.content
                                } catch (e: Exception) {
                                    Log.e("ApiTest", "Error getting recipe: ${e.message}", e)
                                    testResponse = "Error getting recipe: ${e.message}"
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ApiTest", "Error testing connection: ${e.message}", e)
                            testResult = Pair(false, "Error: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test API Connection")
            }
        }
        
        // Display test results
        testResult?.let { (isConnected, message) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Status: ${if (isConnected) "Connected" else "Not Connected"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Display test response if available
        testResponse?.let { response ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Test Recipe:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        response,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 