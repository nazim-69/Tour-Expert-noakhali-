package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import com.example.ui.components.BookingDocumentPrinter
import com.example.ui.components.BookingStatusBadge
import com.example.ui.theme.AlabasterBg
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealCard
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.InkSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleOutline
import com.example.ui.viewmodel.UiState

private val PaperBackground = PureWhite
private val PaperInkPrimary = InkSlate
private val PaperInkSecondary = MutedSlate
private val PaperDivider = SubtleOutline
private val PaperSectionTint = AlabasterBg

@Composable
fun BookingDetailsScreen(
    bookingId: String,
    bookingDetailState: UiState<BookingRecord?>,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onMarkMemoSent: (String) -> Unit = {},
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("booking_details_screen")
    ) {
        // Sticky Screen Toolbar
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("booking_details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                    Column {
                        Text(
                            text = "বুকিং বিস্তারিত (Booking Details)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ডকুমেন্ট আইডি: $bookingId",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandOceanBlue
                        )
                    }
                }

                val loadedBooking = (bookingDetailState as? UiState.Success)?.data
                if (loadedBooking != null) {
                    IconButton(
                        onClick = {
                            BookingDocumentPrinter.printBookingDocument(context, loadedBooking)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("booking_details_toolbar_print")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "প্রিন্ট করুন",
                            tint = BrandOceanBlue
                        )
                    }
                }
            }
        }

        when (bookingDetailState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = BrandOceanBlue)
                        Text(
                            text = "ফায়ারস্টোর থেকে বুকিং তথ্য সংগ্রহ করা হচ্ছে...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                        modifier = Modifier.widthIn(max = 520.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "বুকিং ডকুমেন্ট লোড করা যায়নি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bookingDetailState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পুনরায় চেষ্টা করুন")
                                }
                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("ফিরে যান")
                                }
                            }
                        }
                    }
                }
            }

            is UiState.Success -> {
                val booking = bookingDetailState.data
                if (booking == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 520.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = BrandSunsetOrange,
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    text = "বুকিং তথ্য খুঁজে পাওয়া যায়নি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "বুকিং আইডি '$bookingId' বর্তমানে পাওয়া যায়নি অথবা আপনার দেখার অনুমতি নেই।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("ফিরে যান")
                                }
                            }
                        }
                    }
                } else {
                    PrinterFriendlyBookingVoucher(
                        booking = booking,
                        onCopyId = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("Booking ID", booking.referenceNumber)
                            )
                            onFeedback("বুকিং আইডি ${booking.referenceNumber} কপি হয়েছে!")
                        },
                        onDownloadOrSharePdf = {
                            BookingDocumentPrinter.shareOrDownloadBookingPdf(context, booking)
                                .onFailure { err ->
                                    onFeedback("PDF তৈরি করতে সমস্যা হয়েছে: ${err.localizedMessage}")
                                }
                        },
                        onPrintDocument = {
                            BookingDocumentPrinter.printBookingDocument(context, booking)
                        },
                        onSendGmailMemo = {
                            BookingDocumentPrinter.sendConfirmationMemoToGmail(context, booking)
                                .onSuccess {
                                    onMarkMemoSent(booking.id.ifBlank { booking.referenceNumber })
                                    onFeedback("জিমেইল ইনবক্সের (${booking.touristEmail}) জন্য কনফার্মেশন মেমো প্রস্তুত হয়েছে!")
                                }
                                .onFailure { err ->
                                    onFeedback("জিমেইল মেমো পাঠানো যায়নি: ${err.localizedMessage}")
                                }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrinterFriendlyBookingVoucher(
    booking: BookingRecord,
    onCopyId: () -> Unit,
    onDownloadOrSharePdf: () -> Unit,
    onPrintDocument: () -> Unit,
    onSendGmailMemo: () -> Unit
) {
    val dueAmount = (booking.totalAmount - booking.paidAmount).coerceAtLeast(0)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPad = if (maxWidth >= 680.dp) 24.dp else 14.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPad, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Printer-friendly Crisp Paper Sheet Container
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = PaperBackground,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .border(1.5.dp, PaperDivider, RoundedCornerShape(22.dp))
                        .testTag("printer_friendly_voucher")
                ) {
                    Column {
                        // Official Agency Header Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepTealNight, DeepTealCard)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = BrandConstants.ORGANIZATION_NAME,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = BrandConstants.TAGLINE,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandSunsetOrange
                                        )
                                        Text(
                                            text = "${BrandConstants.ADDRESS} • ${BrandConstants.CONTACT_PHONE}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.78f)
                                        )
                                    }

                                    Surface(
                                        color = BrandEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.border(
                                            1.dp,
                                            BrandEmerald.copy(alpha = 0.45f),
                                            RoundedCornerShape(50.dp)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = BrandEmerald,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "TRAVEL VOUCHER",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Reference ID + Live Status Strip
                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
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
                                                text = "Booking ID / Reference Number",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.75f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = booking.referenceNumber,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.testTag("details_reference_number")
                                                )
                                                IconButton(
                                                    onClick = onCopyId,
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "রেফারেন্স কপি করুন",
                                                        tint = BrandSunsetOrange,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                        BookingStatusBadge(status = booking.status)
                                    }
                                }
                            }
                        }

                        // Brand Color Accent Bar
                        Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                            Box(modifier = Modifier.weight(1f).height(4.dp).background(BrandEmerald))
                            Box(modifier = Modifier.weight(1f).height(4.dp).background(BrandOceanBlue))
                            Box(modifier = Modifier.weight(1f).height(4.dp).background(BrandSunsetOrange))
                        }

                        // Body Sections on Clean Print-Ready White Paper
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section 1: Travel & Event Details
                            VoucherSectionCard(
                                title = "১. ভ্রমণ ও ইভেন্ট বিবরণ (Travel Summary)",
                                icon = Icons.Default.DirectionsBus,
                                accentColor = BrandOceanBlue
                            ) {
                                VoucherRow("ইভেন্টের নাম", booking.eventName)
                                VoucherRow("গন্তব্য (Destination)", booking.destination)
                                VoucherRow("ভ্রমণের তারিখ", booking.tourDate)
                                VoucherRow(
                                    "উঠার স্থান (Boarding Point)",
                                    booking.boardingPoint.ifBlank { BrandConstants.ADDRESS }
                                )
                                VoucherRow("ভ্রমণকারীর সংখ্যা", "${booking.numberOfTourists} জন")
                                VoucherRow(
                                    label = "বুকিং তারিখ ও সময়",
                                    value = BookingDocumentPrinter.formatBookingDate(booking),
                                    showDivider = false
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "বরাদ্দকৃত সিট নম্বরসমূহ (Allocated Seats)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PaperInkSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    booking.selectedSeats.forEach { seat ->
                                        Surface(
                                            color = BrandEmerald.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.border(
                                                1.dp,
                                                BrandEmerald.copy(alpha = 0.55f),
                                                RoundedCornerShape(10.dp)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EventSeat,
                                                    contentDescription = null,
                                                    tint = BrandEmerald,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = seat,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PaperInkPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            DashedTicketSeparator()

                            // Section 2: Passenger / Tourist Personal Details
                            VoucherSectionCard(
                                title = "২. ভ্রমণকারীর বিস্তারিত তথ্য (Tourist Information)",
                                icon = Icons.Default.Person,
                                accentColor = BrandEmerald
                            ) {
                                VoucherRow("পূর্ণ নাম", booking.touristName)
                                VoucherRow(
                                    "জিমেইল (Gmail Inbox)",
                                    booking.touristEmail.ifBlank { "দেওয়া হয়নি" }
                                )
                                VoucherRow(
                                    "কনফার্মেশন মেমো স্ট্যাটাস",
                                    if (booking.confirmationMemoSent) {
                                        "জিমেইলে পাঠানো হয়েছে"
                                    } else {
                                        "প্রস্তুত (নিচের বাটনে ইনবক্সে পাঠান)"
                                    }
                                )
                                VoucherRow("মোবাইল নাম্বার", booking.mobileNumber)
                                VoucherRow("WhatsApp নম্বর", booking.whatsappNumber)
                                VoucherRow("জরুরি যোগাযোগ", booking.emergencyContact)
                                VoucherRow(
                                    "NID / পাসপোর্ট",
                                    booking.nidOrPassport?.takeIf { it.isNotBlank() } ?: "দেওয়া হয়নি"
                                )
                                VoucherRow(
                                    label = "বর্তমান ঠিকানা",
                                    value = booking.address,
                                    showDivider = !booking.specialInstructions.isNullOrBlank()
                                )
                                if (!booking.specialInstructions.isNullOrBlank()) {
                                    VoucherRow(
                                        label = "বিশেষ নির্দেশনা",
                                        value = booking.specialInstructions,
                                        showDivider = false
                                    )
                                }
                            }

                            DashedTicketSeparator()

                            // Section 3: Payment & Verification Breakdown
                            VoucherSectionCard(
                                title = "৩. পেমেন্ট ও ভেরিফিকেশন হিসাব (Payment Summary)",
                                icon = Icons.Default.Payments,
                                accentColor = BrandSunsetOrange
                            ) {
                                // Summary Money Pills
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MoneyStatBox("মোট মূল্য", "৳${booking.totalAmount}", PaperInkPrimary, Modifier.weight(1f))
                                    MoneyStatBox("অগ্রিম জমা", "৳${booking.paidAmount}", BrandEmerald, Modifier.weight(1f))
                                    MoneyStatBox(
                                        "বকেয়া (Due)",
                                        "৳$dueAmount",
                                        if (dueAmount > 0) BrandSunsetOrange else BrandEmerald,
                                        Modifier.weight(1f)
                                    )
                                }

                                VoucherRow("পেমেন্ট মাধ্যম", booking.paymentMethod)
                                VoucherRow("Transaction ID", booking.transactionId)
                                VoucherRow("পেমেন্টের তারিখ", booking.paymentDate)
                                VoucherRow(
                                    label = "পেমেন্ট স্ট্যাটাস",
                                    value = booking.status,
                                    showDivider = !booking.verifiedBy.isNullOrBlank()
                                )
                                if (!booking.verifiedBy.isNullOrBlank()) {
                                    VoucherRow(
                                        label = "যাচাইকারী এডমিন",
                                        value = booking.verifiedBy,
                                        showDivider = false
                                    )
                                }

                                if (!booking.paymentScreenshotUri.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "সংযুক্ত পেমেন্ট স্ক্রিনশট",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PaperInkSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AsyncImage(
                                        model = Uri.parse(booking.paymentScreenshotUri),
                                        contentDescription = "পেমেন্ট স্ক্রিনশট",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, PaperDivider, RoundedCornerShape(14.dp))
                                    )
                                }
                            }

                            // Official Footer Guidelines for Printed Voucher
                            Surface(
                                color = PaperSectionTint,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PaperDivider, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ভ্রমণের নির্দেশনা",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PaperInkPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "• রিপোর্টিংয়ের সময় নির্ধারিত সময়ের ৩০ মিনিট পূর্বে উঠার স্থানে উপস্থিত থাকুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PaperInkSecondary
                                    )
                                    Text(
                                        text = "• জরুরি সহায়তা ও বুকিং যোগাযোগ: ${BrandConstants.CONTACT_PHONE} | পেমেন্ট নম্বর: ${BrandConstants.ADVANCE_PAYMENT_NUMBER}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PaperInkSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar for Printing & PDF Export
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPrintDocument,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("booking_details_print_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("প্রিন্ট করুন", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onDownloadOrSharePdf,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("booking_details_download_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PDF ডাউনলোড", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Button(
                        onClick = onSendGmailMemo,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("booking_details_send_gmail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (booking.confirmationMemoSent) {
                                "পুনরায় জিমেইল ইনবক্সে মেমো পাঠান"
                            } else {
                                "জিমেইল ইনবক্সে কনফার্মেশন মেমো পাঠান"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(
                        onClick = onCopyId,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = BrandSunsetOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "রেফারেন্স আইডি কপি করুন (${booking.referenceNumber})",
                            color = BrandSunsetOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoucherSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        color = PaperSectionTint,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PaperDivider, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PaperInkPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun VoucherRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = PaperInkSecondary,
                modifier = Modifier.weight(0.42f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PaperInkPrimary,
                modifier = Modifier.weight(0.58f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = PaperDivider)
        }
    }
}

@Composable
private fun MoneyStatBox(
    label: String,
    amount: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PaperBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, PaperDivider, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PaperInkSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun DashedTicketSeparator() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        drawLine(
            color = PaperDivider,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
    }
}
