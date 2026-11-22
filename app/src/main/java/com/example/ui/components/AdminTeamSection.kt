package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.firebase.SeedDataFactory
import com.example.data.model.AgencySettings
import com.example.data.model.BrandConstants
import com.example.data.model.TeamMember
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight
import com.example.ui.theme.DeepTealSurface
import kotlinx.coroutines.delay

private fun toBanglaDigits(number: Int): String {
    val map = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return number.toString().padStart(2, '0').map { ch ->
        if (ch in '0'..'9') map[ch - '0'] else ch
    }.joinToString("")
}

private fun ensureEightOfficialAdmins(teamMembers: List<TeamMember>): List<TeamMember> {
    val defaults = SeedDataFactory.defaultTeamMembers
    if (teamMembers.isEmpty()) return defaults
    val byId = teamMembers.associateBy { it.id }
    val merged = defaults.map { def -> byId[def.id] ?: def }
    val extras = teamMembers.filter { member -> defaults.none { it.id == member.id } }
    return (merged + extras).sortedBy { it.sortOrder }
}

@Composable
fun GlassmorphicAdminTeamSection(
    teamMembers: List<TeamMember>,
    settings: AgencySettings,
    onOpenAdminPortal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val admins = remember(teamMembers) { ensureEightOfficialAdmins(teamMembers) }
    var fallbackDialogMember by remember { mutableStateOf<TeamMember?>(null) }

    val buildOfficialWhatsappUrl: (TeamMember) -> String = { member ->
        val personal = if (member.showPublicContact) member.whatsappUrl?.takeIf { it.isNotBlank() } else null
        if (personal != null) {
            personal
        } else {
            val base = settings.whatsappUrl?.takeIf { it.isNotBlank() } ?: "https://wa.me/8801822421919"
            val text = Uri.encode("আসসালামুয়ালাইকুম! আমি ${member.name}-এর সাথে ট্যুর এক্সপার্ট নোয়াখালী বিষয়ে যোগাযোগ করতে চাই।")
            if (base.contains("?")) "$base&text=$text" else "$base?text=$text"
        }
    }

    val handleFacebookClick: (TeamMember) -> Unit = { member ->
        val personalFb = if (member.showPublicContact) member.facebookUrl?.takeIf { it.isNotBlank() } else null
        val targetFb = personalFb ?: settings.facebookUrl?.takeIf { it.isNotBlank() }
        if (targetFb != null) {
            openExternalUrl(context, targetFb)
        } else {
            fallbackDialogMember = member
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        DeepTealNight,
                        DeepTealSurface,
                        Color(0xFF0A2A34)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        BrandEmerald.copy(alpha = 0.45f),
                        BrandOceanBlue.copy(alpha = 0.50f),
                        BrandSunsetOrange.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .testTag("our_admins_section")
    ) {
        // Atmospheric Glassmorphism Glow Blobs
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandEmerald.copy(alpha = 0.22f), Color.Transparent)
                ),
                radius = size.width * 0.46f,
                center = Offset(size.width * 0.12f, size.height * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandOceanBlue.copy(alpha = 0.24f), Color.Transparent)
                ),
                radius = size.width * 0.52f,
                center = Offset(size.width * 0.90f, size.height * 0.45f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BrandSunsetOrange.copy(alpha = 0.20f), Color.Transparent)
                ),
                radius = size.width * 0.44f,
                center = Offset(size.width * 0.20f, size.height * 0.88f)
            )
        }

        Column(modifier = Modifier.padding(18.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.border(
                            1.dp,
                            BrandEmerald.copy(alpha = 0.55f),
                            RoundedCornerShape(50.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অফিশিয়াল টিম • ০৮ জন",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "আমাদের এডমিন",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "আপনার প্রতিটি ভ্রমণের পরিকল্পনা, সিট ব্যবস্থাপনা ও সার্বক্ষণিক নিরাপত্তায় নিবেদিত আমাদের ০৮ সদস্যের এডমিন প্যানেল।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }

                if (onOpenAdminPortal != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onOpenAdminPortal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandOceanBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("admin_section_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("এডমিন পোর্টাল", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Responsive 1 or 2-column Glassmorphic Cards Grid
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 580.dp) 2 else 1
                val rows = admins.chunked(columns)

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    rows.forEachIndexed { rowIdx, rowMembers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            rowMembers.forEachIndexed { colIdx, member ->
                                val globalIdx = rowIdx * columns + colIdx
                                var cardVisible by remember(member.id) { mutableStateOf(false) }
                                LaunchedEffect(member.id) {
                                    delay((globalIdx * 45L).coerceAtMost(280L))
                                    cardVisible = true
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = cardVisible,
                                        enter = fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                                            slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it / 7 } +
                                            scaleIn(tween(320), initialScale = 0.96f)
                                    ) {
                                        AdminGlassProfileCard(
                                            member = member,
                                            index = globalIdx + 1,
                                            onWhatsAppClick = {
                                                openExternalUrl(context, buildOfficialWhatsappUrl(member))
                                            },
                                            onFacebookClick = {
                                                handleFacebookClick(member)
                                            },
                                            onPhoneClick = if (member.showPublicContact && !member.publicPhone.isNullOrBlank()) {
                                                { openPhoneDialer(context, member.publicPhone) }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                            if (rowMembers.size < columns) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    fallbackDialogMember?.let { member ->
        AlertDialog(
            onDismissRequest = { fallbackDialogMember = null },
            title = {
                Text(
                    text = "${member.name} — যোগাযোগ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${member.name}-এর ব্যক্তিগত ফেসবুক প্রোফাইল লিংক এখনো পাবলিক প্রোফাইলে যুক্ত করা হয়নি। সরাসরি যোগাযোগের জন্য অফিসিয়াল হটলাইন বা WhatsApp ব্যবহার করুন।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        fallbackDialogMember = null
                        openExternalUrl(context, buildOfficialWhatsappUrl(member))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            fallbackDialogMember = null
                            openPhoneDialer(context, BrandConstants.CONTACT_PHONE)
                        }
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(BrandConstants.CONTACT_PHONE)
                    }
                    TextButton(onClick = { fallbackDialogMember = null }) {
                        Text("বন্ধ করুন")
                    }
                }
            }
        )
    }
}

@Composable
fun AdminGlassProfileCard(
    member: TeamMember,
    index: Int,
    onWhatsAppClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onPhoneClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSuperAdmin = index == 8 || member.designation.contains("সুপার")
    val accentColor = when (index % 3) {
        1 -> BrandEmerald
        2 -> BrandOceanBlue
        else -> BrandSunsetOrange
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "adminCardScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.34f),
                        accentColor.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.14f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
            .padding(16.dp)
            .testTag("admin_profile_card_${member.id}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Top Row: Serial Pill + Verified Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.border(
                        0.8.dp,
                        accentColor.copy(alpha = 0.6f),
                        RoundedCornerShape(50.dp)
                    )
                ) {
                    Text(
                        text = "#${toBanglaDigits(index)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = (if (isSuperAdmin) BrandSunsetOrange else BrandEmerald).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.border(
                        0.8.dp,
                        (if (isSuperAdmin) BrandSunsetOrange else BrandEmerald).copy(alpha = 0.55f),
                        RoundedCornerShape(50.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "ভেরিফাইড এডমিন",
                            tint = if (isSuperAdmin) BrandSunsetOrange else BrandEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSuperAdmin) "সুপার এডমিন" else "অফিশিয়াল এডমিন",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Photo Placeholder with Gradient Ring & Camera/Portrait Frame
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(BrandEmerald, BrandOceanBlue, BrandSunsetOrange, BrandEmerald)
                            )
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.35f),
                                    DeepTealNight.copy(alpha = 0.92f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "${member.name} প্রোফাইল ছবি প্লেসহোল্ডার",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = member.name.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Photo placeholder badge
                Surface(
                    color = DeepTealNight,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .border(1.2.dp, accentColor, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Name
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Admin Designation
            Text(
                text = member.designation,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSuperAdmin) BrandSunsetOrange else BrandEmerald,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Glass Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.24f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Buttons (WhatsApp, Facebook + optional Phone)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWhatsAppClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandEmerald.copy(alpha = 0.24f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_whatsapp_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onFacebookClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOceanBlue.copy(alpha = 0.24f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, BrandOceanBlue.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_facebook_${member.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = BrandOceanBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Facebook",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onPhoneClick != null && !member.publicPhone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPhoneClick,
                    border = BorderStroke(1.dp, BrandSunsetOrange.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandSunsetOrange.copy(alpha = 0.14f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = BrandSunsetOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = member.publicPhone,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
