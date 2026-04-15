package com.vishaltelangre.nerdcalci.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Attribution
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Legal") },
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
        ) {
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy policy",
                subtitle = Constants.PRIVACY_POLICY_URL.removePrefix("https://"),
                onClick = { openUrl(Constants.PRIVACY_POLICY_URL) }
            )
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Terms of service",
                subtitle = Constants.TERMS_OF_SERVICE_URL.removePrefix("https://"),
                onClick = { openUrl(Constants.TERMS_OF_SERVICE_URL) }
            )
            SettingsItem(
                icon = Icons.Default.Attribution,
                title = "License",
                subtitle = Constants.LICENSE,
                onClick = null
            )
            SettingsItem(
                icon = Icons.Default.Code,
                title = "View source code",
                subtitle = Constants.SOURCE_CODE_URL.removePrefix("https://"),
                onClick = { openUrl(Constants.SOURCE_CODE_URL) }
            )
        }
    }
}
