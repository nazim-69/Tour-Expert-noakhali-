package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TourEvent
import com.example.data.model.TourReview
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.DeepTealSurface
import com.example.ui.viewmodel.UiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

private val RatingLabelsBn = mapOf(
    1 to "উন্নতি প্রয়োজন",
    2 to "মোটামুটি",
    3 to "ভালো লেগেছে",
    4 to "খুব ভালো ছিল",
    5 to "অসাধারণ অভিজ্ঞতা!"
)

@Composable
fun TourReviewsSection(
    reviewsState: UiState<List<TourReview>>,
    events: List<TourEvent>,
    currentUserId: String,
    currentUserDisplayName: String?,
    isAdmin: Boolean,
    isSubmitting: Boolean,
    onSubmitReview: (
        reviewerName: String,
        eventId: String?,
        eventName: String,
        rating: Int,
        comment: String,
        onSuccessClear: () -> Unit
    ) -> Unit,
    onDeleteReview: (String) -> Unit
) {
    val reviews = (reviewsState as? UiState.Success)?.data ?: emptyList()

    var selectedRating by rememberSaveable { mutableIntStateOf(5) }
    var reviewerName by rememberSaveable(currentUserDisplayName) {
        mutableStateOf(currentUserDisplayName?.takeIf { it.isNotBlank() } ?: "")
    }
    var selectedEventId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedEventName by rememberSaveable { mutableStateOf("সাধারণ ভ্রমণ অভিজ্ঞতা") }
    var commentText by rememberSaveable { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val averageRating = remember(reviews) {
        if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("tour_reviews_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dark Teal Glassmorphic Header & Rating Summary Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            DeepTealNight,
                            DeepTealSurface,
                            BrandOceanBlue.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            BrandEmerald.copy(alpha = 0.55f),
                            BrandSunsetOrange.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            BrandEmerald.copy(alpha = 0.4f),
                            RoundedCornerShape(50.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ভ্রমণকারীদের মতামত ও রেটিং",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    if (reviews.isNotEmpty()) {
                        Surface(
                            color = BrandSunsetOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(
                                1.dp,
                                BrandSunsetOrange.copy(alpha = 0.55f),
                                RoundedCornerShape(14.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = BrandSunsetOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", averageRating)} (${reviews.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "আমাদের সাথে ট্যুর করে আপনার কেমন লেগেছে?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "আপনার ভ্রমণের গল্প, অভিজ্ঞতা ও মূল্যবান রেটিং আমাদের আরও ভালো সেবা দিতে অনুপ্রাণিত করে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
        }

        // Interactive Review & Star Rating Form Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    BrandOceanBlue.copy(alpha = 0.28f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rating Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "রেটিং দিন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = BrandSunsetOrange.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "${selectedRating}/5 • ${RatingLabelsBn[selectedRating].orEmpty()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandSunsetOrange,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                // 5 Animated Interactive Stars (Min 48dp Touch Target)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val isFilled = star <= selectedRating
                        val scale by animateFloatAsState(
                            targetValue = if (star == selectedRating) 1.22f else if (isFilled) 1.06f else 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "starScale$star"
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    selectedRating = star
                                    validationError = null
                                }
                                .testTag("review_star_$star")
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star স্টার রেটিং দিন",
                                tint = if (isFilled) BrandSunsetOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .size(34.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                        }
                    }
                }

                // Event / Tour Chip Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "কোন ট্যুরে ভ্রমণ করেছেন? (ঐচ্ছিক)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            val generalSelected = selectedEventId == null
                            FilterChip(
                                selected = generalSelected,
                                onClick = {
                                    selectedEventId = null
                                    selectedEventName = "সাধারণ ভ্রমণ অভিজ্ঞতা"
                                },
                                label = { Text("সাধারণ ভ্রমণ অভিজ্ঞতা") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOceanBlue,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("review_event_chip_general")
                            )
                        }
                        items(events, key = { it.id }) { event ->
                            val isSelected = selectedEventId == event.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedEventId = event.id
                                    selectedEventName = event.name
                                },
                                label = { Text(event.destination.ifBlank { event.name }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOceanBlue,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("review_event_chip_${event.id}")
                            )
                        }
                    }
                }

                // Reviewer Name Input
                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = {
                        if (it.length <= 80) {
                            reviewerName = it
                            validationError = null
                        }
                    },
                    label = { Text("আপনার নাম") },
                    placeholder = { Text("যেমন: মোঃ রাকিবুল হাসান") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOceanBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_name_input")
                )

                // Comment Input ("মন্তব্য করুন")
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        if (it.length <= 600) {
                            commentText = it
                            validationError = null
                        }
                    },
                    label = { Text("মন্তব্য করুন") },
                    placeholder = {
                        Text("আমাদের সার্ভিস, বাস/যাতায়াত, খাবার ও গাইড সম্পর্কে আপনার অনুভূতি লিখুন...")
                    },
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text(
                            text = "${commentText.length}/৬০০ অক্ষর",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment_input")
                )

                AnimatedVisibility(visible = validationError != null) {
                    Text(
                        text = validationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val trimmedName = reviewerName.trim()
                        val trimmedComment = commentText.trim()
                        when {
                            trimmedName.length < 2 -> {
                                validationError = "অনুগ্রহ করে আপনার নাম লিখুন (সর্বনিম্ন ২ অক্ষর)।"
                            }
                            trimmedComment.length < 2 -> {
                                validationError = "অনুগ্রহ করে ট্যুর সম্পর্কে আপনার মন্তব্য লিখুন।"
                            }
                            else -> {
                                validationError = null
                                onSubmitReview(
                                    trimmedName,
                                    selectedEventId,
                                    selectedEventName,
                                    selectedRating,
                                    trimmedComment
                                ) {
                                    commentText = ""
                                    selectedRating = 5
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("review_submit_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("জমা হচ্ছে...", color = Color.White)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "মতামত ও রেটিং জমা দিন",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Community Reviews List
        when (reviewsState) {
            is UiState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = BrandOceanBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("রিভিউ লোড হচ্ছে...", style = MaterialTheme.typography.bodySmall)
                }
            }
            is UiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reviewsState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            is UiState.Success -> {
                if (reviews.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = BrandOceanBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "এখনও কোনো রিভিউ জমা হয়নি — আপনার ভ্রমণ অভিজ্ঞতা শেয়ার করে প্রথম রিভিউ দিন!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        reviews.take(12).forEach { review ->
                            ReviewItemCard(
                                review = review,
                                canDelete = review.userId == currentUserId || isAdmin,
                                onDelete = { onDeleteReview(review.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewItemCard(
    review: TourReview,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                RoundedCornerShape(20.dp)
            )
            .testTag("review_card_${review.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(BrandOceanBlue, BrandEmerald))
                            )
                    ) {
                        Text(
                            text = review.reviewerName.firstOrNull()?.toString() ?: "ভ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.reviewerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "ভেরিফাইড ভ্রমণকারী",
                                tint = BrandEmerald,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = formatReviewTimestamp(review.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_review_${review.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "রিভিউ মুছুন",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (star in 1..5) {
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (star <= review.rating) BrandSunsetOrange else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = RatingLabelsBn[review.rating].orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSunsetOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = BrandOceanBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = review.eventName,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandOceanBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatReviewTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "সদ্য প্রকাশিত"
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        formatter.format(timestamp.toDate())
    } catch (_: Exception) {
        "সদ্য প্রকাশিত"
    }
}
