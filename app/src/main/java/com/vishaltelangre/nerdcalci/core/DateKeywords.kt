package com.vishaltelangre.nerdcalci.core

object DateKeywords {

    /**
     * Keywords that resolve to a date/time at evaluation time.
     * CANNOT be on the left-hand side of an assignment.
     * CAN appear in expression position (e.g., `d = today + 3 weeks`).
     */
    val RELATIVE: Set<String> = setOf("today", "yesterday", "tomorrow", "now")

    /**
     * All reserved date keywords — none may be used as variable or function names.
     */
    val RESERVED: Set<String> = RELATIVE + setOf(
        "before", "after", "ago", "from",
        "since", "till", "until", "through", "between", "and"
    )

    /**
     * Month name → month number (1-based). Case-insensitive lookup.
     * Both full names (january) and short names (jan) are mapped.
     */
    val MONTH_NAMES: Map<String, Int> = mapOf(
        "january" to 1, "jan" to 1,
        "february" to 2, "feb" to 2,
        "march" to 3, "mar" to 3,
        "april" to 4, "apr" to 4,
        "may" to 5,
        "june" to 6, "jun" to 6,
        "july" to 7, "jul" to 7,
        "august" to 8, "aug" to 8,
        "september" to 9, "sep" to 9, "sept" to 9,
        "october" to 10, "oct" to 10,
        "november" to 11, "nov" to 11,
        "december" to 12, "dec" to 12
    )

    /** All recognised month names as a flat set (lowercase), for fast membership test. */
    val ALL_MONTH_NAMES: Set<String> = MONTH_NAMES.keys.toSet()

    /** Returns the month number for a name string, or null if unrecognized. (Case-insensitive.) */
    fun monthNumber(name: String): Int? = MONTH_NAMES[name.lowercase()]
}
