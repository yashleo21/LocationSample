package com.emre1s.firstktapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.emre1s.firstktapp.databinding.LocationLogItemBinding
import com.emre1s.firstktapp.room.LocationLog
import kotlinx.android.synthetic.main.location_log_item.view.*

class AddressLogAdapter: RecyclerView.Adapter<AddressLogAdapter.ViewHolder>() {

    val locationLogList: MutableList<LocationLog> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val locationLogBinding: LocationLogItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
            R.layout.location_log_item, parent, false)
        return ViewHolder(locationLogBinding)
    }

    override fun getItemCount(): Int {
        return locationLogList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(locationLogList[position])
    }


    class ViewHolder(private val locationLogItemBinding: LocationLogItemBinding):
        RecyclerView.ViewHolder(locationLogItemBinding.root) {
        fun bind(data: Any) {
            locationLogItemBinding.setVariable(BR.locationLog, data)
            locationLogItemBinding.executePendingBindings()
        }
    }

}