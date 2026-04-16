package com.vishaltelangre.nerdcalci.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.backup.AutoBackupScheduler
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.data.backup.BackupManager
import com.vishaltelangre.nerdcalci.data.local.AppDatabase
import com.vishaltelangre.nerdcalci.data.local.DatabaseMigrations
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorScreen
import com.vishaltelangre.nerdcalci.ui.calculator.HomeUiEvent
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel
import com.vishaltelangre.nerdcalci.di.CalculatorViewModelFactory
import com.vishaltelangre.nerdcalci.core.LaunchMode
import com.vishaltelangre.nerdcalci.ui.components.formatBackupLocationText
import com.vishaltelangre.nerdcalci.ui.components.RestoreBackupListDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreSourceDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreProgressDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreCompleteDialog
import com.vishaltelangre.nerdcalci.ui.home.HomeScreen
import com.vishaltelangre.nerdcalci.ui.settings.AboutSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.AppearanceSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.CalculatorSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.DataSyncSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.SettingsMainScreen
import com.vishaltelangre.nerdcalci.ui.settings.HomeStartupSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.HelpFeedbackSettingsScreen
import com.vishaltelangre.nerdcalci.ui.settings.LegalSettingsScreen
import com.vishaltelangre.nerdcalci.ui.changelog.ChangelogScreen
import com.vishaltelangre.nerdcalci.ui.help.HelpScreen
import com.vishaltelangre.nerdcalci.ui.search.SearchScreen
import com.vishaltelangre.nerdcalci.ui.theme.NerdCalciTheme
import com.vishaltelangre.nerdcalci.utils.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, Constants.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()

        val prefs = getSharedPreferences(BackupManager.PREFS_NAME, MODE_PRIVATE)
        AutoBackupScheduler.sync(applicationContext, prefs)

        val viewModel: CalculatorViewModel by viewModels {
            CalculatorViewModelFactory(db.calculatorDao(), prefs)
        }

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val colorPalette by viewModel.colorPalette.collectAsState()

            val isDarkTheme = when (currentTheme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            NerdCalciTheme(
                darkTheme = isDarkTheme,
                colorPalette = colorPalette
            ) {
                // Update system bar appearance to match theme
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as ComponentActivity).window
                        // Use WindowInsetsController for system bar appearance
                        WindowCompat.getInsetsController(window, view).apply {
                            isAppearanceLightStatusBars = !isDarkTheme
                            isAppearanceLightNavigationBars = !isDarkTheme
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    CalculatorNavHost(viewModel)
                }
            }
        }
    }
}

@Composable
fun CalculatorNavHost(viewModel: CalculatorViewModel, navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val colorPalette by viewModel.colorPalette.collectAsState()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val backupFrequency by viewModel.backupFrequency.collectAsState()
    val backupLocationMode by viewModel.backupLocationMode.collectAsState()
    val customBackupFolderUri by viewModel.customBackupFolderUri.collectAsState()
    val availableBackups by viewModel.availableBackups.collectAsState()
    val lastBackupAt by viewModel.lastBackupAt.collectAsState()
    val showLineNumbers by viewModel.showLineNumbers.collectAsState()
    val showSuggestions by viewModel.showSuggestions.collectAsState()
    val showSymbolsShortcuts by viewModel.showSymbolsShortcuts.collectAsState()
    val showNumbersShortcuts by viewModel.showNumbersShortcuts.collectAsState()
    val regionCode by viewModel.regionCode.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val syncFolderUri by viewModel.syncFolderUri.collectAsState()
    val lastSyncAt by viewModel.lastSyncAt.collectAsState()
    val launchMode by viewModel.launchMode.collectAsState()
    val launchFileId by viewModel.launchFileId.collectAsState()
    val autoOpenFileId by viewModel.autoOpenFileId.collectAsState()
    val isAutoOpenReady by viewModel.isAutoOpenReady.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState(initial = emptyList())
    val showPrecisionEllipsis by viewModel.showPrecisionEllipsis.collectAsState()
    val showScratchpad by viewModel.showScratchpad.collectAsState()
    val editorFontSize by viewModel.editorFontSize.collectAsState()
    val customBackupFolderSummary = remember(customBackupFolderUri) {
        val uriString = customBackupFolderUri
        if (uriString != null) {
            val parsed = Uri.parse(uriString)
            val segment = parsed.lastPathSegment ?: ""
            val decoded = Uri.decode(segment) ?: ""
            val formatted = FileUtils.formatPathForDisplay(decoded)
            if (formatted.isBlank()) "Custom folder selected" else formatted
        } else {
            "Not selected"
        }
    }
    val currentLocationText = formatBackupLocationText(
        mode = backupLocationMode,
        customFolderSummary = customBackupFolderSummary
    )

    var showHomeRestoreActionDialog by remember { mutableStateOf(false) }
    var showHomeRestoreListDialog by remember { mutableStateOf(false) }
    var suppressHomeAutoOpenOnce by rememberSaveable { mutableStateOf(false) }

    // Export launcher - creates a one-off ZIP file at user-chosen location
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(Constants.EXPORT_MIME_TYPE)
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val result = viewModel.exportAllFiles(context, it)
                result.onSuccess { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(context, "Export failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Import launcher - opens a ZIP file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importFiles(context, it)
        }
    }

    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (_: SecurityException) {
            }
            viewModel.setCustomBackupFolder(context, it)
            Toast.makeText(context, "Custom backup folder saved", Toast.LENGTH_SHORT).show()
        }
    }

    val syncFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (_: SecurityException) {
            }
            viewModel.setSyncFolder(context, it)
            Toast.makeText(context, "Sync folder saved", Toast.LENGTH_SHORT).show()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncFiles(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Reusable slide animations for detail screens
    val slideInFromRight = slideInHorizontally(animationSpec = tween(300), initialOffsetX = { fullWidth: Int -> fullWidth })
    val slideOutToLeft = slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { fullWidth: Int -> -fullWidth / 3 })
    val slideInFromLeft = slideInHorizontally(animationSpec = tween(300), initialOffsetX = { fullWidth: Int -> -fullWidth / 3 })
    val slideOutToRight = slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { fullWidth: Int -> fullWidth })

    val initialStartDestination = remember { if (viewModel.launchMode.value != LaunchMode.NOT_SET) "startup" else "home" }

    NavHost(
        navController = navController,
        startDestination = initialStartDestination
    ) {
        composable("startup") {
            var timedOut by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(5_000L)
                if (!isAutoOpenReady) {
                    timedOut = true
                }
            }

            LaunchedEffect(isAutoOpenReady, autoOpenFileId, timedOut) {
                if (timedOut || (isAutoOpenReady && autoOpenFileId == null)) {
                    navController.navigate("home") {
                        popUpTo("startup") { inclusive = true }
                        launchSingleTop = true
                    }
                } else if (isAutoOpenReady && autoOpenFileId != null) {
                        suppressHomeAutoOpenOnce = true
                        navController.navigate("home") {
                            popUpTo("startup") { inclusive = true }
                            launchSingleTop = true
                        }
                        navController.navigate("editor/$autoOpenFileId")
                    }
                }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Home Screen
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onFileClick = { fileId -> navController.navigate("editor/$fileId") },
                onSettingsClick = { navController.navigate("settings") },
                onHelpClick = { navController.navigate("help") },
                onChangelogClick = { navController.navigate("changelog") },
                onRestoreClick = {
                    viewModel.refreshBackups(context)
                    showHomeRestoreActionDialog = true
                },
                onSearchClick = { navController.navigate("search") },
                launchMode = launchMode,
                autoOpenFileId = autoOpenFileId,
                isAutoOpenReady = isAutoOpenReady,
                suppressAutoOpenScratchpad = suppressHomeAutoOpenOnce,
                showScratchpad = showScratchpad
            )
        }

        // Calculator Editor Screen
        composable(
            "editor/{fileId}",
            arguments = listOf(navArgument("fileId") { type = NavType.LongType }),
            enterTransition = {
                if (initialState.destination.route == "startup") EnterTransition.None else slideInFromRight
            },
            exitTransition = {
                if (targetState.destination.route == "startup") ExitTransition.None else slideOutToLeft
            },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) { backStackEntry ->
            val showPrecisionEllipsis by viewModel.showPrecisionEllipsis.collectAsState()
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            CalculatorScreen(
                fileId = fileId,
                viewModel = viewModel,
                regionCode = regionCode,
                showPrecisionEllipsis = showPrecisionEllipsis,
                editorFontSize = editorFontSize,
                onBack = { navController.popBackStack() },
                onHelp = { navController.navigate("help") },
                onNavigateToFile = { newFileId ->
                    navController.navigate("editor/$newFileId") {
                        popUpTo("editor/$fileId") { inclusive = true }
                    }
                }
            )
        }

        // Settings Screen
        composable(
            "settings",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            SettingsMainScreen(
                onNavigateToAppearance = { navController.navigate("settings_appearance") },
                onNavigateToCalculator = { navController.navigate("settings_calculator") },
                onNavigateToHomeStartup = { navController.navigate("settings_home_startup") },
                onNavigateToDataSync = { navController.navigate("settings_data_sync") },
                onNavigateToHelpFeedback = { navController.navigate("settings_help_feedback") },
                onNavigateToLegal = { navController.navigate("settings_legal") },
                onNavigateToAbout = { navController.navigate("settings_about") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_appearance",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            AppearanceSettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = { theme -> viewModel.setTheme(theme) },
                colorPalette = colorPalette,
                onColorPaletteChange = { palette -> viewModel.setColorPalette(palette) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_calculator",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            val precision by viewModel.precision.collectAsState()
            val rationalMode by viewModel.rationalMode.collectAsState()
            val groupingSeparatorEnabled by viewModel.groupingSeparatorEnabled.collectAsState()
            val showPrecisionEllipsis by viewModel.showPrecisionEllipsis.collectAsState()
            val editorFontSize by viewModel.editorFontSize.collectAsState()

            CalculatorSettingsScreen(
                precision = precision,
                onPrecisionChange = { viewModel.setPrecision(it) },
                rationalMode = rationalMode,
                onRationalModeChange = { viewModel.setRationalMode(it) },
                regionCode = regionCode,
                onRegionCodeChange = { viewModel.setRegionCode(it) },
                groupingSeparatorEnabled = groupingSeparatorEnabled,
                onGroupingSeparatorEnabledChange = { viewModel.setGroupingSeparatorEnabled(it) },
                showPrecisionEllipsis = showPrecisionEllipsis,
                onShowPrecisionEllipsisChange = { viewModel.setShowPrecisionEllipsis(it) },
                editorFontSize = editorFontSize,
                onEditorFontSizeChange = { viewModel.setEditorFontSize(it) },
                showLineNumbers = showLineNumbers,
                onShowLineNumbersChange = { viewModel.setShowLineNumbers(it) },
                showSuggestions = showSuggestions,
                onShowSuggestionsChange = { viewModel.setShowSuggestions(it) },
                showSymbolsShortcuts = showSymbolsShortcuts,
                onShowSymbolsShortcutsChange = { viewModel.setShowSymbolsShortcuts(it) },
                showNumbersShortcuts = showNumbersShortcuts,
                onShowNumbersShortcutsChange = { viewModel.setShowNumbersShortcuts(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_home_startup",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            HomeStartupSettingsScreen(
                launchMode = launchMode,
                onLaunchModeChange = { viewModel.setLaunchMode(it) },
                launchFileId = launchFileId,
                onLaunchFileIdChange = { viewModel.setLaunchFileId(it) },
                allFiles = allFiles,
                onValidateLaunchFile = { fileId, callback ->
                    viewModel.validateLaunchFile(fileId, callback)
                },
                showScratchpad = showScratchpad,
                onShowScratchpadChange = { viewModel.setShowScratchpad(it) },
                onChooseOtherMode = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_data_sync",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            LaunchedEffect(Unit) {
                viewModel.refreshBackups(context)
            }
            DataSyncSettingsScreen(
                autoBackupEnabled = autoBackupEnabled,
                onAutoBackupEnabledChange = { viewModel.setAutoBackupEnabled(context, it) },
                backupFrequency = backupFrequency,
                onBackupFrequencyChange = { viewModel.setBackupFrequency(context, it) },
                backupLocationMode = backupLocationMode,
                backupLocationSummary = customBackupFolderSummary,
                onChooseBackupFolder = { backupFolderLauncher.launch(null) },
                onUseAppStorageLocation = { viewModel.setBackupLocationToAppStorage(context) },
                onBackupNow = {
                    coroutineScope.launch {
                        val result = viewModel.backupNow(context)
                        result.onSuccess { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            Toast.makeText(context, "Backup failed: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onBackupNowAtDifferentLocation = {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
                    val filename = "nerdcalci_backup_$timestamp.zip"
                    exportLauncher.launch(filename)
                },
                lastBackupAt = lastBackupAt,
                availableBackups = availableBackups,
                onRestoreBackup = { viewModel.restoreFromBackup(context, it) },
                onRestoreFromDifferentLocation = {
                    importLauncher.launch(arrayOf(Constants.EXPORT_MIME_TYPE))
                },
                syncEnabled = syncEnabled,
                onSyncEnabledChange = { viewModel.setSyncEnabled(context, it) },
                syncFolderUri = syncFolderUri,
                onChooseSyncFolder = { syncFolderLauncher.launch(null) },
                lastSyncAt = lastSyncAt,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_help_feedback",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            HelpFeedbackSettingsScreen(
                onHelp = { navController.navigate("help") },
                onChangelog = { navController.navigate("changelog") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_legal",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            LegalSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "settings_about",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            val context = LocalContext.current
            val appVersion = remember {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val versionName = packageInfo.versionName ?: "Unknown"
                    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    }
                    "v$versionName ($versionCode)"
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e("MainActivity", "Failed to retrieve app version information", e)
                    "Unknown"
                }
            }
            AboutSettingsScreen(
                appVersion = appVersion,
                onBack = { navController.popBackStack() }
            )
        }

        // Help Screen
        composable(
            "help",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        // Changelog Screen
        composable(
            "changelog",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            ChangelogScreen(onBack = { navController.popBackStack() })
        }

        // Search Screen
        composable(
            "search",
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            SearchScreen(
                viewModel = viewModel,
                onFileClick = { fileId ->
                    navController.navigate("editor/$fileId")
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

    RestoreSourceDialog(
        visible = showHomeRestoreActionDialog,
        hasBackupsInCurrentLocation = availableBackups.isNotEmpty(),
        currentLocationText = currentLocationText,
        onDismiss = { showHomeRestoreActionDialog = false },
        onUseCurrentLocation = {
            showHomeRestoreActionDialog = false
            showHomeRestoreListDialog = true
        },
        onChooseDifferentFile = {
            showHomeRestoreActionDialog = false
            importLauncher.launch(arrayOf(Constants.EXPORT_MIME_TYPE))
        }
    )

    RestoreBackupListDialog(
        visible = showHomeRestoreListDialog,
        currentLocationText = currentLocationText,
        backups = availableBackups,
        onDismiss = { showHomeRestoreListDialog = false },
        onBackupSelected = { backup ->
            showHomeRestoreListDialog = false
            viewModel.restoreFromBackup(context, backup)
        }
    )

    RestoreProgressDialog(
        visible = restoreProgress.isProcessing,
        currentFile = restoreProgress.currentFile,
        current = restoreProgress.current,
        total = restoreProgress.total,
        conflictFile = restoreProgress.conflictFile,
        localModified = restoreProgress.localConflictModified,
        zipModified = restoreProgress.zipConflictModified,
        onResolveConflict = { resolution, remember ->
            viewModel.resolveConflict(resolution, remember)
        },
        onCancel = {
            viewModel.cancelRestore()
        }
    )

    RestoreCompleteDialog(
        visible = restoreProgress.completionMessage != null,
        message = restoreProgress.completionMessage ?: "",
        addedCount = restoreProgress.addedCount,
        overwrittenCount = restoreProgress.overwrittenCount,
        skippedCount = restoreProgress.skippedCount,
        isSuccess = restoreProgress.isSuccess,
        onDismiss = {
            viewModel.dismissRestoreStats()
            navController.navigate("home") {
                popUpTo("home") { inclusive = false }
                launchSingleTop = true
            }
        }
    )
}