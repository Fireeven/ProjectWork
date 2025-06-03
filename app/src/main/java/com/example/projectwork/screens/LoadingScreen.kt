package com.example.projectwork.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }
    
    val steps = listOf(
        "Initializing ProjectWork...",
        "Setting up your workspace...",
        "Loading smart features...",
        "Preparing grocery intelligence...",
        "Almost ready..."
    )
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    // Rotation animation for the outer ring
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Scale animation for the logo
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Progress simulation
    LaunchedEffect(Unit) {
        steps.forEachIndexed { index, _ ->
            currentStep = index
            delay(1000) // Wait 1 second for each step
            progress = (index + 1) / steps.size.toFloat()
        }
        delay(500) // Extra delay before completing
        onLoadingComplete()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Brand area with animated background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Animated background circle
                val density = LocalDensity.current
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawAnimatedCircle(this, primaryColor, density)
                }
                
                // Logo placeholder (you can replace this with your actual logo)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PW",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // App name and tagline
            Text(
                text = "ProjectWork",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Smart Grocery Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom progress bar
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Loading text
                Text(
                    text = if (currentStep < steps.size) steps[currentStep] else "Ready!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress percentage
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Features preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                FeatureIcon(
                    text = "🛒",
                    label = "Smart Lists",
                    isActive = progress > 0.2f
                )
                FeatureIcon(
                    text = "📊",
                    label = "Analytics",
                    isActive = progress > 0.4f
                )
                FeatureIcon(
                    text = "🤖",
                    label = "AI Assistant",
                    isActive = progress > 0.6f
                )
                FeatureIcon(
                    text = "🍳",
                    label = "Recipes",
                    isActive = progress > 0.8f
                )
            }
        }
    }
}

@Composable
private fun FeatureIcon(
    text: String,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.3f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.scale(scale)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .size(48.dp)
                .wrapContentSize(),
            color = Color.Unspecified.copy(alpha = alpha)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
    }
}

private fun drawAnimatedCircle(drawScope: DrawScope, color: Color, density: androidx.compose.ui.unit.Density) {
    with(density) {
        val strokeWidth = 4.dp.toPx()
        val radius = (drawScope.size.minDimension - strokeWidth) / 2
        val center = Offset(drawScope.size.width / 2, drawScope.size.height / 2)
        
        // Draw multiple animated circles
        for (i in 0..2) {
            val animatedRadius = radius - (i * 15.dp.toPx())
            val alpha = 1f - (i * 0.3f)
            
            drawScope.drawCircle(
                color = color.copy(alpha = alpha),
                radius = animatedRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }
    }
} 