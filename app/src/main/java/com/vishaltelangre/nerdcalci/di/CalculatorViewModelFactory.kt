package com.vishaltelangre.nerdcalci.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vishaltelangre.nerdcalci.data.local.CalculatorDao
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

class CalculatorViewModelFactory(private val dao: CalculatorDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
