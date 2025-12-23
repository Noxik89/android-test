package com.networkfrequency.controller

import androidx.lifecycle.LiveData

class FrequencyRepository(private val frequencyDao: FrequencyDao) {
    
    val allLogs: LiveData<List<FrequencyChangeLog>> = frequencyDao.getAllLogs()
    
    suspend fun insertLog(log: FrequencyChangeLog) {
        frequencyDao.insertLog(log)
    }
    
    suspend fun deleteAllLogs() {
        frequencyDao.deleteAllLogs()
    }
}
