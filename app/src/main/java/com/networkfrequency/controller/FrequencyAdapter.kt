package com.networkfrequency.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FrequencyAdapter(
    private val onFrequencyClick: (FrequencyInfo) -> Unit
) : RecyclerView.Adapter<FrequencyAdapter.FrequencyViewHolder>() {
    
    private var frequencies = listOf<FrequencyInfo>()
    
    class FrequencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bandText: TextView = itemView.findViewById(R.id.bandText)
        val frequencyText: TextView = itemView.findViewById(R.id.frequencyText)
        val signalText: TextView = itemView.findViewById(R.id.signalText)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrequencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_frequency, parent, false)
        return FrequencyViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FrequencyViewHolder, position: Int) {
        val frequency = frequencies[position]
        holder.bandText.text = frequency.band
        holder.frequencyText.text = frequency.frequency
        holder.signalText.text = "Signal: ${frequency.signalStrength}"
        
        holder.itemView.setOnClickListener {
            onFrequencyClick(frequency)
        }
    }
    
    override fun getItemCount() = frequencies.size
    
    fun updateFrequencies(newFrequencies: List<FrequencyInfo>) {
        frequencies = newFrequencies
        notifyDataSetChanged()
    }
}
