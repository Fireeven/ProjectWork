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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val features: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            title = "Enterprise Workspace",
            subtitle = "Streamlined Project Management",
            description = "Transform your organization with our comprehensive workspace management platform. Built for enterprise-scale operations with advanced security and compliance features.",
            icon = Icons.Default.Business,
            primaryColor = Color(0xFF1E40AF),
            secondaryColor = Color(0xFF3B82F6),
            features = listOf(
                "Multi-project coordination",
                "Advanced security protocols", 
                "Real-time collaboration tools",
                "Enterprise-grade scalability"
            )
        ),
        OnboardingPage(
            title = "Advanced Analytics",
            subtitle = "Data-Driven Decision Making",
            description = "Leverage powerful analytics and business intelligence tools to gain actionable insights. Monitor performance, track KPIs, and optimize your workflow with comprehensive reporting.",
            icon = Icons.Default.Analytics,
            primaryColor = Color(0xFF059669),
            secondaryColor = Color(0xFF10B981),
            features = listOf(
                "Custom dashboard creation",
                "Predictive analytics engine",
                "Automated report generation",
                "Performance optimization insights"
            )
        ),
        OnboardingPage(
            title = "Ready for Launch",
            subtitle = "Your Digital Transformation Starts Now",
            description = "Everything is configured and ready for your team. Begin your journey towards enhanced productivity, streamlined operations, and measurable business growth.",
            icon = Icons.Default.Rocket,
            primaryColor = Color(0xFF7C3AED),
            secondaryColor = Color(0xFF8B5CF6),
            features = listOf(
                "Instant deployment capabilities",
                "24/7 enterprise support",
                "Seamless system integration",
                "Continuous platform updates"
            )
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
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
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            AnimatedVisibility(
                visible = pagerState.currentPage > 0,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
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
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = pages[pagerState.currentPage].primaryColor
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Next" else "Launch Platform",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (pagerState.currentPage < pages.size - 1) 
                        Icons.AutoMirrored.Filled.ArrowForward 
                    else 
                        Icons.Default.Rocket,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Skip button
        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
        ) {
            TextButton(
                onClick = onComplete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    "Skip Setup",
                    style = MaterialTheme.typography.titleSmall
                )
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
        targetValue = if (isActive) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.7f,
        animationSpec = tween(600),
        label = "alpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .padding(horizontal = 32.dp)
    ) {
        // Professional icon section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            page.primaryColor.copy(alpha = 0.2f),
                            page.secondaryColor.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background decoration
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawProfessionalBackground(this, page.primaryColor, page.secondaryColor)
            }
            
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = page.primaryColor
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title section
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.25.sp
            ),
            color = page.primaryColor,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Professional feature cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            page.features.forEachIndexed { index, feature ->
                item {
                    var visible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(isActive) {
                        if (isActive) {
                            delay(index * 150L)
                            visible = true
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600)) + slideInHorizontally { it / 2 }
                    ) {
                        FeatureCard(
                            feature = feature,
                            color = page.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = feature,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun PageIndicator(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val width by animateFloatAsState(
        targetValue = if (isActive) 32f else 8f,
        animationSpec = tween(300),
        label = "width"
    )
    
    Box(
        modifier = modifier
            .width(width.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isActive) color else Color.White.copy(alpha = 0.3f)
            )
    )
}

private fun drawProfessionalBackground(drawScope: DrawScope, primaryColor: Color, secondaryColor: Color) {
    val size = drawScope.size
    val center = Offset(size.width / 2, size.height / 2)
    
    // Subtle geometric patterns
    repeat(3) { index ->
        val radius = (size.minDimension / 2) * (0.3f + index * 0.15f)
        val alpha = 0.1f / (index + 1)
        
        drawScope.drawCircle(
            color = if (index % 2 == 0) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha),
            radius = radius,
            center = center,
            style = Stroke(width = 3f)
        )
    }
} 