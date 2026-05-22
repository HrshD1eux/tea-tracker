package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.TeaViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TeaViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge rendering active
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val activeTab by viewModel.activeTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (activeTab) {
                                        0 -> "Tea Tracker"
                                        1 -> "Consumption Log"
                                        2 -> "Brew Analytics"
                                        else -> "Settings & Tools"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.testTag("app_top_bar")
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = NavigationBarDefaults.Elevation,
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { viewModel.activeTab.value = 0 },
                                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_item_dashboard")
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { viewModel.activeTab.value = 1 },
                                icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "History") },
                                label = { Text("History") },
                                modifier = Modifier.testTag("nav_item_history")
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { viewModel.activeTab.value = 2 },
                                icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Analytics") },
                                label = { Text("Analytics") },
                                modifier = Modifier.testTag("nav_item_analytics")
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { viewModel.activeTab.value = 3 },
                                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_item_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedNavigationContent(activeTab = activeTab, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNavigationContent(
    activeTab: Int,
    viewModel: TeaViewModel,
    modifier: Modifier = Modifier
) {
    // Crossfade or slide-to-reveal tab switching under 300ms as per styling guidelines
    AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
            fadeIn() with fadeOut()
        },
        label = "tab_switching_anim"
    ) { targetTab ->
        when (targetTab) {
            0 -> DashboardScreen(viewModel = viewModel)
            1 -> HistoryScreen(viewModel = viewModel)
            2 -> AnalyticsScreen(viewModel = viewModel)
            else -> SettingsScreen(viewModel = viewModel)
        }
    }
}
