package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.BookingRecord
import com.example.data.model.BrandConstants
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BookingDocumentPrinter {

    fun formatBookingDate(booking: BookingRecord): String {
        val ts = booking.createdAt?.toDate() ?: Date()
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(ts)
    }

    private fun buildConfirmationPdfDocument(booking: BookingRecord): PdfDocument {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val headerPaint = Paint().apply {
            color = Color.parseColor("#0B222A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 130f, headerPaint)

        val accentPaint = Paint().apply {
            color = Color.parseColor("#27C42D")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 130f, 595f, 136f, accentPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#F77C3C")
            textSize = 14f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#536B73")
            textSize = 12f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#10252C")
            textSize = 13.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CDE0E6")
            strokeWidth = 1f
        }

        canvas.drawText(BrandConstants.ORGANIZATION_NAME, 40f, 54f, titlePaint)
        canvas.drawText(BrandConstants.TAGLINE, 40f, 80f, subtitlePaint)
        canvas.drawText("OFFICIAL BOOKING CONFIRMATION SLIP", 40f, 108f, subtitlePaint.apply {
            color = Color.parseColor("#2596BE")
        })

        val dueAmount = (booking.totalAmount - booking.paidAmount).coerceAtLeast(0)
        val rows = buildList {
            add("Booking ID / Ref" to "${booking.referenceNumber} (Doc: ${booking.id.ifBlank { booking.referenceNumber }})")
            add("Booking Status" to booking.status)
            add("Tourist Name" to booking.touristName)
            if (booking.touristEmail.isNotBlank()) {
                add("Gmail Address" to booking.touristEmail)
            }
            add("Mobile / WhatsApp" to "${booking.mobileNumber} | WA: ${booking.whatsappNumber}")
            if (!booking.nidOrPassport.isNullOrBlank()) {
                add("NID / Passport" to booking.nidOrPassport)
            }
            add("Emergency Contact" to booking.emergencyContact)
            add("Address" to booking.address.take(56))
            add("Event Name" to booking.eventName.take(56))
            add("Destination & Date" to "${booking.destination} • ${booking.tourDate}")
            add("Boarding Point" to booking.boardingPoint.ifBlank { BrandConstants.ADDRESS }.take(56))
            add("Tourists & Seats" to "${booking.numberOfTourists} জন • সিট: ${booking.selectedSeats.joinToString(", ")}")
            add("Total / Paid / Due" to "মোট: ৳${booking.totalAmount} | জমা: ৳${booking.paidAmount} | বাকি: ৳$dueAmount")
            add("Payment Method" to "${booking.paymentMethod} • TrxID: ${booking.transactionId}")
            add("Payment Date" to booking.paymentDate)
            if (!booking.verifiedBy.isNullOrBlank()) {
                add("Verified By" to booking.verifiedBy)
            }
            if (!booking.specialInstructions.isNullOrBlank()) {
                add("Special Notes" to booking.specialInstructions.take(56))
            }
            add("Booking Created" to formatBookingDate(booking))
            add("Agency Helpline" to BrandConstants.CONTACT_PHONE)
        }

        var y = 166f
        val rowStep = if (rows.size > 16) 29.5f else 33f
        rows.forEach { (label, value) ->
            canvas.drawText(label, 42f, y, labelPaint)
            canvas.drawText(value, 210f, y, valuePaint)
            canvas.drawLine(40f, y + 8f, 555f, y + 8f, linePaint)
            y += rowStep
        }

        val footerPaint = Paint().apply {
            color = Color.parseColor("#536B73")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText(
            "ঠিকানা: ${BrandConstants.ADDRESS} | অ্যাডভান্স পেমেন্ট: ${BrandConstants.ADVANCE_PAYMENT_NUMBER}",
            40f,
            775f,
            footerPaint
        )
        canvas.drawText(
            "ভ্রমণের দিন এই ডিজিটাল বুকিং ডকুমেন্ট অথবা রেফারেন্স নম্বরটি সাথে রাখুন।",
            40f,
            797f,
            footerPaint
        )

        doc.finishPage(page)
        return doc
    }

    fun printBookingDocument(context: Context, booking: BookingRecord) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "${BrandConstants.ORGANIZATION_NAME} - ${booking.referenceNumber}"
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("${booking.referenceNumber}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                val pdf = buildConfirmationPdfDocument(booking)
                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        pdf.writeTo(out)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdf.close()
                }
            }
        }
        printManager.print(jobName, adapter, null)
    }

    fun shareOrDownloadBookingPdf(context: Context, booking: BookingRecord): Result<File> {
        return try {
            val dir = File(context.cacheDir, "booking_slips").apply { mkdirs() }
            val pdfFile = File(dir, "${booking.referenceNumber}_slip.pdf")
            val pdf = buildConfirmationPdfDocument(booking)
            FileOutputStream(pdfFile).use { out ->
                pdf.writeTo(out)
            }
            pdf.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "${BrandConstants.ORGANIZATION_NAME} বুকিং স্লিপ - ${booking.referenceNumber}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${BrandConstants.ORGANIZATION_NAME}\n${BrandConstants.TAGLINE}\nরেফারেন্স: ${booking.referenceNumber}\nইভেন্ট: ${booking.eventName}\nসিট: ${booking.selectedSeats.joinToString(", ")}\nস্ট্যাটাস: ${booking.status}\nযোগাযোগ: ${BrandConstants.CONTACT_PHONE}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "বুকিং ডকুমেন্ট ডাউনলোড / শেয়ার করুন")
            )
            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendConfirmationMemoToGmail(context: Context, booking: BookingRecord): Result<Unit> {
        return try {
            val dir = File(context.cacheDir, "booking_slips").apply { mkdirs() }
            val pdfFile = File(dir, "${booking.referenceNumber}_confirmation_memo.pdf")
            val pdf = buildConfirmationPdfDocument(booking)
            FileOutputStream(pdfFile).use { out ->
                pdf.writeTo(out)
            }
            pdf.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val dueAmount = (booking.totalAmount - booking.paidAmount).coerceAtLeast(0)
            val memoBody = buildString {
                appendLine("আসসালামু আলাইকুম ${booking.touristName},")
                appendLine()
                appendLine("${BrandConstants.ORGANIZATION_NAME} (${BrandConstants.TAGLINE}) এর পক্ষ থেকে আপনার বুকিং কনফার্মেশন মেমো নিচে দেওয়া হলো:")
                appendLine()
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("বুকিং আইডি / রেফারেন্স: ${booking.referenceNumber}")
                appendLine("বর্তমান স্ট্যাটাস: ${booking.status}")
                appendLine("ইভেন্টের নাম: ${booking.eventName}")
                appendLine("গন্তব্য: ${booking.destination}")
                appendLine("ভ্রমণের তারিখ: ${booking.tourDate}")
                appendLine("উঠার স্থান: ${booking.boardingPoint.ifBlank { BrandConstants.ADDRESS }}")
                appendLine("ভ্রমণকারীর সংখ্যা: ${booking.numberOfTourists} জন")
                appendLine("বরাদ্দকৃত সিট নম্বর: ${booking.selectedSeats.joinToString(", ")}")
                appendLine("মোট ভাড়া: ৳${booking.totalAmount} | অগ্রিম জমা: ৳${booking.paidAmount} | বকেয়া: ৳$dueAmount")
                appendLine("পেমেন্ট মাধ্যম: ${booking.paymentMethod} (TrxID: ${booking.transactionId})")
                appendLine("জিমেইল ইনবক্স: ${booking.touristEmail}")
                appendLine("মোবাইল: ${booking.mobileNumber}")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine()
                appendLine("সংযুক্ত PDF ফাইলটি আপনার অফিসিয়াল বুকিং কনফার্মেশন মেমো।")
                appendLine("রিপোর্টিংয়ের সময় নির্ধারিত সময়ের ৩০ মিনিট পূর্বে উঠার স্থানে উপস্থিত থাকুন।")
                appendLine("হেল্পলাইন: ${BrandConstants.CONTACT_PHONE} | ঠিকানা: ${BrandConstants.ADDRESS}")
            }

            val subject = "বুকিং কনফার্মেশন মেমো [${booking.referenceNumber}] - ${BrandConstants.ORGANIZATION_NAME}"
            val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                if (booking.touristEmail.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(booking.touristEmail.trim()))
                }
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, memoBody)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.google.android.gm")
            }

            val resolvedGmail = context.packageManager.resolveActivity(gmailIntent, 0)
            if (resolvedGmail != null) {
                context.startActivity(gmailIntent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    if (booking.touristEmail.isNotBlank()) {
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(booking.touristEmail.trim()))
                    }
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, memoBody)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(fallbackIntent, "জিমেইল ইনবক্সে কনফার্মেশন মেমো পাঠান")
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
