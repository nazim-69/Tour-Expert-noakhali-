package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight

@Composable
fun BookingDocumentSlip(
    booking: BookingRecord,
    onFeedback: (String) -> Unit = {}
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BrandOceanBlue.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .testTag("booking_document_slip")
    ) {
        Column {
            // Official Document Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DeepTealNight, Color(0xFF144252))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = BrandConstants.ORGANIZATION_NAME,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = BrandConstants.TAGLINE,
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandSunsetOrange
                            )
                        }
                        Surface(
                            color = BrandEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                                    text = "অফিসিয়াল স্লিপ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reference Number",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                                Text(
                                    text = booking.referenceNumber,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            BookingStatusBadge(status = booking.status)
                        }
                    }
                }
            }

            // Document Details Body
            Column(modifier = Modifier.padding(20.dp)) {
                SlipRow("Booking ID", booking.id.ifBlank { booking.referenceNumber })
                SlipRow("Tourist Name", booking.touristName)
                if (booking.touristEmail.isNotBlank()) {
                    SlipRow("Gmail Address", booking.touristEmail)
                }
                SlipRow("Mobile Number", booking.mobileNumber)
                SlipRow("Event Name", booking.eventName)
                SlipRow("Destination", booking.destination)
                SlipRow("Tour Date", booking.tourDate)
                if (booking.boardingPoint.isNotBlank()) {
                    SlipRow("Boarding Point (উঠার স্থান)", booking.boardingPoint)
                }
                SlipRow("Number of Tourists", "${booking.numberOfTourists} জন")
                SlipRow("Selected Seat Number(s)", booking.selectedSeats.joinToString(", "))
                SlipRow("Paid Amount", "৳${booking.paidAmount} / মোট ৳${booking.totalAmount}")
                SlipRow("Payment Method", "${booking.paymentMethod} (${booking.transactionId})")
                SlipRow("Booking Status", booking.status)
                SlipRow("Booking Date", BookingDocumentPrinter.formatBookingDate(booking))
                SlipRow("Emergency Contact", booking.emergencyContact)
                SlipRow("Contact Number", BrandConstants.CONTACT_PHONE, showDivider = false)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val res = BookingDocumentPrinter.shareOrDownloadBookingPdf(context, booking)
                            res.onFailure {
                                onFeedback("ডকুমেন্ট তৈরি করতে সমস্যা হয়েছে: ${it.localizedMessage}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_booking_document_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ডাউনলোড / শেয়ার", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            BookingDocumentPrinter.printBookingDocument(context, booking)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("print_booking_document_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = BrandOceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রিন্ট করুন", color = BrandOceanBlue, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlipRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.44f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.56f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        }
    }
}
