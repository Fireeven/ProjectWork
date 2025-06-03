package com.example.projectwork.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            title = "Smart Grocery Management",
            subtitle = "Revolutionize Your Shopping",
            description = "Transform your grocery shopping with AI-powered lists, real-time price tracking, and intelligent suggestions. Never forget an item or overspend again.",
            emoji = "🛒",
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF8B5CF6)
        ),
        OnboardingPage(
            title = "AI-Powered Recipe Assistant",
            subtitle = "Cook Like a Pro",
            description = "Get personalized recipe recommendations based on your available ingredients. Our smart AI helps you discover new dishes and make the most of what you have.",
            emoji = "🤖",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF06B6D4)
        ),
        OnboardingPage(
            title = "Advanced Analytics",
            subtitle = "Track Your Spending",
            description = "Gain insights into your shopping patterns with detailed analytics. Monitor your budget, track savings, and make informed decisions about your grocery expenses.",
            emoji = "📊",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFEF4444)
        ),
        OnboardingPage(
            title = "Ready to Get Started?",
            subtitle = "Your Smart Shopping Journey Begins",
            description = "Join thousands of users who have already transformed their grocery shopping experience. Start saving time, money, and effort today.",
            emoji = "🚀",
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFF8B5CF6)
        )
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                pageIndex = page,
                isActive = pagerState.currentPage == page
            )
        }
        
        // Navigation controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            AnimatedVisibility(
                visible = pagerState.currentPage > 0,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    PageIndicator(
                        isActive = index == pagerState.currentPage,
                        color = pages[pagerState.currentPage].primaryColor
                    )
                }
            }
            
            // Next/Finish button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = pages[pagerState.currentPage].primaryColor
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (pagerState.currentPage < pages.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        // Skip button
        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            TextButton(
                onClick = onComplete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Skip", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(600),
        label = "alpha"
    )
    
    // Floating animation for emoji
    val floatingOffset by rememberInfiniteTransition(label = "floating").animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        page.primaryColor.copy(alpha = 0.1f),
                        page.secondaryColor.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    ),
                    radius = 1200f
                )
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animated background decoration
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = (-floatingOffset).dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawAnimatedBackground(this, page.primaryColor, page.secondaryColor)
                }
                
                // Main emoji
                Text(
                    text = page.emoji,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 80.sp
                    ),
                    modifier = Modifier.offset(y = floatingOffset.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = page.primaryColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Feature highlights for each page
            when (pageIndex) {
                0 -> FeatureList(
                    features = listOf(
                        "🎯 Smart shopping lists with AI suggestions",
                        "💰 Real-time price tracking and alerts",
                        "📱 Seamless mobile experience"
                    ),
                    color = page.primaryColor
                )
                1 -> FeatureList(
                    features = listOf(
                        "🍳 Personalized recipe recommendations",
                        "🧠 AI-powered ingredient analysis",
                        "⭐ Save and organize favorite recipes"
                    ),
                    color = page.primaryColor
                )
                2 -> FeatureList(
                    features = listOf(
                        "📈 Detailed spending analytics",
                        "🎯 Budget tracking and insights",
                        "💡 Smart saving recommendations"
                    ),
                    color = page.primaryColor
                )
                3 -> FeatureList(
                    features = listOf(
                        "🚀 Ready in less than 2 minutes",
                        "🔒 Your data stays private and secure",
                        "🆓 Free to use with premium features"
                    ),
                    color = page.primaryColor
                )
            }
        }
    }
}

@Composable
private fun FeatureList(
    features: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        features.forEachIndexed { index, feature ->
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                delay(index * 200L)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInHorizontally { it }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, CircleShape)
                    )
                    
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val width by animateDpAsState(
        targetValue = if (isActive) 24.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "width"
    )
    
    val colorAnimated by animateColorAsState(
        targetValue = if (isActive) color else color.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "color"
    )
    
    Box(
        modifier = modifier
            .width(width)
            .height(8.dp)
            .background(
                color = colorAnimated,
                shape = CircleShape
            )
    )
}

private fun drawAnimatedBackground(
    drawScope: DrawScope,
    primaryColor: Color,
    secondaryColor: Color
) {
    val strokeWidth = 2f // Use Float instead of dp for simplicity
    val center = Offset(drawScope.size.width / 2, drawScope.size.height / 2)
    
    // Draw multiple concentric circles with different colors and sizes
    for (i in 0..3) {
        val radius = (drawScope.size.minDimension / 2) - (i * 50f) // Use Float
        val alpha = 0.3f - (i * 0.05f)
        val color = if (i % 2 == 0) primaryColor else secondaryColor
        
        if (radius > 0) {
            drawScope.drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }
    }
} 