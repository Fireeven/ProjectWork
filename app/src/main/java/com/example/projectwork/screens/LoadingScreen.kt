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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
    
    val loadingSteps = listOf(
        "Initializing secure environment...",
        "Loading enterprise modules...",
        "Configuring user workspace...",
        "Preparing dashboard components...",
        "Finalizing system setup..."
    )
    
    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "progress"
    )
    
    // Rotation animation for the logo ring
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animation for accent elements
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Progress simulation
    LaunchedEffect(Unit) {
        loadingSteps.forEachIndexed { index, _ ->
            currentStep = index
            delay(1200) // Professional pacing
            progress = (index + 1) / loadingSteps.size.toFloat()
        }
        delay(600) // Final pause before transition
        onLoadingComplete()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1419), // Dark blue-gray
                        Color(0xFF1A1F2E), // Darker blue-gray
                        Color(0xFF0F1419)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Professional logo section
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawProfessionalRings(this, pulse)
                }
                
                // Inner logo container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2563EB), // Professional blue
                                    Color(0xFF1D4ED8), // Darker blue
                                    Color(0xFF1E40AF)  // Deep blue
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Company logo/monogram
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PW",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.8f))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Company branding
            Text(
                text = "ProjectWork",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 32.sp,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
            
            Text(
                text = "Enterprise Workspace Solutions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Progress section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Professional progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6),
                                        Color(0xFF06B6D4),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Status text
                Text(
                    text = if (currentStep < loadingSteps.size) loadingSteps[currentStep] else "System ready",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress percentage
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF3B82F6)
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Subtle loading indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                repeat(4) { index ->
                    val delay = index * 200L
                    val alpha by rememberInfiniteTransition(label = "indicator$index").animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, delayMillis = delay.toInt()),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha))
                    )
                }
            }
        }
        
        // Professional footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "© 2025 ProjectWork Enterprise",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.4f)
            )
            Text(
                text = "Version 2.1.0",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun drawProfessionalRings(drawScope: DrawScope, pulse: Float) {
    val center = Offset(drawScope.size.width / 2, drawScope.size.height / 2)
    val baseRadius = drawScope.size.minDimension / 2 * 0.85f
    
    // Outer accent ring
    drawScope.drawCircle(
        color = Color(0xFF3B82F6).copy(alpha = 0.3f * pulse),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 9f, cap = StrokeCap.Round)
    )
    
    // Middle ring with gradient effect
    drawScope.drawArc(
        color = Color(0xFF06B6D4).copy(alpha = 0.6f),
        startAngle = -90f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(
            center.x - baseRadius * 0.8f,
            center.y - baseRadius * 0.8f
        ),
        size = Size(baseRadius * 1.6f, baseRadius * 1.6f),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    
    // Inner accent dots
    repeat(8) { index ->
        val angle = (index * 45f) * (Math.PI / 180f)
        val dotRadius = baseRadius * 0.75f
        val dotCenter = Offset(
            center.x + (dotRadius * kotlin.math.cos(angle)).toFloat(),
            center.y + (dotRadius * kotlin.math.sin(angle)).toFloat()
        )
        
        drawScope.drawCircle(
            color = Color(0xFF8B5CF6).copy(alpha = 0.4f * pulse),
            radius = 6f,
            center = dotCenter
        )
    }
} 