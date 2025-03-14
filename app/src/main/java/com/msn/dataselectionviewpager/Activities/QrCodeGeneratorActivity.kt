package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeGeneratorBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager


class QrCodeGeneratorActivity : AppCompatActivity(), P2PConnectionListener {

    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityQrCodeGeneratorBinding


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        if (AppUtils.isInternetAvailable(this)) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
        } else {
            Toast.makeText(this@QrCodeGeneratorActivity, "Something went wrong. Check internet and GPS.", Toast.LENGTH_SHORT).show()
            finish()
        }
        // Generate QR Code
        generateQRCode(this, p2pConnectionManager.wifiP2pManager, p2pConnectionManager.wifiP2pChannel)

        binding.refresh.setOnClickListener {
            p2pConnectionManager.startDiscovery()
        }

    }

    override fun onClientConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Client!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Client successfully connected")
            startActivity(Intent(this, DataTransferActivity::class.java))
        }
    }

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Group Owner!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Group Owner successfully connected")
            startActivity(Intent(this, DataTransferActivity::class.java))
        }
    }

    override fun onDiscoveryStarted() {
        runOnUiThread {
            Toast.makeText(this, "Discovery Started", Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "Discovery has started")
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
//        Log.d(TAG, "Available Peers: $peers")
    }

    override fun onManualConnectionSuccess() {
        runOnUiThread {
            Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
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


    @SuppressLint("NewApi")
    private fun generateQRCode(context: Context, manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        getWifiDirectDeviceInfo(context, manager, channel) { deviceName, deviceAddress ->
            val deviceInfo = "WIFI_DIRECT:$deviceName:$deviceAddress"
            Log.d("mavirock", "generateQRCode: Device Name: $deviceName, Address: $deviceAddress")
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix: BitMatrix = multiFormatWriter.encode(deviceInfo, BarcodeFormat.QR_CODE, 200, 200)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
                // Display the QR code in an ImageView
                binding.qrImageView.setImageBitmap(bitmap)
                if (AppUtils.isInternetAvailable(this)) {
                    p2pConnectionManager.startDiscovery()
                } else {
                    Toast.makeText(this@QrCodeGeneratorActivity, "Something went wrong. Check internet and GPS.", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    @SuppressLint("NewApi")
    fun getWifiDirectDeviceInfo(
        context: Context,
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel,
        callback: (deviceName: String, deviceAddress: String) -> Unit
    ) {
        try {
            // Check permissions based on Android version
            val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasNearbyWifiDevicesPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not required for Android 12 and below
            }

            if (!hasFineLocationPermission || !hasNearbyWifiDevicesPermission) {
                Log.e("WiFiDirect", "Missing required permissions")
                return
            }
            manager.requestDeviceInfo(channel) { wifiP2pDevice ->
                if (wifiP2pDevice != null) {
                    val deviceName = wifiP2pDevice.deviceName // e.g., "Android_xZnA"
                    val deviceAddress = wifiP2pDevice.deviceAddress // e.g., "02:1A:7D:12:34:56"
                    callback(deviceName, deviceAddress)
                } else {
                    Log.e("WiFiDirect", "Failed to get device info")
                }
            }
        } catch (e: Exception) {
            Log.e("WiFiDirect", "Error getting device info: ${e.message}")
        }
    }







}



