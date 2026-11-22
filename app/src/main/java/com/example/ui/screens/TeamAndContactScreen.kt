package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.AgencySettings
import com.example.data.model.BrandConstants
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.data.model.TourReview
import com.example.ui.components.ContactAndFooterSection
import com.example.ui.components.GlassmorphicAdminTeamSection
import com.example.ui.components.TourReviewsSection
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.viewmodel.UiState

@Composable
fun TeamAndContactScreen(
    teamMembers: List<TeamMember>,
    events: List<TourEvent>,
    settings: AgencySettings,
    reviewsState: UiState<List<TourReview>>,
    isSubmittingReview: Boolean,
    currentUserId: String,
    currentUserDisplayName: String?,
    isAdmin: Boolean,
    focusSection: String? = null,
    focusTrigger: Int = 0,
    onSubmitReview: (String, String?, String, Int, String, () -> Unit) -> Unit,
    onDeleteReview: (String) -> Unit,
    onBookNow: () -> Unit,
    onOpenAdminPortal: () -> Unit,
    onNavigateQuickLink: (String) -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(focusSection, focusTrigger) {
        when (focusSection) {
            "about" -> listState.animateScrollToItem(0)
            "contact" -> listState.animateScrollToItem(3)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // About Section
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(1.dp, BrandOceanBlue.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("আমাদের সম্পর্কে", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${BrandConstants.ORGANIZATION_NAME} — ${BrandConstants.TAGLINE}",
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandSunsetOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ট্যুর এক্সপার্ট নোয়াখালী ভ্রমণপিপাসুদের জন্য নিরাপদ, আনন্দময় ও সুশৃঙ্খল ট্যুর আয়োজন করে। আমাদের অভিজ্ঞ ৮ সদস্যের এডমিন প্যানেল প্রতিটি ইভেন্টের যাতায়াত, সিট ব্যবস্থাপনা, রিজার্ভেশন এবং ভ্রমণকারীদের নিরাপত্তা নিশ্চিত করে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Dedicated Glassmorphic "আমাদের এডমিন" Section (All 8 Official Admins)
        item {
            GlassmorphicAdminTeamSection(
                teamMembers = teamMembers,
                settings = settings,
                onOpenAdminPortal = onOpenAdminPortal
            )
        }

        // Tourist Reviews & Star Rating Section
        item {
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

        // Contact & Footer
        item {
            ContactAndFooterSection(
                settings = settings,
                context = context,
                onBookNow = onBookNow,
                onNavigateQuickLink = onNavigateQuickLink,
                onFeedback = onFeedback
            )
        }
    }
}
