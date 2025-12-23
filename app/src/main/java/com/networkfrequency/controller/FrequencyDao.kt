package com.networkfrequency.controller

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FrequencyDao {
    @Insert
    suspend fun insertLog(log: FrequencyChangeLog)
    
    @Query("SELECT * FROM frequency_change_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<FrequencyChangeLog>>
    
    @Query("SELECT * FROM frequency_change_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): LiveData<List<FrequencyChangeLog>>
    
    @Query("DELETE FROM frequency_change_logs")
    suspend fun deleteAllLogs()
}
