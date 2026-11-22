package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object BrandConstants {
    const val ORGANIZATION_NAME = "ট্যুর এক্সপার্ট নোয়াখালী"
    const val TAGLINE = "ভ্রমণ যেখানে গল্প সেখানে"
    const val ADDRESS = "হাউজিং দক্ষিণ মোড়, নোয়াখালী"
    const val CONTACT_PHONE = "01822421919"
    const val BOOKING_PHONE = "01822421919"
    const val ADVANCE_PAYMENT_NUMBER = "01849958944"
    const val SUPER_ADMIN_EMAIL = "nazimmd74653@gmail.com"
    const val DEFAULT_WEB_APP_URL = "https://ais-pre-cvz5ce7s6eo4edsszl5gqh-42022883973.asia-east1.run.app"
    const val DEV_WEB_APP_URL = "https://ais-dev-cvz5ce7s6eo4edsszl5gqh-42022883973.asia-east1.run.app"
    const val SHARE_APP_DEEP_LINK_HOST = "ais-pre-cvz5ce7s6eo4edsszl5gqh-42022883973.asia-east1.run.app"
    val PAYMENT_METHODS = listOf("বিকাশ", "নগদ", "রকেট", "উপায়")

    val OFFICIAL_ADMIN_NAMES = listOf(
        "আবদুল হাকিম",
        "মাসুদ পারভেজ",
        "ওমর ফারুক জাবের",
        "মুস্তাকিম অশ্রু আদিল",
        "শামছুল আলম অপি",
        "হাসান মাহমুদ",
        "আব্দুর রহিম আরিফ",
        "নাজিম উদ্দিন"
    )
}

enum class EventFilterChip(val labelBn: String) {
    ALL("সব"),
    THIS_MONTH("এই মাস"),
    NEXT_MONTH("পরবর্তী মাস"),
    HILL("পাহাড়"),
    SEA("সমুদ্র"),
    BUDGET("বাজেট ট্যুর"),
    FAMILY("ফ্যামিলি প্ল্যানিং ট্যুর"),
    STUDY("শিক্ষা সফর"),
    CORPORATE("কর্পোরেট ইভেন্ট")
}

enum class SeatStatus {
    AVAILABLE,
    SELECTED,
    BOOKED,
    HOLD
}

enum class VehicleLayoutType(val labelBn: String, val defaultSeats: Int, val columnsPerRow: Int, val aisleAfterCol: Int) {
    COACH_40("লাক্সারি এসি/নন-এসি কোচ (৪০ সিট)", 40, 4, 2),
    MINIBUS_28("মিনিবাস / ট্যুরিস্ট কোচ (২৮ সিট)", 28, 4, 2),
    HIACE_12("মাইক্রোবাস / হাইয়েস (১২ সিট)", 12, 3, 1),
    SHIP_CABIN_32("ক্রুজ শিপ / কেবিন (৩২ সিট)", 32, 4, 2);

    companion object {
        fun fromRaw(raw: String): VehicleLayoutType =
            entries.firstOrNull { it.name == raw } ?: COACH_40
    }
}

enum class BookingStatusType(val value: String, val labelBn: String) {
    PENDING("Pending", "পেন্ডিং"),
    PAYMENT_VERIFICATION_PENDING("Payment Verification Pending", "পেমেন্ট যাচাই চলছে"),
    CONFIRMED("Confirmed", "কনফার্মড"),
    CANCELLED("Cancelled", "বাতিল"),
    COMPLETED("Completed", "সম্পন্ন");

    companion object {
        fun fromRaw(raw: String): BookingStatusType =
            entries.firstOrNull { it.value == raw } ?: PAYMENT_VERIFICATION_PENDING
    }
}

@Keep
@IgnoreExtraProperties
data class TourEvent(
    @DocumentId val id: String = "",
    val name: String = "",
    val destination: String = "",
    val tourDate: String = "",
    val monthTag: String = "THIS_MONTH",
    val category: String = "HILL",
    val duration: String = "",
    val price: Int = 0,
    val advancePayment: Int = 0,
    val shortDescription: String = "",
    val description: String = "",
    val imageKey: String = "hero_bangladesh",
    val customImageUri: String? = null,
    val galleryKeys: List<String> = emptyList(),
    val totalSeats: Int = 40,
    val bookedSeats: List<String> = emptyList(),
    val holdSeats: List<String> = emptyList(),
    val vehicleLayout: String = "COACH_40",
    val bookingStatus: String = "OPEN",
    val featured: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val availableSeatCount: Int
        get() = (totalSeats - bookedSeats.distinct().size - holdSeats.distinct().size).coerceAtLeast(0)

    fun generateSeatCodes(): List<String> {
        val layout = VehicleLayoutType.fromRaw(vehicleLayout)
        val cols = layout.columnsPerRow
        val rowCount = (totalSeats + cols - 1) / cols
        val result = mutableListOf<String>()
        for (r in 0 until rowCount) {
            val rowLetter = ('A' + r).toString()
            for (c in 1..cols) {
                if (result.size < totalSeats) {
                    result.add("$rowLetter$c")
                }
            }
        }
        return result
    }
}

@Keep
@IgnoreExtraProperties
data class DestinationPlace(
    @DocumentId val id: String = "",
    val name: String = "",
    val division: String = "",
    val shortDescription: String = "",
    val fullDetails: String = "",
    val bestTime: String = "",
    val imageKey: String = "hero_bangladesh",
    val relatedEventIds: List<String> = emptyList(),
    val sortOrder: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TeamMember(
    @DocumentId val id: String = "",
    val name: String = "",
    val designation: String = "এডমিন",
    val sortOrder: Int = 1,
    val showPublicContact: Boolean = false,
    val publicPhone: String? = null,
    val whatsappUrl: String? = null,
    val facebookUrl: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminRole(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val adminAccountId: String = "",
    val role: String = "ADMIN",
    val displayName: String = "",
    val canManageEvents: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminAccount(
    @DocumentId val id: String = "",
    val name: String = "",
    val username: String = "",
    val designation: String = "এডমিন",
    val role: String = "ADMIN",
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val boundEmail: String? = null,
    val phone: String? = null,
    val whatsappUrl: String? = null,
    val facebookUrl: String? = null,
    val canManageEvents: Boolean = false,
    val canVerifyPayments: Boolean = false,
    val canManageBookings: Boolean = true,
    val canManageDestinations: Boolean = false,
    val canManageSettings: Boolean = false,
    val active: Boolean = true,
    val sortOrder: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookingRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val referenceNumber: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val destination: String = "",
    val tourDate: String = "",
    val touristName: String = "",
    val touristEmail: String = "",
    val mobileNumber: String = "",
    val whatsappNumber: String = "",
    val numberOfTourists: Int = 1,
    val nidOrPassport: String? = null,
    val emergencyContact: String = "",
    val address: String = "",
    val boardingPoint: String = "",
    val selectedSeats: List<String> = emptyList(),
    val totalAmount: Int = 0,
    val paidAmount: Int = 0,
    val paymentMethod: String = "বিকাশ",
    val transactionId: String = "",
    val paymentDate: String = "",
    val paymentScreenshotUri: String? = null,
    val specialInstructions: String? = null,
    val status: String = "Payment Verification Pending",
    val confirmationMemoSent: Boolean = false,
    val memoSentAt: String? = null,
    val assignedAdminId: String? = null,
    val verifiedBy: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookingStatusLookup(
    @DocumentId val id: String = "",
    val referenceNumber: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val eventName: String = "",
    val destination: String = "",
    val tourDate: String = "",
    val seatCount: Int = 1,
    val seatLabels: String = "",
    val status: String = "Payment Verification Pending",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AgencySettings(
    @DocumentId val id: String = "",
    val organizationName: String = BrandConstants.ORGANIZATION_NAME,
    val tagline: String = BrandConstants.TAGLINE,
    val address: String = BrandConstants.ADDRESS,
    val contactPhone: String = BrandConstants.CONTACT_PHONE,
    val bookingPhone: String = BrandConstants.BOOKING_PHONE,
    val advancePaymentNumber: String = BrandConstants.ADVANCE_PAYMENT_NUMBER,
    val facebookUrl: String? = null,
    val whatsappUrl: String? = "https://wa.me/8801822421919",
    val websiteUrl: String? = BrandConstants.DEFAULT_WEB_APP_URL,
    val welcomeBannerText: String? = "আসসালামুয়ালাইকুম! আমাদের অক্টোবর মাসের বিশেষ ইভেন্টসমূহে বুকিং চলছে।",
    val activeMonthTitle: String? = "অক্টোবর মাসের ইভেন্টসমূহ",
    val bookingCounter: Int = 0,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedWebsiteUrl(): String =
        websiteUrl?.trim()?.takeIf { it.isNotBlank() } ?: BrandConstants.DEFAULT_WEB_APP_URL
}

@Keep
@IgnoreExtraProperties
data class TourReview(
    @DocumentId val id: String = "",
    val userId: String = "",
    val reviewerName: String = "",
    val eventId: String? = null,
    val eventName: String = "সাধারণ ভ্রমণ অভিজ্ঞতা",
    val rating: Int = 5,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

