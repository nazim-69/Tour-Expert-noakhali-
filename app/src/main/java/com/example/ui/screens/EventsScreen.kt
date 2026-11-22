package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AgencySettings
import com.example.data.model.EventFilterChip
import com.example.data.model.TourEvent
import com.example.ui.components.SeatPlanVisualizer
import com.example.ui.components.resolveTravelDrawable
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.BrandSunsetOrangeDark
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.DeepTealSurface
import kotlinx.coroutines.delay

fun filterTourEvents(events: List<TourEvent>, selectedFilter: EventFilterChip): List<TourEvent> =
    when (selectedFilter) {
        EventFilterChip.ALL -> events
        EventFilterChip.THIS_MONTH -> events.filter { it.monthTag == "THIS_MONTH" }
        EventFilterChip.NEXT_MONTH -> events.filter { it.monthTag == "NEXT_MONTH" }
        EventFilterChip.HILL -> events.filter { it.category == "HILL" }
        EventFilterChip.SEA -> events.filter { it.category == "SEA" || it.category == "FOREST" }
        EventFilterChip.BUDGET -> events.filter { it.category == "BUDGET" || it.price <= 2000 }
        EventFilterChip.FAMILY -> events.filter { it.category == "FAMILY" }
        EventFilterChip.STUDY -> events.filter { it.category == "STUDY" }
        EventFilterChip.CORPORATE -> events.filter { it.category == "CORPORATE" }
    }

private fun eventCategoryBadgeBn(event: TourEvent): String =
    when (event.category) {
        "HILL" -> "পাহাড় ট্যুর"
        "SEA" -> "নৌ ও ম্যানগ্রোভ"
        "BUDGET" -> "বাজেট ট্যুর"
        "FAMILY" -> "ফ্যামিলি ট্যুর"
        "STUDY" -> "শিক্ষা সফর"
        "CORPORATE" -> "কর্পোরেট ইভেন্ট"
        else -> "স্পেশাল ইভেন্ট"
    }

private fun eventStatusLabelBn(event: TourEvent): Pair<String, Color> =
    when {
        event.bookingStatus == "CLOSED" -> "বুকিং বন্ধ" to Color(0xFFEF4444)
        event.bookingStatus == "COMPLETED" -> "সম্পন্ন" to BrandOceanBlue
        event.availableSeatCount <= 0 -> "সিট পূর্ণ" to Color(0xFFEF4444)
        event.bookingStatus == "ALMOST_FULL" -> "সিট প্রায় শেষ" to BrandSunsetOrange
        else -> "বুকিং চলছে" to BrandEmerald
    }

@Composable
fun EventsScreen(
    events: List<TourEvent>,
    settings: AgencySettings = AgencySettings(),
    isAdmin: Boolean = false,
    onOpenAdminSchedule: () -> Unit = {},
    onOpenDetail: (String) -> Unit,
    onBookEvent: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(EventFilterChip.ALL) }

    val filteredEvents = remember(events, selectedFilter) {
        filterTourEvents(events, selectedFilter)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("monthly_event_schedule_grid"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DeepTealNight,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(BrandEmerald.copy(alpha = 0.6f), BrandOceanBlue.copy(alpha = 0.6f), BrandSunsetOrange.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(DeepTealNight, DeepTealSurface, Color(0xFF0E333F))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BrandEmerald.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.border(
                                    1.dp,
                                    BrandEmerald.copy(alpha = 0.5f),
                                    RoundedCornerShape(50.dp)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = settings.activeMonthTitle?.takeIf { it.isNotBlank() }
                                            ?: "চলতি মাসের ইভেন্টসমূহ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BrandEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = BrandSunsetOrange.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "${filteredEvents.size}টি ট্যুর",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandSunsetOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "মাসিক ইভেন্ট শিডিউল",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = settings.welcomeBannerText?.takeIf { it.isNotBlank() }
                                ?: "আসসালামুয়ালাইকুম! ট্যুর এক্সপার্ট নোয়াখালী আয়োজিত মাসিক ইভেন্ট শিডিউল থেকে পছন্দের ট্যুর বেছে বুকিং করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.84f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BrandOceanBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "এডমিন কর্তৃক প্রকাশিত অফিসিয়াল শিডিউল • দেখুন ও বুকিং করুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 2
                                )
                            }

                            if (isAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = onOpenAdminSchedule,
                                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("events_admin_manage_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "শিডিউল পরিবর্তন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips Bar
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EventFilterChip.entries) { chip ->
                    val selected = selectedFilter == chip
                    FilterChip(
                        selected = selected,
                        onClick = { selectedFilter = chip },
                        label = {
                            Text(
                                text = chip.labelBn,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOceanBlue,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = BrandOceanBlue.copy(alpha = 0.35f),
                            selectedBorderColor = BrandOceanBlue
                        ),
                        modifier = Modifier.testTag("filter_${chip.name}")
                    )
                }
            }
        }

        // Empty State or Grid Cards
        if (filteredEvents.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandOceanBlue.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = BrandOceanBlue,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "এই ক্যাটাগরিতে বর্তমানে কোনো ইভেন্ট নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "সকল চলমান ইভেন্ট দেখতে 'সব' ফিল্টার নির্বাচন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { selectedFilter = EventFilterChip.ALL },
                            border = BorderStroke(1.2.dp, BrandOceanBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("সব ইভেন্ট দেখুন", color = BrandOceanBlue)
                        }
                    }
                }
            }
        } else {
            itemsIndexed(filteredEvents, key = { _, event -> event.id }) { index, event ->
                var cardVisible by remember(event.id, selectedFilter) { mutableStateOf(false) }
                LaunchedEffect(event.id, selectedFilter) {
                    delay((index * 55L).coerceAtMost(240L))
                    cardVisible = true
                }

                AnimatedVisibility(
                    visible = cardVisible,
                    enter = fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(360, easing = FastOutSlowInEasing)) { it / 6 } +
                        scaleIn(tween(340), initialScale = 0.96f),
                    modifier = Modifier.animateItem()
                ) {
                    EventCatalogCard(
                        event = event,
                        onDetails = { onOpenDetail(event.id) },
                        onBook = { onBookEvent(event.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCatalogCard(
    event: TourEvent,
    onDetails: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "eventCardScale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        BrandEmerald.copy(alpha = 0.35f),
                        BrandOceanBlue.copy(alpha = 0.4f),
                        BrandSunsetOrange.copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("event_card_${event.id}")
    ) {
        Column {
            // Card Hero Image & Overlay Metadata
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onDetails
                    )
            ) {
                Image(
                    painter = painterResource(id = resolveTravelDrawable(event.imageKey)),
                    contentDescription = event.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DeepTealNight.copy(alpha = 0.18f),
                                    Color.Transparent,
                                    DeepTealNight.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Top-Start Category Pill
                Surface(
                    color = DeepTealNight.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .border(1.dp, BrandEmerald.copy(alpha = 0.55f), RoundedCornerShape(50.dp))
                ) {
                    Text(
                        text = eventCategoryBadgeBn(event),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }

                // Top-End Price Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandSunsetOrange, BrandSunsetOrangeDark)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "মাত্র ৳${event.price}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom-Start Destination & Event Name
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Surface(
                        color = BrandEmerald.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.border(
                            0.8.dp,
                            BrandEmerald.copy(alpha = 0.65f),
                            RoundedCornerShape(50.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "গন্তব্য",
                                tint = BrandEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.destination,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

                // Card Body
                val (statusLabel, statusColor) = eventStatusLabelBn(event)
                val canBook = (event.bookingStatus == "OPEN" || event.bookingStatus == "ALMOST_FULL") &&
                    event.availableSeatCount > 0

                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventMetaPill(
                            icon = Icons.Default.CalendarMonth,
                            text = event.tourDate,
                            tint = BrandOceanBlue
                        )
                        EventMetaPill(
                            icon = Icons.Default.Schedule,
                            text = event.duration,
                            tint = BrandSunsetOrange
                        )
                        EventMetaPill(
                            icon = Icons.Default.EventSeat,
                            text = if (event.availableSeatCount > 0) "${event.availableSeatCount} সিট খালি" else "সিট পূর্ণ",
                            tint = statusColor
                        )
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.border(0.8.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(50.dp))
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = event.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subtle 3-brand-color accent bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrandEmerald.copy(alpha = 0.45f),
                                        BrandOceanBlue.copy(alpha = 0.45f),
                                        BrandSunsetOrange.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDetails,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.3.dp, BrandOceanBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOceanBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_details_${event.id}")
                        ) {
                            Text(
                                text = "বিস্তারিত দেখুন",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onBook,
                            enabled = canBook,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandEmerald,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("event_book_${event.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (canBook) "বুকিং করুন" else statusLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun EventMetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.border(0.8.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EventDetailScreen(
    event: TourEvent,
    onBack: () -> Unit,
    onBookWithSeats: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = resolveTravelDrawable(event.imageKey)),
                    contentDescription = event.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(DeepTealNight.copy(alpha = 0.35f), DeepTealNight.copy(alpha = 0.88f)))
                        )
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepTealNight.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Surface(
                        color = BrandSunsetOrange,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "প্যাকেজ মূল্য: ৳${event.price} | অগ্রিম: ৳${event.advancePayment}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${event.destination} • ${event.tourDate} • ${event.duration}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandEmerald
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ইভেন্টের বিস্তারিত বিবরণ", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text("লাইভ সিট প্রাপ্যতা", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                SeatPlanVisualizer(
                    event = event,
                    selectedSeats = emptyList(),
                    maxSelectable = 0,
                    readOnly = true,
                    onToggleSeat = {}
                )

                Spacer(modifier = Modifier.height(18.dp))

                val (statusLabel, _) = eventStatusLabelBn(event)
                val canBook = (event.bookingStatus == "OPEN" || event.bookingStatus == "ALMOST_FULL") &&
                    event.availableSeatCount > 0

                Button(
                    onClick = { onBookWithSeats(event.id) },
                    enabled = canBook,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandEmerald,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("detail_book_now_button")
                ) {
                    Text(
                        text = if (canBook) {
                            "সিট সিলেক্ট ও বুকিং করুন (৳${event.price})"
                        } else {
                            "বর্তমানে বুকিং সম্ভব নয় ($statusLabel)"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
