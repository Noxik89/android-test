package com.networkfrequency.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    
    private lateinit var frequencyManager: NetworkFrequencyManager
    private lateinit var viewModel: FrequencyViewModel
    private lateinit var frequencyAdapter: FrequencyAdapter
    
    private lateinit var operatorNameText: TextView
    private lateinit var networkTypeText: TextView
    private lateinit var currentFrequencyText: TextView
    private lateinit var frequenciesRecyclerView: RecyclerView
    private lateinit var noFrequenciesText: TextView
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var historyFab: ExtendedFloatingActionButton
    
    private var currentNetworkInfo: NetworkInfo? = null
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadNetworkInfo()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        frequencyManager = NetworkFrequencyManager(this)
        viewModel = ViewModelProvider(this)[FrequencyViewModel::class.java]
        
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        
        checkPermissionsAndLoad()
    }
    
    private fun initializeViews() {
        operatorNameText = findViewById(R.id.operatorName)
        networkTypeText = findViewById(R.id.networkType)
        currentFrequencyText = findViewById(R.id.currentFrequency)
        frequenciesRecyclerView = findViewById(R.id.frequenciesRecyclerView)
        noFrequenciesText = findViewById(R.id.noFrequenciesText)
        refreshFab = findViewById(R.id.refreshFab)
        historyFab = findViewById(R.id.historyFab)
    }
    
    private fun setupRecyclerView() {
        frequencyAdapter = FrequencyAdapter { frequency ->
            onFrequencySelected(frequency)
        }
        frequenciesRecyclerView.layoutManager = LinearLayoutManager(this)
        frequenciesRecyclerView.adapter = frequencyAdapter
    }
    
    private fun setupClickListeners() {
        refreshFab.setOnClickListener {
            loadNetworkInfo()
        }
        
        historyFab.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
    
    private fun checkPermissionsAndLoad() {
        if (hasRequiredPermissions()) {
            loadNetworkInfo()
        } else {
            requestPermissions()
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        val phoneStatePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        val networkStatePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        return phoneStatePermission && networkStatePermission
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.READ_PRECISE_PHONE_STATE)
        }
        
        permissionLauncher.launch(permissions.toTypedArray())
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
    
    private fun loadNetworkInfo() {
        val networkInfo = frequencyManager.getNetworkInfo()
        currentNetworkInfo = networkInfo
        
        operatorNameText.text = networkInfo.operatorName
        networkTypeText.text = networkInfo.networkType
        
        if (networkInfo.currentFrequency != null) {
            currentFrequencyText.text = "${networkInfo.currentFrequency.band} - ${networkInfo.currentFrequency.frequency}"
        } else {
            currentFrequencyText.text = getString(R.string.no_data)
        }
        
        if (networkInfo.availableFrequencies.isNotEmpty()) {
            frequenciesRecyclerView.visibility = View.VISIBLE
            noFrequenciesText.visibility = View.GONE
            frequencyAdapter.updateFrequencies(networkInfo.availableFrequencies)
        } else {
            frequenciesRecyclerView.visibility = View.GONE
            noFrequenciesText.visibility = View.VISIBLE
        }
    }
    
    private fun onFrequencySelected(frequency: FrequencyInfo) {
        val currentInfo = currentNetworkInfo ?: return
        val currentFreq = currentInfo.currentFrequency
        
        AlertDialog.Builder(this)
            .setTitle(R.string.select_band)
            .setMessage("${frequency.band}\n${frequency.frequency}\n\n${getString(R.string.band_info_note)}")
            .setPositiveButton(R.string.apply) { _, _ ->
                attemptFrequencyChange(currentFreq, frequency, currentInfo)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun attemptFrequencyChange(
        oldFrequency: FrequencyInfo?,
        newFrequency: FrequencyInfo,
        networkInfo: NetworkInfo
    ) {
        val status = "Logged"
        
        viewModel.logFrequencyChange(
            oldBand = oldFrequency?.band ?: "Unknown",
            oldFrequency = oldFrequency?.frequency ?: "Unknown",
            newBand = newFrequency.band,
            newFrequency = newFrequency.frequency,
            status = status,
            operatorName = networkInfo.operatorName,
            networkType = networkInfo.networkType
        )
        
        Toast.makeText(
            this,
            "${getString(R.string.frequency_changed)}: ${newFrequency.band}",
            Toast.LENGTH_SHORT
        ).show()
        
        loadNetworkInfo()
    }
}
