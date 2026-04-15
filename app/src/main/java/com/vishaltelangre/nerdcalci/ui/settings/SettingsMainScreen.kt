package com.vishaltelangre.nerdcalci.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToHomeStartup: () -> Unit,
    onNavigateToDataSync: () -> Unit,
    onNavigateToHelpFeedback: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            SettingsCategoryItem(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Theme options",
                onClick = onNavigateToAppearance,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.Default.Calculate,
                title = "Calculator",
                subtitle = "Editor options & settings",
                onClick = onNavigateToCalculator,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.Default.RocketLaunch,
                title = "Home & startup",
                subtitle = "Launch mode & home screen shortcuts",
                onClick = onNavigateToHomeStartup,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.Default.DataUsage,
                title = "Data & sync",
                subtitle = "Backups, restore, and sync files",
                onClick = onNavigateToDataSync,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help & feedback",
                subtitle = "Docs, changelog, and report issue",
                onClick = onNavigateToHelpFeedback,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.Default.Gavel,
                title = "Legal",
                subtitle = "Privacy, terms, and license info",
                onClick = onNavigateToLegal,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            SettingsCategoryItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App info, version, etc.",
                onClick = onNavigateToAbout,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}
