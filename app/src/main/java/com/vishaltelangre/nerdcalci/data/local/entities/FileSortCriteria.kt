package com.vishaltelangre.nerdcalci.data.local.entities

import org.json.JSONObject

enum class FileSortOption {
    MODIFIED_AT,
    CREATED_AT,
    NAME;

    companion object {
        fun fromString(value: String): FileSortOption {
            return entries.find { it.name == value } ?: MODIFIED_AT
        }
    }
}

enum class FileSortDirection {
    ASCENDING,
    DESCENDING;

    companion object {
        fun fromString(value: String): FileSortDirection {
            return entries.find { it.name == value } ?: DESCENDING
        }
    }
}

data class FileSortCriteria(
    val option: FileSortOption = FileSortOption.MODIFIED_AT,
    val direction: FileSortDirection = FileSortDirection.DESCENDING
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("option", option.name)
            put("direction", direction.name)
        }.toString()
    }

    companion object {
        fun fromJson(json: String?): FileSortCriteria {
            if (json == null) return FileSortCriteria()
            return try {
                val obj = JSONObject(json)
                FileSortCriteria(
                    option = FileSortOption.fromString(obj.optString("option")),
                    direction = FileSortDirection.fromString(obj.optString("direction"))
                )
            } catch (e: Exception) {
                FileSortCriteria()
            }
        }
    }
}
