package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.BookingRecord
import com.example.data.model.DestinationPlace
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.data.model.VehicleLayoutType
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange

@Composable
fun AdminAnalyticsChartCard(
    bookings: List<BookingRecord>,
    events: List<TourEvent>
) {
    val confirmed = bookings.count { it.status == "Confirmed" || it.status == "Completed" }
    val pending = bookings.count { it.status == "Payment Verification Pending" || it.status == "Pending" }
    val cancelled = bookings.count { it.status == "Cancelled" }
    val total = (confirmed + pending + cancelled).coerceAtLeast(1)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("বুকিং ও সিট অ্যানালিটিক্স", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(106.dp)) {
                        val stroke = Stroke(width = 20f, cap = StrokeCap.Round)
                        val confirmedSweep = 360f * (confirmed.toFloat() / total)
                        val pendingSweep = 360f * (pending.toFloat() / total)
                        val cancelledSweep = 360f * (cancelled.toFloat() / total)

                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                        var start = -90f
                        if (confirmedSweep > 0f) {
                            drawArc(BrandEmerald, start, confirmedSweep, false, style = stroke)
                            start += confirmedSweep
                        }
                        if (pendingSweep > 0f) {
                            drawArc(BrandSunsetOrange, start, pendingSweep, false, style = stroke)
                            start += pendingSweep
                        }
                        if (cancelledSweep > 0f) {
                            drawArc(Color(0xFFEF4444), start, cancelledSweep, false, style = stroke)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${bookings.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("মোট বুকিং", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChartLegendRow("কনফার্মড / সম্পন্ন", confirmed, BrandEmerald)
                    ChartLegendRow("পেমেন্ট যাচাই বাকি", pending, BrandSunsetOrange)
                    ChartLegendRow("বাতিল", cancelled, Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("ইভেন্ট অনুযায়ী সিট অকুপেন্সি", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            events.take(5).forEach { ev ->
                val occupied = ev.bookedSeats.size + ev.holdSeats.size
                val ratio = if (ev.totalSeats > 0) occupied.toFloat() / ev.totalSeats else 0f
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ev.destination, style = MaterialTheme.typography.labelMedium)
                        Text(
                            "$occupied / ${ev.totalSeats} সিট",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandOceanBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        drawRect(color = BrandOceanBlue.copy(alpha = 0.15f))
                        drawRect(
                            color = if (ratio > 0.75f) BrandSunsetOrange else BrandEmerald,
                            topLeft = Offset.Zero,
                            size = Size(size.width * ratio.coerceIn(0f, 1f), size.height)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventEditorDialog(
    existing: TourEvent?,
    onDismiss: () -> Unit,
    onSave: (TourEvent, Boolean) -> Unit
) {
    val isCreate = existing == null
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var destination by remember { mutableStateOf(existing?.destination ?: "") }
    var tourDate by remember { mutableStateOf(existing?.tourDate ?: "") }
    var duration by remember { mutableStateOf(existing?.duration ?: "২ দিন ১ রাত") }
    var priceText by remember { mutableStateOf(existing?.price?.toString() ?: "2199") }
    var advanceText by remember { mutableStateOf(existing?.advancePayment?.toString() ?: "1000") }
    var totalSeatsText by remember { mutableStateOf(existing?.totalSeats?.toString() ?: "40") }
    var shortDesc by remember { mutableStateOf(existing?.shortDescription ?: "") }
    var fullDesc by remember { mutableStateOf(existing?.description ?: "") }
    var monthTag by remember { mutableStateOf(existing?.monthTag ?: "THIS_MONTH") }
    var category by remember { mutableStateOf(existing?.category ?: "HILL") }
    var imageKey by remember { mutableStateOf(existing?.imageKey ?: "hero_bangladesh") }
    var vehicleLayout by remember { mutableStateOf(existing?.vehicleLayout ?: "COACH_40") }
    var bookingStatus by remember { mutableStateOf(existing?.bookingStatus ?: "OPEN") }
    var featured by remember { mutableStateOf(existing?.featured ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val categoryOptions = listOf(
        "HILL" to "পাহাড়",
        "SEA" to "সমুদ্র/নৌ",
        "FOREST" to "বন/ম্যানগ্রোভ",
        "BUDGET" to "বাজেট",
        "FAMILY" to "ফ্যামিলি",
        "STUDY" to "শিক্ষা সফর",
        "CORPORATE" to "কর্পোরেট"
    )
    val monthOptions = listOf(
        "THIS_MONTH" to "এই মাস",
        "NEXT_MONTH" to "পরবর্তী মাস",
        "UPCOMING" to "আসন্ন"
    )
    val statusOptions = listOf(
        "OPEN" to "বুকিং চলছে",
        "ALMOST_FULL" to "সিট প্রায় শেষ",
        "CLOSED" to "বুকিং বন্ধ",
        "COMPLETED" to "সম্পন্ন"
    )
    val imageOptions = listOf(
        "hero_bangladesh" to "বাংলাদেশ",
        "sundarbans" to "সুন্দরবন",
        "hill_tracts" to "পাহাড়/রাঙামাটি",
        "sylhet_coast" to "সিলেট/শ্রীমঙ্গল"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCreate) "নতুন মাসিক ইভেন্ট যোগ করুন" else "মাসিক ইভেন্ট এডিট করুন") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("ইভেন্টের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it; validationError = null },
                    label = { Text("গন্তব্য *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tourDate,
                    onValueChange = { tourDate = it; validationError = null },
                    label = { Text("তারিখ (যেমন: ২২ নভেম্বর) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("সময়কাল *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter(Char::isDigit) },
                        label = { Text("মূল্য (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = advanceText,
                        onValueChange = { advanceText = it.filter(Char::isDigit) },
                        label = { Text("অগ্রিম (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = totalSeatsText,
                    onValueChange = { totalSeatsText = it.filter(Char::isDigit) },
                    label = { Text("মোট সিট (৪–৬০)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("গাড়ির সিট প্লান লেআউট:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VehicleLayoutType.entries.forEach { layout ->
                        FilterChip(
                            selected = vehicleLayout == layout.name,
                            onClick = {
                                vehicleLayout = layout.name
                                totalSeatsText = layout.defaultSeats.toString()
                            },
                            label = { Text("${layout.defaultSeats} সিট (${layout.name.substringBefore('_')})") }
                        )
                    }
                }
                Text("ক্যাটাগরি:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryOptions.forEach { (cat, label) ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(label) })
                    }
                }
                Text("মাসের শিডিউল:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    monthOptions.forEach { (m, label) ->
                        FilterChip(selected = monthTag == m, onClick = { monthTag = m }, label = { Text(label) })
                    }
                }
                Text("বুকিং স্ট্যাটাস:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusOptions.forEach { (st, label) ->
                        FilterChip(selected = bookingStatus == st, onClick = { bookingStatus = st }, label = { Text(label) })
                    }
                }
                Text("ছবি নির্বাচন:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    imageOptions.forEach { (key, label) ->
                        FilterChip(selected = imageKey == key, onClick = { imageKey = key }, label = { Text(label) })
                    }
                }
                PermissionSwitchRow("ফিচার্ড ও স্পেশাল অফার হিসেবে দেখান", featured) { featured = it }
                OutlinedTextField(
                    value = shortDesc,
                    onValueChange = { shortDesc = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fullDesc,
                    onValueChange = { fullDesc = it },
                    label = { Text("সম্পূর্ণ বিবরণ *") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toIntOrNull()?.coerceIn(1, 500000) ?: 1500
                    val adv = (advanceText.toIntOrNull() ?: 500).coerceIn(0, price)
                    val seats = (totalSeatsText.toIntOrNull() ?: 40).coerceIn(4, 60)
                    if (name.isBlank() || destination.isBlank() || tourDate.isBlank()) {
                        validationError = "অনুগ্রহ করে ইভেন্টের নাম, গন্তব্য ও তারিখ পূরণ করুন।"
                    } else {
                        onSave(
                            (existing ?: TourEvent()).copy(
                                name = name.trim(),
                                destination = destination.trim(),
                                tourDate = tourDate.trim(),
                                monthTag = monthTag,
                                category = category,
                                duration = duration.ifBlank { "২ দিন ১ রাত" },
                                price = price,
                                advancePayment = adv,
                                shortDescription = shortDesc.ifBlank { "$destination স্পেশাল ট্যুর প্যাকেজ" },
                                description = fullDesc.ifBlank { "$destination সম্পূর্ণ ট্যুর প্যাকেজ।" },
                                imageKey = imageKey,
                                galleryKeys = listOf(imageKey),
                                totalSeats = seats,
                                vehicleLayout = vehicleLayout,
                                bookingStatus = bookingStatus,
                                featured = featured
                            ),
                            isCreate
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AdminAccountEditorDialog(
    account: AdminAccount,
    teamMember: TeamMember?,
    onDismiss: () -> Unit,
    onSave: (AdminAccount, String?, Boolean) -> Unit
) {
    var designation by remember { mutableStateOf(account.designation) }
    var phone by remember { mutableStateOf(account.phone ?: "") }
    var whatsappUrl by remember { mutableStateOf(account.whatsappUrl ?: "") }
    var facebookUrl by remember { mutableStateOf(account.facebookUrl ?: "") }
    var boundEmail by remember { mutableStateOf(account.boundEmail ?: "") }
    var newPasscode by remember { mutableStateOf("") }
    var showPublicContact by remember { mutableStateOf(teamMember?.showPublicContact ?: false) }

    var canManageEvents by remember { mutableStateOf(account.canManageEvents) }
    var canVerifyPayments by remember { mutableStateOf(account.canVerifyPayments) }
    var canManageBookings by remember { mutableStateOf(account.canManageBookings) }
    var canManageDestinations by remember { mutableStateOf(account.canManageDestinations) }
    var canManageSettings by remember { mutableStateOf(account.canManageSettings) }
    var active by remember { mutableStateOf(account.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${account.name} — এডমিন কনফিগারেশন") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ইউজারনেম: ${account.username} (${account.role})", style = MaterialTheme.typography.labelMedium, color = BrandOceanBlue)
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("পদবি (Designation)") }, singleLine = true)
                OutlinedTextField(
                    value = newPasscode,
                    onValueChange = { newPasscode = it },
                    label = { Text("নতুন পাসওয়ার্ড সেট/রিসেট (খালি রাখলে অপরিবর্তিত)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(value = boundEmail, onValueChange = { boundEmail = it }, label = { Text("Google Email (ঐচ্ছিক)") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") }, singleLine = true)
                OutlinedTextField(value = whatsappUrl, onValueChange = { whatsappUrl = it }, label = { Text("WhatsApp লিংক (ঐচ্ছিক)") }, singleLine = true)
                OutlinedTextField(value = facebookUrl, onValueChange = { facebookUrl = it }, label = { Text("Facebook লিংক (ঐচ্ছিক)") }, singleLine = true)

                PermissionSwitchRow("পাবলিক প্রোফাইলে যোগাযোগ দেখান", showPublicContact) { showPublicContact = it }
                PermissionSwitchRow("অ্যাকাউন্ট সক্রিয় (Active)", active) { active = it }
                PermissionSwitchRow("ইভেন্ট ম্যানেজ অনুমতি", canManageEvents) { canManageEvents = it }
                PermissionSwitchRow("পেমেন্ট ভেরিফাই অনুমতি", canVerifyPayments) { canVerifyPayments = it }
                PermissionSwitchRow("বুকিং ম্যানেজ অনুমতি", canManageBookings) { canManageBookings = it }
                PermissionSwitchRow("পর্যটন স্থান অনুমতি", canManageDestinations) { canManageDestinations = it }
                PermissionSwitchRow("ওয়েবসাইট/অ্যাপ সেটিংস অনুমতি", canManageSettings) { canManageSettings = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        account.copy(
                            designation = designation.ifBlank { "এডমিন" },
                            phone = phone.ifBlank { null },
                            whatsappUrl = whatsappUrl.ifBlank { null },
                            facebookUrl = facebookUrl.ifBlank { null },
                            boundEmail = boundEmail.ifBlank { null },
                            canManageEvents = canManageEvents,
                            canVerifyPayments = canVerifyPayments,
                            canManageBookings = canManageBookings,
                            canManageDestinations = canManageDestinations,
                            canManageSettings = canManageSettings,
                            active = active
                        ),
                        newPasscode.ifBlank { null },
                        showPublicContact
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue)
            ) {
                Text("আপডেট করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
private fun PermissionSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DestinationEditorDialog(
    existing: DestinationPlace?,
    onDismiss: () -> Unit,
    onSave: (DestinationPlace, Boolean) -> Unit
) {
    val isCreate = existing == null
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var division by remember { mutableStateOf(existing?.division ?: "") }
    var bestTime by remember { mutableStateOf(existing?.bestTime ?: "অক্টোবর – মার্চ") }
    var shortDesc by remember { mutableStateOf(existing?.shortDescription ?: "") }
    var fullDetails by remember { mutableStateOf(existing?.fullDetails ?: "") }
    var imageKey by remember { mutableStateOf(existing?.imageKey ?: "hero_bangladesh") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCreate) "নতুন পর্যটন স্থান যোগ করুন" else "পর্যটন স্থান এডিট করুন") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("স্থানের নাম *") }, singleLine = true)
                OutlinedTextField(value = division, onValueChange = { division = it }, label = { Text("বিভাগ / জেলা *") }, singleLine = true)
                OutlinedTextField(value = bestTime, onValueChange = { bestTime = it }, label = { Text("ভ্রমণের সেরা সময় *") }, singleLine = true)
                Text("ছবি:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("hero_bangladesh", "sundarbans", "hill_tracts", "sylhet_coast").forEach { key ->
                        FilterChip(selected = imageKey == key, onClick = { imageKey = key }, label = { Text(key) })
                    }
                }
                OutlinedTextField(value = shortDesc, onValueChange = { shortDesc = it }, label = { Text("সংক্ষিপ্ত বিবরণ *") })
                OutlinedTextField(value = fullDetails, onValueChange = { fullDetails = it }, label = { Text("বিস্তারিত বিবরণ *") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && division.isNotBlank()) {
                        onSave(
                            (existing ?: DestinationPlace()).copy(
                                name = name.trim(),
                                division = division.trim(),
                                bestTime = bestTime.ifBlank { "সারা বছর" },
                                shortDescription = shortDesc.ifBlank { "$name ভ্রমণ গাইড" },
                                fullDetails = fullDetails.ifBlank { "$name বাংলাদেশের অন্যতম সুন্দর পর্যটন কেন্দ্র।" },
                                imageKey = imageKey,
                                sortOrder = existing?.sortOrder ?: 99
                            ),
                            isCreate
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
