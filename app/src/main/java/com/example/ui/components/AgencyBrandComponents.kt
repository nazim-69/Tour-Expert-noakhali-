package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AgencySettings
import com.example.data.model.BookingStatusType
import com.example.data.model.BrandConstants
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.BrandSunsetOrange
import com.example.ui.theme.DeepTealNight

@DrawableRes
fun resolveTravelDrawable(imageKey: String): Int =
    when (imageKey) {
        "sundarbans" -> R.drawable.img_dest_sundarbans
        "hill_tracts" -> R.drawable.img_dest_hill_tracts
        "sylhet_coast" -> R.drawable.img_dest_sylhet_coast
        else -> R.drawable.img_hero_bangladesh
    }

@Composable
fun BookingStatusBadge(status: String) {
    val type = BookingStatusType.fromRaw(status)
    val color = when (type) {
        BookingStatusType.CONFIRMED -> BrandEmerald
        BookingStatusType.PAYMENT_VERIFICATION_PENDING -> BrandSunsetOrange
        BookingStatusType.PENDING -> BrandOceanBlue
        BookingStatusType.CANCELLED -> Color(0xFFEF4444)
        BookingStatusType.COMPLETED -> Color(0xFF10B981)
    }
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(50.dp))
    ) {
        Text(
            text = type.labelBn,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun openPhoneDialer(context: Context, phone: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }
}

fun openExternalUrl(context: Context, url: String) {
    runCatching {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    }
}

fun copyPublicLinkToClipboard(context: Context, url: String): Boolean {
    return runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText("Tour Expert Website Link", url))
        true
    }.getOrDefault(false)
}

fun shareWebsiteAppLink(
    context: Context,
    url: String,
    activeMonthTitle: String? = null
) {
    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    val monthLine = activeMonthTitle?.takeIf { it.isNotBlank() }?.let { "\n📅 $it" }.orEmpty()
    val shareText = buildString {
        append("${BrandConstants.ORGANIZATION_NAME} — ${BrandConstants.TAGLINE}")
        append(monthLine)
        append("\n\nঅনলাইনে মাসিক ট্যুর ইভেন্ট, সিট প্ল্যান দেখতে এবং বুকিং করতে নিচের লিংকে প্রবেশ করুন:")
        append("\n$normalized")
        append("\n\nহটলাইন ও বুকিং: ${BrandConstants.CONTACT_PHONE}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "${BrandConstants.ORGANIZATION_NAME} — ওয়েবসাইট ও বুকিং লিংক")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    runCatching {
        context.startActivity(Intent.createChooser(sendIntent, "ওয়েবসাইট ও অ্যাপ লিংক শেয়ার করুন"))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharePublicLinkCard(
    settings: AgencySettings,
    context: Context,
    onFeedback: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shareUrl = settings.resolvedWebsiteUrl()
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = BrandOceanBlue.copy(alpha = 0.08f),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BrandOceanBlue.copy(alpha = 0.38f), RoundedCornerShape(22.dp))
            .testTag("share_public_link_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "ওয়েবসাইট লিংক",
                    tint = BrandOceanBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ওয়েবসাইট ও অ্যাপ লিংক শেয়ার করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "এই লিংকটি যাকে দেবেন, তিনিই সরাসরি প্রবেশ করে ইভেন্ট দেখতে ও বুকিং করতে পারবেন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = shareUrl,
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandOceanBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        shareWebsiteAppLink(context, shareUrl, settings.activeMonthTitle)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("share_public_link_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("লিংক শেয়ার করুন")
                }

                OutlinedButton(
                    onClick = {
                        val copied = copyPublicLinkToClipboard(context, shareUrl)
                        onFeedback?.invoke(
                            if (copied) "ওয়েবসাইট / অ্যাপ লিংক কপি করা হয়েছে!"
                            else "লিংক কপি করা যায়নি।"
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("copy_public_link_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("লিংক কপি")
                }

                OutlinedButton(
                    onClick = { openExternalUrl(context, shareUrl) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_website_link_button")
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = BrandOceanBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ওয়েবসাইট খুলুন")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactAndFooterSection(
    settings: AgencySettings,
    context: Context,
    onBookNow: () -> Unit,
    onNavigateQuickLink: (String) -> Unit,
    onFeedback: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        // Contact Section Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "যোগাযোগ করুন",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "যেকোনো ট্যুর পরামর্শ, কাস্টম প্যাকেজ বা সিট বুকিংয়ের জন্য সরাসরি যোগাযোগ করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ঠিকানা: ${BrandConstants.ADDRESS}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = BrandOceanBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হটলাইন ও বুকিং: ${BrandConstants.CONTACT_PHONE}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { openPhoneDialer(context, BrandConstants.CONTACT_PHONE) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOceanBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কল করুন")
                    }

                    Button(
                        onClick = {
                            val wa = settings.whatsappUrl?.takeIf { it.isNotBlank() }
                                ?: "https://wa.me/8801822421919"
                            openExternalUrl(context, wa)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp")
                    }

                    if (!settings.facebookUrl.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { openExternalUrl(context, settings.facebookUrl) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                tint = BrandOceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Facebook")
                        }
                    }

                    Button(
                        onClick = onBookNow,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSunsetOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বুকিং করুন")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SharePublicLinkCard(
            settings = settings,
            context = context,
            onFeedback = onFeedback,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Official Dark Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(DeepTealNight, Color(0xFF07171C)))
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = BrandConstants.ORGANIZATION_NAME,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = BrandConstants.TAGLINE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandSunsetOrange
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ঠিকানা: ${BrandConstants.ADDRESS}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = "যোগাযোগ: ${BrandConstants.CONTACT_PHONE} | বুকিং: ${BrandConstants.BOOKING_PHONE}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Advance Payment: ${BrandConstants.ADVANCE_PAYMENT_NUMBER} (বিকাশ | নগদ | রকেট | উপায়)",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandEmerald
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Quick Links",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandOceanBlue
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "হোম" to "home",
                        "ইভেন্ট" to "events",
                        "পর্যটন স্থান" to "destinations",
                        "বুকিং" to "booking",
                        "যোগাযোগ" to "team"
                    ).forEach { (label, route) ->
                        TextButton(onClick = { onNavigateQuickLink(route) }) {
                            Text(label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
