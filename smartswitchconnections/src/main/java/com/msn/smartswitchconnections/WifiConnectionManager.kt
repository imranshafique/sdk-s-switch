package com.msn.smartswitchconnections


import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager

interface ConnectionListener {
    fun onConnectionEstablished()
    fun onPeersFound(peers: List<WifiP2pDevice>)
}

class WifiConnectionManager(private val context: Context) {

    var connectionListener: ConnectionListener? = null
    private val wifiP2pManager: WifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val wifiP2pChannel: WifiP2pManager.Channel = wifiP2pManager.initialize(context, context.mainLooper, null)

    // Start searching for peers and establish a connection
    fun startConnectionSearch() {
        // Start discovering peers
        wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery started successfully
                println("Discovery started")
            }

            override fun onFailure(reason: Int) {
                // Handle failure
                println("Discovery failed: $reason")
            }
        })

        // Register to listen for connection info
        wifiP2pManager.requestConnectionInfo(wifiP2pChannel, object : WifiP2pManager.ConnectionInfoListener {
            override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
                if (info != null && info.groupFormed) {
                    // Connection is established
                    onConnectionSuccess()
                }
            }
        })

        // Register to listen for peer discovery results
        wifiP2pManager.requestPeers(wifiP2pChannel, object : WifiP2pManager.PeerListListener {
            override fun onPeersAvailable(peers: WifiP2pDeviceList) {
                // Callback to the app with discovered peers
                onPeersDiscovered(peers.deviceList.toList())
            }
        })
    }

    private fun onConnectionSuccess() {
        connectionListener?.onConnectionEstablished()
    }

    private fun onPeersDiscovered(peers: List<WifiP2pDevice>) {
        connectionListener?.onPeersFound(peers)
    }
}



