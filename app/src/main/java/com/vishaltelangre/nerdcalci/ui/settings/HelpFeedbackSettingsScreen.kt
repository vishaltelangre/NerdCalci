package com.vishaltelangre.nerdcalci.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.RssFeed
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
import androidx.compose.ui.platform.LocalContext
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.IntentUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackSettingsScreen(
    onHelp: () -> Unit,
    onChangelog: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help & feedback") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Help",
                subtitle = "View the calculator usage guide and documentation",
                onClick = onHelp
            )
            SettingsItem(
                icon = Icons.Default.RssFeed,
                title = "Changelog",
                subtitle = "See what's new in this version and past updates",
                onClick = onChangelog
            )
            SettingsItem(
                icon = Icons.Default.BugReport,
                title = "Report an issue",
                subtitle = Constants.SUPPORT_ISSUES_URL.removePrefix("https://"),
                onClick = { IntentUtils.openUrl(context, Constants.SUPPORT_ISSUES_URL) }
            )
            Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
        }
    }
}
