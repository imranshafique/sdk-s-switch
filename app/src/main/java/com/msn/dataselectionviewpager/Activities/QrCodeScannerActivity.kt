package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeScannerBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import org.json.JSONException
import org.json.JSONObject

class QrCodeScannerActivity : AppCompatActivity(), P2PConnectionListener {
    private lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    private var deviceIpAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        binding.barcodeView.decodeContinuous { result ->
            result?.let {
                runOnUiThread {
                    Log.d(TAG, "Scanned Text: ${it.text}")

                    Toast.makeText(this, "Scanned: ${it.text}", Toast.LENGTH_LONG).show()
                    binding.barcodeView.pause() // Pause scanner after scan

                    processScannedQR(it.text) // Extract IP and start connection
                }
            }
        }
    }

    /**
     * Extracts IP address and starts peer discovery
     */
    private fun processScannedQR(qrData: String) {
        try {
            val jsonObject = JSONObject(qrData)
            deviceIpAddress = jsonObject.getString("ip")
            Log.d(TAG, "Extracted IP: $deviceIpAddress")

            // Start Wi-Fi Direct discovery
            p2pConnectionManager.startDiscovery()
        } catch (e: JSONException) {
            Log.e(TAG, "Invalid QR Code format", e)
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when peer discovery finds available devices
     */
    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        if (peers.isEmpty()) {
            Log.e(TAG, "No devices found.")
            Toast.makeText(this, "No devices found. Try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val device = peers.firstOrNull() // Pick the first available device
        device?.let {
            Log.d(TAG, "Attempting to connect to: ${it.deviceName} (${it.deviceAddress})")
            connectToPeer(it)
        } ?: Log.e(TAG, "No valid device found.")
    }

    /**
     * Establishes a Wi-Fi Direct connection
     */
    private fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        p2pConnectionManager.wifiP2pManager.connect(
            p2pConnectionManager.wifiP2pChannel, config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Successfully connected to device: ${device.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connection failed with reason: $reason")
                    val errorMessage = when (reason) {
                        WifiP2pManager.BUSY -> "Wi-Fi Direct is busy, try again later."
                        WifiP2pManager.ERROR -> "An internal error occurred."
                        WifiP2pManager.P2P_UNSUPPORTED -> "Wi-Fi Direct is not supported on this device."
                        WifiP2pManager.NO_SERVICE_REQUESTS -> "No service requests found."
                        else -> "Unknown error code: $reason"
                    }
                    Toast.makeText(this@QrCodeScannerActivity, "Connection failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * Once connected, start data transfer using the extracted IP address
     */
    override fun onClientConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Client!", Toast.LENGTH_SHORT).show()
            deviceIpAddress?.let { startDataTransfer(it) }
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Group Owner!", Toast.LENGTH_SHORT).show()
            deviceIpAddress?.let { startDataTransfer(it) }
        }
    }

    /**
     * Open DataTransferActivity with the IP address
     */
    private fun startDataTransfer(ip: String) {
        val intent = Intent(this, DataTransferActivity::class.java).apply {
            putExtra("DEVICE_IP", ip)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }

    override fun onManualConnectionSuccess() = Unit
    override fun onError(message: String) = Unit
    override fun onDiscoveryStarted() = Unit
}

