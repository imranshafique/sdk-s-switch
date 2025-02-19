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
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeGeneratorBinding
import com.msn.smartswitch.Models.Utilities.Companion.PORT
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface

class QrCodeGeneratorActivity : AppCompatActivity() , P2PConnectionListener {
    private lateinit var binding: ActivityQrCodeGeneratorBinding
    val TAG=javaClass.simpleName
    private lateinit var p2pConnectionManager: P2PConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        val ip = getLocalIpAddress() // Get the device's IP
        Log.d("mavi", "Device Ip Address on Receiver Side  : $ip ")
        onServerStarted(ip?:return)


    }

    private fun generateQRCode(ip: String, port: Int): Bitmap {
        val qrContent = JSONObject().apply {
            put("ip", ip)
            put("port", port)
        }.toString()

        val bitMatrix = MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 300, 300)
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)

        for (x in 0 until 300) {
            for (y in 0 until 300) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    // Call this after Server starts
    private fun onServerStarted(ip: String) {
        val qrBitmap = generateQRCode(ip, PORT)
        connectToDevice(ip)


        Log.d("mavi", "qrBitmap  Image Data  : $qrBitmap ")

        // Show this bitmap in an ImageView
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("P2PConnectionManager", "Error getting local IP address", ex)
        }
        return null
    }

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
            deviceAddress = ip
        }

        p2pConnectionManager.wifiP2pManager.connect(
            p2pConnectionManager.wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Connection successful to device: ${ip}")
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
                    Log.e("mavi", "Connection failed: $errorMessage")
                }
            }
        )
    }

    override fun onDiscoveryStarted() {
        runOnUiThread {
            Log.d(TAG, "onDiscoveryStarted: ")
        }
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        runOnUiThread {

        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Log.d(TAG, "onGroupOwnerConnected: ")
            startActivity(Intent(this@QrCodeGeneratorActivity, DataReceiveActivity::class.java))
        }
    }

    override fun onClientConnected() {
        runOnUiThread {
            Log.d(TAG, "onClientConnected: ")
            startActivity(Intent(this@QrCodeGeneratorActivity, DataReceiveActivity::class.java))
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


}