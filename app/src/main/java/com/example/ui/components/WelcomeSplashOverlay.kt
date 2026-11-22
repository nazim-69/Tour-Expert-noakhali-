package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BrandConstants
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.DeepTealSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeSplashOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(600)) + scaleOut(tween(600), targetScale = 1.06f)
    ) {
        var showEmblem by remember { mutableStateOf(false) }
        var showWelcomePill by remember { mutableStateOf(false) }
        var showBrandTitle by remember { mutableStateOf(false) }
        var showTagline by remember { mutableStateOf(false) }
        var showDestinationChips by remember { mutableStateOf(false) }

        val routeProgress = remember { Animatable(0f) }
        val mountainRise = remember { Animatable(0f) }
        val underlineProgress = remember { Animatable(0f) }
        val backdropScale = remember { Animatable(1.16f) }

        val currentOnDismiss by rememberUpdatedState(onDismiss)

        LaunchedEffect(visible) {
            if (!visible) return@LaunchedEffect
            showEmblem = false
            showWelcomePill = false
            showBrandTitle = false
            showTagline = false
            showDestinationChips = false
            routeProgress.snapTo(0f)
            mountainRise.snapTo(0f)
            underlineProgress.snapTo(0f)
            backdropScale.snapTo(1.16f)

            launch {
                backdropScale.animateTo(1.02f, tween(3400, easing = FastOutSlowInEasing))
            }
            launch {
                mountainRise.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
            }
            launch {
                routeProgress.animateTo(1f, tween(2400, easing = FastOutSlowInEasing))
            }

            showEmblem = true
            delay(240)
            showWelcomePill = true
            delay(260)
            showBrandTitle = true
            delay(280)
            showTagline = true
            launch {
                underlineProgress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            }
            delay(260)
            showDestinationChips = true

            delay(2100)
            currentOnDismiss()
        }

        val infinite = rememberInfiniteTransition(label = "splashAmbient")
        val cloudShift by infinite.animateFloat(
            initialValue = -36f,
            targetValue = 36f,
            animationSpec = infiniteRepeatable(
                animation = tween(4200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cloudShift"
        )
        val dashPhase by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 64f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dashPhase"
        )
        val haloRotation by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(14000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "haloRotation"
        )
        val floatY by infinite.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatY"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepTealNight)
                .pointerInput(Unit) {}
                .testTag("welcome_splash_overlay")
        ) {
            // Subtle zooming Bangladesh landscape backdrop
            Image(
                painter = painterResource(id = R.drawable.img_hero_bangladesh),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = backdropScale.value
                        scaleY = backdropScale.value
                    }
                    .alpha(0.24f)
            )

            // Deep atmospheric gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DeepTealNight.copy(alpha = 0.88f),
                                DeepTealSurface.copy(alpha = 0.84f),
                                Color(0xFF07232B).copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            // Animated clouds, rising mountains, curved flight path & moving airplane
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val riseOffset = (1f - mountainRise.value) * (h * 0.14f)

                // Soft glowing clouds
                drawCircle(
                    color = BrandOceanBlue.copy(alpha = 0.10f),
                    radius = w * 0.25f,
                    center = Offset(w * 0.18f + cloudShift, h * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = w * 0.20f,
                    center = Offset(w * 0.78f - cloudShift, h * 0.24f)
                )
                drawCircle(
                    color = BrandEmerald.copy(alpha = 0.08f),
                    radius = w * 0.18f,
                    center = Offset(w * 0.52f + cloudShift * 0.6f, h * 0.14f)
                )

                // Back mountain silhouette
                val backMountain = Path().apply {
                    moveTo(0f, h * 0.88f + riseOffset)
                    lineTo(w * 0.20f, h * 0.68f + riseOffset)
                    lineTo(w * 0.46f, h * 0.78f + riseOffset)
                    lineTo(w * 0.74f, h * 0.65f + riseOffset)
                    lineTo(w, h * 0.79f + riseOffset)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(backMountain, color = BrandOceanBlue.copy(alpha = 0.18f))

                // Front mountain silhouette
                val frontMountain = Path().apply {
                    moveTo(0f, h * 0.94f + riseOffset)
                    lineTo(w * 0.32f, h * 0.75f + riseOffset)
                    lineTo(w * 0.62f, h * 0.86f + riseOffset)
                    lineTo(w * 0.88f, h * 0.74f + riseOffset)
                    lineTo(w, h * 0.84f + riseOffset)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(frontMountain, color = BrandEmerald.copy(alpha = 0.18f))

                // Curved route path with progressive PathMeasure segment & flying plane marker
                val fullRoute = Path().apply {
                    moveTo(w * 0.10f, h * 0.77f)
                    cubicTo(
                        w * 0.32f, h * 0.60f,
                        w * 0.66f, h * 0.84f,
                        w * 0.90f, h * 0.63f
                    )
                }

                // Subtle guide dashed route
                drawPath(
                    path = fullRoute,
                    color = Color.White.copy(alpha = 0.12f),
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 14f), phase = -dashPhase)
                    )
                )

                // Active animated route segment
                val pathMeasure = PathMeasure().apply { setPath(fullRoute, false) }
                val totalLen = pathMeasure.length
                if (w > 0f && h > 0f && totalLen > 0f) {
                    val activePath = Path()
                    pathMeasure.getSegment(0f, totalLen * routeProgress.value, activePath, true)
                    drawPath(
                        path = activePath,
                        brush = Brush.horizontalGradient(
                            listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange)
                        ),
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )

                    // Origin pin
                    drawCircle(
                        color = BrandEmerald,
                        radius = 10f,
                        center = Offset(w * 0.10f, h * 0.77f)
                    )
                    // Destination pulse
                    if (routeProgress.value > 0.92f) {
                        drawCircle(
                            color = BrandSunsetOrange.copy(alpha = 0.35f),
                            radius = 22f,
                            center = Offset(w * 0.90f, h * 0.63f)
                        )
                    }
                    drawCircle(
                        color = BrandSunsetOrange,
                        radius = 10f * routeProgress.value,
                        center = Offset(w * 0.90f, h * 0.63f)
                    )

                    // Animated airplane silhouette flying along curve tangent
                    val dist = (totalLen * routeProgress.value).coerceIn(0f, totalLen)
                    val pos = pathMeasure.getPosition(dist)
                    val tan = pathMeasure.getTangent(dist)
                    val angleDeg = Math.toDegrees(atan2(tan.y.toDouble(), tan.x.toDouble())).toFloat()

                    rotate(degrees = angleDeg, pivot = pos) {
                        val plane = Path().apply {
                            moveTo(pos.x + 20f, pos.y)
                            lineTo(pos.x - 10f, pos.y - 14f)
                            lineTo(pos.x - 4f, pos.y)
                            lineTo(pos.x - 10f, pos.y + 14f)
                            close()
                        }
                        drawPath(plane, color = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Skip / Enter control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("skip_welcome_button")
                    ) {
                        Text(
                            text = "প্রবেশ করুন",
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "প্রবেশ করুন",
                            tint = BrandSunsetOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center Staggered Typography & Emblem Showcase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Rotating Ring + Brand Emblem
                    AnimatedVisibility(
                        visible = showEmblem,
                        enter = fadeIn(tween(500)) + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialScale = 0.65f
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .graphicsLayer { translationY = floatY },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                rotate(haloRotation) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                BrandEmerald,
                                                BrandOceanBlue,
                                                BrandSunsetOrange,
                                                BrandEmerald
                                            )
                                        ),
                                        style = Stroke(
                                            width = 5f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 12f))
                                        )
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .clip(CircleShape)
                                    .background(DeepTealNight)
                                    .border(2.dp, Color.White.copy(alpha = 0.22f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_launcher_emblem),
                                    contentDescription = BrandConstants.ORGANIZATION_NAME,
                                    modifier = Modifier
                                        .size(94.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. "স্বাগতম" Pill
                    AnimatedVisibility(
                        visible = showWelcomePill,
                        enter = fadeIn(tween(450)) + scaleIn(
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            initialScale = 0.8f
                        ) + slideInVertically(tween(450)) { 24 }
                    ) {
                        Surface(
                            color = BrandEmerald.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.border(
                                1.2.dp,
                                BrandEmerald.copy(alpha = 0.55f),
                                RoundedCornerShape(50.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "স্বাগতম",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BrandEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. "ট্যুর এক্সপার্ট নোয়াখালী"
                    AnimatedVisibility(
                        visible = showBrandTitle,
                        enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { 36 }
                    ) {
                        Text(
                            text = BrandConstants.ORGANIZATION_NAME,
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. "ভ্রমণ যেখানে গল্প সেখানে" + Animated Gradient Underline
                    AnimatedVisibility(
                        visible = showTagline,
                        enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { 28 }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = BrandConstants.TAGLINE,
                                style = MaterialTheme.typography.headlineSmall,
                                color = BrandSunsetOrange,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .width((180f * underlineProgress.value).dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 5. Floating Destination Preview Chips
                    AnimatedVisibility(
                        visible = showDestinationChips,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 24 }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("সুন্দরবন", "সাজেক", "রাঙামাটি", "বান্দরবান", "শ্রীমঙ্গল").forEach { place ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(50.dp),
                                    modifier = Modifier.border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.18f),
                                        RoundedCornerShape(50.dp)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = BrandOceanBlue,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = place,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Address & Progress Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(routeProgress.value)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(BrandEmerald, BrandSunsetOrange))
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = BrandOceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = BrandConstants.ADDRESS,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                }
            }
        }
    }
}
