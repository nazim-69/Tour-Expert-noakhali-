package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.AgencySettings
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import com.example.data.model.DestinationPlace
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.ui.components.BookingDocumentPrinter
import com.example.ui.components.BookingDocumentSlip
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.SeatPlanVisualizer
import com.example.ui.components.SharePublicLinkCard
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminPortalScreen(
    hasFirestoreAdminPrivilege: Boolean,
    isBootstrapSuperAdmin: Boolean,
    currentUserEmail: String?,
    activeAdminAccount: AdminAccount?,
    adminAccounts: List<AdminAccount>,
    teamMembers: List<TeamMember>,
    events: List<TourEvent>,
    destinations: List<DestinationPlace>,
    allBookings: List<BookingRecord>,
    settings: AgencySettings,
    cloudEventsEmpty: Boolean,
    onLoginIndividualAdmin: (String, String) -> Unit,
    onLogoutIndividualAdmin: () -> Unit,
    onTriggerSeed: () -> Unit,
    onVerifyBookingStatus: (BookingRecord, String) -> Unit,
    onSaveEvent: (TourEvent, Boolean) -> Unit,
    onUpdateEventBookingStatus: (TourEvent, String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onSaveDestination: (DestinationPlace, Boolean) -> Unit,
    onDeleteDestination: (String) -> Unit,
    onUpdateAdminAccount: (AdminAccount, String?, Boolean) -> Unit,
    onUpdateAgencySettings: (String?, String?, String?, String?, String?) -> Unit,
    onOpenBookingDetails: (String) -> Unit,
    onMarkMemoSent: (String) -> Unit,
    initialTab: Int = 0,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    if (!hasFirestoreAdminPrivilege) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandSunsetOrange.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BrandSunsetOrange,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "এডমিন ড্যাশবোর্ড সুরক্ষিত",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার বর্তমান অ্যাকাউন্ট (${currentUserEmail ?: "অতিথি"}) এডমিন হিসেবে অনুমোদিত নয়। অনুমোদিত ০৮ জন এডমিনের মধ্যে কেউ অনুগ্রহ করে অনুমোদিত Google অ্যাকাউন্ট দিয়ে সাইন-ইন করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var adminMonthFilter by remember { mutableStateOf("ALL") }

    var showIndividualLoginSheet by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<TourEvent?>(null) }
    var creatingEvent by remember { mutableStateOf(false) }
    var inspectingSeatEvent by remember { mutableStateOf<TourEvent?>(null) }
    var editingAdmin by remember { mutableStateOf<AdminAccount?>(null) }
    var editingDestination by remember { mutableStateOf<DestinationPlace?>(null) }
    var creatingDestination by remember { mutableStateOf(false) }
    var previewBooking by remember { mutableStateOf<BookingRecord?>(null) }

    var fbUrlInput by remember(settings.facebookUrl) { mutableStateOf(settings.facebookUrl ?: "") }
    var waUrlInput by remember(settings.whatsappUrl) { mutableStateOf(settings.whatsappUrl ?: "") }
    var websiteUrlInput by remember(settings.websiteUrl) { mutableStateOf(settings.resolvedWebsiteUrl()) }
    var bannerInput by remember(settings.welcomeBannerText) { mutableStateOf(settings.welcomeBannerText ?: "") }
    var activeMonthTitleInput by remember(settings.activeMonthTitle) {
        mutableStateOf(settings.activeMonthTitle ?: "অক্টোবর মাসের ইভেন্টসমূহ")
    }

    val isSuper = isBootstrapSuperAdmin || activeAdminAccount?.role == "SUPER_ADMIN"
    val canEvents = isSuper || (activeAdminAccount?.canManageEvents == true)
    val canVerify = isSuper || (activeAdminAccount?.canVerifyPayments == true)
    val canDestinations = isSuper || (activeAdminAccount?.canManageDestinations == true)
    val canSettings = isSuper || (activeAdminAccount?.canManageSettings == true)

    Column(modifier = Modifier.fillMaxSize()) {
        // Active Admin Role Header
        Surface(
            color = BrandOceanBlue.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandOceanBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeAdminAccount?.name ?: "সুপার এডমিন",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeAdminAccount?.role ?: "SUPER_ADMIN"} • ${activeAdminAccount?.designation ?: "পূর্ণ নিয়ন্ত্রণ"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandEmerald
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showIndividualLoginSheet = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("এডমিন সুইচ", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("ওভারভিউ") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("বুকিং ও রেফারেন্স (${allBookings.size})") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("ইভেন্ট ও সিট প্লান") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("এডমিন (০৮)") })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("গন্তব্য ও সেটিংস") })
        }

        when (selectedTab) {
            // TAB 0: OVERVIEW
            0 -> {
                val totalSeats = events.sumOf { it.totalSeats }
                val bookedSeats = events.sumOf { it.bookedSeats.size }
                val availableSeats = events.sumOf { it.availableSeatCount }
                val pendingCount = allBookings.count { it.status == "Payment Verification Pending" || it.status == "Pending" }
                val confirmedCount = allBookings.count { it.status == "Confirmed" || it.status == "Completed" }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (cloudEventsEmpty) {
                        item {
                            Button(
                                onClick = onTriggerSeed,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ক্লাউড ডেটাবেসে ডিফল্ট ইভেন্ট ও ৮ এডমিন সিঙ্ক করুন")
                            }
                        }
                    }

                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatTile("মোট ইভেন্ট", "${events.size}", BrandOceanBlue, Modifier.fillMaxWidth(0.48f))
                            StatTile("মোট বুকিং", "${allBookings.size}", BrandEmerald, Modifier.fillMaxWidth(0.48f))
                            StatTile("কনফার্মড বুকিং", "$confirmedCount", BrandEmerald, Modifier.fillMaxWidth(0.48f))
                            StatTile("পেমেন্ট যাচাই বাকি", "$pendingCount", BrandSunsetOrange, Modifier.fillMaxWidth(0.48f))
                            StatTile("খালি সিট", "$availableSeats", BrandOceanBlue, Modifier.fillMaxWidth(0.48f))
                            StatTile("বুকড সিট", "$bookedSeats / $totalSeats", BrandSunsetOrange, Modifier.fillMaxWidth(0.48f))
                        }
                    }

                    item {
                        SharePublicLinkCard(
                            settings = settings,
                            context = context,
                            onFeedback = onFeedback
                        )
                    }

                    item {
                        AdminAnalyticsChartCard(bookings = allBookings, events = events)
                    }
                }
            }

            // TAB 1: BOOKINGS, PAYMENTS & REFERENCES SEARCH
            1 -> {
                val filteredBookings = remember(allBookings, searchQuery, statusFilter) {
                    allBookings.filter { b ->
                        val matchesStatus = when (statusFilter) {
                            "PENDING" -> b.status == "Payment Verification Pending" || b.status == "Pending"
                            "CONFIRMED" -> b.status == "Confirmed" || b.status == "Completed"
                            "CANCELLED" -> b.status == "Cancelled"
                            else -> true
                        }
                        val q = searchQuery.trim().lowercase()
                        val matchesQuery = q.isEmpty() ||
                            b.id.lowercase().contains(q) ||
                            b.referenceNumber.lowercase().contains(q) ||
                            b.touristName.lowercase().contains(q) ||
                            b.mobileNumber.lowercase().contains(q) ||
                            b.eventName.lowercase().contains(q) ||
                            b.selectedSeats.any { it.lowercase().contains(q) }
                        matchesStatus && matchesQuery
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("খুঁজুন: Reference, নাম, মোবাইল, ইভেন্ট বা সিট...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_booking_search")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ALL" to "সব", "PENDING" to "যাচাই বাকি", "CONFIRMED" to "কনফার্মড", "CANCELLED" to "বাতিল").forEach { (key, label) ->
                                FilterChip(
                                    selected = statusFilter == key,
                                    onClick = { statusFilter = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    items(filteredBookings, key = { it.id }) { booking ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = booking.referenceNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = BrandOceanBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    BookingStatusBadge(booking.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${booking.touristName} (${booking.mobileNumber})", style = MaterialTheme.typography.titleSmall)
                                if (booking.touristEmail.isNotBlank()) {
                                    Text(
                                        text = "Gmail: ${booking.touristEmail}${if (booking.confirmationMemoSent) " ✓ মেমো পাঠানো হয়েছে" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandOceanBlue
                                    )
                                }
                                Text("${booking.eventName} • সিট: ${booking.selectedSeats.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "পেমেন্ট: ৳${booking.paidAmount} (${booking.paymentMethod}) • TrxID: ${booking.transactionId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandSunsetOrange
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (canVerify && booking.status != "Confirmed") {
                                        Button(
                                            onClick = { onVerifyBookingStatus(booking, "Confirmed") },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("কনফার্ম")
                                        }
                                    }
                                    if (canVerify && booking.status != "Cancelled") {
                                        OutlinedButton(
                                            onClick = { onVerifyBookingStatus(booking, "Cancelled") },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("বাতিল", color = Color(0xFFEF4444))
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            BookingDocumentPrinter.sendConfirmationMemoToGmail(context, booking)
                                                .onSuccess {
                                                    onMarkMemoSent(booking.id.ifBlank { booking.referenceNumber })
                                                }
                                                .onFailure { err ->
                                                    onFeedback("জিমেইল খুলতে সমস্যা হয়েছে: ${err.localizedMessage}")
                                                }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("জিমেইল মেমো")
                                    }
                                    OutlinedButton(
                                        onClick = { onOpenBookingDetails(booking.id.ifBlank { booking.referenceNumber }) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("বিস্তারিত / প্রিন্ট")
                                    }
                                    OutlinedButton(
                                        onClick = { previewBooking = booking },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("স্লিপ / PDF")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: EVENTS & MONTHLY SCHEDULE CONTROL (ADMIN-ONLY)
            2 -> {
                val monthEvents = remember(events, adminMonthFilter) {
                    if (adminMonthFilter == "ALL") events else events.filter { it.monthTag == adminMonthFilter }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BrandOceanBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "প্রতিমাসের ইভেন্ট শিডিউল নিয়ন্ত্রণ (শুধুমাত্র এডমিন)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "এখান থেকে আপনি ইভেন্ট যোগ, তারিখ/মূল্য পরিবর্তন এবং বুকিং চালু বা বন্ধ করতে পারবেন। সকল ভ্রমণকারী (Viewers) লাইভ এই শিডিউল দেখতে ও বুকিং করতে পারবেন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (canEvents || canSettings) {
                                    OutlinedTextField(
                                        value = activeMonthTitleInput,
                                        onValueChange = { activeMonthTitleInput = it },
                                        label = { Text("সক্রিয় মাসের শিরোনাম (যেমন: নভেম্বর মাসের ইভেন্টসমূহ)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_active_month_title_input")
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateAgencySettings(
                                                    fbUrlInput.ifBlank { null },
                                                    waUrlInput.ifBlank { null },
                                                    bannerInput.ifBlank { null },
                                                    activeMonthTitleInput.ifBlank { null },
                                                    websiteUrlInput.ifBlank { BrandConstants.DEFAULT_WEB_APP_URL }
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("মাসের শিরোনাম আপডেট")
                                        }
                                        if (canEvents) {
                                            Button(
                                                onClick = { creatingEvent = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_add_event_button")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("নতুন ইভেন্ট")
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = BrandSunsetOrange.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = BrandSunsetOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "আপনার এডমিন অ্যাকাউন্টে ইভেন্ট শিডিউল পরিবর্তনের অনুমতি নেই।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BrandSunsetOrange
                                            )
                                        }
                                    }
                                }

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        "ALL" to "সব মাস",
                                        "THIS_MONTH" to "এই মাস",
                                        "NEXT_MONTH" to "পরবর্তী মাস",
                                        "UPCOMING" to "আসন্ন"
                                    ).forEach { (key, label) ->
                                        FilterChip(
                                            selected = adminMonthFilter == key,
                                            onClick = { adminMonthFilter = key },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(monthEvents, key = { it.id }) { ev ->
                        val monthLabel = when (ev.monthTag) {
                            "THIS_MONTH" -> "এই মাস"
                            "NEXT_MONTH" -> "পরবর্তী মাস"
                            else -> "আসন্ন"
                        }
                        val isOpen = ev.bookingStatus == "OPEN" || ev.bookingStatus == "ALMOST_FULL"

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ev.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${ev.tourDate} • $monthLabel • ৳${ev.price} • খালি: ${ev.availableSeatCount}/${ev.totalSeats}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandOceanBlue
                                        )
                                        Text(
                                            text = "স্ট্যাটাস: ${ev.bookingStatus}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOpen) BrandEmerald else BrandSunsetOrange
                                        )
                                    }
                                    if (canEvents) {
                                        Row {
                                            IconButton(onClick = { editingEvent = ev }) {
                                                Icon(Icons.Default.Edit, contentDescription = "এডিট")
                                            }
                                            IconButton(onClick = { onDeleteEvent(ev.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { inspectingSeatEvent = ev },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("সিট প্লান (${ev.vehicleLayout})", maxLines = 1)
                                    }
                                    if (canEvents) {
                                        OutlinedButton(
                                            onClick = {
                                                val nextStatus = if (isOpen) "CLOSED" else "OPEN"
                                                onUpdateEventBookingStatus(ev, nextStatus)
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = if (isOpen) "বুকিং বন্ধ করুন" else "বুকিং চালু করুন",
                                                color = if (isOpen) BrandSunsetOrange else BrandEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: 08 INDIVIDUAL ADMINS & RBAC
            3 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "০৮ জন এডমিন অ্যাকাউন্ট ও রোল ম্যানেজমেন্ট",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "প্রতিটি এডমিনের আলাদা পাসওয়ার্ড (PBKDF2 হ্যাশড), মোবাইল, হোয়াটসঅ্যাপ, ফেসবুক এবং পারমিশন কনফিগার করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(adminAccounts, key = { it.id }) { acc ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${acc.sortOrder}. ${acc.name}", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${acc.designation} • @${acc.username} (${acc.role})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandOceanBlue
                                    )
                                    Text(
                                        text = if (acc.active) "সক্রিয়" else "নিষ্ক্রিয়",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (acc.active) BrandEmerald else Color(0xFFEF4444)
                                    )
                                }
                                if (isSuper) {
                                    Button(
                                        onClick = { editingAdmin = acc },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("ম্যানেজ")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: DESTINATIONS & WEBSITE/APP SETTINGS
            4 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (canSettings) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("অফিসিয়াল সেটিংস", style = MaterialTheme.typography.titleMedium)
                                    OutlinedTextField(
                                        value = fbUrlInput,
                                        onValueChange = { fbUrlInput = it },
                                        label = { Text("Facebook Page URL") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = waUrlInput,
                                        onValueChange = { waUrlInput = it },
                                        label = { Text("WhatsApp URL") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = websiteUrlInput,
                                        onValueChange = { websiteUrlInput = it },
                                        label = { Text("পাবলিক ওয়েবসাইট / অ্যাপ শেয়ার লিংক") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_website_url_input")
                                    )
                                    OutlinedTextField(
                                        value = bannerInput,
                                        onValueChange = { bannerInput = it },
                                        label = { Text("হোমপেজ ঘোষণা বার্তা") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onUpdateAgencySettings(
                                                fbUrlInput.ifBlank { null },
                                                waUrlInput.ifBlank { null },
                                                bannerInput.ifBlank { null },
                                                activeMonthTitleInput.ifBlank { null },
                                                websiteUrlInput.ifBlank { BrandConstants.DEFAULT_WEB_APP_URL }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("সেটিংস সংরক্ষণ করুন")
                                    }
                                }
                            }
                        }
                    }

                    if (canDestinations) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("পর্যটন স্থান সমূহ (${destinations.size})", style = MaterialTheme.typography.titleMedium)
                                Button(
                                    onClick = { creatingDestination = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("নতুন যোগ")
                                }
                            }
                        }

                        items(destinations, key = { it.id }) { dest ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(dest.name, style = MaterialTheme.typography.titleSmall)
                                        Text(dest.division, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row {
                                        IconButton(onClick = { editingDestination = dest }) {
                                            Icon(Icons.Default.Edit, contentDescription = "এডিট")
                                        }
                                        IconButton(onClick = { onDeleteDestination(dest.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Individual Admin Login / Switch Modal
    if (showIndividualLoginSheet) {
        var usernameInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { showIndividualLoginSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ব্যক্তিগত এডমিন অ্যাকাউন্ট লগইন", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "৮ জন এডমিনের যে কেউ নিজের ইউজারনেম ও পাসওয়ার্ড (PBKDF2 ভেরিফায়েড) দিয়ে লগইন করতে পারবেন।",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("ইউজারনেম (যেমন: abdulhakim, nazimuddin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("পাসওয়ার্ড") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onLoginIndividualAdmin(usernameInput, passwordInput)
                        showIndividualLoginSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("লগইন করুন")
                }
                OutlinedButton(
                    onClick = {
                        onLogoutIndividualAdmin()
                        showIndividualLoginSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ডিফল্ট সুপার এডমিন সেশনে ফিরুন")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (creatingEvent || editingEvent != null) {
        EventEditorDialog(
            existing = editingEvent,
            onDismiss = {
                creatingEvent = false
                editingEvent = null
            },
            onSave = { ev, isCreate ->
                onSaveEvent(ev, isCreate)
                creatingEvent = false
                editingEvent = null
            }
        )
    }

    inspectingSeatEvent?.let { ev ->
        ModalBottomSheet(onDismissRequest = { inspectingSeatEvent = null }) {
            Box(modifier = Modifier.padding(16.dp)) {
                SeatPlanVisualizer(
                    event = ev,
                    selectedSeats = emptyList(),
                    maxSelectable = 0,
                    readOnly = true,
                    onToggleSeat = {}
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    editingAdmin?.let { acc ->
        AdminAccountEditorDialog(
            account = acc,
            teamMember = teamMembers.firstOrNull { it.id == acc.id },
            onDismiss = { editingAdmin = null },
            onSave = { updated, passcode, showPublic ->
                onUpdateAdminAccount(updated, passcode, showPublic)
                editingAdmin = null
            }
        )
    }

    if (creatingDestination || editingDestination != null) {
        DestinationEditorDialog(
            existing = editingDestination,
            onDismiss = {
                creatingDestination = false
                editingDestination = null
            },
            onSave = { dest, isCreate ->
                onSaveDestination(dest, isCreate)
                creatingDestination = false
                editingDestination = null
            }
        )
    }

    previewBooking?.let { slip ->
        ModalBottomSheet(onDismissRequest = { previewBooking = null }) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                BookingDocumentSlip(booking = slip, onFeedback = onFeedback)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
