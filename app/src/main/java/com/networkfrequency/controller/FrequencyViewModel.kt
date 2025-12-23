package com.networkfrequency.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FrequencyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: FrequencyRepository
    val allLogs: LiveData<List<FrequencyChangeLog>>
    
    init {
        val frequencyDao = FrequencyDatabase.getDatabase(application).frequencyDao()
        repository = FrequencyRepository(frequencyDao)
        allLogs = repository.allLogs
    }
    
    fun insertLog(log: FrequencyChangeLog) = viewModelScope.launch {
        repository.insertLog(log)
    }
    
    fun logFrequencyChange(
        oldBand: String,
        oldFrequency: String,
        newBand: String,
        newFrequency: String,
        status: String,
        operatorName: String,
        networkType: String
    ) {
        val log = FrequencyChangeLog(
            timestamp = System.currentTimeMillis(),
            oldBand = oldBand,
            oldFrequency = oldFrequency,
            newBand = newBand,
            newFrequency = newFrequency,
            status = status,
            operatorName = operatorName,
            networkType = networkType
        )
        insertLog(log)
    }
}
