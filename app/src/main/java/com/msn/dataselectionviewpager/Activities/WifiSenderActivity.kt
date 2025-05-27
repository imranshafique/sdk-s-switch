package com.msn.dataselectionviewpager.Activities


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.msn.dataselectionviewpager.Adapter.DeviceListAdapter
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.databinding.ActivityWaitingSenderScreenBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager


class WifiSenderActivity : AppCompatActivity(), P2PConnectionListener {
    lateinit var binding:ActivityWaitingSenderScreenBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    private val deviceListAdapter = DeviceListAdapter { device ->
        // On device click, initiate connection
        connectToDevice(device)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityWaitingSenderScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.deviceListRecyclerView.adapter = deviceListAdapter
        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()
        if (AppUtils.isInternetAvailable(this)) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.startDiscovery()
        }else{
            Toast.makeText(this@WifiSenderActivity,resources?.getString(R.string.something_went_wrong_check_internet_and_gps),Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.refresh.setOnClickListener {
            if (AppUtils.isInternetAvailable(this) ) {
                binding.deviceListRecyclerView.visibility = View.GONE
                p2pConnectionManager.disconnectWifiDirectIfConnected()
                p2pConnectionManager.startDiscovery()
            }else{
                Toast.makeText(this@WifiSenderActivity, resources?.getString(R.string.something_went_wrong_check_internet_and_gps),Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onClientConnected() {
        runOnUiThread {
            Toast.makeText(this, resources?.getString(R.string.connected_as_client), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Client successfully connected")
            startActivity(Intent(this, DataTransferActivity::class.java))
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Toast.makeText(this, resources?.getString(R.string.connected_as_group_owner), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Group Owner successfully connected")
            startActivity(Intent(this, DataTransferActivity::class.java))
        }
    }


    override fun onDiscoveryStarted() {
        runOnUiThread {
            Toast.makeText(this, resources?.getString(R.string.discovery_started), Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "Discovery has started")
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        Log.d(TAG, "Available Peers: $peers")
    }



    override fun onManualConnectionSuccess() {
        runOnUiThread {
            Toast.makeText(this, resources?.getString(R.string.connected_successfully), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }
    private fun connectToDevice(device: WifiP2pDevice) {
        val locationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val nearbyWifiPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) == PackageManager.PERMISSION_GRANTED


        Log.d("ConnectionAttempt", "Attempting connection to device: ${device.deviceName} - ${device.deviceAddress}")
        Toast.makeText(this,
            getString(R.string.connecting_to, device.deviceName), Toast.LENGTH_SHORT).show()

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        p2pConnectionManager.wifiP2pManager?.connect(
            p2pConnectionManager.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("ConnectionStatus",
                        getString(R.string.connection_successful_to_device, device.deviceName))
                    Toast.makeText(this@WifiSenderActivity, getString(R.string.connecting_to, device.deviceName), Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.e("ConnectionStatus", "Connection failed with reason: $reason")
                    Toast.makeText(this@WifiSenderActivity, getString(R.string.connecting_to, device.deviceName), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )
    }

}