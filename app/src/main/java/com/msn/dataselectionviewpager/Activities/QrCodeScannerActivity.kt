package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.Utils.AppUtils.isLocationEnabled
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeScannerBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager


class QrCodeScannerActivity : AppCompatActivity(), P2PConnectionListener {

    lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    private lateinit var locationManager: LocationManager

    private val qrCodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            val deviceInfo = result.contents.split(":")
            if (deviceInfo[0] == "WIFI_DIRECT") {
                val deviceName = deviceInfo[1]
                val deviceAddress = deviceInfo[2]
                val device = WifiP2pDevice().apply {
                    this.deviceName = deviceName
                    this.deviceAddress = deviceAddress
                }
                connectToDevice(device)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()


        if (AppUtils.isInternetAvailable(this) && isLocationEnabled(this)) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.startDiscovery()
        } else {
            Toast.makeText(this@QrCodeScannerActivity, "Something went wrong. Check internet and GPS.", Toast.LENGTH_SHORT).show()
            finish()
        }
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a QR code")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
        qrCodeLauncher.launch(options)

    }



    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }

    override fun onDiscoveryStarted() {
        Log.d(TAG, "Discovery started")
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        Log.d(TAG, "Available peers: $peers")
    }

    override fun onGroupOwnerConnected() {
        Log.d(TAG, "Connected as Group Owner")
        startActivity(Intent(this, DataReceiveActivity::class.java))
    }

    override fun onClientConnected() {
        Log.d(TAG, "Connected as Client")
        startActivity(Intent(this, DataReceiveActivity::class.java))
    }

    override fun onManualConnectionSuccess() {
        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Log.d(TAG, "Error: $message")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "Connecting to ${device.deviceName}...", Toast.LENGTH_SHORT).show()

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        p2pConnectionManager.wifiP2pManager.connect(
            p2pConnectionManager.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("ConnectionStatus", "Connection successful to device: ${device.deviceName}")
                    Toast.makeText(this@QrCodeScannerActivity, "Connected to ${device.deviceName}!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.e("ConnectionStatus", "Connection failed with reason: $reason")
                    Toast.makeText(this@QrCodeScannerActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )
    }


    }



