package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.core.common.UiState
import com.example.core.data.AuthRepository
import com.example.core.data.BatangasAiRepository
import com.example.core.data.TourismFirestoreRepository
import com.example.feature.auth.AuthViewModel
import com.example.feature.auth.SignInScreen
import com.example.feature.auth.TravelerRegistrationScreen
import com.example.feature.dashboard.DashboardScreen
import com.example.feature.dashboard.DashboardViewModel
import com.example.feature.planner.AiPlannerViewModel
import com.example.feature.planner.PlannerScreen
import com.example.feature.profile.ProfileScreen
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

private enum class TopDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    EXPLORE(
        route = "explore",
        label = "Explore",
        selectedIcon = Icons.Rounded.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_explore"
    ),
    PLANNER(
        route = "planner",
        label = "AI & Trip",
        selectedIcon = Icons.Rounded.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_planner"
    ),
    ACCOUNT(
        route = "account",
        label = "Account",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_account"
    )
}

@Composable
fun AppNavigation(
    authRepository: AuthRepository = remember { AuthRepository() }
) {
    val context = LocalContext.current
    val currentUser by authRepository.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = authRepository.currentUser())

    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AuthViewModel(authRepository) }
        }
    )
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.attemptAutoSignInOnce(context)
    }

    val user = currentUser
    if (user == null) {
        SignInScreen(
            uiState = authUiState,
            onGoogleSignInClick = { authViewModel.onGoogleSignInClick(context) }
        )
    } else {
        key(user.uid) {
            AuthenticatedSessionShell(
                user = user,
                onLogoutClick = { authViewModel.signOut(context) }
            )
        }
    }
}

@Composable
private fun AuthenticatedSessionShell(
    user: FirebaseUser,
    onLogoutClick: () -> Unit
) {
    val dashboardViewModel: DashboardViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DashboardViewModel(
                    repository = TourismFirestoreRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val aiViewModel: AiPlannerViewModel = viewModel(
        key = "ai_${user.uid}",
        factory = viewModelFactory {
            initializer { AiPlannerViewModel(BatangasAiRepository()) }
        }
    )

    val profileState by dashboardViewModel.profileState.collectAsStateWithLifecycle()
    val savedSpotsState by dashboardViewModel.savedSpotsState.collectAsStateWithLifecycle()
    val itineraryState by dashboardViewModel.itineraryState.collectAsStateWithLifecycle()
    val selectedCategory by dashboardViewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by dashboardViewModel.searchQuery.collectAsStateWithLifecycle()
    val actionStatus by dashboardViewModel.actionStatus.collectAsStateWithLifecycle()
    val conciergeState by aiViewModel.conciergeState.collectAsStateWithLifecycle()
    val generatedPlanState by aiViewModel.itineraryPlanState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionStatus.message, actionStatus.error) {
        val text = actionStatus.error ?: actionStatus.message
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            dashboardViewModel.clearStatus()
        }
    }

    when (val state = profileState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading your San Juan traveler profile...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLogoutClick) {
                        Text("Return to Sign-In")
                    }
                }
            }
        }

        is UiState.Success -> {
            val profile = state.data
            if (profile == null) {
                TravelerRegistrationScreen(
                    initialName = user.displayName.orEmpty(),
                    email = user.email ?: "traveler@batangasconnect.ph",
                    isSaving = actionStatus.isBusy,
                    errorMessage = actionStatus.error,
                    onCompleteRegistration = { fullName, homeCity, style ->
                        dashboardViewModel.createProfile(
                            fullName = fullName,
                            email = user.email ?: "traveler@batangasconnect.ph",
                            homeCity = homeCity,
                            travelerStyle = style
                        )
                    },
                    onSignOutClick = onLogoutClick
                )
            } else {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val navigateToTab: (TopDestination) -> Unit = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavigationRail = maxWidth >= 600.dp

                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (!useNavigationRail) {
                                NavigationBar {
                                    TopDestination.entries.forEach { dest ->
                                        val selected = currentRoute == dest.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateToTab(dest) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = { Text(dest.label) },
                                            modifier = Modifier.testTag(dest.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (useNavigationRail) {
                                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TopDestination.entries.forEach { dest ->
                                        val selected = currentRoute == dest.route
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { navigateToTab(dest) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = { Text(dest.label) },
                                            modifier = Modifier.testTag(dest.testTag)
                                        )
                                    }
                                }
                            }

                            NavHost(
                                navController = navController,
                                startDestination = TopDestination.EXPLORE.route,
                                modifier = Modifier.weight(1f)
                            ) {
                                composable(TopDestination.EXPLORE.route) {
                                    DashboardScreen(
                                        profile = profile,
                                        destinations = dashboardViewModel.catalog,
                                        selectedCategory = selectedCategory,
                                        searchQuery = searchQuery,
                                        savedSpotsState = savedSpotsState,
                                        itineraryState = itineraryState,
                                        onSelectCategory = dashboardViewModel::selectCategory,
                                        onSearchQueryChange = dashboardViewModel::updateSearchQuery,
                                        onToggleSave = dashboardViewModel::toggleSavedSpot,
                                        onAddToItinerary = dashboardViewModel::addDestinationToItinerary,
                                        onNavigateToAiPlanner = { navigateToTab(TopDestination.PLANNER) }
                                    )
                                }
                                composable(TopDestination.PLANNER.route) {
                                    PlannerScreen(
                                        profile = profile,
                                        itineraryState = itineraryState,
                                        conciergeState = conciergeState,
                                        generatedPlanState = generatedPlanState,
                                        onToggleVisited = dashboardViewModel::toggleVisited,
                                        onDeleteItem = dashboardViewModel::deleteItineraryItem,
                                        onAddCustomStop = dashboardViewModel::addCustomItineraryItem,
                                        onAskConcierge = aiViewModel::askConcierge,
                                        onGenerateItinerary = aiViewModel::generateItinerary,
                                        onSaveAiBatch = { stops, onSaved ->
                                            dashboardViewModel.addAiItineraryBatch(stops) {
                                                aiViewModel.clearGeneratedPlan()
                                                onSaved()
                                            }
                                        }
                                    )
                                }
                                composable(TopDestination.ACCOUNT.route) {
                                    ProfileScreen(
                                        profile = profile,
                                        savedSpotsState = savedSpotsState,
                                        catalog = dashboardViewModel.catalog,
                                        onUpdateProfile = dashboardViewModel::updateProfile,
                                        onRemoveSavedSpot = { destination ->
                                            dashboardViewModel.toggleSavedSpot(destination, currentlySaved = true)
                                        },
                                        onLogoutClick = onLogoutClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
