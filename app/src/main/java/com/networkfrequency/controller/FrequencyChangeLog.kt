package com.networkfrequency.controller

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frequency_change_logs")
data class FrequencyChangeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val oldBand: String,
    val oldFrequency: String,
    val newBand: String,
    val newFrequency: String,
    val status: String,
    val operatorName: String,
    val networkType: String
)
