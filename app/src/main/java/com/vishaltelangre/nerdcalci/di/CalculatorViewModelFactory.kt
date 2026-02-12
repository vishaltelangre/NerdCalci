package com.vishaltelangre.nerdcalci.di

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

/**
 * Factory for creating CalculatorViewModel with constructor parameters.
 *
 * ViewModels cannot have constructor parameters without a custom factory because
 * the Android system creates them. This factory enables dependency injection by:
 * - Accepting CalculatorDao and SharedPreferences as parameters
 * - Passing them to CalculatorViewModel when the system requests creation
 *
 * Usage in Activity/Fragment:
 * ```
 * val factory = CalculatorViewModelFactory(dao, prefs)
 * val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]
 * ```
 */
class CalculatorViewModelFactory(
    private val dao: CalculatorDao,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(dao, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
