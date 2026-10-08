package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.GenerationMode
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinish: (suggestedPrompt: String?, mode: GenerationMode) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    var selectedSamplePrompt by remember { mutableStateOf<String?>(null) }
    var selectedMode by remember { mutableStateOf(GenerationMode.TEXT_TO_VIDEO) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("onboarding_screen")
    ) {
        // Top Bar: App Branding & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricIndigo, ElectricViolet, CyberPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Nirdosh AI Video",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip Tour",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        // Horizontal Pager Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> OnboardingSlideTextToVideo(
                        onSelectPrompt = {
                            selectedSamplePrompt = it
                            selectedMode = GenerationMode.TEXT_TO_VIDEO
                        }
                    )
                    1 -> OnboardingSlidePromptEnhancement()
                    2 -> OnboardingSlideImageToVideo(
                        onSelectMode = {
                            selectedMode = GenerationMode.IMAGE_TO_VIDEO
                            selectedSamplePrompt = "Make the character slowly turn toward the camera with gentle wind motion."
                        }
                    )
                    3 -> OnboardingSlideFormatsAndCredits()
                }
            }
        }

        // Bottom Controls: Animated Indicators & Next / Get Started Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "indicatorWidth"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = tween(300),
                        label = "indicatorColor"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Bottom Navigation Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Back",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                val isLastPage = pagerState.currentPage == pageCount - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish(selectedSamplePrompt, selectedMode)
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(if (pagerState.currentPage > 0) 2f else 1f)
                        .height(52.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricIndigo, ElectricViolet, CyberPink)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag(if (isLastPage) "onboarding_start_button" else "onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLastPage) "Start Creating (50⚡ Free)" else "Next Feature",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SLIDE 1: Text-to-Video Core Capabilities
// -------------------------------------------------------------
@Composable
private fun OnboardingSlideTextToVideo(
    onSelectPrompt: (String) -> Unit
) {
    var activePromptChoice by remember { mutableStateOf("Himalayas Sunrise") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hero Visual Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF131A2A))
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
                contentDescription = "Himalayas Cinematic Shot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Play badge overlay
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(listOf(ElectricIndigo, ElectricViolet)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Top Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Text to Video AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Turn Imagination into Video",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Simply describe any scene, character, or fantasy world. Nirdosh AI synthesizes fluid 60fps high-definition video directly from natural text.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive sample prompt selector
        Text(
            text = "Try tapping a prompt inspiration:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val samples = listOf(
                "Himalayas Sunrise" to "A cinematic drone shot flying over the Himalayas at sunrise, realistic clouds, golden sunlight.",
                "Cyberpunk Street" to "Cyberpunk samurai walking down neon wet streets in Tokyo rain, reflections, hyper-detailed.",
                "Cute Red Panda" to "Cute red panda astronaut exploring alien bioluminescent forest, 3D Pixar style."
            )

            samples.forEach { (label, promptText) ->
                val isSelected = activePromptChoice == label
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ElectricViolet.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(1.dp, ElectricViolet) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            activePromptChoice = label
                            onSelectPrompt(promptText)
                        }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SLIDE 2: Cinematic Prompt Enhancer (Interactive Live Demo)
// -------------------------------------------------------------
@Composable
private fun OnboardingSlidePromptEnhancement() {
    var isEnhanced by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Visual Transformation Comparison Card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricViolet.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("11-Attribute Enhancer", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = if (isEnhanced) "✨ Enhanced" else "Raw Prompt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnhanced) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Text Box
                AnimatedContent(
                    targetState = isEnhanced,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "promptEnhancement"
                ) { enhanced ->
                    if (enhanced) {
                        Column {
                            Text(
                                text = "\"A young boy walking through a futuristic metropolis at midnight, vibrant neon reflections on rain-slicked asphalt, smooth tracking 35mm lens, volumetric god rays, anamorphic bokeh, 8k ultra-detailed photorealistic render.\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AttributeChip(label = "Atmosphere", color = ElectricIndigo)
                                AttributeChip(label = "Lighting", color = RadiantAmber)
                                AttributeChip(label = "Camera Motion", color = NeonCyan)
                                AttributeChip(label = "8k Quality", color = CyberPink)
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "\"A boy walking in a city.\"",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Simple 3-word idea without lighting, camera motion, or style details.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Demo Button
                Button(
                    onClick = { isEnhanced = !isEnhanced },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("onboarding_toggle_enhance"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnhanced) MaterialTheme.colorScheme.surfaceVariant else ElectricViolet
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isEnhanced) MaterialTheme.colorScheme.onSurface else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnhanced) "Reset to Simple Prompt" else "Tap to Enhance Prompt Live",
                        fontWeight = FontWeight.Bold,
                        color = if (isEnhanced) MaterialTheme.colorScheme.onSurface else Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Cinematic Prompt Enhancer",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Never worry about complex prompting. One tap injects professional cinematography cues: volumetric lighting, anamorphic lenses, atmospheric weather, and motion dynamics.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

// -------------------------------------------------------------
// SLIDE 3: Image-to-Video Motion Capabilities
// -------------------------------------------------------------
@Composable
private fun OnboardingSlideImageToVideo(
    onSelectMode: () -> Unit
) {
    var activeMotionType by remember { mutableStateOf("Wind & Hair") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Visual Image-to-Video Demonstration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=800&q=80",
                        contentDescription = "Image to Video Source",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay showing animation instruction
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Motion: \"${
                                    when (activeMotionType) {
                                        "Wind & Hair" -> "Slow turn toward camera, hair drifting in evening breeze"
                                        "Camera Orbit" -> "Smooth 360 degree panoramic orbit around character"
                                        else -> "Slow-motion rain droplets falling with soft focal pull"
                                    }
                                }\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive motion selection pills
                Text(
                    text = "Select motion animation style:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Wind & Hair", "Camera Orbit", "Slow Motion").forEach { motion ->
                        val isSelected = activeMotionType == motion
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(1.dp, NeonCyan) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    activeMotionType = motion
                                    onSelectMode()
                                }
                        ) {
                            Text(
                                text = motion,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Animate Any Still Photo",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pick any image from your gallery. Describe the motion you want to see—natural wind, breathing, camera pan, or character gestures—and watch it come alive.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

// -------------------------------------------------------------
// SLIDE 4: Formats, Ratios & Free Credits
// -------------------------------------------------------------
@Composable
private fun OnboardingSlideFormatsAndCredits() {
    var selectedRatioDemo by remember { mutableStateOf("16:9") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Welcome Gift Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            ElectricViolet.copy(alpha = 0.25f),
                            CyberPink.copy(alpha = 0.2f),
                            RadiantAmber.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = RadiantAmber.copy(alpha = 0.2f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = RadiantAmber,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "50 Free Welcome Credits",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Enough to generate up to 25 HD video scenes immediately without any payment setup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Ratio & Multi-platform Compatibility
        Text(
            text = "Export for Any Platform:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ratios = listOf(
                "16:9" to "YouTube / TV",
                "9:16" to "Reels / TikTok",
                "1:1" to "Square Feed",
                "4:5" to "Social Portrait"
            )

            ratios.forEach { (ratio, sub) ->
                val isSelected = selectedRatioDemo == ratio
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedRatioDemo = ratio }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ratio,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sub.take(7),
                            fontSize = 9.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Ready to Create?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your studio is loaded and ready. Tap below to launch the video creation engine.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttributeChip(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
