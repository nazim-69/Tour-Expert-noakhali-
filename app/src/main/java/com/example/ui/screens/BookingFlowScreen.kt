package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import com.example.data.model.TourEvent
import com.example.notifications.TourNotificationManager
import com.example.ui.components.BookingDocumentPrinter
import com.example.ui.components.BookingDocumentSlip
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.SeatPlanVisualizer
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.viewmodel.BookingSubmitState
import com.example.ui.viewmodel.LookupState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BOARDING_POINT_PRESETS = listOf(
    "হাউজিং দক্ষিণ মোড়, নোয়াখালী",
    "মাইজদী বাজার",
    "সোনাপুর জিরো পয়েন্ট",
    "চৌমুহনী চৌরাস্তা",
    "ফেনী মহিপাল"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFlowScreen(
    events: List<TourEvent>,
    initialEventId: String?,
    initialTab: Int = 0,
    currentUserEmail: String?,
    myBookings: List<BookingRecord>,
    bookingSubmitState: BookingSubmitState,
    lookupState: LookupState,
    onSubmitBooking: (
        eventId: String,
        touristName: String,
        touristEmail: String,
        mobileNumber: String,
        whatsappNumber: String,
        numberOfTourists: Int,
        nidOrPassport: String?,
        emergencyContact: String,
        address: String,
        boardingPoint: String,
        selectedSeats: List<String>,
        paidAmount: Int,
        paymentMethod: String,
        transactionId: String,
        paymentDate: String,
        paymentScreenshotUri: String?,
        specialInstructions: String?
    ) -> Unit,
    onResetSubmitState: () -> Unit,
    onLookupReference: (String) -> Unit,
    onOpenBookingDetails: (String) -> Unit,
    onMarkMemoSent: (String) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    var selectedEventId by remember(events, initialEventId) {
        mutableStateOf(
            initialEventId?.takeIf { id -> events.any { it.id == id } }
                ?: events.firstOrNull()?.id
                ?: ""
        )
    }
    val selectedEvent = events.firstOrNull { it.id == selectedEventId } ?: events.firstOrNull()
    var eventDropdownExpanded by remember { mutableStateOf(false) }

    var numberOfTourists by remember { mutableIntStateOf(1) }
    var selectedSeats by remember { mutableStateOf<List<String>>(emptyList()) }

    var touristName by remember { mutableStateOf("") }
    var touristEmail by remember(currentUserEmail) { mutableStateOf(currentUserEmail ?: "") }
    var mobileNumber by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var boardingPoint by remember { mutableStateOf(BOARDING_POINT_PRESETS.first()) }
    var nidOrPassport by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }

    var paymentMethod by remember { mutableStateOf(BrandConstants.PAYMENT_METHODS.first()) }
    var transactionId by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()))
    }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    var previewBookingSlip by remember { mutableStateOf<BookingRecord?>(null) }
    var searchRefQuery by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> screenshotUri = uri }
    )

    // Keep selected seats synced when event or tourist count changes
    LaunchedEffect(selectedEvent?.id, selectedEvent?.bookedSeats, selectedEvent?.holdSeats) {
        if (selectedEvent != null) {
            val occupied = (selectedEvent.bookedSeats + selectedEvent.holdSeats).toSet()
            selectedSeats = selectedSeats.filter { it !in occupied }.take(numberOfTourists)
        }
    }

    LaunchedEffect(selectedEvent?.id, numberOfTourists) {
        if (selectedEvent != null && paidAmountText.isBlank()) {
            paidAmountText = (selectedEvent.advancePayment * numberOfTourists).toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("সিট প্লান ও বুকিং") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("আমার বুকিং (${myBookings.size})") }
            )
        }

        if (activeTab == 0) {
            if (bookingSubmitState is BookingSubmitState.Success) {
                val createdBooking = bookingSubmitState.booking
                LaunchedEffect(createdBooking.referenceNumber) {
                    TourNotificationManager.showBookingUpdateNotification(
                        context = context,
                        booking = createdBooking,
                        isNewSubmission = true
                    )
                    events.firstOrNull { it.id == createdBooking.eventId }?.let { ev ->
                        TourNotificationManager.scheduleTourDateReminder(context, ev)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn(initialScale = 0.94f)) {
                            Surface(
                                color = BrandEmerald.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, BrandEmerald, RoundedCornerShape(22.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandEmerald,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "বুকিং সফলভাবে জমা হয়েছে!",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = BrandEmerald,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "অ্যাডমিন পেমেন্ট যাচাই করার পর আপনার জিমেইল (${createdBooking.touristEmail}) ইনবক্সে কনফার্মেশন মেমো পাঠানো হবে।",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = DeepTealNight,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "অটো-জেনারেটেড ইউনিক বুকিং আইডি",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrandEmerald
                                                )
                                                Text(
                                                    text = createdBooking.referenceNumber,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.testTag("generated_booking_id_text")
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(
                                                        ClipData.newPlainText("Booking ID", createdBooking.referenceNumber)
                                                    )
                                                    onFeedback("বুকিং আইডি ${createdBooking.referenceNumber} কপি হয়েছে!")
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "বুকিং আইডি কপি করুন",
                                                    tint = BrandSunsetOrange
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            BookingDocumentPrinter.sendConfirmationMemoToGmail(context, createdBooking)
                                                .onSuccess {
                                                    onMarkMemoSent(createdBooking.id.ifBlank { createdBooking.referenceNumber })
                                                }
                                                .onFailure { err ->
                                                    onFeedback("জিমেইল খুলতে সমস্যা হয়েছে: ${err.localizedMessage}")
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("send_gmail_memo_after_submit")
                                    ) {
                                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("জিমেইল ইনবক্সে কনফার্মেশন মেমো পাঠান")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        BookingDocumentSlip(
                            booking = createdBooking,
                            onFeedback = onFeedback
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                val targetId = createdBooking.id.ifBlank {
                                    createdBooking.referenceNumber
                                }
                                onOpenBookingDetails(targetId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("open_booking_details_after_submit")
                        ) {
                            Text("সম্পূর্ণ বুকিং ডিটেইলস ও প্রিন্ট ভাউচার দেখুন")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                selectedSeats = emptyList()
                                transactionId = ""
                                screenshotUri = null
                                onResetSubmitState()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("আরেকটি নতুন বুকিং করুন")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 0. Auto Booking ID Banner
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = DeepTealNight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange)),
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ইভেন্ট বুকিং ফর্ম",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "সাবমিট করার সাথে সাথে ইউনিক বুকিং আইডি (যেমন: TEN-2026-0001) স্বয়ংক্রিয়ভাবে তৈরি হবে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Event Name Selector
                    item {
                        Text("১. ইভেন্ট নাম ও টুরিস্ট সংখ্যা নির্বাচন", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.2.dp, BrandOceanBlue.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { eventDropdownExpanded = true }
                                    .testTag("booking_event_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = BrandOceanBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "ইভেন্ট নাম *",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrandOceanBlue
                                            )
                                            Text(
                                                text = selectedEvent?.name ?: "ইভেন্ট নির্বাচন করুন",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (selectedEvent != null) {
                                                Text(
                                                    text = "${selectedEvent.destination} • ${selectedEvent.tourDate} • ৳${selectedEvent.price}/জন",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "ইভেন্ট তালিকা",
                                        tint = BrandOceanBlue
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = eventDropdownExpanded,
                                onDismissRequest = { eventDropdownExpanded = false }
                            ) {
                                events.forEach { ev ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(ev.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${ev.destination} • ${ev.tourDate} • ৳${ev.price}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrandOceanBlue
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedEventId = ev.id
                                            selectedSeats = emptyList()
                                            paidAmountText = (ev.advancePayment * numberOfTourists).toString()
                                            eventDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(events, key = { it.id }) { ev ->
                                FilterChip(
                                    selected = selectedEvent?.id == ev.id,
                                    onClick = {
                                        selectedEventId = ev.id
                                        selectedSeats = emptyList()
                                        paidAmountText = (ev.advancePayment * numberOfTourists).toString()
                                    },
                                    label = { Text("${ev.destination} • ৳${ev.price}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandOceanBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    if (selectedEvent != null) {
                        // Tourist count stepper
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("যাত্রীর সংখ্যা (Number of Tourists)", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "মোট ভাড়া: ৳${selectedEvent.price * numberOfTourists} | ন্যূনতম অগ্রিম: ৳${selectedEvent.advancePayment * numberOfTourists}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandOceanBlue
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (numberOfTourists > 1) {
                                                    numberOfTourists--
                                                    if (selectedSeats.size > numberOfTourists) {
                                                        selectedSeats = selectedSeats.take(numberOfTourists)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "কমান")
                                        }
                                        Text(
                                            text = "$numberOfTourists",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                if (numberOfTourists < 10 && numberOfTourists < selectedEvent.availableSeatCount) {
                                                    numberOfTourists++
                                                }
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "বাড়ান")
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Interactive Visual Seat Plan
                        item {
                            SeatPlanVisualizer(
                                event = selectedEvent,
                                selectedSeats = selectedSeats,
                                maxSelectable = numberOfTourists,
                                onToggleSeat = { seat ->
                                    selectedSeats = if (seat in selectedSeats) {
                                        selectedSeats - seat
                                    } else if (selectedSeats.size < numberOfTourists) {
                                        selectedSeats + seat
                                    } else {
                                        selectedSeats
                                    }
                                }
                            )
                        }

                        // 3. Tourist Info Form (নাম, মোবাইল নাম্বার, উঠার স্থান ও বিস্তারিত)
                        item {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BrandOceanBlue.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("২. টুরিস্টের বিস্তারিত তথ্য", style = MaterialTheme.typography.titleLarge)

                                    OutlinedTextField(
                                        value = touristName,
                                        onValueChange = { touristName = it },
                                        label = { Text("নাম (ভ্রমণকারীর পূর্ণ নাম) *") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandOceanBlue) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_name_input")
                                    )
                                    OutlinedTextField(
                                        value = touristEmail,
                                        onValueChange = { touristEmail = it },
                                        label = { Text("জিমেইল ঠিকানা (Gmail Address) *") },
                                        placeholder = { Text("example@gmail.com") },
                                        supportingText = {
                                            Text(
                                                text = "বুকিং কনফার্মেশন মেমো এই জিমেইল ইনবক্সে পাঠানো হবে।",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrandOceanBlue
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandSunsetOrange) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_gmail_input")
                                    )
                                    OutlinedTextField(
                                        value = mobileNumber,
                                        onValueChange = { mobileNumber = it },
                                        label = { Text("মোবাইল নাম্বার *") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandEmerald) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_mobile_input")
                                    )
                                    Column {
                                        OutlinedTextField(
                                            value = whatsappNumber,
                                            onValueChange = { whatsappNumber = it },
                                            label = { Text("WhatsApp নম্বর *") },
                                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandEmerald) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("booking_whatsapp_input")
                                        )
                                        if (mobileNumber.isNotBlank() && whatsappNumber != mobileNumber) {
                                            TextButton(
                                                onClick = { whatsappNumber = mobileNumber },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "মোবাইল নাম্বারটি WhatsApp হিসেবে ব্যবহার করুন",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrandOceanBlue
                                                )
                                            }
                                        }
                                    }

                                    // Boarding Point (উঠার স্থান) with Quick Noakhali Pickup Presets
                                    Text(
                                        text = "উঠার স্থান নির্বাচন করুন বা লিখুন *",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = BrandSunsetOrange
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        BOARDING_POINT_PRESETS.forEach { preset ->
                                            FilterChip(
                                                selected = boardingPoint == preset,
                                                onClick = { boardingPoint = preset },
                                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BrandSunsetOrange,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = boardingPoint,
                                        onValueChange = { boardingPoint = it },
                                        label = { Text("উঠার স্থান (Boarding Point) *") },
                                        leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BrandSunsetOrange) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_boarding_point_input")
                                    )

                                    OutlinedTextField(
                                        value = nidOrPassport,
                                        onValueChange = { nidOrPassport = it },
                                        label = { Text("NID / পাসপোর্ট নম্বর (ঐচ্ছিক)") },
                                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BrandOceanBlue) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = emergencyContact,
                                        onValueChange = { emergencyContact = it },
                                        label = { Text("জরুরি যোগাযোগ নম্বর *") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandSunsetOrange) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_emergency_input")
                                    )
                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("বর্তমান ঠিকানা *") },
                                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandOceanBlue) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_address_input")
                                    )
                                    OutlinedTextField(
                                        value = specialInstructions,
                                        onValueChange = { specialInstructions = it },
                                        label = { Text("রেফারেন্স / বিশেষ নির্দেশনা (ঐচ্ছিক)") },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 4. Advance Payment Checkout Section
                        item {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, BrandSunsetOrange.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("৩. অ্যাডভান্স পেমেন্ট সেকশন", style = MaterialTheme.typography.titleLarge)

                                    Surface(
                                        color = BrandSunsetOrange.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "অফিসিয়াল অ্যাডভান্স পেমেন্ট নম্বর",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = BrandSunsetOrange
                                                )
                                                Text(
                                                    text = BrandConstants.ADVANCE_PAYMENT_NUMBER,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "বিকাশ | নগদ | রকেট | উপায় (Send Money)",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(
                                                        ClipData.newPlainText("Payment Number", BrandConstants.ADVANCE_PAYMENT_NUMBER)
                                                    )
                                                    onFeedback("পেমেন্ট নম্বর ${BrandConstants.ADVANCE_PAYMENT_NUMBER} কপি হয়েছে!")
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "কপি করুন", tint = BrandSunsetOrange)
                                            }
                                        }
                                    }

                                    Text("পেমেন্ট মাধ্যম নির্বাচন করুন:", style = MaterialTheme.typography.labelLarge)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BrandConstants.PAYMENT_METHODS.forEach { method ->
                                            FilterChip(
                                                selected = paymentMethod == method,
                                                onClick = { paymentMethod = method },
                                                label = { Text(method) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BrandEmerald,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = transactionId,
                                        onValueChange = { transactionId = it },
                                        label = { Text("Transaction ID (TrxID) *") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_trx_input")
                                    )
                                    OutlinedTextField(
                                        value = paidAmountText,
                                        onValueChange = { paidAmountText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("প্রদত্ত টাকার পরিমাণ (৳) *") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("booking_amount_input")
                                    )
                                    OutlinedTextField(
                                        value = paymentDate,
                                        onValueChange = { paymentDate = it },
                                        label = { Text("পেমেন্টের তারিখ *") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("upload_screenshot_button")
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (screenshotUri == null) "পেমেন্ট স্ক্রিনশট আপলোড করুন (ঐচ্ছিক)"
                                            else "স্ক্রিনশট নির্বাচিত হয়েছে — পরিবর্তন করুন"
                                        )
                                    }

                                    if (screenshotUri != null) {
                                        AsyncImage(
                                            model = screenshotUri,
                                            contentDescription = "পেমেন্ট স্ক্রিনশট",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        )
                                    }
                                }
                            }
                        }

                        // Error banner & Submit CTA
                        item {
                            val activeError = validationError ?: (bookingSubmitState as? BookingSubmitState.Error)?.message
                            if (activeError != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activeError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            val isSubmitting = bookingSubmitState is BookingSubmitState.Submitting
                            Button(
                                onClick = {
                                    validationError = null
                                    val paidAmount = paidAmountText.toIntOrNull() ?: -1
                                    when {
                                        selectedSeats.size != numberOfTourists ->
                                            validationError = "অনুগ্রহ করে $numberOfTourists টি সিট নির্বাচন করুন।"
                                        touristName.trim().length < 2 ->
                                            validationError = "ভ্রমণকারীর পূর্ণ নাম লিখুন।"
                                        !touristEmail.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) ->
                                            validationError = "সঠিক জিমেইল ঠিকানা (Gmail Address) দিন।"
                                        mobileNumber.trim().length < 6 ->
                                            validationError = "সঠিক মোবাইল নাম্বার দিন।"
                                        whatsappNumber.trim().length < 6 ->
                                            validationError = "সঠিক WhatsApp নম্বর দিন।"
                                        boardingPoint.trim().length < 2 ->
                                            validationError = "উঠার স্থান লিখুন বা নির্বাচন করুন।"
                                        emergencyContact.trim().length < 6 ->
                                            validationError = "জরুরি যোগাযোগ নম্বর দিন।"
                                        address.trim().length < 3 ->
                                            validationError = "আপনার ঠিকানা লিখুন।"
                                        transactionId.trim().length < 3 ->
                                            validationError = "সঠিক Transaction ID দিন।"
                                        paidAmount <= 0 ->
                                            validationError = "সঠিক টাকার পরিমাণ দিন।"
                                        else -> {
                                            onSubmitBooking(
                                                selectedEvent.id,
                                                touristName,
                                                touristEmail,
                                                mobileNumber,
                                                whatsappNumber,
                                                numberOfTourists,
                                                nidOrPassport.ifBlank { null },
                                                emergencyContact,
                                                address,
                                                boardingPoint,
                                                selectedSeats,
                                                paidAmount,
                                                paymentMethod,
                                                transactionId,
                                                paymentDate,
                                                screenshotUri?.toString(),
                                                specialInstructions.ifBlank { null }
                                            )
                                        }
                                    }
                                },
                                enabled = !isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("submit_booking_button")
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "বুকিং নিশ্চিত ও পেমেন্ট জমা দিন",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        } else {
            // Tab 1: My Bookings & Reference Lookup
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("রেফারেন্স নম্বর দিয়ে স্ট্যাটাস দেখুন", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchRefQuery,
                                    onValueChange = { searchRefQuery = it },
                                    placeholder = { Text("TEN-2026-0001") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onLookupReference(searchRefQuery) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "খুঁজুন")
                                }
                            }
                            if (lookupState is LookupState.Found) {
                                val st = lookupState.status
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = BrandOceanBlue.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(st.referenceNumber, fontWeight = FontWeight.Bold)
                                            Text("${st.eventName} • সিট: ${st.seatLabels}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        BookingStatusBadge(st.status)
                                    }
                                }
                            } else if (lookupState is LookupState.NotFound) {
                                Text("কোনো তথ্য পাওয়া যায়নি।", color = BrandSunsetOrange, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    }
                }

                item {
                    Text("আপনার বুকিং তালিকা", style = MaterialTheme.typography.titleLarge)
                }

                if (myBookings.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "আপনার এখনো কোনো বুকিং নেই। 'সিট প্লান ও বুকিং' ট্যাব থেকে পছন্দের ট্যুর বুক করুন!",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    items(myBookings, key = { it.id }) { b ->
                        val docId = b.id.ifBlank { b.referenceNumber }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable { onOpenBookingDetails(docId) }
                                .testTag("my_booking_card_$docId")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = b.referenceNumber,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = BrandOceanBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    BookingStatusBadge(status = b.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(b.eventName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${b.tourDate} • সিট: ${b.selectedSeats.joinToString(", ")} • পেমেন্ট: ৳${b.paidAmount} (${b.paymentMethod})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onOpenBookingDetails(docId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("open_booking_details_$docId")
                                    ) {
                                        Text("বুকিং ডিটেইলস", maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { previewBookingSlip = b },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text("কুইক স্লিপ / PDF", maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewBookingSlip?.let { slip ->
        ModalBottomSheet(onDismissRequest = { previewBookingSlip = null }) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                BookingDocumentSlip(booking = slip, onFeedback = onFeedback)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
