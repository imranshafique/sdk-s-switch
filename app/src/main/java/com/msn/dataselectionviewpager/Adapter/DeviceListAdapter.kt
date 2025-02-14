package com.msn.dataselectionviewpager.Adapter


import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R

class DeviceListAdapter(private val onDeviceClick: (WifiP2pDevice) -> Unit) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private var deviceList: List<WifiP2pDevice> = emptyList()
    private var selectedDevice: WifiP2pDevice? = null

    // ViewHolder class
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceName)  // Adjust the ID accordingly

        fun bind(device: WifiP2pDevice) {
            deviceNameTextView.text = device.deviceName

            // Set a click listener to select the device
            itemView.setOnClickListener {
                selectedDevice = device
                onDeviceClick(device) // Trigger the onDeviceClick callback
                notifyDataSetChanged() // Notify that a selection has been made
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device)
    }

    override fun getItemCount(): Int = deviceList.size

    // Method to update the device list
    fun updateDeviceList(devices: List<WifiP2pDevice>) {
        deviceList = devices
        notifyDataSetChanged()
    }

    // Method to get the selected device
    fun getSelectedDevice(): WifiP2pDevice? {
        return selectedDevice
    }
}