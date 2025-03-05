package com.msn.dataselectionviewpager.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeGeneratorBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager


class QrCodeGeneratorActivity : AppCompatActivity(), P2PConnectionListener {

    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityQrCodeGeneratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        if (AppUtils.isInternetAvailable(this)) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.startDiscovery()
        } else {
            Toast.makeText(this@QrCodeGeneratorActivity, "Something went wrong. Check internet and GPS.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Generate QR Code
        generateQRCode()
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

    private fun generateQRCode() {
        val deviceInfo = "WIFI_DIRECT:${p2pConnectionManager.getDeviceName()}:${p2pConnectionManager.getDeviceAddress()}"
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(deviceInfo, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            // Display the QR code in an ImageView
            binding.qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




}



