package com.networkfrequency.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    
    private var logs = listOf<FrequencyChangeLog>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val changeText: TextView = itemView.findViewById(R.id.changeText)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val log = logs[position]
        holder.timestampText.text = dateFormat.format(Date(log.timestamp))
        holder.statusText.text = log.status
        holder.changeText.text = "${log.oldBand} (${log.oldFrequency}) → ${log.newBand} (${log.newFrequency})"
    }
    
    override fun getItemCount() = logs.size
    
    fun updateLogs(newLogs: List<FrequencyChangeLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}
