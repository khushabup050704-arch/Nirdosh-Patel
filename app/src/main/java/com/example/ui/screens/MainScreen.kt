package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.models.VideoGeneration
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AppHeader
import com.example.ui.components.GetCreditsDialog
import com.example.ui.components.SafetyPolicyDialog
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.theme.ElectricViolet
import com.example.viewmodel.VideoGeneratorViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab(val title: String) {
    HOME("Home"),
    CREATE("Create"),
    HISTORY("History"),
    PROFILE("Profile")
}

@Composable
fun MainScreen(viewModel: VideoGeneratorViewModel) {
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    val user by viewModel.currentUser.collectAsState()
    val playingVideo by viewModel.activePlayingVideo.collectAsState()
    val showCredits by viewModel.showCreditDialog.collectAsState()
    val showSettings by viewModel.showSettingsSheet.collectAsState()
    val showAdmin by viewModel.showAdminDialog.collectAsState()
    val showSafety by viewModel.showSafetyDialog.collectAsState()
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val allGenerations by viewModel.allGenerations.collectAsState()

    // Handle back button: return to HOME tab before exiting
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        currentTab = ScreenTab.HOME
    }

    // Collect snackbar notifications
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppHeader(
                credits = user?.credits ?: 0,
                onCreditsClick = { viewModel.showCreditDialog.value = true },
                onSettingsClick = { viewModel.showSettingsSheet.value = true },
                onAdminClick = { viewModel.showAdminDialog.value = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { currentTab = ScreenTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricViolet,
                        selectedTextColor = ElectricViolet,
                        indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Create Studio Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.CREATE,
                    onClick = { currentTab = ScreenTab.CREATE },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.CREATE) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                            contentDescription = "Create"
                        )
                    },
                    label = { Text("Create", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricViolet,
                        selectedTextColor = ElectricViolet,
                        indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_create")
                )

                // History Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricViolet,
                        selectedTextColor = ElectricViolet,
                        indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                // Profile Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.PROFILE,
                    onClick = { currentTab = ScreenTab.PROFILE },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricViolet,
                        selectedTextColor = ElectricViolet,
                        indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    ScreenTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCreate = { currentTab = ScreenTab.CREATE },
                        onNavigateToHistory = { currentTab = ScreenTab.HISTORY },
                        onPlayVideo = { viewModel.openVideoPlayer(it) }
                    )

                    ScreenTab.CREATE -> CreateScreen(
                        viewModel = viewModel,
                        onPlayVideo = { viewModel.openVideoPlayer(it) }
                    )

                    ScreenTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onPlayVideo = { viewModel.openVideoPlayer(it) },
                        onRegenerate = { prompt ->
                            viewModel.setPrompt(prompt)
                            currentTab = ScreenTab.CREATE
                        }
                    )

                    ScreenTab.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onGetCreditsClick = { viewModel.showCreditDialog.value = true },
                        onSettingsClick = { viewModel.showSettingsSheet.value = true },
                        onSafetyPolicyClick = { viewModel.showSafetyDialog.value = true }
                    )
                }
            }
        }
    }

    // Video Player Dialog
    if (playingVideo != null) {
        VideoPlayerDialog(
            video = playingVideo!!,
            onDismiss = { viewModel.closeVideoPlayer() },
            onGenerateAgain = { prompt ->
                viewModel.setPrompt(prompt)
                currentTab = ScreenTab.CREATE
            },
            onDelete = { id -> viewModel.deleteGeneration(id) },
            onSaveTrimmedClip = { orig, startMs, endMs ->
                viewModel.saveTrimmedClip(orig, startMs, endMs)
            }
        )
    }

    // Get Credits Dialog
    if (showCredits) {
        GetCreditsDialog(
            currentCredits = user?.credits ?: 0,
            onPurchasePackage = { pkg -> viewModel.purchaseCredits(pkg) },
            onDismiss = { viewModel.showCreditDialog.value = false }
        )
    }

    // Settings Bottom Sheet
    if (showSettings) {
        val apiUrl by viewModel.customApiUrl.collectAsState()
        val apiKey by viewModel.customApiKey.collectAsState()
        SettingsBottomSheet(
            currentApiUrl = apiUrl,
            currentApiKey = apiKey,
            onSaveSettings = { url, key -> viewModel.updateApiSettings(url, key) },
            onShowSafetyPolicy = {
                viewModel.showSettingsSheet.value = false
                viewModel.showSafetyDialog.value = true
            },
            onDismiss = { viewModel.showSettingsSheet.value = false }
        )
    }

    // Admin Dashboard Dialog
    if (showAdmin) {
        AdminDashboardDialog(
            totalGenerations = allGenerations.size,
            failedJobsCount = allGenerations.count { it.status == VideoGeneration.STATUS_FAILED },
            userCredits = user?.credits ?: 0,
            onGrantCredits = { amount -> viewModel.adminGrantCredits(amount) },
            onDismiss = { viewModel.showAdminDialog.value = false }
        )
    }

    // Safety Policy & Terms Dialog
    if (showSafety) {
        SafetyPolicyDialog(
            onDismiss = { viewModel.showSafetyDialog.value = false }
        )
    }

    // Interactive Onboarding Screen Overlay
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { suggestedPrompt, mode ->
                viewModel.completeOnboarding(suggestedPrompt, mode)
                currentTab = ScreenTab.CREATE
            },
            onSkip = {
                viewModel.completeOnboarding()
            }
        )
    }
}
