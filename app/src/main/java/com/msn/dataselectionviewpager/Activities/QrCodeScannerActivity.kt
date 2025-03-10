package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    private val TAG = "mavi"
    private lateinit var locationManager: LocationManager
    private var scannedDeviceName: String? = null

    private var isConnecting = false // Flag to prevent multiple calls

    private val qrCodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents != null) {
                val deviceInfo = result.contents.split(":")
                if (deviceInfo[0] == "WIFI_DIRECT") {
                    val deviceName = deviceInfo[1]
                    scannedDeviceName = deviceName
                    val deviceAddress = deviceInfo.subList(2, deviceInfo.size)
                        .joinToString(":") // Reconstruct MAC address
                    val device = WifiP2pDevice().apply {
                        this.deviceName = deviceName
                        this.deviceAddress = deviceAddress // Set the MAC address correctly
                    }
                    Log.d(
                        "mavirock",
                        "deviceName: ${device.deviceName}, deviceAddress: ${device.deviceAddress}"
                    )
                    p2pConnectionManager.startDiscovery()
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
            Toast.makeText(this@QrCodeScannerActivity, "Something went wrong. Check internet and GPS", Toast.LENGTH_SHORT).show()
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
        Log.d("peers", "Available peers: $peers")
        Log.d(TAG, "Available scannedDeviceName: $scannedDeviceName")
        if (isConnecting) {
            Log.d(TAG, "Already attempting to connect. Skipping redundant calls.")
            return // Prevent duplicate connection attempts
        }
        scannedDeviceName = scannedDeviceName ?: return
        val targetDevice = peers.find { it.deviceName == scannedDeviceName }

        if (targetDevice != null) {
            Log.d(TAG, "Found scanned device: ${targetDevice.deviceName} - ${targetDevice.deviceAddress}")
            isConnecting = true // Set flag before connecting
            proceedToConnect(targetDevice) // No existing group, proceed directly
        } else {
            Log.d(TAG, "Scanned device not found in available peers. Retrying discovery...")
            p2pConnectionManager.startDiscovery()
        }
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

    private fun proceedToConnect(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress

        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mavi", "NEARBY_WIFI_DEVICES permission not granted")
                return
            }
        } else { // Android 12 and below
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mavi", "ACCESS_FINE_LOCATION permission not granted")
                return
            }
        }

        Log.d("mavi", "Try to connect")

        p2pConnectionManager.wifiP2pManager.connect(
            p2pConnectionManager.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("mavi", "Connection successful to device: ${device.deviceName}")
                    Toast.makeText(this@QrCodeScannerActivity, "Connected to ${device.deviceName}!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.e("mavi", "Connection failed with reason: $reason")
                    Toast.makeText(this@QrCodeScannerActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                    isConnecting = false // Reset flag on failure
                }
            }
        )
    }



}



