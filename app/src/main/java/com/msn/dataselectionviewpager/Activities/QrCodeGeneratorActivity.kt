package com.msn.dataselectionviewpager.Activities

import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        p2pConnectionManager.generateQRCode {
            binding.qrImageView.setImageBitmap(it)
            p2pConnectionManager.startDiscovery()

        }

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
        Log.d(TAG, "Available Peers: $peers")
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
    fun getDeviceName(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, "device_name") ?: "Unknown Device"
    }
}



