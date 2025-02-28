package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeGeneratorBinding
import com.msn.smartswitch.Models.Utilities.Companion.PORT
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

class QrCodeGeneratorActivity : AppCompatActivity(), P2PConnectionListener {
    private lateinit var binding: ActivityQrCodeGeneratorBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        val localIpAddress = getLocalIpAddress()
        Log.d(TAG, "Device Local IP Address: $localIpAddress")

        localIpAddress?.let { onServerStarted(it) }
    }

    /**
     * Generate Wi-Fi Direct QR Code using IP Address
     */
    fun generateWiFiDirectQRCode(groupOwnerAddress: String, deviceAddress: String): Bitmap {
        val qrData = "$groupOwnerAddress;$deviceAddress"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Called when the server starts
     */
    private fun onServerStarted(ipAddress: String) {
        val qrBitmap = generateWiFiDirectQRCode(ipAddress, PORT.toString())
        Log.d(TAG, "Generated QR Code for IP: $ipAddress")
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    /**
     * Get Local IP Address
     */
    private fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
        } catch (ex: Exception) {
            Log.e(TAG, "Error getting local IP address", ex)
            null
        }
    }

    /**
     * Connect to a device using IP Address (retrieved from QR Code)
     */
    private fun connectToDevice(ip: String) {
        val locationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val nearbyWifiPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) == PackageManager.PERMISSION_GRANTED

        val config = WifiP2pConfig().apply {
            deviceAddress = ip // Use IP Address instead of MAC
        }

        p2pConnectionManager.wifiP2pManager.connect(
            p2pConnectionManager.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Connection successful to device: $ip")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connection failed with reason: $reason")
                    Toast.makeText(this@QrCodeGeneratorActivity, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                    val errorMessage = when (reason) {
                        WifiP2pManager.BUSY -> "Wi-Fi Direct is busy, try again later."
                        WifiP2pManager.ERROR -> "An internal error occurred."
                        WifiP2pManager.P2P_UNSUPPORTED -> "Wi-Fi Direct is not supported on this device."
                        WifiP2pManager.NO_SERVICE_REQUESTS -> "No service requests found."
                        else -> "Unknown error code: $reason"
                    }
                    Log.e(TAG, "Connection failed: $errorMessage")
                }
            }
        )
    }

    override fun onDiscoveryStarted() {
        runOnUiThread {
            Log.d(TAG, "onDiscoveryStarted")
        }
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        runOnUiThread {
            if (peers.isNotEmpty()) {
                val firstDevice = peers.first()
                Log.d(TAG, "Discovered Peer: ${firstDevice.deviceAddress}")
            } else {
                Log.d(TAG, "No peers found.")
            }
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Log.d(TAG, "onGroupOwnerConnected")
            startActivity(Intent(this, DataReceiveActivity::class.java))
        }
    }

    override fun onClientConnected() {
        runOnUiThread {
            Log.d(TAG, "onClientConnected")
            startActivity(Intent(this, DataReceiveActivity::class.java))
        }
    }

    override fun onManualConnectionSuccess() {
        runOnUiThread {
            Log.d(TAG, "onManualConnectionSuccess")
            Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Log.d(TAG, "onError: $message")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

