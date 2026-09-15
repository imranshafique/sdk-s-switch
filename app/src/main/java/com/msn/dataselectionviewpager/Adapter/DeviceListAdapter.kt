package com.msn.dataselectionviewpager.Adapter


import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R

class DeviceListAdapter(private val onDeviceClick: (WifiP2pDevice) -> Unit) :
    ListAdapter<WifiP2pDevice, DeviceListAdapter.DeviceViewHolder>(DeviceDiffCallback) {

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
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
    }

    fun updateDeviceList(devices: List<WifiP2pDevice>) {
        submitList(devices.toList())
    }

    // Method to get the selected device
    fun getSelectedDevice(): WifiP2pDevice? {
        return selectedDevice
    }

    private companion object DeviceDiffCallback : DiffUtil.ItemCallback<WifiP2pDevice>() {
        override fun areItemsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice) =
            oldItem.deviceAddress == newItem.deviceAddress

        override fun areContentsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice) =
            oldItem == newItem
    }
}
