package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.SeatStatus
import com.example.data.model.TourEvent
import com.example.data.model.VehicleLayoutType
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.SeatAvailable
import com.example.ui.theme.SeatBooked
import com.example.ui.theme.SeatHold
import com.example.ui.theme.SeatSelected

@Composable
fun SeatPlanVisualizer(
    event: TourEvent,
    selectedSeats: List<String>,
    maxSelectable: Int,
    readOnly: Boolean = false,
    onToggleSeat: (String) -> Unit
) {
    val layout = VehicleLayoutType.fromRaw(event.vehicleLayout)
    val seatCodes = event.generateSeatCodes()
    val rows = seatCodes.chunked(layout.columnsPerRow)
    val bookedSet = event.bookedSeats.toSet()
    val holdSet = event.holdSeats.toSet()
    val selectedSet = selectedSeats.toSet()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Vehicle Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BrandOceanBlue.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = BrandOceanBlue,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ইন্টারেক্টিভ সিট প্ল্যান",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = layout.labelBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = BrandEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "খালি: ${event.availableSeatCount}/${event.totalSeats}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SeatLegendChip("খালি", SeatAvailable)
                SeatLegendChip("সিলেক্টেড", SeatSelected)
                SeatLegendChip("হোল্ড", SeatHold)
                SeatLegendChip("বুকড", SeatBooked)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bus Front / Driver Cabin Indicator
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "প্রবেশ গেট (বাম)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ড্রাইভার / ক্যাপ্টেন কেবিন",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandOceanBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seat Grid Rows
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { rowSeats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowSeats.forEachIndexed { idx, code ->
                            if (idx == layout.aisleAfterCol) {
                                Spacer(modifier = Modifier.width(24.dp))
                            } else if (idx > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            val status = when {
                                code in bookedSet -> SeatStatus.BOOKED
                                code in holdSet -> SeatStatus.HOLD
                                code in selectedSet -> SeatStatus.SELECTED
                                else -> SeatStatus.AVAILABLE
                            }

                            val canTap = !readOnly && (
                                status == SeatStatus.SELECTED ||
                                    (status == SeatStatus.AVAILABLE && selectedSeats.size < maxSelectable)
                                )

                            SeatTile(
                                code = code,
                                status = status,
                                enabled = canTap,
                                onClick = { onToggleSeat(code) }
                            )
                        }
                    }
                }
            }

            if (!readOnly) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = BrandOceanBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedSeats.isEmpty()) {
                            "ভ্রমণকারীর সংখ্যা অনুযায়ী সর্বোচ্চ $maxSelectable টি সিট নির্বাচন করুন।"
                        } else {
                            "নির্বাচিত সিট (${selectedSeats.size}/$maxSelectable): ${selectedSeats.joinToString(", ")}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandOceanBlue,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatLegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeatTile(
    code: String,
    status: SeatStatus,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when (status) {
            SeatStatus.AVAILABLE -> SeatAvailable.copy(alpha = 0.12f)
            SeatStatus.SELECTED -> SeatSelected
            SeatStatus.HOLD -> SeatHold.copy(alpha = 0.22f)
            SeatStatus.BOOKED -> SeatBooked.copy(alpha = 0.22f)
        },
        label = "seatBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            SeatStatus.AVAILABLE -> SeatAvailable
            SeatStatus.SELECTED -> SeatSelected
            SeatStatus.HOLD -> SeatHold
            SeatStatus.BOOKED -> SeatBooked
        },
        label = "seatBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (status == SeatStatus.SELECTED) 1.07f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "seatScale"
    )

    val contentColor = when (status) {
        SeatStatus.SELECTED -> Color.White
        SeatStatus.AVAILABLE -> SeatAvailable
        SeatStatus.HOLD -> SeatHold
        SeatStatus.BOOKED -> SeatBooked
    }

    val icon = when (status) {
        SeatStatus.SELECTED -> Icons.Default.Check
        SeatStatus.HOLD -> Icons.Default.HourglassTop
        SeatStatus.BOOKED -> Icons.Default.Lock
        SeatStatus.AVAILABLE -> Icons.Default.EventSeat
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .size(width = 56.dp, height = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("seat_$code"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = "সিট $code (${status.name})",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = code,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
