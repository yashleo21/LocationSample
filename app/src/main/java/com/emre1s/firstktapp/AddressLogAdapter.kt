package com.emre1s.firstktapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.location_log_item.view.*

class AddressLogAdapter: RecyclerView.Adapter<AddressLogAdapter.ViewHolder>() {

    val locationLogList: MutableList<LocationLog> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_log_item, parent, false))
    }

    override fun getItemCount(): Int {
        return locationLogList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.latitude.text = "Latitude: ${locationLogList.get(position).latitude} Longitude: ${locationLogList.get(position).longitude}"
        holder.address.text = locationLogList.get(position).address
    }


    class ViewHolder(inflate: View) : RecyclerView.ViewHolder(inflate) {
        val latitude = inflate.rv_latitude
        val address = inflate.rv_address
    }

}