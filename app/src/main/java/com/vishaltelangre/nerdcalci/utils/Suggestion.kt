package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.UnitCategory

enum class SuggestionType {
    VARIABLE, LOCAL_FUNCTION, GLOBAL_FUNCTION, CONSTANT, DYNAMIC_VARIABLE, FILE, UNIT, KEYWORD
}

data class Suggestion(
    val name: String,
    val type: SuggestionType,
    val matchIndices: List<Int> = emptyList(),
    val score: Int = 0,
    val description: String? = null,
    val replaceStart: Int? = null
)

data class SuggestionContextInfo(
    val word: String,
    val type: SuggestionType,
    val isExplicitTrigger: Boolean,
    val unitCategory: UnitCategory? = null,
    val replaceStart: Int? = null,
    val argumentIndex: Int? = null,
    val unitStart: Int? = null,
    val needsSpace: Boolean = false
)
