package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.SeedDataFactory
import com.example.data.firebase.TourRepository
import com.example.notifications.TourNotificationManager
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.CredentialAuthHelper
import com.example.ui.components.AnimatedMobileMenuOverlay
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.StickyTopBarHeader
import com.example.ui.components.TopMenuItem
import com.example.ui.components.WelcomeSplashOverlay
import com.example.ui.components.shareWebsiteAppLink
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.BookingDetailsScreen
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.DestinationsScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TeamAndContactScreen
import com.example.ui.theme.BrandOceanBlue
import com.example.ui.theme.TourExpertTheme
import com.example.ui.viewmodel.TourViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_EVENTS = "events"
const val ROUTE_DESTINATIONS = "destinations"
const val ROUTE_BOOKING = "booking"
const val ROUTE_TEAM = "team"
const val ROUTE_ADMIN = "admin"

class MainActivity : ComponentActivity() {
    private var pendingBookingId by mutableStateOf<String?>(null)
    private var pendingEventId by mutableStateOf<String?>(null)
    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeIntentExtras(intent)
        setContent {
            TourExpertTheme {
                AppContent(
                    pendingBookingId = pendingBookingId,
                    pendingEventId = pendingEventId,
                    pendingRoute = pendingRoute,
                    onConsumePendingBookingId = { pendingBookingId = null },
                    onConsumePendingEventId = { pendingEventId = null },
                    onConsumePendingRoute = { pendingRoute = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntentExtras(intent)
    }

    private fun consumeIntentExtras(intent: Intent?) {
        intent ?: return
        intent.getStringExtra(TourNotificationManager.EXTRA_BOOKING_ID)?.takeIf { it.isNotBlank() }?.let {
            pendingBookingId = it
            intent.removeExtra(TourNotificationManager.EXTRA_BOOKING_ID)
        }
        intent.getStringExtra(TourNotificationManager.EXTRA_EVENT_ID)?.takeIf { it.isNotBlank() }?.let {
            pendingEventId = it
            intent.removeExtra(TourNotificationManager.EXTRA_EVENT_ID)
        }

        val uri = intent.data
        if (uri != null) {
            uri.getQueryParameter("booking")?.takeIf { it.isNotBlank() }?.let { pendingBookingId = it }
            uri.getQueryParameter("event")?.takeIf { it.isNotBlank() }?.let { pendingEventId = it }
            uri.getQueryParameter("page")?.takeIf { it.isNotBlank() }?.let { page ->
                pendingRoute = normalizeDeepLinkRoute(page)
            }

            val segments = uri.pathSegments.orEmpty()
            when {
                segments.size >= 2 && segments[0] == "event" -> pendingEventId = segments[1]
                segments.size >= 2 && segments[0] == "booking" -> pendingBookingId = segments[1]
                segments.size == 1 -> normalizeDeepLinkRoute(segments[0])?.let { pendingRoute = it }
            }
            intent.data = null
        }
    }

    private fun normalizeDeepLinkRoute(raw: String): String? =
        when (raw.trim().lowercase()) {
            "home" -> ROUTE_HOME
            "events" -> ROUTE_EVENTS
            "destinations" -> ROUTE_DESTINATIONS
            "booking" -> ROUTE_BOOKING
            "team", "about", "contact" -> ROUTE_TEAM
            "admin" -> ROUTE_ADMIN
            else -> null
        }
}

@Composable
fun AppContent(
    pendingBookingId: String? = null,
    pendingEventId: String? = null,
    pendingRoute: String? = null,
    onConsumePendingBookingId: () -> Unit = {},
    onConsumePendingEventId: () -> Unit = {},
    onConsumePendingRoute: () -> Unit = {}
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var showWelcomeSplash by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val user = currentUser
        if (user == null) {
            AuthScreen(
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    showWelcomeSplash = true
                }
            )
        } else {
            val uid = user.uid
            key(uid) {
                TourExpertMainShell(
                    currentUserId = uid,
                    currentUserEmail = user.email,
                    currentUserDisplayName = user.displayName,
                    isEmailVerified = user.isEmailVerified,
                    pendingBookingId = pendingBookingId,
                    pendingEventId = pendingEventId,
                    pendingRoute = pendingRoute,
                    onConsumePendingBookingId = onConsumePendingBookingId,
                    onConsumePendingEventId = onConsumePendingEventId,
                    onConsumePendingRoute = onConsumePendingRoute,
                    onReplayWelcome = { showWelcomeSplash = true }
                )
            }
        }

        WelcomeSplashOverlay(
            visible = showWelcomeSplash,
            onDismiss = { showWelcomeSplash = false }
        )
    }
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun TourExpertMainShell(
    currentUserId: String,
    currentUserEmail: String?,
    currentUserDisplayName: String?,
    isEmailVerified: Boolean,
    pendingBookingId: String?,
    pendingEventId: String?,
    pendingRoute: String?,
    onConsumePendingBookingId: () -> Unit,
    onConsumePendingEventId: () -> Unit,
    onConsumePendingRoute: () -> Unit,
    onReplayWelcome: () -> Unit
) {
    val viewModel: TourViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                TourViewModel(
                    repository = TourRepository(db),
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    isEmailVerified = isEmailVerified
                )
            }
        }
    )

    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
    val destinationsState by viewModel.destinationsState.collectAsStateWithLifecycle()
    val teamMembersState by viewModel.teamMembersState.collectAsStateWithLifecycle()
    val agencySettings by viewModel.agencySettings.collectAsStateWithLifecycle()
    val myBookingsState by viewModel.myBookingsState.collectAsStateWithLifecycle()
    val allBookingsState by viewModel.allBookingsState.collectAsStateWithLifecycle()
    val adminAccountsState by viewModel.adminAccountsState.collectAsStateWithLifecycle()
    val hasFirestoreAdminPrivilege by viewModel.hasFirestoreAdminPrivilege.collectAsStateWithLifecycle()
    val activeAdminAccount by viewModel.activeAdminAccount.collectAsStateWithLifecycle()
    val cloudEventsEmpty by viewModel.cloudEventsEmpty.collectAsStateWithLifecycle()
    val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
    val isSubmittingReview by viewModel.isSubmittingReview.collectAsStateWithLifecycle()
    val activeBookingDetailId by viewModel.activeBookingDetailId.collectAsStateWithLifecycle()
    val bookingDetailState by viewModel.bookingDetailState.collectAsStateWithLifecycle()
    val bookingSubmitState by viewModel.bookingSubmitState.collectAsStateWithLifecycle()
    val lookupState by viewModel.lookupState.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val events = (eventsState as? UiState.Success)?.data ?: SeedDataFactory.defaultEvents
    val destinations = (destinationsState as? UiState.Success)?.data ?: SeedDataFactory.defaultDestinations
    val teamMembers = (teamMembersState as? UiState.Success)?.data ?: SeedDataFactory.defaultTeamMembers
    val myBookings = (myBookingsState as? UiState.Success)?.data ?: emptyList()
    val allBookings = (allBookingsState as? UiState.Success)?.data ?: emptyList()
    val adminAccounts = (adminAccountsState as? UiState.Success)?.data ?: emptyList()

    var currentRoute by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    var activeMenuId by rememberSaveable { mutableStateOf("home") }
    var mobileMenuExpanded by remember { mutableStateOf(false) }
    var teamFocusSection by rememberSaveable { mutableStateOf<String?>(null) }
    var teamFocusTrigger by rememberSaveable { mutableIntStateOf(0) }
    var detailEventId by rememberSaveable { mutableStateOf<String?>(null) }
    var bookingPreselectedEventId by rememberSaveable { mutableStateOf<String?>(null) }
    var bookingInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var adminInitialTab by rememberSaveable { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    var showNotificationCenter by remember { mutableStateOf(false) }
    var notificationCount by remember {
        mutableIntStateOf(TourNotificationManager.loadPreferencesState(context).history.size)
    }

    LaunchedEffect(Unit) {
        TourNotificationManager.ensureChannels(context)
    }

    LaunchedEffect(myBookings) {
        TourNotificationManager.processLiveBookingsForUpdates(context, myBookings)
        notificationCount = TourNotificationManager.loadPreferencesState(context).history.size
    }

    LaunchedEffect(events) {
        TourNotificationManager.processLiveEventsForOffers(context, events)
        notificationCount = TourNotificationManager.loadPreferencesState(context).history.size
    }

    LaunchedEffect(bookingSubmitState) {
        notificationCount = TourNotificationManager.loadPreferencesState(context).history.size
    }

    LaunchedEffect(pendingBookingId) {
        val bId = pendingBookingId
        if (!bId.isNullOrBlank()) {
            mobileMenuExpanded = false
            detailEventId = null
            viewModel.openBookingDetail(bId)
            onConsumePendingBookingId()
        }
    }

    LaunchedEffect(pendingEventId) {
        val eId = pendingEventId
        if (!eId.isNullOrBlank()) {
            mobileMenuExpanded = false
            viewModel.closeBookingDetail()
            detailEventId = eId
            onConsumePendingEventId()
        }
    }

    LaunchedEffect(pendingRoute) {
        val r = pendingRoute
        if (!r.isNullOrBlank()) {
            mobileMenuExpanded = false
            detailEventId = null
            viewModel.closeBookingDetail()
            currentRoute = r
            activeMenuId = when (r) {
                ROUTE_HOME -> "home"
                ROUTE_EVENTS -> "events"
                ROUTE_DESTINATIONS -> "destinations"
                ROUTE_BOOKING -> "booking"
                ROUTE_TEAM -> "about"
                ROUTE_ADMIN -> "admin"
                else -> "home"
            }
            onConsumePendingRoute()
        }
    }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    if (mobileMenuExpanded) {
        BackHandler { mobileMenuExpanded = false }
    } else if (activeBookingDetailId != null) {
        BackHandler { viewModel.closeBookingDetail() }
    } else if (detailEventId != null) {
        BackHandler { detailEventId = null }
    } else if (currentRoute != ROUTE_HOME) {
        BackHandler {
            currentRoute = ROUTE_HOME
            activeMenuId = "home"
        }
    }

    val navItems = listOf(
        NavTabItem(ROUTE_HOME, "হোম", Icons.Filled.Home, Icons.Outlined.Home),
        NavTabItem(ROUTE_EVENTS, "ইভেন্ট", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        NavTabItem(ROUTE_DESTINATIONS, "গন্তব্য", Icons.Filled.Explore, Icons.Outlined.Explore),
        NavTabItem(ROUTE_BOOKING, "বুকিং", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        NavTabItem(ROUTE_ADMIN, "এডমিন", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    )

    val defaultMenuIdForRoute: (String) -> String = { route ->
        when (route) {
            ROUTE_HOME -> "home"
            ROUTE_EVENTS -> "events"
            ROUTE_DESTINATIONS -> "destinations"
            ROUTE_BOOKING -> "booking"
            ROUTE_TEAM -> "about"
            ROUTE_ADMIN -> "admin"
            else -> "home"
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        mobileMenuExpanded = false
        detailEventId = null
        viewModel.closeBookingDetail()
        adminInitialTab = 0
        currentRoute = route
        activeMenuId = defaultMenuIdForRoute(route)
    }

    val openBookingForEvent: (String?, Int) -> Unit = { eventId, tab ->
        mobileMenuExpanded = false
        detailEventId = null
        viewModel.closeBookingDetail()
        bookingPreselectedEventId = eventId
        bookingInitialTab = tab
        currentRoute = ROUTE_BOOKING
        activeMenuId = "booking"
    }

    val handleTopMenuSelect: (TopMenuItem) -> Unit = { item ->
        mobileMenuExpanded = false
        detailEventId = null
        viewModel.closeBookingDetail()
        activeMenuId = item.id
        when (item.id) {
            "seat_plan" -> {
                bookingPreselectedEventId = null
                bookingInitialTab = 0
                currentRoute = ROUTE_BOOKING
            }
            "booking" -> {
                bookingPreselectedEventId = null
                bookingInitialTab = 0
                currentRoute = ROUTE_BOOKING
            }
            "about", "contact" -> {
                teamFocusSection = item.focusSection
                teamFocusTrigger++
                currentRoute = ROUTE_TEAM
            }
            else -> {
                currentRoute = item.route
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                StickyTopBarHeader(
                    activeMenuId = activeMenuId,
                    mobileMenuExpanded = mobileMenuExpanded,
                    showMobilePillStrip = detailEventId == null && activeBookingDetailId == null,
                    notificationCount = notificationCount,
                    onToggleMobileMenu = { mobileMenuExpanded = !mobileMenuExpanded },
                    onCloseMobileMenu = { mobileMenuExpanded = false },
                    onSelectMenuItem = handleTopMenuSelect,
                    onNotificationsClick = { showNotificationCenter = true },
                    onBookNowClick = { openBookingForEvent(null, 0) }
                )
            },
            bottomBar = {
                if (!useNavigationRail && detailEventId == null && activeBookingDetailId == null) {
                    NavigationBar(windowInsets = WindowInsets.navigationBars) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag("nav_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val activeDetailEvent = detailEventId?.let { id ->
                            events.firstOrNull { it.id == id }
                        }
                        val bookingDetailId = activeBookingDetailId

                        if (bookingDetailId != null) {
                            BookingDetailsScreen(
                                bookingId = bookingDetailId,
                                bookingDetailState = bookingDetailState,
                                onRetry = viewModel::retryBookingDetail,
                                onBack = viewModel::closeBookingDetail,
                                onMarkMemoSent = viewModel::markConfirmationMemoSent,
                                onFeedback = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        } else if (activeDetailEvent != null) {
                            EventDetailScreen(
                                event = activeDetailEvent,
                                onBack = { detailEventId = null },
                                onBookWithSeats = { eventId ->
                                    openBookingForEvent(eventId, 0)
                                }
                            )
                        } else if (eventsState is UiState.Loading && events.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = BrandOceanBlue)
                            }
                        } else {
                            AnimatedContent(
                                targetState = currentRoute,
                                transitionSpec = {
                                    fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                                },
                                label = "mainRouteContent"
                            ) { route ->
                                when (route) {
                                    ROUTE_HOME -> HomeScreen(
                                        events = events,
                                        destinations = destinations,
                                        teamMembers = teamMembers,
                                        settings = agencySettings,
                                        lookupState = lookupState,
                                        reviewsState = reviewsState,
                                        isSubmittingReview = isSubmittingReview,
                                        currentUserId = currentUserId,
                                        currentUserDisplayName = currentUserDisplayName,
                                        isAdmin = hasFirestoreAdminPrivilege,
                                        onLookupReference = viewModel::lookupReference,
                                        onOpenBookingDetails = { bId -> viewModel.openBookingDetail(bId) },
                                        onSubmitReview = viewModel::submitReview,
                                        onDeleteReview = viewModel::deleteReview,
                                        onNavigateToEvents = { navigateTo(ROUTE_EVENTS) },
                                        onNavigateToDestinations = { navigateTo(ROUTE_DESTINATIONS) },
                                        onNavigateToSeatPlan = { evId -> openBookingForEvent(evId, 0) },
                                        onNavigateToBooking = { evId -> openBookingForEvent(evId, 0) },
                                        onNavigateToEventDetail = { evId -> detailEventId = evId },
                                        onNavigateToTeamAndContact = { navigateTo(ROUTE_TEAM) },
                                        onNavigateToRoute = { target -> navigateTo(target) },
                                        onFeedback = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )

                                    ROUTE_EVENTS -> EventsScreen(
                                        events = events,
                                        settings = agencySettings,
                                        isAdmin = hasFirestoreAdminPrivilege,
                                        onOpenAdminSchedule = {
                                            navigateTo(ROUTE_ADMIN)
                                            adminInitialTab = 2
                                        },
                                        onOpenDetail = { evId -> detailEventId = evId },
                                        onBookEvent = { evId -> openBookingForEvent(evId, 0) }
                                    )

                                    ROUTE_DESTINATIONS -> DestinationsScreen(
                                        destinations = destinations,
                                        events = events,
                                        onBookEvent = { evId -> openBookingForEvent(evId, 0) },
                                        onOpenEventDetail = { evId -> detailEventId = evId }
                                    )

                                    ROUTE_BOOKING -> BookingFlowScreen(
                                        events = events,
                                        initialEventId = bookingPreselectedEventId,
                                        initialTab = bookingInitialTab,
                                        currentUserEmail = currentUserEmail,
                                        myBookings = myBookings,
                                        bookingSubmitState = bookingSubmitState,
                                        lookupState = lookupState,
                                        onSubmitBooking = viewModel::submitBooking,
                                        onResetSubmitState = viewModel::resetBookingSubmitState,
                                        onLookupReference = viewModel::lookupReference,
                                        onOpenBookingDetails = { bId -> viewModel.openBookingDetail(bId) },
                                        onMarkMemoSent = viewModel::markConfirmationMemoSent,
                                        onFeedback = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )

                                    ROUTE_TEAM -> TeamAndContactScreen(
                                        teamMembers = teamMembers,
                                        events = events,
                                        settings = agencySettings,
                                        reviewsState = reviewsState,
                                        isSubmittingReview = isSubmittingReview,
                                        currentUserId = currentUserId,
                                        currentUserDisplayName = currentUserDisplayName,
                                        isAdmin = hasFirestoreAdminPrivilege,
                                        focusSection = teamFocusSection,
                                        focusTrigger = teamFocusTrigger,
                                        onSubmitReview = viewModel::submitReview,
                                        onDeleteReview = viewModel::deleteReview,
                                        onBookNow = { openBookingForEvent(null, 0) },
                                        onOpenAdminPortal = { navigateTo(ROUTE_ADMIN) },
                                        onNavigateQuickLink = { target -> navigateTo(target) },
                                        onFeedback = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )

                                    ROUTE_ADMIN -> AdminPortalScreen(
                                        hasFirestoreAdminPrivilege = hasFirestoreAdminPrivilege,
                                        isBootstrapSuperAdmin = viewModel.isBootstrapSuperAdmin,
                                        currentUserEmail = currentUserEmail,
                                        activeAdminAccount = activeAdminAccount,
                                        adminAccounts = adminAccounts,
                                        teamMembers = teamMembers,
                                        events = events,
                                        destinations = destinations,
                                        allBookings = allBookings,
                                        settings = agencySettings,
                                        cloudEventsEmpty = cloudEventsEmpty,
                                        onLoginIndividualAdmin = viewModel::loginAsIndividualAdmin,
                                        onLogoutIndividualAdmin = viewModel::logoutAdminSession,
                                        onTriggerSeed = viewModel::triggerSeedData,
                                        onVerifyBookingStatus = viewModel::verifyAndUpdateBookingStatus,
                                        onSaveEvent = viewModel::saveEvent,
                                        onUpdateEventBookingStatus = viewModel::updateEventBookingStatus,
                                        onDeleteEvent = viewModel::deleteEvent,
                                        onSaveDestination = viewModel::saveDestination,
                                        onDeleteDestination = viewModel::deleteDestination,
                                        onUpdateAdminAccount = viewModel::updateAdminAccount,
                                        onUpdateAgencySettings = viewModel::updateAgencySettings,
                                        onOpenBookingDetails = { bId -> viewModel.openBookingDetail(bId) },
                                        onMarkMemoSent = viewModel::markConfirmationMemoSent,
                                        initialTab = adminInitialTab,
                                        onFeedback = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedMobileMenuOverlay(
                    expanded = mobileMenuExpanded,
                    activeMenuId = activeMenuId,
                    currentUserEmail = currentUserEmail,
                    onDismiss = { mobileMenuExpanded = false },
                    onSelectMenuItem = handleTopMenuSelect,
                    onReplayWelcome = {
                        navigateTo(ROUTE_HOME)
                        onReplayWelcome()
                    },
                    onShareAppLink = {
                        shareWebsiteAppLink(
                            context = context,
                            url = agencySettings.resolvedWebsiteUrl(),
                            activeMonthTitle = agencySettings.activeMonthTitle
                        )
                    },
                    onSignOut = {
                        CredentialAuthHelper.signOut(credentialManager, scope)
                    }
                )
            }
        }

        if (showNotificationCenter) {
            NotificationCenterSheet(
                events = events,
                myBookings = myBookings,
                onDismiss = {
                    showNotificationCenter = false
                    notificationCount = TourNotificationManager.loadPreferencesState(context).history.size
                },
                onOpenBookingDetails = { bId ->
                    showNotificationCenter = false
                    viewModel.openBookingDetail(bId)
                },
                onOpenEventDetail = { eId ->
                    showNotificationCenter = false
                    viewModel.closeBookingDetail()
                    detailEventId = eId
                },
                onFeedback = { msg ->
                    notificationCount = TourNotificationManager.loadPreferencesState(context).history.size
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
        }
    }
}
