package com.example.projectwork.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }
    
    val smartSteps = listOf(
        "Initializing Smart Cart AI...",
        "Loading intelligent algorithms...",
        "Connecting to product database...",
        "Preparing smart recommendations...",
        "Ready to optimize your shopping..."
    )
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic),
        label = "progress"
    )
    
    // Brain pulse animation
    val brainPulse by rememberInfiniteTransition(label = "brainPulse").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brainPulse"
    )
    
    // Connection lines animation
    val connectionAlpha by rememberInfiniteTransition(label = "connections").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "connectionAlpha"
    )
    
    // Logo rotation
    val logoRotation by rememberInfiniteTransition(label = "logoRotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logoRotation"
    )
    
    // Progress simulation
    LaunchedEffect(Unit) {
        smartSteps.forEachIndexed { index, _ ->
            currentStep = index
            delay(1000)
            progress = (index + 1) / smartSteps.size.toFloat()
        }
        delay(800)
        onLoadingComplete()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)), // Clean light background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(48.dp)
        ) {
            // Smart Cart Logo Section
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(brainPulse),
                contentAlignment = Alignment.Center
            ) {
                // Gradient background circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(logoRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4ADE80), // Green
                                    Color(0xFF06B6D4), // Cyan
                                    Color(0xFF3B82F6)  // Blue
                                ),
                                radius = 300f
                            )
                        )
                )
                
                // Brain and cart illustration
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(0.8f)
                ) {
                    drawSmartCartLogo(this, connectionAlpha)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Brand name
            Text(
                text = "Smart Cart",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF1E293B)
            )
            
            Text(
                text = "Intelligent Shopping Assistant",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Minimalist progress section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Clean progress indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4ADE80),
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Status text
                Text(
                    text = if (currentStep < smartSteps.size) smartSteps[currentStep] else "Welcome to Smart Cart!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Minimalist loading dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val delay = index * 300L
                        val scale by rememberInfiniteTransition(label = "dot$index").animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, delayMillis = delay.toInt()),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(Color(0xFF06B6D4))
                        )
                    }
                }
            }
        }
        
        // Clean footer
        Text(
            text = "Powered by AI • Version 2.1",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

private fun drawSmartCartLogo(drawScope: DrawScope, connectionAlpha: Float) {
    val center = Offset(drawScope.size.width / 2, drawScope.size.height / 2)
    val brainCenter = Offset(center.x, center.y - 20f)
    
    // Draw brain outline (simplified)
    val brainPath = Path().apply {
        // Simplified brain shape
        addOval(
            androidx.compose.ui.geometry.Rect(
                offset = Offset(brainCenter.x - 25f, brainCenter.y - 15f),
                size = Size(50f, 30f)
            )
        )
    }
    
    drawScope.drawPath(
        path = brainPath,
        color = Color.White.copy(alpha = 0.9f),
        style = Stroke(width = 3f)
    )
    
    // Draw brain segments
    drawScope.drawLine(
        start = Offset(brainCenter.x, brainCenter.y - 10f),
        end = Offset(brainCenter.x, brainCenter.y + 10f),
        color = Color.White.copy(alpha = 0.7f),
        strokeWidth = 2f
    )
    
    // Draw connection lines to cart
    val cartCenter = Offset(center.x, center.y + 30f)
    
    // Connection nodes
    val nodes = listOf(
        Offset(brainCenter.x - 15f, brainCenter.y + 5f),
        Offset(brainCenter.x + 15f, brainCenter.y + 5f),
        Offset(cartCenter.x - 10f, cartCenter.y - 15f),
        Offset(cartCenter.x + 10f, cartCenter.y - 15f)
    )
    
    // Draw connection lines
    nodes.forEachIndexed { index, node ->
        if (index < 2) {
            val targetNode = nodes[index + 2]
            drawScope.drawLine(
                start = node,
                end = targetNode,
                color = Color.White.copy(alpha = connectionAlpha * 0.8f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        
        // Draw connection nodes
        drawScope.drawCircle(
            color = Color.White.copy(alpha = connectionAlpha),
            radius = 3f,
            center = node
        )
    }
    
    // Draw simplified shopping cart
    val cartWidth = 20f
    val cartHeight = 15f
    
    // Cart body
    drawScope.drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(cartCenter.x - cartWidth/2, cartCenter.y - cartHeight/2),
        size = Size(cartWidth, cartHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
        style = Stroke(width = 2.5f)
    )
    
    // Cart handle
    drawScope.drawLine(
        start = Offset(cartCenter.x - cartWidth/2 - 5f, cartCenter.y - cartHeight/2 + 3f),
        end = Offset(cartCenter.x - cartWidth/2, cartCenter.y - cartHeight/2 + 3f),
        color = Color.White.copy(alpha = 0.9f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    
    // Cart wheels
    drawScope.drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = 3f,
        center = Offset(cartCenter.x - 6f, cartCenter.y + cartHeight/2 + 6f)
    )
    drawScope.drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = 3f,
        center = Offset(cartCenter.x + 6f, cartCenter.y + cartHeight/2 + 6f)
    )
} 