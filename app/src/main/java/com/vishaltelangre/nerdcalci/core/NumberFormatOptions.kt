package com.vishaltelangre.nerdcalci.core

enum class NumberSeparatorPreset(val prefValue: String) {
    LOCALE("locale"),
    COMMA("comma"),
    DOT("dot"),
    SPACE("space"),
    OFF("off");

    companion object {
        fun fromPrefValue(value: String?): NumberSeparatorPreset {
            return entries.firstOrNull { it.prefValue == value } ?: LOCALE
        }
    }
}

enum class NumberDecimalPreset(val prefValue: String) {
    LOCALE("locale"),
    COMMA("comma"),
    DOT("dot");

    companion object {
        fun fromPrefValue(value: String?): NumberDecimalPreset {
            return entries.firstOrNull { it.prefValue == value } ?: LOCALE
        }
    }
}

data class NumberFormatSettings(
    val separators: NumberSeparatorPreset = NumberSeparatorPreset.LOCALE,
    val decimal: NumberDecimalPreset = NumberDecimalPreset.LOCALE,
    val useIndianStyle: Boolean = false
)
