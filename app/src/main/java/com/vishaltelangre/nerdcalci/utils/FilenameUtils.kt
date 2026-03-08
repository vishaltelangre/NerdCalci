package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.core.Constants

object FilenameUtils {
    /**
     * Generates a unique filename by appending a numerical suffix (e.g., " (1)")
     * if the name is already taken.
     *
     * @param baseName The starting name.
     * @param isNameTaken A lambda that returns true if the given name is already in use.
     * @return A unique version of the baseName.
     */
    suspend fun generateUniqueFileName(
        baseName: String,
        isNameTaken: suspend (String) -> Boolean
    ): String {
        var name = baseName
        var counter = 1
        while (isNameTaken(name)) {
            val suffix = " ($counter)"
            val maxBaseLength = Constants.MAX_FILE_NAME_LENGTH - suffix.length
            val truncatedBase = if (baseName.length > maxBaseLength) {
                baseName.take(maxBaseLength)
            } else {
                baseName
            }
            name = "$truncatedBase$suffix"
            counter++
        }
        return name
    }
}
