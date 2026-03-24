package com.vishaltelangre.nerdcalci.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vishaltelangre.nerdcalci.di.CalculatorViewModelFactory
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorScreen
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel
import com.vishaltelangre.nerdcalci.ui.components.formatBackupLocationText
import com.vishaltelangre.nerdcalci.ui.components.RestoreBackupListDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreSourceDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreProgressDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreCompleteDialog
import com.vishaltelangre.nerdcalci.ui.help.HelpScreen
import com.vishaltelangre.nerdcalci.ui.home.HomeScreen
import com.vishaltelangre.nerdcalci.ui.settings.SettingsScreen
import com.vishaltelangre.nerdcalci.ui.changelog.ChangelogScreen
import com.vishaltelangre.nerdcalci.ui.search.SearchScreen
import com.vishaltelangre.nerdcalci.ui.theme.NerdCalciTheme
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
            val isDarkTheme = when (currentTheme) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            NerdCalciTheme(darkTheme = isDarkTheme) {
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
    val restoreProgress by viewModel.restoreProgress.collectAsState()
    
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val syncFolderUri by viewModel.syncFolderUri.collectAsState()
    val lastSyncAt by viewModel.lastSyncAt.collectAsState()
    val customBackupFolderSummary = remember(customBackupFolderUri) {
        customBackupFolderUri?.let { uriString ->
            val parsed = Uri.parse(uriString)
            Uri.decode(parsed.lastPathSegment ?: "")
                .ifBlank { "Custom folder selected" }
        } ?: "Not selected"
    }
    val currentLocationText = formatBackupLocationText(
        mode = backupLocationMode,
        customFolderSummary = customBackupFolderSummary
    )
    var showHomeRestoreActionDialog by remember { mutableStateOf(false) }
    var showHomeRestoreListDialog by remember { mutableStateOf(false) }

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

    NavHost(navController = navController, startDestination = "home") {
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
                onSearchClick = { navController.navigate("search") }
            )
        }

        // Calculator Editor Screen
        composable(
            "editor/{fileId}",
            arguments = listOf(navArgument("fileId") { type = NavType.LongType }),
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) { backStackEntry ->
            val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
            CalculatorScreen(
                fileId = fileId,
                viewModel = viewModel,
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
            LaunchedEffect(Unit) {
                viewModel.refreshBackups(context)
            }
            val precision by viewModel.precision.collectAsState()
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = { theme -> viewModel.setTheme(theme) },
                autoBackupEnabled = autoBackupEnabled,
                onAutoBackupEnabledChange = { enabled -> viewModel.setAutoBackupEnabled(context, enabled) },
                backupFrequency = backupFrequency,
                onBackupFrequencyChange = { frequency -> viewModel.setBackupFrequency(context, frequency) },
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
                onRestoreBackup = { backup ->
                    viewModel.restoreFromBackup(context, backup)
                },
                onRestoreFromDifferentLocation = { importLauncher.launch(arrayOf("application/zip")) },
                precision = precision,
                onPrecisionChange = { newPrecision -> viewModel.setPrecision(newPrecision) },
                showLineNumbers = showLineNumbers,
                onShowLineNumbersChange = { newShowLineNumbers -> viewModel.setShowLineNumbers(newShowLineNumbers) },
                showSuggestions = showSuggestions,
                onShowSuggestionsChange = { newShowSuggestions -> viewModel.setShowSuggestions(newShowSuggestions) },
                showSymbolsShortcuts = showSymbolsShortcuts,
                onShowSymbolsShortcutsChange = { viewModel.setShowSymbolsShortcuts(it) },
                showNumbersShortcuts = showNumbersShortcuts,
                onShowNumbersShortcutsChange = { viewModel.setShowNumbersShortcuts(it) },
                syncEnabled = syncEnabled,
                onSyncEnabledChange = { viewModel.setSyncEnabled(context, it) },
                syncFolderUri = syncFolderUri,
                onChooseSyncFolder = { syncFolderLauncher.launch(null) },
                lastSyncAt = lastSyncAt,
                onHelp = { navController.navigate("help") },
                onChangelog = { navController.navigate("changelog") },
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
            importLauncher.launch(arrayOf("application/zip"))
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
