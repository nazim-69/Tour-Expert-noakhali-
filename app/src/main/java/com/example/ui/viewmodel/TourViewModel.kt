package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.SeedDataFactory
import com.example.data.firebase.TourRepository
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
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface BookingSubmitState {
    data object Idle : BookingSubmitState
    data object Submitting : BookingSubmitState
    data class Success(val booking: BookingRecord) : BookingSubmitState
    data class Error(val message: String) : BookingSubmitState
}

sealed interface LookupState {
    data object Idle : LookupState
    data object Searching : LookupState
    data class Found(val status: BookingStatusLookup) : LookupState
    data object NotFound : LookupState
    data class Error(val message: String) : LookupState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TourViewModel(
    private val repository: TourRepository,
    private val currentUserId: String,
    private val currentUserEmail: String?,
    private val isEmailVerified: Boolean
) : ViewModel() {

    val isBootstrapSuperAdmin: Boolean =
        isEmailVerified && currentUserEmail == BrandConstants.SUPER_ADMIN_EMAIL

    val eventsState: StateFlow<UiState<List<TourEvent>>> =
        repository.observeEvents()
            .map<List<TourEvent>, UiState<List<TourEvent>>> { list ->
                UiState.Success(list.ifEmpty { SeedDataFactory.defaultEvents })
            }
            .unwrapFirestoreErrors("events")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val cloudEventsEmpty: StateFlow<Boolean> =
        repository.observeEvents()
            .map { it.isEmpty() }
            .catch { emit(false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val destinationsState: StateFlow<UiState<List<DestinationPlace>>> =
        repository.observeDestinations()
            .map<List<DestinationPlace>, UiState<List<DestinationPlace>>> { list ->
                UiState.Success(list.ifEmpty { SeedDataFactory.defaultDestinations })
            }
            .unwrapFirestoreErrors("destinations")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val teamMembersState: StateFlow<UiState<List<TeamMember>>> =
        repository.observeTeamMembers()
            .map<List<TeamMember>, UiState<List<TeamMember>>> { list ->
                UiState.Success(list.ifEmpty { SeedDataFactory.defaultTeamMembers })
            }
            .unwrapFirestoreErrors("team_members")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val agencySettings: StateFlow<AgencySettings> =
        repository.observeAgencySettings()
            .catch { error ->
                if (error is CancellationException) throw error
                emit(SeedDataFactory.defaultSettings)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedDataFactory.defaultSettings)

    val myBookingsState: StateFlow<UiState<List<BookingRecord>>> =
        repository.observeMyBookings(currentUserId)
            .map<List<BookingRecord>, UiState<List<BookingRecord>>> { UiState.Success(it) }
            .unwrapFirestoreErrors("my_bookings")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val myAdminRole: StateFlow<AdminRole?> =
        repository.observeMyAdminRole(currentUserId)
            .catch { error ->
                if (error is CancellationException) throw error
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val hasFirestoreAdminPrivilege: StateFlow<Boolean> =
        myAdminRole
            .map { role -> isBootstrapSuperAdmin || role != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), isBootstrapSuperAdmin)

    val allBookingsState: StateFlow<UiState<List<BookingRecord>>> =
        hasFirestoreAdminPrivilege
            .flatMapLatest { isAdmin ->
                if (!isAdmin) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeAllBookings()
                        .map<List<BookingRecord>, UiState<List<BookingRecord>>> { UiState.Success(it) }
                        .unwrapFirestoreErrors("all_bookings")
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val adminAccountsState: StateFlow<UiState<List<AdminAccount>>> =
        hasFirestoreAdminPrivilege
            .flatMapLatest { isAdmin ->
                if (!isAdmin) {
                    flowOf(UiState.Success(emptyList()))
                } else {
                    repository.observeAdminAccounts()
                        .map<List<AdminAccount>, UiState<List<AdminAccount>>> { UiState.Success(it) }
                        .unwrapFirestoreErrors("admin_accounts")
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _selectedAdminSession = MutableStateFlow<AdminAccount?>(null)
    val selectedAdminSession: StateFlow<AdminAccount?> = _selectedAdminSession.asStateFlow()

    val activeAdminAccount: StateFlow<AdminAccount?> =
        combine(
            selectedAdminSession,
            adminAccountsState,
            myAdminRole
        ) { selected, accountsState, role ->
            if (selected != null) {
                selected
            } else if (isBootstrapSuperAdmin && accountsState is UiState.Success) {
                accountsState.data.firstOrNull { it.role == "SUPER_ADMIN" }
                    ?: accountsState.data.lastOrNull()
            } else if (role != null && accountsState is UiState.Success) {
                accountsState.data.firstOrNull { it.id == role.adminAccountId }
            } else {
                null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val reviewsState: StateFlow<UiState<List<TourReview>>> =
        repository.observeReviews()
            .map<List<TourReview>, UiState<List<TourReview>>> { UiState.Success(it) }
            .unwrapFirestoreErrors("reviews")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview.asStateFlow()

    private val _activeBookingDetailId = MutableStateFlow<String?>(null)
    val activeBookingDetailId: StateFlow<String?> = _activeBookingDetailId.asStateFlow()

    private val _bookingDetailRefreshTick = MutableStateFlow(0)

    val bookingDetailState: StateFlow<UiState<BookingRecord?>> =
        combine(_activeBookingDetailId, _bookingDetailRefreshTick) { bookingId, _ -> bookingId }
            .flatMapLatest { bookingId ->
                if (bookingId.isNullOrBlank()) {
                    flowOf(UiState.Success(null))
                } else {
                    repository.observeBookingById(bookingId)
                        .map<BookingRecord?, UiState<BookingRecord?>> { UiState.Success(it) }
                        .unwrapFirestoreErrors("booking_detail_$bookingId")
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _bookingSubmitState = MutableStateFlow<BookingSubmitState>(BookingSubmitState.Idle)
    val bookingSubmitState: StateFlow<BookingSubmitState> = _bookingSubmitState.asStateFlow()

    private val _lookupState = MutableStateFlow<LookupState>(LookupState.Idle)
    val lookupState: StateFlow<LookupState> = _lookupState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            if (isBootstrapSuperAdmin) {
                repository.ensureInitialSeedIfAdmin()
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun openBookingDetail(bookingId: String) {
        val cleanId = bookingId.trim()
        if (cleanId.isNotBlank()) {
            _activeBookingDetailId.value = cleanId
        }
    }

    fun retryBookingDetail() {
        _bookingDetailRefreshTick.value += 1
    }

    fun closeBookingDetail() {
        _activeBookingDetailId.value = null
    }

    fun resetBookingSubmitState() {
        _bookingSubmitState.value = BookingSubmitState.Idle
    }

    fun submitBooking(
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
    ) {
        viewModelScope.launch {
            _bookingSubmitState.value = BookingSubmitState.Submitting
            val result = repository.submitBookingTransaction(
                eventId = eventId,
                touristName = touristName,
                touristEmail = touristEmail,
                mobileNumber = mobileNumber,
                whatsappNumber = whatsappNumber,
                numberOfTourists = numberOfTourists,
                nidOrPassport = nidOrPassport,
                emergencyContact = emergencyContact,
                address = address,
                boardingPoint = boardingPoint,
                selectedSeats = selectedSeats,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                transactionId = transactionId,
                paymentDate = paymentDate,
                paymentScreenshotUri = paymentScreenshotUri,
                specialInstructions = specialInstructions
            )
            result.fold(
                onSuccess = { booking ->
                    _bookingSubmitState.value = BookingSubmitState.Success(booking)
                },
                onFailure = { error ->
                    _bookingSubmitState.value = BookingSubmitState.Error(
                        error.localizedMessage ?: "বুকিং সম্পন্ন করতে সমস্যা হয়েছে।"
                    )
                }
            )
        }
    }

    fun markConfirmationMemoSent(bookingId: String) {
        viewModelScope.launch {
            repository.markConfirmationMemoSent(bookingId).fold(
                onSuccess = {
                    _actionMessage.value = "কনফার্মেশন মেমো স্ট্যাটাস আপডেট হয়েছে।"
                },
                onFailure = { /* Ignore silent tracking error if offline */ }
            )
        }
    }

    fun lookupReference(referenceNumber: String) {
        viewModelScope.launch {
            _lookupState.value = LookupState.Searching
            val result = repository.lookupBookingStatus(referenceNumber)
            result.fold(
                onSuccess = { status ->
                    _lookupState.value = if (status != null) {
                        LookupState.Found(status)
                    } else {
                        LookupState.NotFound
                    }
                },
                onFailure = { error ->
                    _lookupState.value = LookupState.Error(
                        error.localizedMessage ?: "রেফারেন্স খোঁজা সম্ভব হয়নি।"
                    )
                }
            )
        }
    }

    fun resetLookupState() {
        _lookupState.value = LookupState.Idle
    }

    fun loginAsIndividualAdmin(usernameOrId: String, passcode: String) {
        val state = adminAccountsState.value
        if (state !is UiState.Success) {
            _actionMessage.value = "এডমিন অ্যাকাউন্ট তালিকা লোড হচ্ছে..."
            return
        }
        val cleanKey = usernameOrId.trim().lowercase()
        val match = state.data.firstOrNull {
            it.username.lowercase() == cleanKey || it.id.lowercase() == cleanKey || it.name == usernameOrId.trim()
        }
        if (match == null) {
            _actionMessage.value = "এডমিন অ্যাকাউন্ট খুঁজে পাওয়া যায়নি।"
            return
        }
        if (!match.active) {
            _actionMessage.value = "এই এডমিন অ্যাকাউন্টটি সাময়িকভাবে নিষ্ক্রিয় রয়েছে।"
            return
        }
        if (PasswordHasher.verifyPassword(passcode, match.passwordSalt, match.passwordHash)) {
            _selectedAdminSession.value = match
            _actionMessage.value = "${match.name} (${match.designation}) হিসেবে এডমিন ড্যাশবোর্ডে প্রবেশ করেছেন।"
        } else {
            _actionMessage.value = "ভুল পাসওয়ার্ড! সুপার এডমিন ড্যাশবোর্ড থেকে পাসওয়ার্ড রিসেট করুন।"
        }
    }

    fun logoutAdminSession() {
        _selectedAdminSession.value = null
    }

    fun verifyAndUpdateBookingStatus(
        booking: BookingRecord,
        newStatus: String,
        assignedAdminId: String? = null
    ) {
        val actorName = activeAdminAccount.value?.name ?: currentUserEmail ?: "Admin"
        viewModelScope.launch {
            val result = repository.updateBookingStatusTransaction(
                booking = booking,
                newStatus = newStatus,
                verifiedBy = actorName,
                assignedAdminId = assignedAdminId ?: booking.assignedAdminId
            )
            result.fold(
                onSuccess = {
                    _actionMessage.value = "বুকিং ${booking.referenceNumber} এর স্ট্যাটাস '$newStatus' এ আপডেট হয়েছে।"
                },
                onFailure = { e ->
                    _actionMessage.value = e.localizedMessage ?: "স্ট্যাটাস আপডেট ব্যর্থ হয়েছে।"
                }
            )
        }
    }

    private fun canCurrentActorManageEvents(): Boolean {
        if (!hasFirestoreAdminPrivilege.value) return false
        if (isBootstrapSuperAdmin) return true
        val activeAcc = activeAdminAccount.value
        if (activeAcc != null) {
            return activeAcc.role == "SUPER_ADMIN" || activeAcc.canManageEvents
        }
        return myAdminRole.value?.canManageEvents == true
    }

    fun saveEvent(event: TourEvent, isCreate: Boolean) {
        if (!canCurrentActorManageEvents()) {
            _actionMessage.value = "শুধুমাত্র অনুমোদিত এডমিনরা প্রতিমাসের ইভেন্ট শিডিউল পরিবর্তন করতে পারবেন।"
            return
        }
        viewModelScope.launch {
            repository.saveEvent(event, isCreate).fold(
                onSuccess = { _actionMessage.value = "মাসিক ইভেন্ট শিডিউল সফলভাবে সংরক্ষিত হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "ইভেন্ট সংরক্ষণ ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun updateEventBookingStatus(event: TourEvent, newStatus: String) {
        if (!canCurrentActorManageEvents()) {
            _actionMessage.value = "শুধুমাত্র অনুমোদিত এডমিনরা ইভেন্ট স্ট্যাটাস পরিবর্তন করতে পারবেন।"
            return
        }
        viewModelScope.launch {
            repository.saveEvent(event.copy(bookingStatus = newStatus), isCreate = false).fold(
                onSuccess = {
                    val label = when (newStatus) {
                        "OPEN" -> "বুকিং চালু"
                        "ALMOST_FULL" -> "সিট প্রায় শেষ"
                        "CLOSED" -> "বুকিং বন্ধ"
                        "COMPLETED" -> "সম্পন্ন"
                        else -> newStatus
                    }
                    _actionMessage.value = "${event.name} এর স্ট্যাটাস '$label' এ আপডেট হয়েছে।"
                },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "স্ট্যাটাস আপডেট ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun deleteEvent(eventId: String) {
        if (!canCurrentActorManageEvents()) {
            _actionMessage.value = "শুধুমাত্র অনুমোদিত এডমিনরা ইভেন্ট শিডিউল থেকে ইভেন্ট মুছতে পারবেন।"
            return
        }
        viewModelScope.launch {
            repository.deleteEvent(eventId).fold(
                onSuccess = { _actionMessage.value = "ইভেন্ট শিডিউল থেকে মুছে ফেলা হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "ইভেন্ট মোছা ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun saveDestination(dest: DestinationPlace, isCreate: Boolean) {
        viewModelScope.launch {
            repository.saveDestination(dest, isCreate).fold(
                onSuccess = { _actionMessage.value = "পর্যটন স্থান সংরক্ষিত হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "সংরক্ষণ ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun deleteDestination(destId: String) {
        viewModelScope.launch {
            repository.deleteDestination(destId).fold(
                onSuccess = { _actionMessage.value = "পর্যটন স্থান মুছে ফেলা হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "মোছা ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun updateAdminAccount(
        account: AdminAccount,
        newPasscode: String?,
        showPublicContact: Boolean
    ) {
        viewModelScope.launch {
            repository.updateAdminAccountAndTeamVisibility(account, newPasscode, showPublicContact).fold(
                onSuccess = { _actionMessage.value = "${account.name} এর তথ্য ও অনুমতি সংরক্ষিত হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "আপডেট ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun updateAgencySettings(
        facebookUrl: String?,
        whatsappUrl: String?,
        welcomeBannerText: String?,
        activeMonthTitle: String? = null,
        websiteUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.updateAgencySettings(
                facebookUrl = facebookUrl,
                whatsappUrl = whatsappUrl,
                welcomeBannerText = welcomeBannerText,
                activeMonthTitle = activeMonthTitle,
                websiteUrl = websiteUrl
            ).fold(
                onSuccess = { _actionMessage.value = "মাসিক শিডিউল, লিংক ও এজেন্সি সেটিংস সফলভাবে আপডেট হয়েছে।" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "সেটিংস আপডেট ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun triggerSeedData() {
        viewModelScope.launch {
            repository.ensureInitialSeedIfAdmin().fold(
                onSuccess = { _actionMessage.value = "অফিসিয়াল ইভেন্ট, গন্তব্য ও ৮ এডমিন ডেটা সিঙ্ক সম্পন্ন!" },
                onFailure = { _actionMessage.value = it.localizedMessage ?: "সিঙ্ক ব্যর্থ হয়েছে।" }
            )
        }
    }

    fun submitReview(
        reviewerName: String,
        eventId: String?,
        eventName: String,
        rating: Int,
        comment: String,
        onSuccessClear: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingReview.value = true
            val result = repository.submitReview(
                reviewerName = reviewerName,
                eventId = eventId,
                eventName = eventName,
                rating = rating,
                comment = comment
            )
            _isSubmittingReview.value = false
            result.fold(
                onSuccess = {
                    onSuccessClear()
                    _actionMessage.value = "আপনার মূল্যবান মতামত ও রেটিংয়ের জন্য ধন্যবাদ!"
                },
                onFailure = { error ->
                    _actionMessage.value = error.localizedMessage ?: "মতামত জমা দিতে সমস্যা হয়েছে।"
                }
            )
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            repository.deleteReview(reviewId).fold(
                onSuccess = { _actionMessage.value = "মতামতটি মুছে ফেলা হয়েছে।" },
                onFailure = { error ->
                    _actionMessage.value = error.localizedMessage ?: "মতামত মোছা সম্ভব হয়নি।"
                }
            )
        }
    }

    private fun <T> Flow<UiState<T>>.unwrapFirestoreErrors(tagLabel: String): Flow<UiState<T>> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing $tagLabel", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "ডেটা লোড করতে সমস্যা হয়েছে"))
        }

    private companion object {
        const val TAG = "TourVM"
    }
}
