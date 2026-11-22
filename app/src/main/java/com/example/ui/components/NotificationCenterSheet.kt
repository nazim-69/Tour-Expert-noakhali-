package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingRecord
import com.example.data.model.TourEvent
import com.example.notifications.LocalNotificationItem
import com.example.notifications.NotificationPreferencesState
import com.example.notifications.TourNotificationManager
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationCenterSheet(
    events: List<TourEvent>,
    myBookings: List<BookingRecord>,
    onDismiss: () -> Unit,
    onOpenBookingDetails: (String) -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    var prefsState by remember {
        mutableStateOf(TourNotificationManager.loadPreferencesState(context))
    }
    var permissionGranted by remember {
        mutableStateOf(TourNotificationManager.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionGranted = granted
            if (granted) {
                TourNotificationManager.ensureChannels(context)
                onFeedback("নোটিফিকেশন অনুমতি সফলভাবে চালু হয়েছে!")
            } else {
                onFeedback("সিস্টেম নোটিফিকেশন বন্ধ রয়েছে — তবে ইন-অ্যাপ অ্যালার্ট দেখতে পাবেন।")
            }
        }
    )

    val refreshPrefs = {
        prefsState = TourNotificationManager.loadPreferencesState(context)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .testTag("notification_center_sheet"),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandOceanBlue.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = BrandOceanBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "নোটিফিকেশন ও রিমাইন্ডার সেন্টার",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ট্যুর তারিখ, বিশেষ অফার ও বুকিং কনফার্মেশন আপডেট",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Surface(
                        color = BrandSunsetOrange.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandSunsetOrange.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "রিমাইন্ডার ও বুকিং অ্যালার্ট পেতে নোটিফিকেশন অনুমতি দিন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("request_notification_permission_button")
                            ) {
                                Text("নোটিফিকেশন চালু করুন")
                            }
                        }
                    }
                }
            }

            // Category Toggles Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        NotificationToggleRow(
                            icon = Icons.Default.Alarm,
                            accentColor = BrandOceanBlue,
                            title = "আসন্ন ট্যুর তারিখ রিমাইন্ডার",
                            subtitle = "ট্যুরের ১ দিন আগে সকাল ৯টায় রিমাইন্ডার",
                            checked = prefsState.tourRemindersEnabled,
                            onCheckedChange = { checked ->
                                prefsState = TourNotificationManager.setCategoryEnabled(
                                    context,
                                    TourNotificationManager.CATEGORY_TOUR_REMINDER,
                                    checked
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        NotificationToggleRow(
                            icon = Icons.Default.Campaign,
                            accentColor = BrandSunsetOrange,
                            title = "বিশেষ অফার ও ডিসকাউন্ট অ্যালার্ট",
                            subtitle = "নতুন বাজেট ও স্পেশাল ট্যুর প্যাকেজ ঘোষণা",
                            checked = prefsState.specialOffersEnabled,
                            onCheckedChange = { checked ->
                                prefsState = TourNotificationManager.setCategoryEnabled(
                                    context,
                                    TourNotificationManager.CATEGORY_SPECIAL_OFFER,
                                    checked
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        NotificationToggleRow(
                            icon = Icons.Default.ConfirmationNumber,
                            accentColor = BrandEmerald,
                            title = "বুকিং কনফার্মেশন ও স্ট্যাটাস আপডেট",
                            subtitle = "বুকিং যাচাই, কনফার্ম ও জিমেইল মেমো আপডেট",
                            checked = prefsState.bookingUpdatesEnabled,
                            onCheckedChange = { checked ->
                                prefsState = TourNotificationManager.setCategoryEnabled(
                                    context,
                                    TourNotificationManager.CATEGORY_BOOKING_UPDATE,
                                    checked
                                )
                            }
                        )
                    }
                }
            }

            // Quick Notification Simulator / Immediate Preview Strip
            item {
                Text(
                    text = "তাৎক্ষণিক রিমাইন্ডার ও অফার প্রিভিউ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sampleEvent = events.firstOrNull()
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (sampleEvent != null) {
                                TourNotificationManager.scheduleTourDateReminder(
                                    context = context,
                                    event = sampleEvent,
                                    customTriggerAtMillis = System.currentTimeMillis() + 5_000L
                                )
                                refreshPrefs()
                                onFeedback("${sampleEvent.name} এর জন্য ৫ সেকেন্ডের মধ্যে রিমাইন্ডার আসবে!")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_tour_reminder_button")
                    ) {
                        Text("ট্যুর রিমাইন্ডার (৫ সে.)")
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            val offerEv = events.firstOrNull { it.featured } ?: sampleEvent
                            TourNotificationManager.showSpecialOfferNotification(
                                context = context,
                                title = "অক্টোবর স্পেশাল অফার!",
                                message = if (offerEv != null) {
                                    "${offerEv.name} (${offerEv.tourDate}) মাত্র ৳${offerEv.price} — এখনই সিট বুক করুন!"
                                } else {
                                    "নোয়াখালী থেকে রাঙামাটি, সুন্দরবন ও বান্দরবান ভ্রমণে বিশেষ বাজেট প্যাকেজ চলছে!"
                                },
                                eventId = offerEv?.id
                            )
                            refreshPrefs()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_special_offer_button")
                    ) {
                        Text("বিশেষ অফার অ্যালার্ট")
                    }

                    val latestBooking = myBookings.firstOrNull()
                    if (latestBooking != null) {
                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                TourNotificationManager.showBookingUpdateNotification(
                                    context = context,
                                    booking = latestBooking,
                                    isNewSubmission = false
                                )
                                refreshPrefs()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("test_booking_update_button")
                        ) {
                            Text("বুকিং আপডেট অ্যালার্ট")
                        }
                    }
                }
            }

            // Upcoming Tour Event Reminders Scheduler
            if (events.isNotEmpty()) {
                item {
                    Text(
                        text = "আসন্ন ট্যুর তারিখ রিমাইন্ডার সেট করুন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(events.take(6), key = { it.id }) { ev ->
                    val isScheduled = prefsState.scheduledReminders.any { it.eventId == ev.id }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isScheduled) BrandEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isScheduled) BrandEmerald.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${ev.tourDate} • ${ev.destination} • ৳${ev.price}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandOceanBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (isScheduled) {
                                        TourNotificationManager.cancelTourDateReminder(context, ev.id)
                                        onFeedback("${ev.name} রিমাইন্ডার বন্ধ করা হয়েছে।")
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        TourNotificationManager.scheduleTourDateReminder(context, ev)
                                        onFeedback("${ev.tourDate} তারিখের ট্যুর রিমাইন্ডার সেট হয়েছে!")
                                    }
                                    refreshPrefs()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScheduled) Icons.Default.AlarmOn else Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (isScheduled) BrandEmerald else BrandOceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isScheduled) "চালু আছে" else "সেট করুন",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isScheduled) BrandEmerald else BrandOceanBlue
                                )
                            }
                        }
                    }
                }
            }

            // Notification History Feed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক নোটিফিকেশন (${prefsState.history.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (prefsState.history.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                prefsState = TourNotificationManager.clearHistory(context)
                            }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সব মুছুন")
                        }
                    }
                }
            }

            if (prefsState.history.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "এখনো কোনো নোটিফিকেশন আসেনি। ট্যুর রিমাইন্ডার চালু করুন অথবা উপরের বাটনে প্রিভিউ দেখুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(prefsState.history, key = { it.id }) { item ->
                    NotificationHistoryCard(
                        item = item,
                        onClick = {
                            if (!item.bookingId.isNullOrBlank()) {
                                onDismiss()
                                onOpenBookingDetails(item.bookingId)
                            } else if (!item.eventId.isNullOrBlank()) {
                                onDismiss()
                                onOpenEventDetail(item.eventId)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f))
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
        )
    }
}

@Composable
private fun NotificationHistoryCard(
    item: LocalNotificationItem,
    onClick: () -> Unit
) {
    val badgeColor = when (item.category) {
        TourNotificationManager.CATEGORY_BOOKING_UPDATE -> BrandEmerald
        TourNotificationManager.CATEGORY_SPECIAL_OFFER -> BrandSunsetOrange
        else -> BrandOceanBlue
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepTealNight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(item.timestampMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
