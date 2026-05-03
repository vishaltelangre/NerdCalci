package com.vishaltelangre.nerdcalci.utils

import java.util.Locale
import com.vishaltelangre.nerdcalci.core.Constants

object RegionUtils {
    const val SYSTEM_DEFAULT = "system"
    private val localeCache = mutableMapOf<String, Locale>()

    /**
     * Returns a list of all available regions (ISO country codes) with their display names.
     * The list is sorted by the display name.
     */
    fun getAvailableRegions(): List<Pair<String, String>> {
        return Locale.getISOCountries().map { countryCode ->
            val locale = Locale("", countryCode)
            Pair(countryCode, locale.displayCountry)
        }.filter { it.second.isNotEmpty() }.sortedBy { it.second }
    }

    fun getLocaleForRegion(regionCode: String, systemLocale: Locale = Locale.getDefault()): Locale {
        if (regionCode == SYSTEM_DEFAULT) return systemLocale
        
        return localeCache.getOrPut(regionCode) {
            val available = Locale.getAvailableLocales()
            val expectedLanguage = regionCode.lowercase()
            
            val exactMatch = available.find { it.country == regionCode && it.language == expectedLanguage }
            if (exactMatch != null) return@getOrPut exactMatch

            val enMatch = available.find { it.country == regionCode && it.language == "en" }
            if (enMatch != null) return@getOrPut enMatch

            available.find { it.country == regionCode } ?: Locale("", regionCode)
        }
    }

    /**
     * Determines the natural date format order for a given locale.
     * Uses java.text.DateFormat to inspect the short date pattern.
     */
    fun getDefaultDateFormat(locale: Locale): String {
        return try {
            val df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, locale)
            val pattern = (df as? java.text.SimpleDateFormat)?.toPattern() ?: ""
            when {
                pattern.startsWith("d", ignoreCase = true) -> Constants.DATE_FORMAT_DMY
                pattern.startsWith("M", ignoreCase = true) -> Constants.DATE_FORMAT_MDY
                pattern.startsWith("y", ignoreCase = true) -> Constants.DATE_FORMAT_YMD
                else -> Constants.DATE_FORMAT_DMY // Fallback for most of the world
            }
        } catch (_: Exception) {
            Constants.DATE_FORMAT_DMY
        }
    }
}
