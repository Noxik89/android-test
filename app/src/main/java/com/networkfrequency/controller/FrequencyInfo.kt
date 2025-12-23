package com.networkfrequency.controller

data class FrequencyInfo(
    val band: String,
    val frequency: String,
    val signalStrength: String,
    val channel: Int = 0,
    val isActive: Boolean = false
)

data class NetworkInfo(
    val operatorName: String,
    val networkType: String,
    val currentFrequency: FrequencyInfo?,
    val availableFrequencies: List<FrequencyInfo>
)
