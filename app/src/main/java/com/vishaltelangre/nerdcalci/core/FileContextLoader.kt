package com.vishaltelangre.nerdcalci.core

/**
 * Interface for loading the calculation context of another file on demand.
 * Used for multi-file variables cross-referencing.
 */
interface FileContextLoader {
    /**
     * Loads and evaluates the context of the given [fileName].
     * [loadingStack] is used to prevent circular references and infinite recursion.
     */
    suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext?
}
