package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BrandConstants
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.DeepTealSurface

data class TopMenuItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val route: String,
    val focusSection: String? = null
)

val OFFICIAL_TOP_MENU_ITEMS = listOf(
    TopMenuItem("home", "হোম", Icons.Default.Home, "home"),
    TopMenuItem("about", "আমাদের সম্পর্কে", Icons.Default.Info, "team", "about"),
    TopMenuItem("events", "ইভেন্ট", Icons.Default.CalendarMonth, "events"),
    TopMenuItem("destinations", "পর্যটন স্থান", Icons.Default.Explore, "destinations"),
    TopMenuItem("seat_plan", "সিট প্লান", Icons.Default.EventSeat, "booking", "seat_plan"),
    TopMenuItem("admin", "এডমিন", Icons.Default.Groups, "admin"),
    TopMenuItem("booking", "বুকিং", Icons.Default.ConfirmationNumber, "booking", "booking"),
    TopMenuItem("contact", "যোগাযোগ", Icons.Default.Phone, "team", "contact")
)

@Composable
fun AnimatedHamburgerButton(
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hamburgerMorph"
    )
    val density = LocalDensity.current
    val shiftPx = with(density) { 6.5.dp.toPx() }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isOpen) BrandOceanBlue.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (isOpen) BrandOceanBlue.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("hamburger_menu_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.5.dp)
                    .graphicsLayer {
                        translationY = -shiftPx * (1f - progress)
                        rotationZ = 45f * progress
                    }
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isOpen) BrandSunsetOrange else Color.White)
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.5.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        scaleX = (1f - progress).coerceAtLeast(0.01f)
                        alpha = 1f - progress
                    }
                    .clip(RoundedCornerShape(50.dp))
                    .background(BrandEmerald)
            )
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.5.dp)
                    .graphicsLayer {
                        translationY = shiftPx * (1f - progress)
                        rotationZ = -45f * progress
                    }
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isOpen) BrandOceanBlue else Color.White)
            )
        }
    }
}

@Composable
fun StickyTopBarHeader(
    activeMenuId: String,
    mobileMenuExpanded: Boolean,
    showMobilePillStrip: Boolean,
    notificationCount: Int = 0,
    onToggleMobileMenu: () -> Unit,
    onCloseMobileMenu: () -> Unit,
    onSelectMenuItem: (TopMenuItem) -> Unit,
    onNotificationsClick: () -> Unit,
    onBookNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DeepTealNight.copy(alpha = 0.98f),
                        DeepTealSurface.copy(alpha = 0.96f)
                    )
                )
            )
            .statusBarsPadding()
            .testTag("sticky_top_navigation_bar")
    ) {
        val isWideDesktop = maxWidth >= 900.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            // Primary Sticky Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (isWideDesktop) Modifier else Modifier.weight(1f)
                ) {
                    if (!isWideDesktop) {
                        AnimatedHamburgerButton(
                            isOpen = mobileMenuExpanded,
                            onClick = onToggleMobileMenu
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onCloseMobileMenu()
                                onSelectMenuItem(OFFICIAL_TOP_MENU_ITEMS.first())
                            }
                            .padding(vertical = 2.dp, horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    Brush.sweepGradient(
                                        listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange, BrandEmerald)
                                    ),
                                    CircleShape
                                )
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_launcher_emblem),
                                contentDescription = BrandConstants.ORGANIZATION_NAME,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = BrandConstants.ORGANIZATION_NAME,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = BrandConstants.TAGLINE,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandSunsetOrange,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Inline 8 menu links on tablet/desktop screens
                if (isWideDesktop) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OFFICIAL_TOP_MENU_ITEMS.forEach { item ->
                            TopBarPillLink(
                                item = item,
                                selected = activeMenuId == item.id,
                                onClick = { onSelectMenuItem(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Notification Center Bell Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                        .clickable {
                            onCloseMobileMenu()
                            onNotificationsClick()
                        }
                        .testTag("topbar_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "নোটিফিকেশন ও রিমাইন্ডার",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(7.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(BrandSunsetOrange)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        onCloseMobileMenu()
                        onBookNowClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandEmerald,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("topbar_book_now_button")
                ) {
                    Text(
                        text = "বুকিং করুন",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Sticky Horizontal Menu Link Ribbon on Mobile
            if (!isWideDesktop && showMobilePillStrip) {
                val stripListState = rememberLazyListState()
                val activeIndex = remember(activeMenuId) {
                    OFFICIAL_TOP_MENU_ITEMS.indexOfFirst { it.id == activeMenuId }.coerceAtLeast(0)
                }
                LaunchedEffect(activeIndex) {
                    stripListState.animateScrollToItem(activeIndex)
                }

                LazyRow(
                    state = stripListState,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(OFFICIAL_TOP_MENU_ITEMS, key = { _, item -> item.id }) { _, item ->
                        TopBarPillLink(
                            item = item,
                            selected = activeMenuId == item.id,
                            onClick = {
                                onCloseMobileMenu()
                                onSelectMenuItem(item)
                            }
                        )
                    }
                }
            }

            // Brand Gradient Bottom Border Accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange)
                        )
                    )
            )
        }
    }
}

@Composable
fun AnimatedMobileMenuOverlay(
    expanded: Boolean,
    activeMenuId: String,
    currentUserEmail: String?,
    onDismiss: () -> Unit,
    onSelectMenuItem: (TopMenuItem) -> Unit,
    onReplayWelcome: () -> Unit,
    onShareAppLink: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.50f else 0f,
        animationSpec = tween(220),
        label = "mobileMenuScrim"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (expanded || scrimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(scrimAlpha)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(220, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = DeepTealNight.copy(alpha = 0.98f),
                tonalElevation = 8.dp,
                shadowElevation = 14.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_animated_menu_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    val rows = OFFICIAL_TOP_MENU_ITEMS.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    val selected = activeMenuId == item.id
                                    Surface(
                                        color = if (selected) {
                                            BrandOceanBlue.copy(alpha = 0.28f)
                                        } else {
                                            Color.White.copy(alpha = 0.07f)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) BrandEmerald else Color.White.copy(alpha = 0.14f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                onDismiss()
                                                onSelectMenuItem(item)
                                            }
                                            .testTag("mobile_menu_item_${item.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                                tint = if (selected) BrandEmerald else BrandOceanBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!currentUserEmail.isNullOrBlank()) {
                        Text(
                            text = currentUserEmail,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onReplayWelcome()
                            },
                            border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mobile_menu_replay_welcome")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "স্বাগতম",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onShareAppLink()
                            },
                            border = BorderStroke(1.dp, BrandOceanBlue.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mobile_menu_share_link")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "লিংক শেয়ার",
                                tint = BrandOceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "শেয়ার",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onSignOut()
                            },
                            border = BorderStroke(1.dp, BrandSunsetOrange.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mobile_menu_signout")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = BrandSunsetOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সাইন-আউট",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarPillLink(
    item: TopMenuItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) BrandOceanBlue.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) BrandEmerald else Color.White.copy(alpha = 0.16f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .testTag("top_nav_link_${item.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (selected) BrandEmerald else BrandSunsetOrange,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
