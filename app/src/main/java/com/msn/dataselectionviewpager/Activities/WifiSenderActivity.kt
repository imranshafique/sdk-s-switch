package com.msn.dataselectionviewpager.Activities


import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager


class WifiSenderActivity : AppCompatActivity(), P2PConnectionListener {

    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_sender_screen)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()
        if (AppUtils.isInternetAvailable(this)) {
            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.startDiscovery()
        }else{
            Toast.makeText(this@WifiSenderActivity,"SomeThing went Wrong check internet and gps",Toast.LENGTH_SHORT).show()
            finish()
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
}
