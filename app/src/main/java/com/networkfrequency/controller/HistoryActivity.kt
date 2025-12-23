package com.networkfrequency.controller

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var viewModel: FrequencyViewModel
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var noHistoryText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.change_history)
        
        viewModel = ViewModelProvider(this)[FrequencyViewModel::class.java]
        
        initializeViews()
        setupRecyclerView()
        observeHistory()
    }
    
    private fun initializeViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        noHistoryText = findViewById(R.id.noHistoryText)
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }
    
    private fun observeHistory() {
        viewModel.allLogs.observe(this) { logs ->
            if (logs.isEmpty()) {
                historyRecyclerView.visibility = View.GONE
                noHistoryText.visibility = View.VISIBLE
            } else {
                historyRecyclerView.visibility = View.VISIBLE
                noHistoryText.visibility = View.GONE
                historyAdapter.updateLogs(logs)
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
