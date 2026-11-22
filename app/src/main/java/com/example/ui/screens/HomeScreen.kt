package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AgencySettings
import com.example.data.model.BrandConstants
import com.example.data.model.DestinationPlace
import com.example.data.model.EventFilterChip
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.data.model.TourReview
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.ContactAndFooterSection
import com.example.ui.components.GlassmorphicAdminTeamSection
import com.example.ui.components.TourReviewsSection
import com.example.ui.components.resolveTravelDrawable
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.viewmodel.LookupState
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    events: List<TourEvent>,
    destinations: List<DestinationPlace>,
    teamMembers: List<TeamMember>,
    settings: AgencySettings,
    lookupState: LookupState,
    reviewsState: UiState<List<TourReview>>,
    isSubmittingReview: Boolean,
    currentUserId: String,
    currentUserDisplayName: String?,
    isAdmin: Boolean,
    onLookupReference: (String) -> Unit,
    onOpenBookingDetails: (String) -> Unit,
    onSubmitReview: (String, String?, String, Int, String, () -> Unit) -> Unit,
    onDeleteReview: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToDestinations: () -> Unit,
    onNavigateToSeatPlan: (String) -> Unit,
    onNavigateToBooking: (String?) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToTeamAndContact: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var refInput by remember { mutableStateOf("") }
    var homeEventFilter by remember { mutableStateOf(EventFilterChip.ALL) }
    val filteredHomeEvents = remember(events, homeEventFilter) {
        filterTourEvents(events, homeEventFilter)
    }

    val floatAnim = rememberInfiniteTransition(label = "heroFloat")
    val floatingOffset by floatAnim.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Parallax Hero Section
        item {
            val parallaxOffset = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset * 0.42f
            } else {
                0f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_bangladesh),
                    contentDescription = BrandConstants.ORGANIZATION_NAME,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = parallaxOffset
                            scaleX = 1.08f
                            scaleY = 1.08f
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DeepTealNight.copy(alpha = 0.45f),
                                    DeepTealNight.copy(alpha = 0.72f),
                                    DeepTealNight.copy(alpha = 0.94f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Floating Welcome Pill
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .graphicsLayer { translationY = floatingOffset }
                            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(50.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নোয়াখালীর বিশ্বস্ত ট্রাভেল অর্গানাইজেশন",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    Column {
                        Text(
                            text = BrandConstants.ORGANIZATION_NAME,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = BrandConstants.TAGLINE,
                            style = MaterialTheme.typography.headlineSmall,
                            color = BrandSunsetOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = settings.welcomeBannerText
                                ?: "আসসালামুয়ালাইকুম! নিরাপদ, সাশ্রয়ী ও স্মরণীয় ভ্রমণের জন্য আমাদের সাথে যুক্ত হোন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.86f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToEvents,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("hero_events_button")
                            ) {
                                Text("ইভেন্ট দেখুন", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = { onNavigateToBooking(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("hero_book_button")
                            ) {
                                Text("বুকিং করুন", style = MaterialTheme.typography.labelLarge)
                            }

                            OutlinedButton(
                                onClick = onNavigateToTeamAndContact,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("hero_contact_button")
                            ) {
                                Text("যোগাযোগ করুন", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Reference Status Checker
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = BrandOceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বুকিং স্ট্যাটাস চেক (রেফারেন্স নম্বর)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = refInput,
                            onValueChange = { refInput = it },
                            placeholder = { Text("উদাঃ TEN-2026-0001") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_reference_input")
                        )
                        Button(
                            onClick = { onLookupReference(refInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("home_reference_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "খুঁজুন")
                        }
                    }

                    when (lookupState) {
                        LookupState.Searching -> {
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("খোঁজা হচ্ছে...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is LookupState.Found -> {
                            val item = lookupState.status
                            Surface(
                                color = BrandOceanBlue.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.referenceNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        BookingStatusBadge(status = item.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.eventName} (${item.tourDate})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "সিট: ${item.seatLabels} (${item.seatCount} জন)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.userId == currentUserId || isAdmin) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                onOpenBookingDetails(item.bookingId.ifBlank { item.referenceNumber })
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("home_lookup_open_details")
                                        ) {
                                            Text("সম্পূর্ণ বুকিং ডিটেইলস ও প্রিন্ট ভাউচার দেখুন")
                                        }
                                    }
                                }
                            }
                        }
                        LookupState.NotFound -> {
                            Text(
                                text = "এই রেফারেন্স নম্বরে কোনো বুকিং পাওয়া যায়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandSunsetOrange,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        is LookupState.Error -> {
                            Text(
                                text = lookupState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        LookupState.Idle -> Unit
                    }
                }
            }
        }

        // 3. Monthly Event Schedule Section
        item {
            SectionHeaderRow(
                title = "মাসিক ইভেন্ট শিডিউল",
                subtitle = settings.activeMonthTitle?.takeIf { it.isNotBlank() }
                    ?: "এডমিন কর্তৃক প্রকাশিত স্পেশাল ট্যুর প্যাকেজসমূহ",
                actionLabel = "সব ইভেন্ট",
                onAction = onNavigateToEvents
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        EventFilterChip.ALL,
                        EventFilterChip.THIS_MONTH,
                        EventFilterChip.HILL,
                        EventFilterChip.SEA,
                        EventFilterChip.BUDGET,
                        EventFilterChip.FAMILY
                    )
                ) { chip ->
                    val selected = homeEventFilter == chip
                    FilterChip(
                        selected = selected,
                        onClick = { homeEventFilter = chip },
                        label = { Text(chip.labelBn) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOceanBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("home_filter_${chip.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredHomeEvents.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "এই ক্যাটাগরিতে কোনো ইভেন্ট নেই। অনুগ্রহ করে 'সব' নির্বাচন করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val columns = if (maxWidth >= 640.dp) 2 else 1
                    val rows = filteredHomeEvents.chunked(columns)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        rows.forEach { rowEvents ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowEvents.forEach { event ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        EventCatalogCard(
                                            event = event,
                                            onDetails = { onNavigateToEventDetail(event.id) },
                                            onBook = { onNavigateToSeatPlan(event.id) }
                                        )
                                    }
                                }
                                if (rowEvents.size < columns) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Popular Destinations Preview
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeaderRow(
                title = "জনপ্রিয় পর্যটন স্থান",
                subtitle = "বাংলাদেশের সেরা ভ্রমণ গন্তব্যসমূহ",
                actionLabel = "সব গন্তব্য",
                onAction = onNavigateToDestinations
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(destinations.take(6), key = { it.id }) { place ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .width(190.dp)
                            .height(210.dp)
                            .clickable(onClick = onNavigateToDestinations)
                    ) {
                        Box {
                            Image(
                                painter = painterResource(id = resolveTravelDrawable(place.imageKey)),
                                contentDescription = place.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, DeepTealNight.copy(alpha = 0.88f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = place.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = place.division,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandEmerald
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. About Us ("আমাদের সম্পর্কে")
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, BrandEmerald.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BrandEmerald.copy(alpha = 0.14f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "আমাদের সম্পর্কে",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = BrandConstants.TAGLINE,
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandSunsetOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${BrandConstants.ORGANIZATION_NAME} একটি বিশ্বস্ত ও পেশাদার ভ্রমণ আয়োজক প্রতিষ্ঠান। আমরা নোয়াখালী ও সারা দেশের ভ্রমণপ্রেমীদের জন্য নিয়মিত মাসিক ইভেন্ট, বাজেট ট্যুর, ফ্যামিলি ট্রিপ, শিক্ষা সফর এবং কর্পোরেট ট্যুর পরিচালনা করি। অনলাইনে সিট পছন্দ করে সহজ ও নিরাপদ বুকিং অভিজ্ঞতা দেওয়াই আমাদের মূল লক্ষ্য।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Dedicated Glassmorphic "আমাদের এডমিন" Section (08 Admins)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            GlassmorphicAdminTeamSection(
                teamMembers = teamMembers,
                settings = settings,
                onOpenAdminPortal = { onNavigateToRoute("admin") }
            )
        }

        // 7. Tourist Experience Review & Star Rating ("আমাদের সাথে ট্যুর করে আপনার কেমন লেগেছে?")
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TourReviewsSection(
                reviewsState = reviewsState,
                events = events,
                currentUserId = currentUserId,
                currentUserDisplayName = currentUserDisplayName,
                isAdmin = isAdmin,
                isSubmitting = isSubmittingReview,
                onSubmitReview = onSubmitReview,
                onDeleteReview = onDeleteReview
            )
        }

        // 8. Contact & Official Footer
        item {
            Spacer(modifier = Modifier.height(14.dp))
            ContactAndFooterSection(
                settings = settings,
                context = context,
                onBookNow = { onNavigateToBooking(null) },
                onNavigateQuickLink = onNavigateToRoute,
                onFeedback = onFeedback
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onAction) {
            Text(actionLabel, color = BrandOceanBlue, style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = BrandOceanBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

