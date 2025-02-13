package com.msn.smartswitch.SenderConnectionClasses


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ft.features.local_transfer.smart_switch.connection.ClientClass
import com.ft.features.local_transfer.smart_switch.connection.ServerClass
import com.msn.smartswitch.Models.AppConstant.serverAddress
import com.msn.smartswitch.Models.Utilities
import com.msn.smartswitch.ServerClient.ConnectionCallBack

class P2PConnectionManager(private val context: Context) {

    private var listener: P2PConnectionListener? = null
    private val TAG = javaClass.simpleName
    val wifiP2pManager: WifiP2pManager by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    val wifiP2pChannel: WifiP2pManager.Channel by lazy {
        wifiP2pManager.initialize(context, context.mainLooper, null)
    }
    private var broadcastReceiver: BroadcastReceiver? = null
    private var isClientStarted = false
    private var isServerStarted = false
    fun setListener(listener: P2PConnectionListener) {
        this.listener = listener
    }

    fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        wifiP2pManager.requestPeers(wifiP2pChannel) { peerList ->
                            listener?.onPeersAvailable(peerList.deviceList.toList())
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        Log.d(TAG, "onReceive: WIFI_P2P_CONNECTION_CHANGED_ACTION wifiP2pChannel: $wifiP2pChannel")
                        wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { wifiP2pInfo ->
                            Log.d(TAG, "onReceive Connection changed: ${wifiP2pInfo.groupOwnerAddress}")
                            serverAddress = wifiP2pInfo.groupOwnerAddress
                            Log.d(TAG, "onReceive: serverAddress: $serverAddress")
                            if (wifiP2pInfo.groupOwnerAddress != null) {

                                if (wifiP2pInfo.isGroupOwner) {
                                    Log.d(TAG, "onReceive Device is group owner, starting server")
                                    if (!isServerStarted){
                                        val serverClass = ServerClass(object:ConnectionCallBack{
                                            override fun onSuccess() {
                                                listener?.onGroupOwnerConnected()
                                            }

                                            override fun onFailure(reason: String) {
                                                Log.d(TAG, "onFailure: ")
                                            }

                                        })
                                        serverClass.start()
                                        isServerStarted = true
                                    }

                                    
                                } else {
                                    Log.d(TAG, "onReceive Device is client, starting client class")
                                    if (!isClientStarted){
                                        val clientClass = ClientClass(wifiP2pInfo.groupOwnerAddress,
                                            object:ConnectionCallBack{
                                                override fun onSuccess() {
                                                    listener?.onClientConnected()

                                                }

                                                override fun onFailure(reason: String) {
                                                    Log.d(TAG, "onFailure: ")
                                                }

                                            })
                                        clientClass.start()
                                        isClientStarted = true
                                    }

                                }
                            } else {
                                Log.d(TAG, "onReceive No connection found")
                            }
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun unregisterReceiver() {
        broadcastReceiver?.let {
            context.unregisterReceiver(it)
            broadcastReceiver = null
        }
    }
    fun disconnectWifiDirectIfConnected() {
        wifiP2pManager.requestGroupInfo(
            wifiP2pChannel
        )
        { group ->
            Log.d(TAG, "disconnectWifiDirectIfConnected: ")
            if (group != null && wifiP2pManager != null && wifiP2pChannel != null) {
                wifiP2pManager.removeGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "onSuccess: wifiDisConnected")
                        startDiscovery()
                    }
                    override fun onFailure(reason: Int) {
                    }
                })
            }
        }
    }
    fun startDiscovery() {
        isServerStarted = false
        isClientStarted = false
        wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "startDiscovery onSuccess: ")
                listener?.onDiscoveryStarted()
            }

            override fun onFailure(reason: Int) {
                listener?.onError("Discovery failed: $reason")
            }
        })
    }

    fun requestConnectionInfo() {
        wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { connInfo ->
            if (connInfo.groupOwnerAddress != null) {
                listener?.onManualConnectionSuccess()
            } else {
                listener?.onError("No Connection Found")
            }
        }
    }

    fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        wifiP2pManager.connect(
            wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("ConnectionStatus", "Connection initiated to device: ${device.deviceName}")
                    listener?.onManualConnectionSuccess()
                }

                override fun onFailure(reason: Int) {
                    Log.e("ConnectionStatus", "Connection failed with reason: $reason")
                    listener?.onError("Connection failed: $reason")
                }
            }
        )
    }
}




interface P2PConnectionListener {
    fun onDiscoveryStarted()
    fun onPeersAvailable(peers: List<WifiP2pDevice>)
    fun onGroupOwnerConnected()
    fun onClientConnected()
    fun onManualConnectionSuccess()
    fun onError(message: String)
}
