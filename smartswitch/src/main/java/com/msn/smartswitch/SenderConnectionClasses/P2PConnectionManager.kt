package com.msn.smartswitch.SenderConnectionClasses


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.msn.smartswitch.ServerClient.ClientClass
import com.msn.smartswitch.ServerClient.ServerClass
import com.msn.smartswitch.Models.AppConstant.serverAddress
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
                    override fun onFailure(reason: Int) = Unit
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

    fun connectToPeer(deviceAddress: String) {
        Log.d("mavi", "connectToPeer called" )

        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
            wps.setup = WpsInfo.PBC
        }

        wifiP2pManager.connect(
            wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Connection successful to device: ${deviceAddress}")
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
                    Log.e("mavi", "Connection failed: $errorMessage")
                    listener?.onError(errorMessage)
                }
            }
        )

    }
    fun getDeviceName(): String {
        return Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            ?: Build.MODEL // Fallback to device model if name is not set
    }

    fun getDeviceAddress(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo.macAddress
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
