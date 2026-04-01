package com.vishaltelangre.nerdcalci.utils

import java.util.Locale

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
}
