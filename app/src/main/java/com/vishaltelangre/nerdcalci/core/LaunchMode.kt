package com.vishaltelangre.nerdcalci.core

/**
 * Launch modes for the application.
 *
 * [NOT_SET] - No file auto-opened, shows home screen.
 * [SCRATCHPAD] - Auto-opens the temporary scratchpad.
 * [JOURNAL] - Auto-opens today's date-based file (YYYY-MM-DD).
 * [SPECIFIC_FILE] - Auto-opens a specific file chosen by the user.
 */
enum class LaunchMode(val prefValue: String) {
    NOT_SET("not_set"),
    SCRATCHPAD("scratchpad"),
    JOURNAL("journal"),
    SPECIFIC_FILE("specific_file");

    companion object {
        fun fromPrefValue(value: String?): LaunchMode =
            entries.find { it.prefValue == value } ?: NOT_SET
    }
}
