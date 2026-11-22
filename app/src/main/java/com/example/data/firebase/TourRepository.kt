package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.AdminAccount
import com.example.data.model.AdminRole
import com.example.data.model.AgencySettings
import com.example.data.model.BookingRecord
import com.example.data.model.BookingStatusLookup
import com.example.data.model.BrandConstants
import com.example.data.model.DestinationPlace
import com.example.data.model.TeamMember
import com.example.data.model.TourEvent
import com.example.data.model.TourReview
import com.example.data.security.PasswordHasher
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TourRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("অনুগ্রহ করে প্রথমে Google অ্যাকাউন্ট দিয়ে সাইন-ইন করুন।")

    private fun isBootstrapSuperAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.isEmailVerified && user.email == BrandConstants.SUPER_ADMIN_EMAIL
    }

    fun observeEvents(): Flow<List<TourEvent>> =
        db.collection("events")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TourEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.tourDate }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "events")
                throw error
            }

    fun observeDestinations(): Flow<List<DestinationPlace>> =
        db.collection("destinations")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DestinationPlace::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "destinations")
                throw error
            }

    fun observeTeamMembers(): Flow<List<TeamMember>> =
        db.collection("team_members")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TeamMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "team_members")
                throw error
            }

    fun observeAgencySettings(): Flow<AgencySettings> =
        db.collection("settings").document("agency")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(AgencySettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: SeedDataFactory.defaultSettings
                } else {
                    SeedDataFactory.defaultSettings
                }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.GET, "settings/agency")
                throw error
            }

    fun observeMyBookings(userId: String): Flow<List<BookingRecord>> =
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BookingRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "bookings")
                throw error
            }

    fun observeAllBookings(): Flow<List<BookingRecord>> =
        db.collection("bookings")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BookingRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "bookings")
                throw error
            }

    fun observeBookingById(bookingId: String): Flow<BookingRecord?> {
        val cleanId = bookingId.trim()
        if (!cleanId.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) {
            return kotlinx.coroutines.flow.flowOf(null)
        }
        return db.collection("bookings").document(cleanId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(BookingRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.GET, "bookings/$cleanId")
                throw error
            }
    }

    fun observeAdminAccounts(): Flow<List<AdminAccount>> =
        db.collection("admin_accounts")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AdminAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "admin_accounts")
                throw error
            }

    fun observeMyAdminRole(uid: String): Flow<AdminRole?> =
        db.collection("admins").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.GET, "admins/$uid")
                throw error
            }

    fun observeReviews(): Flow<List<TourReview>> =
        db.collection("reviews")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TourReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                logFlowErrorIfAuthenticated(error, OperationType.LIST, "reviews")
                throw error
            }

    suspend fun ensureInitialSeedIfAdmin(): Result<Unit> {
        return try {
            val uid = requireUserId()
            val adminDoc = db.collection("admins").document(uid).get().await()
            val canSeed = isBootstrapSuperAdmin() || adminDoc.exists()
            if (!canSeed) return Result.success(Unit)

            val settingsSnap = db.collection("settings").document("agency").get().await()
            val eventsSnap = db.collection("events").limit(1).get().await()
            val teamSnap = db.collection("team_members").limit(1).get().await()
            val adminAccountsSnap = db.collection("admin_accounts").limit(1).get().await()

            val batch = db.batch()
            var hasWrites = false

            if (!settingsSnap.exists()) {
                val s = SeedDataFactory.defaultSettings
                val map = mapOf(
                    "organizationName" to s.organizationName,
                    "tagline" to s.tagline,
                    "address" to s.address,
                    "contactPhone" to s.contactPhone,
                    "bookingPhone" to s.bookingPhone,
                    "advancePaymentNumber" to s.advancePaymentNumber,
                    "facebookUrl" to s.facebookUrl,
                    "whatsappUrl" to s.whatsappUrl,
                    "websiteUrl" to s.websiteUrl,
                    "welcomeBannerText" to s.welcomeBannerText,
                    "activeMonthTitle" to s.activeMonthTitle,
                    "bookingCounter" to 0,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                batch.set(db.collection("settings").document("agency"), map)
                hasWrites = true
            }

            if (eventsSnap.isEmpty) {
                SeedDataFactory.defaultEvents.forEach { ev ->
                    batch.set(db.collection("events").document(ev.id), eventToCreateMap(ev))
                }
                hasWrites = true
            }

            val destSnap = db.collection("destinations").limit(1).get().await()
            if (destSnap.isEmpty) {
                SeedDataFactory.defaultDestinations.forEach { dest ->
                    batch.set(db.collection("destinations").document(dest.id), destinationToCreateMap(dest))
                }
                hasWrites = true
            }

            if (teamSnap.isEmpty) {
                SeedDataFactory.defaultTeamMembers.forEach { tm ->
                    batch.set(db.collection("team_members").document(tm.id), teamMemberToMap(tm))
                }
                hasWrites = true
            }

            if (adminAccountsSnap.isEmpty && isBootstrapSuperAdmin()) {
                SeedDataFactory.buildInitialAdminAccounts().forEach { acc ->
                    batch.set(db.collection("admin_accounts").document(acc.id), adminAccountToCreateMap(acc))
                }
                val userEmail = auth.currentUser?.email ?: BrandConstants.SUPER_ADMIN_EMAIL
                batch.set(
                    db.collection("admins").document(uid),
                    mapOf(
                        "uid" to uid,
                        "email" to userEmail,
                        "adminAccountId" to "admin_8",
                        "role" to "SUPER_ADMIN",
                        "displayName" to "নাজিম উদ্দিন",
                        "canManageEvents" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                hasWrites = true
            }

            if (hasWrites) {
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed")
            Result.failure(e)
        }
    }

    suspend fun submitBookingTransaction(
        eventId: String,
        touristName: String,
        touristEmail: String,
        mobileNumber: String,
        whatsappNumber: String,
        numberOfTourists: Int,
        nidOrPassport: String?,
        emergencyContact: String,
        address: String,
        boardingPoint: String,
        selectedSeats: List<String>,
        paidAmount: Int,
        paymentMethod: String,
        transactionId: String,
        paymentDate: String,
        paymentScreenshotUri: String?,
        specialInstructions: String?
    ): Result<BookingRecord> {
        return try {
            val uid = requireUserId()
            val cleanEmail = touristEmail.trim().take(160)
            require(eventId.isNotBlank()) { "ইভেন্ট নির্বাচন করুন।" }
            require(touristName.trim().length in 2..100) { "ভ্রমণকারীর সঠিক নাম লিখুন।" }
            require(cleanEmail.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
                "সঠিক জিমেইল ঠিকানা (Gmail Address) দিন।"
            }
            require(mobileNumber.trim().length in 6..20) { "সঠিক মোবাইল নম্বর দিন।" }
            require(whatsappNumber.trim().length in 6..20) { "সঠিক WhatsApp নম্বর দিন।" }
            require(numberOfTourists in 1..20) { "ভ্রমণকারীর সংখ্যা ১ থেকে ২০ এর মধ্যে হতে হবে।" }
            require(selectedSeats.size == numberOfTourists) { "ভ্রমণকারীর সংখ্যার সমান সিট নির্বাচন করুন।" }
            require(emergencyContact.trim().length in 6..40) { "জরুরি যোগাযোগ নম্বর দিন।" }
            require(address.trim().length in 3..250) { "সম্পূর্ণ ঠিকানা লিখুন।" }
            require(boardingPoint.trim().length in 2..160) { "বাস/ট্যুর উঠার স্থান লিখুন।" }
            require(paymentMethod in BrandConstants.PAYMENT_METHODS) { "সঠিক পেমেন্ট মাধ্যম নির্বাচন করুন।" }
            require(transactionId.trim().length in 3..80) { "সঠিক Transaction ID দিন।" }
            require(paidAmount >= 0) { "পেমেন্টের পরিমাণ সঠিক নয়।" }

            val settingsRef = db.collection("settings").document("agency")
            val eventRef = db.collection("events").document(eventId)

            val createdRecord = db.runTransaction { tx ->
                val settingsSnap = tx.get(settingsRef)
                val eventSnap = tx.get(eventRef)
                if (!eventSnap.exists()) {
                    throw IllegalStateException("নির্বাচিত ইভেন্টটি পাওয়া যায়নি।")
                }

                val event = eventSnap.toObject(TourEvent::class.java)
                    ?: throw IllegalStateException("ইভেন্টের তথ্য পড়া যায়নি।")
                if (event.bookingStatus !in listOf("OPEN", "ALMOST_FULL")) {
                    throw IllegalStateException("এই ইভেন্টের বুকিং বর্তমানে বন্ধ রয়েছে।")
                }

                val occupied = (event.bookedSeats + event.holdSeats).toSet()
                val conflict = selectedSeats.firstOrNull { it in occupied }
                if (conflict != null) {
                    throw IllegalStateException("দুঃখিত! সিট $conflict ইতিমধ্যেই বুক বা হোল্ড হয়ে গেছে। অন্য সিট নির্বাচন করুন।")
                }

                val currentCounter = (settingsSnap.getLong("bookingCounter") ?: 0L).toInt()
                val nextCounter = currentCounter + 1
                val referenceNumber = "TEN-2026-${nextCounter.toString().padStart(4, '0')}"

                val updatedHoldSeats = (event.holdSeats + selectedSeats).distinct()
                val totalAmount = event.price * numberOfTourists

                if (settingsSnap.exists()) {
                    tx.update(
                        settingsRef,
                        mapOf(
                            "bookingCounter" to nextCounter,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    throw IllegalStateException("এজেন্সি সেটিংস প্রস্তুত নয়। অনুগ্রহ করে এডমিনকে জানান।")
                }

                tx.update(
                    eventRef,
                    mapOf(
                        "holdSeats" to updatedHoldSeats,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                val bookingRef = db.collection("bookings").document(referenceNumber)
                val lookupRef = db.collection("booking_status_lookup").document(referenceNumber)

                val cleanNid = nidOrPassport?.trim()?.takeIf { it.isNotBlank() }?.take(40)
                val cleanScreenshot = paymentScreenshotUri?.trim()?.takeIf { it.isNotBlank() }?.take(500)
                val cleanNotes = specialInstructions?.trim()?.takeIf { it.isNotBlank() }?.take(500)

                val bookingMap = mapOf(
                    "userId" to uid,
                    "referenceNumber" to referenceNumber,
                    "eventId" to eventId,
                    "eventName" to event.name.take(140),
                    "destination" to event.destination.take(100),
                    "tourDate" to event.tourDate.take(60),
                    "touristName" to touristName.trim(),
                    "touristEmail" to cleanEmail,
                    "mobileNumber" to mobileNumber.trim(),
                    "whatsappNumber" to whatsappNumber.trim(),
                    "numberOfTourists" to numberOfTourists,
                    "nidOrPassport" to cleanNid,
                    "emergencyContact" to emergencyContact.trim(),
                    "address" to address.trim(),
                    "boardingPoint" to boardingPoint.trim().take(160),
                    "selectedSeats" to selectedSeats,
                    "totalAmount" to totalAmount,
                    "paidAmount" to paidAmount,
                    "paymentMethod" to paymentMethod,
                    "transactionId" to transactionId.trim(),
                    "paymentDate" to paymentDate.trim().take(40),
                    "paymentScreenshotUri" to cleanScreenshot,
                    "specialInstructions" to cleanNotes,
                    "status" to "Payment Verification Pending",
                    "confirmationMemoSent" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                val lookupMap = mapOf(
                    "referenceNumber" to referenceNumber,
                    "bookingId" to referenceNumber,
                    "userId" to uid,
                    "eventName" to event.name.take(140),
                    "destination" to event.destination.take(100),
                    "tourDate" to event.tourDate.take(60),
                    "seatCount" to numberOfTourists,
                    "seatLabels" to selectedSeats.joinToString(", ").take(120),
                    "status" to "Payment Verification Pending",
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                tx.set(bookingRef, bookingMap)
                tx.set(lookupRef, lookupMap)

                BookingRecord(
                    id = referenceNumber,
                    userId = uid,
                    referenceNumber = referenceNumber,
                    eventId = eventId,
                    eventName = event.name,
                    destination = event.destination,
                    tourDate = event.tourDate,
                    touristName = touristName.trim(),
                    touristEmail = cleanEmail,
                    mobileNumber = mobileNumber.trim(),
                    whatsappNumber = whatsappNumber.trim(),
                    numberOfTourists = numberOfTourists,
                    nidOrPassport = cleanNid,
                    emergencyContact = emergencyContact.trim(),
                    address = address.trim(),
                    boardingPoint = boardingPoint.trim(),
                    selectedSeats = selectedSeats,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    paymentMethod = paymentMethod,
                    transactionId = transactionId.trim(),
                    paymentDate = paymentDate.trim(),
                    paymentScreenshotUri = cleanScreenshot,
                    specialInstructions = cleanNotes,
                    status = "Payment Verification Pending",
                    confirmationMemoSent = false
                )
            }.await()

            Result.success(createdRecord)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "bookings")
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatusTransaction(
        booking: BookingRecord,
        newStatus: String,
        verifiedBy: String?,
        assignedAdminId: String?
    ): Result<Unit> {
        val docPath = "bookings/${booking.id}"
        return try {
            requireUserId()
            val bookingRef = db.collection("bookings").document(booking.id)
            val lookupRef = db.collection("booking_status_lookup").document(booking.referenceNumber)
            val eventRef = db.collection("events").document(booking.eventId)

            db.runTransaction { tx ->
                val eventSnap = tx.get(eventRef)
                if (eventSnap.exists()) {
                    val event = eventSnap.toObject(TourEvent::class.java)
                    if (event != null) {
                        val seats = booking.selectedSeats.toSet()
                        var newBooked = event.bookedSeats.toMutableSet()
                        var newHold = event.holdSeats.toMutableSet()

                        when (newStatus) {
                            "Confirmed", "Completed" -> {
                                newHold.removeAll(seats)
                                newBooked.addAll(seats)
                            }
                            "Cancelled" -> {
                                newHold.removeAll(seats)
                                newBooked.removeAll(seats)
                            }
                            "Payment Verification Pending", "Pending" -> {
                                newBooked.removeAll(seats)
                                newHold.addAll(seats)
                            }
                        }

                        tx.update(
                            eventRef,
                            mapOf(
                                "bookedSeats" to newBooked.toList(),
                                "holdSeats" to newHold.toList(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                }

                val bookingUpdates = mutableMapOf<String, Any>(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                verifiedBy?.takeIf { it.isNotBlank() }?.let { bookingUpdates["verifiedBy"] = it.take(80) }
                assignedAdminId?.takeIf { it.isNotBlank() }?.let { bookingUpdates["assignedAdminId"] = it.take(64) }

                tx.update(bookingRef, bookingUpdates)
                tx.update(
                    lookupRef,
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markConfirmationMemoSent(bookingId: String): Result<Unit> {
        val cleanId = bookingId.trim()
        val path = "bookings/$cleanId"
        return try {
            requireUserId()
            val timestampText = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH)
                .format(java.util.Date())
            db.collection("bookings").document(cleanId).update(
                mapOf(
                    "confirmationMemoSent" to true,
                    "memoSentAt" to timestampText.take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun lookupBookingStatus(referenceNumber: String): Result<BookingStatusLookup?> {
        val cleanRef = referenceNumber.trim().uppercase()
        if (!cleanRef.matches(Regex("^[A-Za-z0-9_\\-]{4,40}$"))) {
            return Result.failure(IllegalArgumentException("সঠিক রেফারেন্স নম্বর লিখুন (যেমন: TEN-2026-0001)।"))
        }
        val path = "booking_status_lookup/$cleanRef"
        return try {
            requireUserId()
            val snap = db.collection("booking_status_lookup").document(cleanRef).get().await()
            val result = if (snap.exists()) {
                snap.toObject(BookingStatusLookup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun saveEvent(event: TourEvent, isCreate: Boolean): Result<Unit> {
        val eventId = event.id.ifBlank { "event_${System.currentTimeMillis()}" }
        val path = "events/$eventId"
        return try {
            requireUserId()
            val docRef = db.collection("events").document(eventId)
            if (isCreate) {
                docRef.set(eventToCreateMap(event.copy(id = eventId))).await()
            } else {
                val updates = mapOf(
                    "name" to event.name.trim().take(140),
                    "destination" to event.destination.trim().take(100),
                    "tourDate" to event.tourDate.trim().take(60),
                    "monthTag" to event.monthTag,
                    "category" to event.category,
                    "duration" to event.duration.trim().take(60),
                    "price" to event.price,
                    "advancePayment" to event.advancePayment,
                    "shortDescription" to event.shortDescription.trim().take(400),
                    "description" to event.description.trim().take(3000),
                    "imageKey" to event.imageKey,
                    "galleryKeys" to event.galleryKeys.ifEmpty { listOf(event.imageKey) }.take(10),
                    "totalSeats" to event.totalSeats.coerceAtLeast((event.bookedSeats.size + event.holdSeats.size).coerceAtLeast(4)),
                    "bookedSeats" to event.bookedSeats,
                    "holdSeats" to event.holdSeats,
                    "vehicleLayout" to event.vehicleLayout,
                    "bookingStatus" to event.bookingStatus,
                    "featured" to event.featured,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        val path = "events/$eventId"
        return try {
            requireUserId()
            db.collection("events").document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveDestination(dest: DestinationPlace, isCreate: Boolean): Result<Unit> {
        val destId = dest.id.ifBlank { "dest_${System.currentTimeMillis()}" }
        val path = "destinations/$destId"
        return try {
            requireUserId()
            val docRef = db.collection("destinations").document(destId)
            if (isCreate) {
                docRef.set(destinationToCreateMap(dest.copy(id = destId))).await()
            } else {
                docRef.update(
                    mapOf(
                        "name" to dest.name.trim().take(100),
                        "division" to dest.division.trim().take(80),
                        "shortDescription" to dest.shortDescription.trim().take(400),
                        "fullDetails" to dest.fullDetails.trim().take(3000),
                        "bestTime" to dest.bestTime.trim().take(120),
                        "imageKey" to dest.imageKey,
                        "relatedEventIds" to dest.relatedEventIds,
                        "sortOrder" to dest.sortOrder,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDestination(destId: String): Result<Unit> {
        val path = "destinations/$destId"
        return try {
            requireUserId()
            db.collection("destinations").document(destId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun updateAdminAccountAndTeamVisibility(
        account: AdminAccount,
        newPasscode: String?,
        showPublicContact: Boolean
    ): Result<Unit> {
        val accPath = "admin_accounts/${account.id}"
        return try {
            requireUserId()
            val salt = if (!newPasscode.isNullOrBlank()) PasswordHasher.generateSaltHex() else account.passwordSalt
            val hash = if (!newPasscode.isNullOrBlank()) PasswordHasher.hashPassword(newPasscode, salt) else account.passwordHash

            val accUpdates = mutableMapOf<String, Any>(
                "designation" to account.designation.trim().take(120),
                "role" to account.role,
                "passwordHash" to hash,
                "passwordSalt" to salt,
                "canManageEvents" to account.canManageEvents,
                "canVerifyPayments" to account.canVerifyPayments,
                "canManageBookings" to account.canManageBookings,
                "canManageDestinations" to account.canManageDestinations,
                "canManageSettings" to account.canManageSettings,
                "active" to account.active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            account.boundEmail?.trim()?.takeIf { it.isNotBlank() }?.let { accUpdates["boundEmail"] = it.take(160) }
            account.phone?.trim()?.takeIf { it.isNotBlank() }?.let { accUpdates["phone"] = it.take(30) }
            account.whatsappUrl?.trim()?.takeIf { it.isNotBlank() }?.let { accUpdates["whatsappUrl"] = it.take(300) }
            account.facebookUrl?.trim()?.takeIf { it.isNotBlank() }?.let { accUpdates["facebookUrl"] = it.take(300) }

            val teamMap = mapOf(
                "name" to account.name,
                "designation" to account.designation.trim().take(120),
                "sortOrder" to account.sortOrder,
                "showPublicContact" to showPublicContact,
                "publicPhone" to if (showPublicContact) account.phone?.trim()?.takeIf { it.isNotBlank() } else null,
                "whatsappUrl" to if (showPublicContact) account.whatsappUrl?.trim()?.takeIf { it.isNotBlank() } else null,
                "facebookUrl" to if (showPublicContact) account.facebookUrl?.trim()?.takeIf { it.isNotBlank() } else null,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val batch = db.batch()
            batch.update(db.collection("admin_accounts").document(account.id), accUpdates)
            batch.set(db.collection("team_members").document(account.id), teamMap)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, accPath)
            Result.failure(e)
        }
    }

    suspend fun updateAgencySettings(
        facebookUrl: String?,
        whatsappUrl: String?,
        welcomeBannerText: String?,
        activeMonthTitle: String? = null,
        websiteUrl: String? = null
    ): Result<Unit> {
        val path = "settings/agency"
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "organizationName" to BrandConstants.ORGANIZATION_NAME,
                "tagline" to BrandConstants.TAGLINE,
                "address" to BrandConstants.ADDRESS,
                "contactPhone" to BrandConstants.CONTACT_PHONE,
                "bookingPhone" to BrandConstants.BOOKING_PHONE,
                "advancePaymentNumber" to BrandConstants.ADVANCE_PAYMENT_NUMBER,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            facebookUrl?.trim()?.takeIf { it.isNotBlank() }?.let { updates["facebookUrl"] = it.take(300) }
            whatsappUrl?.trim()?.takeIf { it.isNotBlank() }?.let { updates["whatsappUrl"] = it.take(300) }
            websiteUrl?.trim()?.let { clean ->
                updates["websiteUrl"] = clean.ifBlank { BrandConstants.DEFAULT_WEB_APP_URL }.take(300)
            }
            welcomeBannerText?.trim()?.takeIf { it.isNotBlank() }?.let { updates["welcomeBannerText"] = it.take(300) }
            activeMonthTitle?.trim()?.takeIf { it.isNotBlank() }?.let { updates["activeMonthTitle"] = it.take(80) }

            db.collection("settings").document("agency").update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitReview(
        reviewerName: String,
        eventId: String?,
        eventName: String,
        rating: Int,
        comment: String
    ): Result<Unit> {
        val cleanName = reviewerName.trim().take(80)
        val cleanComment = comment.trim().take(600)
        val cleanEventName = eventName.trim().ifBlank { "সাধারণ ভ্রমণ অভিজ্ঞতা" }.take(140)
        val cleanEventId = eventId?.trim()?.takeIf {
            it.isNotBlank() && it.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))
        }

        return try {
            val uid = requireUserId()
            require(cleanName.length in 2..80) { "অনুগ্রহ করে আপনার নাম লিখুন (সর্বনিম্ন ২ অক্ষর)।" }
            require(rating in 1..5) { "অনুগ্রহ করে ১ থেকে ৫ স্টার রেটিং নির্বাচন করুন।" }
            require(cleanComment.length in 2..600) { "অনুগ্রহ করে আপনার মন্তব্য লিখুন (সর্বনিম্ন ২ অক্ষর)।" }

            val reviewId = "rev_${System.currentTimeMillis()}_${uid.take(6)}"
            val reviewMap = mapOf(
                "userId" to uid,
                "reviewerName" to cleanName,
                "eventId" to cleanEventId,
                "eventName" to cleanEventName,
                "rating" to rating,
                "comment" to cleanComment,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            db.collection("reviews").document(reviewId).set(reviewMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "reviews")
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        val cleanId = reviewId.trim()
        val path = "reviews/$cleanId"
        return try {
            requireUserId()
            require(cleanId.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) { "সঠিক রিভিউ আইডি পাওয়া যায়নি।" }
            db.collection("reviews").document(cleanId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    private fun eventToCreateMap(event: TourEvent): Map<String, Any> =
        mapOf(
            "name" to event.name.trim().take(140),
            "destination" to event.destination.trim().take(100),
            "tourDate" to event.tourDate.trim().take(60),
            "monthTag" to event.monthTag,
            "category" to event.category,
            "duration" to event.duration.trim().take(60),
            "price" to event.price,
            "advancePayment" to event.advancePayment,
            "shortDescription" to event.shortDescription.trim().take(400),
            "description" to event.description.trim().take(3000),
            "imageKey" to event.imageKey,
            "customImageUri" to event.customImageUri,
            "galleryKeys" to event.galleryKeys.take(10),
            "totalSeats" to event.totalSeats,
            "bookedSeats" to event.bookedSeats,
            "holdSeats" to event.holdSeats,
            "vehicleLayout" to event.vehicleLayout,
            "bookingStatus" to event.bookingStatus,
            "featured" to event.featured,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

    private fun destinationToCreateMap(dest: DestinationPlace): Map<String, Any> =
        mapOf(
            "name" to dest.name.trim().take(100),
            "division" to dest.division.trim().take(80),
            "shortDescription" to dest.shortDescription.trim().take(400),
            "fullDetails" to dest.fullDetails.trim().take(3000),
            "bestTime" to dest.bestTime.trim().take(120),
            "imageKey" to dest.imageKey,
            "relatedEventIds" to dest.relatedEventIds.take(10),
            "sortOrder" to dest.sortOrder,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

    private fun teamMemberToMap(tm: TeamMember): Map<String, Any> =
        mapOf(
            "name" to tm.name,
            "designation" to tm.designation,
            "sortOrder" to tm.sortOrder,
            "showPublicContact" to tm.showPublicContact,
            "publicPhone" to tm.publicPhone,
            "whatsappUrl" to tm.whatsappUrl,
            "facebookUrl" to tm.facebookUrl,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

    private fun adminAccountToCreateMap(acc: AdminAccount): Map<String, Any> =
        mapOf(
            "name" to acc.name,
            "username" to acc.username,
            "designation" to acc.designation,
            "role" to acc.role,
            "passwordHash" to acc.passwordHash,
            "passwordSalt" to acc.passwordSalt,
            "boundEmail" to acc.boundEmail,
            "phone" to acc.phone,
            "whatsappUrl" to acc.whatsappUrl,
            "facebookUrl" to acc.facebookUrl,
            "canManageEvents" to acc.canManageEvents,
            "canVerifyPayments" to acc.canVerifyPayments,
            "canManageBookings" to acc.canManageBookings,
            "canManageDestinations" to acc.canManageDestinations,
            "canManageSettings" to acc.canManageSettings,
            "active" to acc.active,
            "sortOrder" to acc.sortOrder,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

    private fun logFlowErrorIfAuthenticated(
        error: Throwable,
        operationType: OperationType,
        path: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}
