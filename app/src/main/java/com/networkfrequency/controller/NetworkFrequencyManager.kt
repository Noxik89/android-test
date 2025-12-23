package com.networkfrequency.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

class NetworkFrequencyManager(private val context: Context) {
    
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    fun hasRequiredPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getNetworkInfo(): NetworkInfo {
        if (!hasRequiredPermissions()) {
            return NetworkInfo(
                operatorName = "Permission Required",
                networkType = "Unknown",
                currentFrequency = null,
                availableFrequencies = emptyList()
            )
        }
        
        val operatorName = telephonyManager.networkOperatorName ?: "Unknown"
        val networkType = getNetworkTypeName(telephonyManager.dataNetworkType)
        
        val cellInfoList = try {
            if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PRECISE_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
            ) {
                telephonyManager.allCellInfo
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        }
        
        val frequencies = mutableListOf<FrequencyInfo>()
        var currentFrequency: FrequencyInfo? = null
        
        cellInfoList?.forEach { cellInfo ->
            val frequencyInfo = parseCellInfo(cellInfo)
            if (frequencyInfo != null) {
                frequencies.add(frequencyInfo)
                if (cellInfo.isRegistered && currentFrequency == null) {
                    currentFrequency = frequencyInfo.copy(isActive = true)
                }
            }
        }
        
        return NetworkInfo(
            operatorName = operatorName,
            networkType = networkType,
            currentFrequency = currentFrequency,
            availableFrequencies = frequencies
        )
    }
    
    private fun parseCellInfo(cellInfo: CellInfo): FrequencyInfo? {
        return when (cellInfo) {
            is CellInfoLte -> {
                val identity = cellInfo.cellIdentity as CellIdentityLte
                val signalStrength = cellInfo.cellSignalStrength.dbm
                val earfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    identity.earfcn
                } else {
                    0
                }
                val band = getLteBandFromEarfcn(earfcn)
                val frequency = getLteFrequencyFromEarfcn(earfcn)
                
                FrequencyInfo(
                    band = "LTE Band $band",
                    frequency = "$frequency MHz",
                    signalStrength = "$signalStrength dBm",
                    channel = earfcn
                )
            }
            is CellInfoWcdma -> {
                val identity = cellInfo.cellIdentity as CellIdentityWcdma
                val signalStrength = cellInfo.cellSignalStrength.dbm
                val uarfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    identity.uarfcn
                } else {
                    0
                }
                val band = getWcdmaBandFromUarfcn(uarfcn)
                val frequency = getWcdmaFrequencyFromUarfcn(uarfcn)
                
                FrequencyInfo(
                    band = "WCDMA Band $band",
                    frequency = "$frequency MHz",
                    signalStrength = "$signalStrength dBm",
                    channel = uarfcn
                )
            }
            is CellInfoNr -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val identity = cellInfo.cellIdentity as CellIdentityNr
                    val signalStrength = cellInfo.cellSignalStrength.dbm
                    val nrarfcn = identity.nrarfcn
                    val band = "NR"
                    
                    FrequencyInfo(
                        band = "5G NR",
                        frequency = "${nrarfcn / 1000} MHz",
                        signalStrength = "$signalStrength dBm",
                        channel = nrarfcn
                    )
                } else {
                    null
                }
            }
            else -> null
        }
    }
    
    private fun getLteBandFromEarfcn(earfcn: Int): Int {
        return when {
            earfcn in 0..599 -> 1
            earfcn in 600..1199 -> 2
            earfcn in 1200..1949 -> 3
            earfcn in 1950..2399 -> 4
            earfcn in 2400..2649 -> 5
            earfcn in 2650..2749 -> 6
            earfcn in 2750..3449 -> 7
            earfcn in 3450..3799 -> 8
            earfcn in 3800..4149 -> 9
            earfcn in 4150..4749 -> 10
            earfcn in 9210..9659 -> 20
            earfcn in 36000..36199 -> 33
            earfcn in 36200..36349 -> 34
            earfcn in 36350..36949 -> 35
            earfcn in 36950..37549 -> 36
            earfcn in 37550..37749 -> 37
            earfcn in 37750..38249 -> 38
            earfcn in 38250..38649 -> 39
            earfcn in 38650..39649 -> 40
            earfcn in 39650..41589 -> 41
            earfcn in 41590..43589 -> 42
            earfcn in 43590..45589 -> 43
            else -> 0
        }
    }
    
    private fun getLteFrequencyFromEarfcn(earfcn: Int): Int {
        val band = getLteBandFromEarfcn(earfcn)
        return when (band) {
            1 -> 2100
            2 -> 1900
            3 -> 1800
            4 -> 1700
            5 -> 850
            7 -> 2600
            8 -> 900
            20 -> 800
            28 -> 700
            38 -> 2600
            40 -> 2300
            41 -> 2500
            else -> 0
        }
    }
    
    private fun getWcdmaBandFromUarfcn(uarfcn: Int): Int {
        return when {
            uarfcn in 10562..10838 -> 1
            uarfcn in 9662..9938 -> 2
            uarfcn in 1162..1513 -> 3
            uarfcn in 1537..1738 -> 4
            uarfcn in 4357..4458 -> 5
            uarfcn in 4387..4413 -> 6
            uarfcn in 2237..2563 -> 7
            uarfcn in 2937..3088 -> 8
            else -> 0
        }
    }
    
    private fun getWcdmaFrequencyFromUarfcn(uarfcn: Int): Int {
        val band = getWcdmaBandFromUarfcn(uarfcn)
        return when (band) {
            1 -> 2100
            2 -> 1900
            3 -> 1800
            4 -> 1700
            5 -> 850
            8 -> 900
            else -> 0
        }
    }
    
    private fun getNetworkTypeName(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            
            else -> "Unknown"
        }
    }
}
