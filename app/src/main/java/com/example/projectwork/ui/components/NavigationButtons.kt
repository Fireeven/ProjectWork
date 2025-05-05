package com.example.projectwork.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A navigation bar with multiple buttons in a row
 * 
 * @param onBackClick Callback for when the back button is clicked
 * @param onWelcomeClick Callback for navigating to welcome screen
 * @param showChatDialog MutableState to control the visibility of the chat dialog
 * @param modifier Optional modifier for customizing the layout
 * @param showBackButton Whether to show the back button (default: true)
 * @param showWelcomeButton Whether to show the welcome button (default: false)
 * @param showChatButton Whether to show the chatbot button (default: true)
 */
@Composable
fun NavigationButtons(
    onBackClick: () -> Unit,
    onWelcomeClick: () -> Unit = {},
    showChatDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    showWelcomeButton: Boolean = false,
    showChatButton: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = if (showBackButton || showWelcomeButton) {
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            } else {
                modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side button(s)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    FloatingActionButton(
                        onClick = onBackClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
                
                if (showWelcomeButton) {
                    FloatingActionButton(
                        onClick = onWelcomeClick,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Back to Welcome Screen"
                        )
                    }
                }
            }
            
            // Right side - chat button
            if (showChatButton) {
                FloatingActionButton(
                    onClick = { showChatDialog.value = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Open Recipe Chatbot"
                    )
                }
            }
        }
    }
}

/**
 * A standalone back button
 */
@Composable
fun BackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onBackClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go Back"
        )
    }
}

/**
 * A standalone welcome screen button
 */
@Composable
fun WelcomeButton(
    onWelcomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onWelcomeClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "Back to Welcome Screen"
        )
    }
}

/**
 * A standalone chatbot button
 */
@Composable
fun ChatBotButton(
    showChatDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { showChatDialog.value = true },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Open Recipe Chatbot"
        )
    }
} 