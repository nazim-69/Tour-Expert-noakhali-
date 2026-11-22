package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TourReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(TourNotificationManager.EXTRA_TITLE)
            ?: "ট্যুর এক্সপার্ট নোয়াখালী রিমাইন্ডার"
        val message = intent.getStringExtra(TourNotificationManager.EXTRA_MESSAGE)
            ?: "আপনার আসন্ন ভ্রমণের সময় হয়ে এসেছে!"
        val category = intent.getStringExtra(TourNotificationManager.EXTRA_CATEGORY)
            ?: TourNotificationManager.CATEGORY_TOUR_REMINDER
        val bookingId = intent.getStringExtra(TourNotificationManager.EXTRA_BOOKING_ID)
        val eventId = intent.getStringExtra(TourNotificationManager.EXTRA_EVENT_ID)
        val notifId = intent.getIntExtra(
            TourNotificationManager.EXTRA_NOTIF_ID,
            System.currentTimeMillis().toInt()
        )

        val channelId = when (category) {
            TourNotificationManager.CATEGORY_SPECIAL_OFFER -> TourNotificationManager.CHANNEL_SPECIAL_OFFERS
            TourNotificationManager.CATEGORY_BOOKING_UPDATE -> TourNotificationManager.CHANNEL_BOOKING_UPDATES
            else -> TourNotificationManager.CHANNEL_TOUR_REMINDERS
        }

        TourNotificationManager.postNotification(
            context = context,
            channelId = channelId,
            category = category,
            notifId = notifId,
            title = title,
            message = message,
            bookingId = bookingId,
            eventId = eventId
        )
    }
}
