package com.vishaltelangre.nerdcalci.ui.calculator

enum class SuggestionType {
    VARIABLE, LOCAL_FUNCTION, GLOBAL_FUNCTION, CONSTANT, DYNAMIC_VARIABLE
}

data class Suggestion(
    val name: String,
    val type: SuggestionType,
    val matchIndices: List<Int> = emptyList(),
    val score: Int = 0
)
