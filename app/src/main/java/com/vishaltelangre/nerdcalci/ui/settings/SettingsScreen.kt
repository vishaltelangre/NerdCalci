package com.vishaltelangre.nerdcalci.ui.settings

import kotlin.math.roundToInt

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Attribution
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.vishaltelangre.nerdcalci.ui.components.RestoreSourceDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreBackupListDialog
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.BorderHorizontal
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.utils.RegionUtils
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupFrequency
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily
import com.vishaltelangre.nerdcalci.core.LaunchMode
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.ui.components.RegionSelectorDialog
import com.vishaltelangre.nerdcalci.ui.components.formatBackupLocationText
import com.vishaltelangre.nerdcalci.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    autoBackupEnabled: Boolean,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    backupFrequency: BackupFrequency,
    onBackupFrequencyChange: (BackupFrequency) -> Unit,
    backupLocationMode: BackupLocationMode,
    backupLocationSummary: String,
    onChooseBackupFolder: () -> Unit,
    onUseAppStorageLocation: () -> Unit,
    onBackupNow: () -> Unit,
    onBackupNowAtDifferentLocation: () -> Unit,
    lastBackupAt: Long?,
    availableBackups: List<BackupFileInfo>,
    onRestoreBackup: (BackupFileInfo) -> Unit,
    onRestoreFromDifferentLocation: () -> Unit,
    precision: Int,
    onPrecisionChange: (Int) -> Unit,
    showLineNumbers: Boolean,
    onShowLineNumbersChange: (Boolean) -> Unit,
    showSuggestions: Boolean,
    onShowSuggestionsChange: (Boolean) -> Unit,
    showSymbolsShortcuts: Boolean,
    onShowSymbolsShortcutsChange: (Boolean) -> Unit,
    showNumbersShortcuts: Boolean,
    onShowNumbersShortcutsChange: (Boolean) -> Unit,
    regionCode: String,
    onRegionCodeChange: (String) -> Unit,
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    syncFolderUri: String?,
    onChooseSyncFolder: () -> Unit,
    lastSyncAt: Long?,
    rationalMode: Boolean,
    onRationalModeChange: (Boolean) -> Unit,
    groupingSeparatorEnabled: Boolean,
    onGroupingSeparatorEnabledChange: (Boolean) -> Unit,
    showPrecisionEllipsis: Boolean,
    onShowPrecisionEllipsisChange: (Boolean) -> Unit,
    launchMode: LaunchMode,
    onLaunchModeChange: (LaunchMode) -> Unit,
    launchFileId: Long?,
    onLaunchFileIdChange: (Long?) -> Unit,
    showScratchpad: Boolean,
    onShowScratchpadChange: (Boolean) -> Unit,
    editorFontSize: Float,
    onEditorFontSizeChange: (Float) -> Unit,
    allFiles: List<FileEntity>,
    onAutoValidateLaunchFile: () -> Unit,
    onValidateLaunchFile: (Long, (Boolean) -> Unit) -> Unit,
    onHelp: () -> Unit,
    onChangelog: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showRestoreActionDialog by remember { mutableStateOf(false) }
    var showBackupNowActionDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showLaunchModeDialog by remember { mutableStateOf(false) }
    var showSelectFileDialog by remember { mutableStateOf(false) }

    var sliderValue by remember(precision) { mutableStateOf(precision.toFloat()) }

    val appVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "Unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "v$versionName ($versionCode)"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    val restoreSubtitle = if (availableBackups.isEmpty()) {
        "Use current location or choose a different file"
    } else {
        "Use current location (contains ${availableBackups.size} backups) or choose a different file"
    }
    val currentLocationText = formatBackupLocationText(
        mode = backupLocationMode,
        customFolderSummary = backupLocationSummary
    )
    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    // Validate the specific file if set
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (launchMode == LaunchMode.SPECIFIC_FILE) {
            onAutoValidateLaunchFile()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
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
            SettingsSection(title = "Theme")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val options = listOf(
                    Triple("light", "Light", Icons.Default.LightMode),
                    Triple("dark", "Dark", Icons.Default.DarkMode),
                    Triple("system", "System", Icons.Default.DarkMode)
                )

                options.forEachIndexed { index, (value, label, icon) ->
                    SegmentedButton(
                        selected = currentTheme == value,
                        onClick = { onThemeChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = currentTheme == value) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Calculator")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Result precision",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${sliderValue.roundToInt()} decimal places",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                androidx.compose.material3.Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onPrecisionChange(sliderValue.roundToInt()) },
                    valueRange = Constants.MIN_PRECISION.toFloat()..Constants.MAX_PRECISION.toFloat(),
                    steps = Constants.MAX_PRECISION - Constants.MIN_PRECISION - 1,
                    modifier = Modifier.padding(top = 8.dp)
                )

            }

            SettingsSliderItem(
                icon = Icons.Default.Info,
                title = "Editor font size",
                editorFontSize,
                onEditorFontSizeChange,
                valueRange = Constants.MIN_EDITOR_FONT_SIZE..Constants.MAX_EDITOR_FONT_SIZE,
                steps = (Constants.MAX_EDITOR_FONT_SIZE - Constants.MIN_EDITOR_FONT_SIZE - 1).roundToInt(),
                valueFormatter = { it.roundToInt().toString() }
            )

            SettingsToggleItem(
                icon = Icons.Default.FormatListNumbered,
                title = "Show line numbers",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showLineNumbers,
                onCheckedChange = onShowLineNumbersChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Chat,
                title = "Show suggestions",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showSuggestions,
                onCheckedChange = onShowSuggestionsChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Calculate,
                title = "Show symbols shortcuts",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showSymbolsShortcuts,
                onCheckedChange = onShowSymbolsShortcutsChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Pin,
                title = "Show numbers shortcuts (Numpad)",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showNumbersShortcuts,
                onCheckedChange = onShowNumbersShortcutsChange
            )

            SettingsToggleItem(
                icon = Icons.Default.BorderHorizontal,
                title = "Rational mode",
                subtitle = "Display results as a ratio of two integers (fractions) for exactness when possible.",
                checked = rationalMode,
                onCheckedChange = onRationalModeChange
            )

            SettingsDropdownItem(
                icon = Icons.Default.Public,
                title = "Region",
                value = if (regionCode == RegionUtils.SYSTEM_DEFAULT) {
                    val systemCountryName = RegionUtils.getAvailableRegions()
                        .find { it.first == java.util.Locale.getDefault().country }?.second
                        ?: java.util.Locale.getDefault().displayCountry
                    if (systemCountryName.isNotEmpty()) "System default ($systemCountryName)" else "System default"
                } else
                    RegionUtils.getAvailableRegions().find { it.first == regionCode }?.second
                        ?: regionCode,
                onClick = { showRegionDialog = true }
            )

            SettingsToggleItem(
                icon = Icons.Default.SpaceBar,
                title = "Show grouping separators in numbers",
                subtitle = "",
                checked = groupingSeparatorEnabled,
                onCheckedChange = onGroupingSeparatorEnabledChange,
                modifier = Modifier.padding(start = 32.dp)
            )

            Text(
                text = remember(regionCode, groupingSeparatorEnabled, precision) {
                    MathEngine.formatDisplayResult(
                        "12345678.90",
                        precision,
                        regionCode = regionCode,
                        groupingSeparatorEnabled = groupingSeparatorEnabled
                    )
                },
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FiraCodeFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 56.dp).padding(bottom = 8.dp)
            )

            SettingsToggleItem(
                icon = Icons.Default.MoreHoriz,
                title = "Truncate too precise numbers with ellipsis",
                subtitle = "Show ellipsis (e.g., 0.123…) for results with more decimal places than the current precision setting.",
                checked = showPrecisionEllipsis,
                onCheckedChange = onShowPrecisionEllipsisChange
            )

            val mutedEllipsisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            Text(
                text = remember(showPrecisionEllipsis, precision, mutedEllipsisColor) {
                    val base = "0.12345678901"
                    val formatted = MathEngine.formatDisplayResult(
                        base,
                        precision,
                        showEllipsis = showPrecisionEllipsis
                    )
                    buildAnnotatedString {
                        if (showPrecisionEllipsis && formatted.endsWith("…")) {
                            append(formatted.dropLast(1))
                            withStyle(SpanStyle(color = mutedEllipsisColor, fontWeight = FontWeight.Normal)) {
                                append("…")
                            }
                        } else {
                            append(formatted)
                        }
                    }
                },
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FiraCodeFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 56.dp).padding(bottom = 8.dp)
            )

            if (launchMode != LaunchMode.SCRATCHPAD) {
                SettingsToggleItem(
                    icon = Icons.Default.FlashOn,
                    title = "Show temporary scratchpad shortcut on home",
                    subtitle = "Always show a bolt icon on the home screen to quickly open the temporary scratchpad.",
                    checked = showScratchpad,
                    onCheckedChange = onShowScratchpadChange
                )
            }

            SettingsDropdownItem(
                icon = when (launchMode) {
                    LaunchMode.NOT_SET -> Icons.Default.Restore
                    LaunchMode.SCRATCHPAD -> Icons.Default.FlashOn
                    LaunchMode.JOURNAL -> Icons.Default.Schedule
                    LaunchMode.SPECIFIC_FILE -> Icons.Default.Description
                },
                title = "Auto-open file on launch",
                value = when (launchMode) {
                    LaunchMode.NOT_SET -> "Not set"
                    LaunchMode.SCRATCHPAD -> "Temporary scratchpad"
                    LaunchMode.JOURNAL -> "Daily journal (YYYY-MM-DD)"
                    LaunchMode.SPECIFIC_FILE -> {
                        val file = allFiles.find { it.id == launchFileId }
                        file?.name ?: "Choose file..."
                    }
                },
                onClick = {
                    if (launchMode == LaunchMode.SPECIFIC_FILE && launchFileId != null) {
                        onValidateLaunchFile(launchFileId) { exists ->
                            if (!exists) {
                                // If stored file is gone, show selector immediately
                                showSelectFileDialog = true
                            } else {
                                showLaunchModeDialog = true
                            }
                        }
                    } else {
                        showLaunchModeDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Data")

            SettingsToggleItem(
                icon = Icons.Default.Backup,
                title = "Automatic backup",
                subtitle = null,
                checked = autoBackupEnabled,
                onCheckedChange = onAutoBackupEnabledChange
            )

            if (autoBackupEnabled) {
                SettingsDropdownItem(
                    icon = Icons.Default.AccessTimeFilled,
                    title = "Backup frequency",
                    value = if (backupFrequency == BackupFrequency.DAILY) "Daily" else "Weekly",
                    onClick = { showFrequencyDialog = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Backup location",
                    subtitle = currentLocationText,
                    onClick = { showLocationDialog = true }
                )

            }

            Spacer(modifier = Modifier.height(10.dp))
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Back up now",
                subtitle = if (lastBackupAt != null) {
                    "Last backup ${formatRelativeTime(lastBackupAt)}"
                } else {
                    "Create a backup immediately"
                },
                onClick = { showBackupNowActionDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Restore,
                title = "Restore from backup",
                subtitle = restoreSubtitle,
                onClick = { showRestoreActionDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Sync (Experimental)")

            SettingsToggleItem(
                icon = Icons.Default.Sync,
                title = "Sync files",
                subtitle = "Sync files across devices using your preferred sync method (e.g., Syncthing, WebDAV, etc.)",
                checked = syncEnabled,
                onCheckedChange = { confirmed ->
                    if (confirmed && syncFolderUri == null) {
                        onChooseSyncFolder()
                    } else {
                        onSyncEnabledChange(confirmed)
                    }
                }
            )

            if (syncEnabled) {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Sync location",
                    subtitle = formatSyncFolderSubtitle(syncFolderUri) ?: "Choose folder",
                    onClick = onChooseSyncFolder
                )

                if (lastSyncAt != null) {
                    Text(
                        text = "Last synced ${formatRelativeTime(lastSyncAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Help & Feedback")
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
                title = "Report an Issue",
                subtitle = Constants.SUPPORT_ISSUES_URL.removePrefix("https://"),
                onClick = {
                    openUrl(Constants.SUPPORT_ISSUES_URL)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Legal")
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = Constants.PRIVACY_POLICY_URL.removePrefix("https://"),
                onClick = { openUrl(Constants.PRIVACY_POLICY_URL) }
            )

            SettingsItem(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                subtitle = Constants.TERMS_OF_SERVICE_URL.removePrefix("https://"),
                onClick = { openUrl(Constants.TERMS_OF_SERVICE_URL) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "About")
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = appVersion,
                onClick = onChangelog
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Source Code",
                subtitle = Constants.SOURCE_CODE_URL.removePrefix("https://"),
                onClick = {
                    openUrl(Constants.SOURCE_CODE_URL)
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LogoDev,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Developer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Constants.DEVELOPER_NAME,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/vishaltelangre"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = "Buy me a coffee",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            SettingsItem(
                icon = Icons.Default.Attribution,
                title = "License",
                subtitle = Constants.LICENSE,
                onClick = null
            )
        }
    }

    RestoreBackupListDialog(
        visible = showRestoreDialog,
        currentLocationText = currentLocationText,
        backups = availableBackups,
        onDismiss = { showRestoreDialog = false },
        onBackupSelected = { backup ->
            showRestoreDialog = false
            onRestoreBackup(backup)
        }
    )

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Backup location") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(
                        onClick = {
                            showLocationDialog = false
                            onUseAppStorageLocation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = backupLocationMode != BackupLocationMode.APP_STORAGE
                    ) {
                        Text(if (backupLocationMode == BackupLocationMode.APP_STORAGE) "Using default app storage" else "Use default app storage")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showLocationDialog = false
                            onChooseBackupFolder()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose custom folder")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = labelValueText("Current location:", currentLocationText),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("Backup frequency") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            showFrequencyDialog = false
                            onBackupFrequencyChange(BackupFrequency.DAILY)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (backupFrequency == BackupFrequency.DAILY) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (backupFrequency == BackupFrequency.DAILY) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            showFrequencyDialog = false
                            onBackupFrequencyChange(BackupFrequency.WEEKLY)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (backupFrequency == BackupFrequency.WEEKLY) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (backupFrequency == BackupFrequency.WEEKLY) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBackupNowActionDialog) {
        AlertDialog(
            onDismissRequest = { showBackupNowActionDialog = false },
            title = { Text("Back up now") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showBackupNowActionDialog = false
                            onBackupNow()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use current backup location")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            showBackupNowActionDialog = false
                            onBackupNowAtDifferentLocation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose different location")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = labelValueText("Current location:", currentLocationText),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupNowActionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    RestoreSourceDialog(
        visible = showRestoreActionDialog,
        hasBackupsInCurrentLocation = availableBackups.isNotEmpty(),
        currentLocationText = currentLocationText,
        onDismiss = { showRestoreActionDialog = false },
        onUseCurrentLocation = {
            showRestoreActionDialog = false
            showRestoreDialog = true
        },
        onChooseDifferentFile = {
            showRestoreActionDialog = false
            onRestoreFromDifferentLocation()
        }
    )

    RegionSelectorDialog(
        visible = showRegionDialog,
        currentRegionCode = regionCode,
        onSelect = {
            onRegionCodeChange(it)
            showRegionDialog = false
        },
        onDismiss = { showRegionDialog = false }
    )

    if (showLaunchModeDialog) {
        LaunchModeDialog(
            currentMode = launchMode,
            onSelect = { mode ->
                showLaunchModeDialog = false
                if (mode == LaunchMode.SPECIFIC_FILE) {
                    showSelectFileDialog = true
                } else {
                    onLaunchModeChange(mode)
                }
            },
            onDismiss = { showLaunchModeDialog = false }
        )
    }

    if (showSelectFileDialog) {
        SelectAutoOpenFileDialog(
            files = allFiles,
            currentFileId = launchFileId,
            onSelect = { fileId ->
                showSelectFileDialog = false
                onLaunchModeChange(LaunchMode.SPECIFIC_FILE)
                onLaunchFileIdChange(fileId)
            },
            onDismiss = { showSelectFileDialog = false }
        )
    }
}

@Composable
private fun LaunchModeDialog(
    currentMode: LaunchMode,
    onSelect: (LaunchMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-open on launch") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    LaunchMode.NOT_SET to "Not set",
                    LaunchMode.SCRATCHPAD to "Temporary scratchpad",
                    LaunchMode.JOURNAL to "Daily journal (YYYY-MM-DD)",
                    LaunchMode.SPECIFIC_FILE to "Specific file"
                )

                options.forEach { (mode, label) ->
                    TextButton(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (currentMode == mode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SelectAutoOpenFileDialog(
    files: List<FileEntity>,
    currentFileId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select file") },
        text = {
            if (files.isEmpty()) {
                Text("No files available to select.")
            } else {
                val sortedFiles = remember(files) { files.sortedBy { it.name } }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    items(sortedFiles) { file ->
                        TextButton(
                            onClick = { onSelect(file.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentFileId == file.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (currentFileId == file.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsDropdownItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatRelativeTime(value: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        value,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

private fun formatSyncFolderSubtitle(syncFolderUri: String?): String? {
    if (syncFolderUri.isNullOrBlank()) return null

    return try {
        val decoded = Uri.decode(Uri.parse(syncFolderUri).lastPathSegment ?: "")
        if (decoded.isBlank()) {
            FileUtils.formatPathForDisplay(syncFolderUri)
        } else {
            FileUtils.formatPathForDisplay(decoded)
        }
    } catch (_: Exception) {
        FileUtils.formatPathForDisplay(syncFolderUri)
    }
}

private fun labelValueText(label: String, value: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
        append(label)
    }
    append(" ")
    append(value)
}
