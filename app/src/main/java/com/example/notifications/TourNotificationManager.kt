package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import com.example.data.model.TourEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class LocalNotificationItem(
    val id: String,
    val category: String,
    val title: String,
    val message: String,
    val timestampMillis: Long,
    val bookingId: String? = null,
    val eventId: String? = null
)

data class ScheduledTourReminder(
    val eventId: String,
    val eventName: String,
    val tourDate: String,
    val triggerAtMillis: Long
)

data class NotificationPreferencesState(
    val tourRemindersEnabled: Boolean = true,
    val specialOffersEnabled: Boolean = true,
    val bookingUpdatesEnabled: Boolean = true,
    val scheduledReminders: List<ScheduledTourReminder> = emptyList(),
    val history: List<LocalNotificationItem> = emptyList()
)

object TourNotificationManager {

    const val CHANNEL_TOUR_REMINDERS = "tour_date_reminders"
    const val CHANNEL_SPECIAL_OFFERS = "special_offers"
    const val CHANNEL_BOOKING_UPDATES = "booking_updates"

    const val CATEGORY_TOUR_REMINDER = "TOUR_REMINDER"
    const val CATEGORY_SPECIAL_OFFER = "SPECIAL_OFFER"
    const val CATEGORY_BOOKING_UPDATE = "BOOKING_UPDATE"

    const val EXTRA_BOOKING_ID = "extra_booking_id"
    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_TITLE = "extra_notif_title"
    const val EXTRA_MESSAGE = "extra_notif_message"
    const val EXTRA_CATEGORY = "extra_notif_category"
    const val EXTRA_NOTIF_ID = "extra_notif_id"

    private const val PREFS_NAME = "tour_expert_local_notifications"
    private const val KEY_TOUR_REMINDERS = "pref_tour_reminders"
    private const val KEY_SPECIAL_OFFERS = "pref_special_offers"
    private const val KEY_BOOKING_UPDATES = "pref_booking_updates"
    private const val KEY_SCHEDULED_LIST = "pref_scheduled_reminders"
    private const val KEY_HISTORY = "pref_notification_history"
    private const val KEY_BOOKING_STATUS_CACHE = "pref_booking_status_cache"
    private const val KEY_KNOWN_EVENT_IDS = "pref_known_event_ids"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            CHANNEL_TOUR_REMINDERS,
            "আসন্ন ট্যুর তারিখ রিমাইন্ডার",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "আপনার বুকিং করা ও পছন্দের ট্যুরের তারিখ মনে করিয়ে দেওয়ার নোটিফিকেশন"
        }

        val offerChannel = NotificationChannel(
            CHANNEL_SPECIAL_OFFERS,
            "বিশেষ অফার ও নতুন প্যাকেজ",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "নতুন বাজেট ট্যুর, ফ্যামিলি প্যাকেজ ও বিশেষ ডিসকাউন্ট অফার অ্যালার্ট"
        }

        val bookingChannel = NotificationChannel(
            CHANNEL_BOOKING_UPDATES,
            "বুকিং কনফার্মেশন ও স্ট্যাটাস আপডেট",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "বুকিং জমা, পেমেন্ট যাচাই ও জিমেইল কনফার্মেশন মেমো সংক্রান্ত আপডেট"
        }

        manager.createNotificationChannels(listOf(reminderChannel, offerChannel, bookingChannel))
    }

    fun loadPreferencesState(context: Context): NotificationPreferencesState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return NotificationPreferencesState(
            tourRemindersEnabled = prefs.getBoolean(KEY_TOUR_REMINDERS, true),
            specialOffersEnabled = prefs.getBoolean(KEY_SPECIAL_OFFERS, true),
            bookingUpdatesEnabled = prefs.getBoolean(KEY_BOOKING_UPDATES, true),
            scheduledReminders = readScheduledReminders(context),
            history = readHistory(context)
        )
    }

    fun setCategoryEnabled(context: Context, category: String, enabled: Boolean): NotificationPreferencesState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = when (category) {
            CATEGORY_TOUR_REMINDER -> KEY_TOUR_REMINDERS
            CATEGORY_SPECIAL_OFFER -> KEY_SPECIAL_OFFERS
            else -> KEY_BOOKING_UPDATES
        }
        prefs.edit().putBoolean(key, enabled).apply()
        return loadPreferencesState(context)
    }

    fun clearHistory(context: Context): NotificationPreferencesState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
        return loadPreferencesState(context)
    }

    fun showBookingUpdateNotification(
        context: Context,
        booking: BookingRecord,
        isNewSubmission: Boolean = false
    ) {
        ensureChannels(context)
        val state = loadPreferencesState(context)
        if (!state.bookingUpdatesEnabled) return

        val title = when {
            isNewSubmission -> "বুকিং জমা হয়েছে (${booking.referenceNumber})"
            booking.status == "Confirmed" -> "বুকিং কনফার্ম হয়েছে! (${booking.referenceNumber})"
            booking.status == "Completed" -> "ভ্রমণ সম্পন্ন হয়েছে — রিভিউ দিন!"
            booking.status == "Cancelled" -> "বুকিং স্ট্যাটাস আপডেট (${booking.referenceNumber})"
            else -> "বুকিং আপডেট (${booking.referenceNumber})"
        }

        val message = when {
            isNewSubmission ->
                "${booking.eventName} এর ${booking.selectedSeats.joinToString(", ")} সিট হোল্ড করা হয়েছে। কনফার্মেশন মেমো আপনার জিমেইল (${booking.touristEmail}) এর জন্য প্রস্তুত।"
            booking.status == "Confirmed" ->
                "অভিনন্দন ${booking.touristName}! আপনার ${booking.eventName} (${booking.tourDate}) বুকিং কনফার্ম হয়েছে। জিমেইল (${booking.touristEmail}) ইনবক্সে কনফার্মেশন মেমো পাঠাতে বা স্লিপ দেখতে ট্যাপ করুন।"
            booking.status == "Cancelled" ->
                "${booking.eventName} এর বুকিংটি বাতিল করা হয়েছে। বিস্তারিত জানতে যোগাযোগ করুন: ${BrandConstants.CONTACT_PHONE}"
            else ->
                "${booking.eventName} — বর্তমান স্ট্যাটাস: ${booking.status}। বিস্তারিত দেখতে ট্যাপ করুন।"
        }

        postNotification(
            context = context,
            channelId = CHANNEL_BOOKING_UPDATES,
            category = CATEGORY_BOOKING_UPDATE,
            notifId = ("booking_" + booking.referenceNumber + "_" + booking.status).hashCode(),
            title = title,
            message = message,
            bookingId = booking.id.ifBlank { booking.referenceNumber },
            eventId = null
        )
    }

    fun showSpecialOfferNotification(
        context: Context,
        title: String,
        message: String,
        eventId: String? = null
    ) {
        ensureChannels(context)
        val state = loadPreferencesState(context)
        if (!state.specialOffersEnabled) return

        postNotification(
            context = context,
            channelId = CHANNEL_SPECIAL_OFFERS,
            category = CATEGORY_SPECIAL_OFFER,
            notifId = ("offer_" + (eventId ?: title)).hashCode(),
            title = title,
            message = message,
            bookingId = null,
            eventId = eventId
        )
    }

    fun showTourReminderNotification(
        context: Context,
        title: String,
        message: String,
        eventId: String? = null,
        bookingId: String? = null
    ) {
        ensureChannels(context)
        val state = loadPreferencesState(context)
        if (!state.tourRemindersEnabled) return

        postNotification(
            context = context,
            channelId = CHANNEL_TOUR_REMINDERS,
            category = CATEGORY_TOUR_REMINDER,
            notifId = ("reminder_" + (eventId ?: bookingId ?: title)).hashCode(),
            title = title,
            message = message,
            bookingId = bookingId,
            eventId = eventId
        )
    }

    fun scheduleTourDateReminder(
        context: Context,
        event: TourEvent,
        customTriggerAtMillis: Long? = null
    ): ScheduledTourReminder {
        ensureChannels(context)
        val triggerAt = customTriggerAtMillis ?: computeReminderTimestamp(event.tourDate)
        val item = ScheduledTourReminder(
            eventId = event.id,
            eventName = event.name,
            tourDate = event.tourDate,
            triggerAtMillis = triggerAt
        )
        scheduleAlarmInternal(context, item)

        val current = readScheduledReminders(context).filterNot { it.eventId == event.id } + item
        saveScheduledReminders(context, current)
        return item
    }

    fun cancelTourDateReminder(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TourReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            ("alarm_$eventId").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)

        val updated = readScheduledReminders(context).filterNot { it.eventId == eventId }
        saveScheduledReminders(context, updated)
    }

    fun rescheduleSavedReminders(context: Context) {
        val now = System.currentTimeMillis()
        val reminders = readScheduledReminders(context)
        val valid = reminders.filter { it.triggerAtMillis > now - 60_000L }
        valid.forEach { scheduleAlarmInternal(context, it) }
        saveScheduledReminders(context, valid)
    }

    fun processLiveBookingsForUpdates(context: Context, bookings: List<BookingRecord>) {
        if (bookings.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString(KEY_BOOKING_STATUS_CACHE, null)
        val cachedMap = mutableMapOf<String, String>()
        if (cachedJson != null) {
            try {
                val obj = JSONObject(cachedJson)
                obj.keys().forEach { k -> cachedMap[k] = obj.optString(k, "") }
            } catch (_: Exception) {
            }
        }

        val isFirstBaseline = cachedJson == null
        val updatedObj = JSONObject()
        bookings.forEach { b ->
            val key = b.id.ifBlank { b.referenceNumber }
            updatedObj.put(key, b.status)
            if (!isFirstBaseline) {
                val previous = cachedMap[key]
                if (previous != null && previous != b.status) {
                    showBookingUpdateNotification(context, b, isNewSubmission = false)
                }
            }
        }
        prefs.edit().putString(KEY_BOOKING_STATUS_CACHE, updatedObj.toString()).apply()
    }

    fun processLiveEventsForOffers(context: Context, events: List<TourEvent>) {
        if (events.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val known = prefs.getStringSet(KEY_KNOWN_EVENT_IDS, null)
        val currentIds = events.map { it.id }.toSet()
        if (known == null) {
            prefs.edit().putStringSet(KEY_KNOWN_EVENT_IDS, currentIds).apply()
            return
        }
        val newEvents = events.filter { it.id !in known && (it.featured || it.category == "BUDGET") }
        newEvents.take(2).forEach { ev ->
            showSpecialOfferNotification(
                context = context,
                title = "নতুন স্পেশাল ট্যুর অফার!",
                message = "${ev.name} (${ev.tourDate}) — মাত্র ৳${ev.price}। খালি সিট: ${ev.availableSeatCount}টি!",
                eventId = ev.id
            )
        }
        if (newEvents.isNotEmpty()) {
            prefs.edit().putStringSet(KEY_KNOWN_EVENT_IDS, known + currentIds).apply()
        }
    }

    private fun scheduleAlarmInternal(context: Context, reminder: ScheduledTourReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val title = "আসন্ন ট্যুর রিমাইন্ডার: ${reminder.eventName}"
        val message = "আগামী ${reminder.tourDate} তারিখে আপনার ট্যুর যাত্রা! ব্যাগ গুছিয়ে নিন ও নির্ধারিত সময়ের ৩০ মিনিট আগে উঠার স্থানে উপস্থিত থাকুন।"
        val notifId = ("alarm_" + reminder.eventId).hashCode()

        val intent = Intent(context, TourReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_CATEGORY, CATEGORY_TOUR_REMINDER)
            putExtra(EXTRA_EVENT_ID, reminder.eventId)
            putExtra(EXTRA_NOTIF_ID, notifId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val safeTrigger = reminder.triggerAtMillis.coerceAtLeast(System.currentTimeMillis() + 2000L)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTrigger, pendingIntent)
    }

    internal fun postNotification(
        context: Context,
        channelId: String,
        category: String,
        notifId: Int,
        title: String,
        message: String,
        bookingId: String?,
        eventId: String?
    ) {
        ensureChannels(context)
        recordHistory(
            context,
            LocalNotificationItem(
                id = "${System.currentTimeMillis()}_$notifId",
                category = category,
                title = title,
                message = message,
                timestampMillis = System.currentTimeMillis(),
                bookingId = bookingId,
                eventId = eventId
            )
        )

        if (!hasNotificationPermission(context)) return

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!bookingId.isNullOrBlank()) putExtra(EXTRA_BOOKING_ID, bookingId)
            if (!eventId.isNullOrBlank()) putExtra(EXTRA_EVENT_ID, eventId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun recordHistory(context: Context, item: LocalNotificationItem) {
        val current = readHistory(context)
        val updated = (listOf(item) + current).take(25)
        val array = JSONArray()
        updated.forEach { n ->
            array.put(
                JSONObject().apply {
                    put("id", n.id)
                    put("category", n.category)
                    put("title", n.title)
                    put("message", n.message)
                    put("timestampMillis", n.timestampMillis)
                    if (!n.bookingId.isNullOrBlank()) put("bookingId", n.bookingId)
                    if (!n.eventId.isNullOrBlank()) put("eventId", n.eventId)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, array.toString())
            .apply()
    }

    private fun readHistory(context: Context): List<LocalNotificationItem> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        LocalNotificationItem(
                            id = obj.optString("id"),
                            category = obj.optString("category"),
                            title = obj.optString("title"),
                            message = obj.optString("message"),
                            timestampMillis = obj.optLong("timestampMillis"),
                            bookingId = obj.optString("bookingId").takeIf { it.isNotBlank() },
                            eventId = obj.optString("eventId").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveScheduledReminders(context: Context, items: List<ScheduledTourReminder>) {
        val arr = JSONArray()
        items.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("eventId", r.eventId)
                    put("eventName", r.eventName)
                    put("tourDate", r.tourDate)
                    put("triggerAtMillis", r.triggerAtMillis)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SCHEDULED_LIST, arr.toString())
            .apply()
    }

    private fun readScheduledReminders(context: Context): List<ScheduledTourReminder> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCHEDULED_LIST, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ScheduledTourReminder(
                            eventId = obj.optString("eventId"),
                            eventName = obj.optString("eventName"),
                            tourDate = obj.optString("tourDate"),
                            triggerAtMillis = obj.optLong("triggerAtMillis")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun computeReminderTimestamp(tourDateRaw: String): Long {
        val ascii = tourDateRaw
            .replace('০', '0').replace('১', '1').replace('২', '2').replace('৩', '3').replace('৪', '4')
            .replace('৫', '5').replace('৬', '6').replace('৭', '7').replace('৮', '8').replace('৯', '9')
        val dayMatch = Regex("\\d{1,2}").find(ascii)?.value?.toIntOrNull()?.coerceIn(1, 28)

        val monthMap = mapOf(
            "জানুয়ারি" to Calendar.JANUARY, "jan" to Calendar.JANUARY,
            "ফেব্রুয়ারি" to Calendar.FEBRUARY, "feb" to Calendar.FEBRUARY,
            "মার্চ" to Calendar.MARCH, "mar" to Calendar.MARCH,
            "এপ্রিল" to Calendar.APRIL, "apr" to Calendar.APRIL,
            "মে" to Calendar.MAY, "may" to Calendar.MAY,
            "জুন" to Calendar.JUNE, "jun" to Calendar.JUNE,
            "জুলাই" to Calendar.JULY, "jul" to Calendar.JULY,
            "আগস্ট" to Calendar.AUGUST, "aug" to Calendar.AUGUST,
            "সেপ্টেম্বর" to Calendar.SEPTEMBER, "sep" to Calendar.SEPTEMBER,
            "অক্টোবর" to Calendar.OCTOBER, "oct" to Calendar.OCTOBER,
            "নভেম্বর" to Calendar.NOVEMBER, "nov" to Calendar.NOVEMBER,
            "ডিসেম্বর" to Calendar.DECEMBER, "dec" to Calendar.DECEMBER
        )
        val lower = tourDateRaw.lowercase()
        val parsedMonth = monthMap.entries.firstOrNull { lower.contains(it.key) }?.value

        val now = Calendar.getInstance()
        if (dayMatch != null && parsedMonth != null) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, parsedMonth)
                set(Calendar.DAY_OF_MONTH, dayMatch)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                add(Calendar.DAY_OF_YEAR, -1) // Remind 1 day before tour at 9:00 AM
            }
            if (cal.timeInMillis <= now.timeInMillis) {
                cal.add(Calendar.YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Default fallback: 24 hours from now
        return now.timeInMillis + 24L * 60L * 60L * 1000L
    }
}
