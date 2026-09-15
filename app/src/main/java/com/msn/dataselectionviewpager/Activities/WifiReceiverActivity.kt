package com.msn.dataselectionviewpager.Activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.Adapter.DeviceListAdapter
import com.msn.dataselectionviewpager.databinding.ActivityNewDeviceListBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager

class WifiReceiverActivity : AppCompatActivity(), P2PConnectionListener {

    lateinit var binding: ActivityNewDeviceListBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName


    private val deviceListAdapter = DeviceListAdapter { device ->
        // On device click, initiate connection
        connectToDevice(device)
    }
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
        // Initialize P2PConnectionManager
        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        // Set up RecyclerView
        binding.deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceListRecyclerView.adapter = deviceListAdapter

        if (AppUtils.isInternetAvailable(this)&& isLocationEnabled()) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.startDiscovery()
        }else{
            Toast.makeText(this@WifiReceiverActivity,"SomeThing went Wrong check internet and gps",Toast.LENGTH_SHORT).show()
            finish()
        }
        // Start discovery

        binding.refresh.setOnClickListener {
            if (AppUtils.isInternetAvailable(this) && isLocationEnabled()) {
                binding.deviceListRecyclerView.visibility = View.GONE
                p2pConnectionManager.disconnectWifiDirectIfConnected()
                p2pConnectionManager.startDiscovery()
            }else{
                Toast.makeText(this@WifiReceiverActivity,"SomeThing went Wrong check internet and gps ",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    override fun onDiscoveryStarted() {
        runOnUiThread {
            Log.d(TAG, "onDiscoveryStarted: ")
        }
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        runOnUiThread {
            binding.deviceListRecyclerView.visibility = View.VISIBLE

            // Update RecyclerView with available devices
            deviceListAdapter.updateDeviceList(peers)
            Log.d(TAG, "onPeersAvailable: peers: $peers")
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Log.d(TAG, "onGroupOwnerConnected: ")
            startActivity(Intent(this@WifiReceiverActivity, DataReceiveActivity::class.java))
        }
    }

    override fun onClientConnected() {
        runOnUiThread {
            Log.d(TAG, "onClientConnected: ")
            startActivity(Intent(this@WifiReceiverActivity, DataReceiveActivity::class.java))
        }
    }

    override fun onManualConnectionSuccess() {
        runOnUiThread {
            Log.d(TAG, "onManualConnectionSuccess: ")
            Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Log.d(TAG, "onError: $message")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToDevice(device: WifiP2pDevice) {
        if (!hasWifiDirectPermissions()) {
            Toast.makeText(this, R.string.permission_not_granted_go_to_settings, Toast.LENGTH_SHORT).show()
            return
        }

         Log.d("ConnectionAttempt", "Attempting connection to device: ${device.deviceName} - ${device.deviceAddress}")
        Toast.makeText(this, "Connecting to ${device.deviceName}...", Toast.LENGTH_SHORT).show()

         val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        p2pConnectionManager?.wifiP2pManager?.connect(
            p2pConnectionManager?.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("ConnectionStatus", "Connection successful to device: ${device.deviceName}")
                    Toast.makeText(this@WifiReceiverActivity, "Connected to ${device.deviceName}!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.e("ConnectionStatus", "Connection failed with reason: $reason")
                    Toast.makeText(this@WifiReceiverActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )
    }

    private fun hasWifiDirectPermissions(): Boolean {
        val hasLocation = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasNearbyWifi = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED

        return hasLocation && hasNearbyWifi
    }


    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }
}







/*
package com.msn.dataselectionviewpager.cheking

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.msn.dataselectionviewpager.R

// DeviceListActivity.kt

 import android.widget.Toast
 import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import android.net.wifi.p2p.WifiP2pDevice

class DeviceListActivity : AppCompatActivity(), P2PConnectionListener {

    private lateinit var p2pConnectionManager: P2PConnectionManager
    private lateinit var deviceListRecyclerView: RecyclerView
    private val deviceListAdapter = DeviceListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        // Initialize P2PConnectionManager
        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        // Set up RecyclerView
        deviceListRecyclerView = findViewById(R.id.deviceListRecyclerView)
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceListRecyclerView.adapter = deviceListAdapter

        // Start discovery
        p2pConnectionManager.startDiscovery()
    }

    override fun onDiscoveryStarted() {
        runOnUiThread {
            Toast.makeText(this, "Wi-Fi P2P Discovery Started", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        runOnUiThread {
            // Update RecyclerView with available devices
            deviceListAdapter.updateDeviceList(peers)
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Group Owner!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClientConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Client!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onManualConnectionSuccess() {
        runOnUiThread {
            Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }
}
*/
