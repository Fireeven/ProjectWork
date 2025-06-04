package com.example.projectwork.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SmartFeaturePage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val benefits: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val smartPages = listOf(
        SmartFeaturePage(
            title = "AI-Powered Shopping",
            subtitle = "Intelligence that learns from you",
            description = "Experience the future of grocery shopping with our advanced AI that understands your preferences, suggests items, and optimizes your shopping experience.",
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFF4ADE80),
            benefits = listOf(
                "Smart product recommendations",
                "Personalized shopping lists",
                "Price optimization alerts",
                "Inventory tracking"
            )
        ),
        SmartFeaturePage(
            title = "Seamless Organization",
            subtitle = "Everything in perfect order",
            description = "Keep your shopping organized with intuitive categorization, smart sorting, and automated list management that saves you time and reduces stress.",
            icon = Icons.Default.Category,
            accentColor = Color(0xFF06B6D4),
            benefits = listOf(
                "Auto-categorized items",
                "Smart list sharing",
                "Store layout optimization",
                "Quick add shortcuts"
            )
        ),
        SmartFeaturePage(
            title = "Ready to Start",
            subtitle = "Your smart shopping journey begins",
            description = "Join thousands who have transformed their shopping experience. Start saving time, money, and effort with Smart Cart's intelligent features.",
            icon = Icons.Default.ShoppingCart,
            accentColor = Color(0xFF3B82F6),
            benefits = listOf(
                "Instant setup",
                "No learning curve",
                "Works offline",
                "Always improving"
            )
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            SmartPageContent(
                page = smartPages[page],
                pageIndex = page,
                isActive = pagerState.currentPage == page
            )
        }
        
        // Minimalist navigation
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                repeat(smartPages.size) { index ->
                    SmartPageIndicator(
                        isActive = index == pagerState.currentPage,
                        accentColor = smartPages[pagerState.currentPage].accentColor
                    )
                }
            }
            
            // Action button
            Button(
                onClick = {
                    if (pagerState.currentPage < smartPages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = smartPages[pagerState.currentPage].accentColor
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = if (pagerState.currentPage < smartPages.size - 1) "Continue" else "Start Shopping Smart",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
        
        // Skip option
        AnimatedVisibility(
            visible = pagerState.currentPage < smartPages.size - 1,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            TextButton(
                onClick = onComplete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF64748B)
                )
            ) {
                Text(
                    "Skip",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun SmartPageContent(
    page: SmartFeaturePage,
    pageIndex: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val iconScale by rememberInfiniteTransition(label = "iconPulse$pageIndex").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .padding(horizontal = 32.dp, vertical = 40.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Feature icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(iconScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.1f),
                            page.accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 200f
                    )
                )
                .border(
                    width = 1.dp,
                    color = page.accentColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = page.accentColor
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Content section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = page.accentColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                ),
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Benefits list with better spacing
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .fillMaxWidth()
            ) {
                page.benefits.forEachIndexed { index, benefit ->
                    item {
                        var visible by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(isActive) {
                            if (isActive) {
                                delay(index * 100L)
                                visible = true
                            }
                        }
                        
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(500)) + slideInHorizontally { it / 3 }
                        ) {
                            SmartBenefitItem(
                                benefit = benefit,
                                accentColor = page.accentColor
                            )
                        }
                    }
                }
            }
        }
        
        // Add bottom spacing to ensure no overlap with navigation
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
private fun SmartBenefitItem(
    benefit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = accentColor
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = benefit,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF334155)
        )
    }
}

@Composable
private fun SmartPageIndicator(
    isActive: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val width by animateFloatAsState(
        targetValue = if (isActive) 24f else 8f,
        animationSpec = tween(300),
        label = "width"
    )
    
    Box(
        modifier = modifier
            .width(width.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isActive) accentColor else Color(0xFFE2E8F0)
            )
    )
} 