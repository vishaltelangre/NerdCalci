package com.vishaltelangre.nerdcalci.core

/**
 * Application-wide constants
 *
 * Note:
 * - App name is defined in res/values/strings.xml as "app_name"
 * - App version is defined in app/build.gradle.kts as "versionName" and "versionCode"
 */
object Constants {
    // Database
    const val DATABASE_NAME = "calci-db"

    // General info
    const val SOURCE_CODE_URL = "https://github.com/vishaltelangre/NerdCalci"
    const val PRIVACY_POLICY_URL = "https://github.com/vishaltelangre/NerdCalci/blob/main/PRIVACY.md"
    const val TERMS_OF_SERVICE_URL = "https://github.com/vishaltelangre/NerdCalci/blob/main/TERMS.md"
    const val SUPPORT_ISSUES_URL = "https://github.com/vishaltelangre/NerdCalci/issues/new"
    const val DEVELOPER_NAME = "Vishal Telangre"
    const val DEVELOPER_TWITTER_URL = "https://x.com/suruwat"
    const val BUY_ME_COFFEE_URL = "https://buymeacoffee.com/vishaltelangre"
    const val LICENSE = "GNU General Public License v3.0"

    // File management
    const val MAX_FILE_NAME_LENGTH = 50
    const val MAX_PINNED_FILES = 10
    const val EMPTY_FILE_CLEANUP_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes

    // Undo/Redo
    const val MAX_HISTORY_SIZE = 30

    // Export/Import
    const val EXPORT_FILE_EXTENSION = ".nerdcalci"
    const val EXPORT_MIME_TYPE = "application/zip"
    const val DEFAULT_BACKUP_KEEP_COUNT = 30

    // Variable/Function naming
    /**
     * Regex pattern for valid variable and function names in calculator expressions.
     * Names must start with a letter or underscore, followed by letters, digits, or underscores.
     * Examples: "price", "rate_2", "rate3", "_private", "__internal__"
     * Invalid: "2rate", "rate-disc", "rate with disc"
     */
    const val VAR_FUNC_NAME_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*$"

    // Decimal precision settings
    const val SYNC_ENGINE_PRECISION = "precision"
    const val DEFAULT_PRECISION = 2
    const val MIN_PRECISION = 0
    const val MAX_PRECISION = 10

    const val SYNC_ENGINE_RATIONAL_MODE = "rational_mode"
    const val DEFAULT_RATIONAL_MODE = false

    // Temporary scratchpad settings
    const val PREF_AUTO_OPEN_SCRATCHPAD = "auto_open_scratchpad" // @deprecated
    const val PREF_SHOW_SCRATCHPAD = "show_scratchpad"
    const val PREF_LAUNCH_MODE = "launch_mode"
    const val PREF_LAUNCH_FILE_ID = "launch_file_id"
    const val PREF_SHOW_PRECISION_ELLIPSIS = "show_precision_ellipsis"
    const val PREF_FILE_SORT_CRITERIA = "file_sort_criteria"
    const val PREF_COLOR_PALETTE = "color_palette"
    const val PREF_DYNAMIC_COLOR = "dynamic_color"
    const val SCRATCHPAD_DISPLAY_NAME = "Scratchpad"

    // Editor font size settings
    const val PREF_EDITOR_FONT_SIZE = "editor_font_size"
    const val DEFAULT_EDITOR_FONT_SIZE = 16f
    const val MIN_EDITOR_FONT_SIZE = 6f
    const val MAX_EDITOR_FONT_SIZE = 30f
}
