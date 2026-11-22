package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DestinationPlace
import com.example.data.model.TourEvent
import com.example.ui.components.resolveTravelDrawable
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationsScreen(
    destinations: List<DestinationPlace>,
    events: List<TourEvent>,
    onBookEvent: (String) -> Unit,
    onOpenEventDetail: (String) -> Unit
) {
    var selectedPlace by remember { mutableStateOf<DestinationPlace?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "জনপ্রিয় পর্যটন স্থান",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "সাজেক, কক্সবাজার, সুন্দরবন, রাঙামাটি, বান্দরবান, সিলেট, শ্রীমঙ্গল ও টাঙ্গুয়ার হাওর",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(destinations, key = { it.id }) { place ->
            val relatedEvents = events.filter {
                place.relatedEventIds.contains(it.id) || it.destination.contains(place.name)
            }
            val isExpanded = selectedPlace?.id == place.id
            val zoom by animateFloatAsState(
                targetValue = if (isExpanded) 1.08f else 1f,
                animationSpec = tween(400),
                label = "destZoom"
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { selectedPlace = place }
                    .testTag("destination_card_${place.id}")
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    ) {
                        Image(
                            painter = painterResource(id = resolveTravelDrawable(place.imageKey)),
                            contentDescription = place.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoom
                                    scaleY = zoom
                                }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(listOf(Color.Transparent, DeepTealNight.copy(alpha = 0.85f)))
                                )
                        )

                        Surface(
                            color = BrandEmerald.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = place.division,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সেরা সময়: ${place.bestTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandSunsetOrange
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = place.shortDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (relatedEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "আসন্ন ইভেন্ট (${relatedEvents.size} টি)",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandOceanBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            relatedEvents.forEach { ev ->
                                Surface(
                                    color = BrandOceanBlue.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { onOpenEventDetail(ev.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${ev.name} (${ev.tourDate})",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "৳${ev.price}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = BrandSunsetOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { selectedPlace = place },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ভ্রমণের বিস্তারিত")
                        }
                    }
                }
            }
        }
    }

    selectedPlace?.let { place ->
        val related = events.filter {
            place.relatedEventIds.contains(it.id) || it.destination.contains(place.name)
        }
        ModalBottomSheet(onDismissRequest = { selectedPlace = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(place.name, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${place.division} • সেরা সময়: ${place.bestTime}", style = MaterialTheme.typography.labelMedium, color = BrandEmerald)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(place.fullDetails, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                if (related.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedPlace = null
                            onBookEvent(related.first().id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${related.first().name} বুকিং করুন")
                    }
                } else {
                    OutlinedButton(
                        onClick = { selectedPlace = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("বন্ধ করুন")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
