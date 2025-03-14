package com.msn.smartswitch.SenderConnectionClasses


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.msn.smartswitch.Models.AppConstant.serverAddress
import com.msn.smartswitch.ServerClient.ClientClass
import com.msn.smartswitch.ServerClient.ConnectionCallBack
import com.msn.smartswitch.ServerClient.ServerClass

class P2PConnectionManager(private val context: Context) {
    private val QR_CODE_SIZE = 200
    private var listener: P2PConnectionListener? = null
    private val TAG = "mavi"
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

                            Log.d(TAG, "peerList ${peerList.deviceList.toString()}")

                            listener?.onPeersAvailable(peerList.deviceList.toList())
                        }
                    }

                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        Log.d(
                            TAG,
                            "onReceive: WIFI_P2P_CONNECTION_CHANGED_ACTION wifiP2pChannel: $wifiP2pChannel"
                        )
                        wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { wifiP2pInfo ->
                            Log.d(
                                TAG,
                                "onReceive Connection changed: ${wifiP2pInfo.groupOwnerAddress}"
                            )
                            serverAddress = wifiP2pInfo.groupOwnerAddress
                            Log.d(TAG, "onReceive: serverAddress: $serverAddress")
                            if (wifiP2pInfo.groupOwnerAddress != null) {
                                if (wifiP2pInfo.isGroupOwner) {
                                    Log.d(TAG, "onReceive Device is group owner, starting server")
                                    if (!isServerStarted) {
                                        val serverClass = ServerClass(object : ConnectionCallBack {
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
                                    if (!isClientStarted) {
                                        val clientClass = ClientClass(wifiP2pInfo.groupOwnerAddress,
                                            object : ConnectionCallBack {
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
                Log.d(TAG, "startDiscovery onFailure ")

                listener?.onError("Discovery failed: $reason")
            }
        })
    }

    // Generating QR Code with Device Info

    fun generateQRCode(
        callback: (Bitmap?) -> Unit
    ) {
        getWifiDirectDeviceInfo { deviceName, deviceAddress ->
            val deviceInfo = "WIFI_DIRECT:$deviceName:$deviceAddress"
            Log.d(TAG, "Device Name: $deviceName, Address: $deviceAddress")

            try {
                val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                    deviceInfo,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE
                )
                callback(BarcodeEncoder().createBitmap(bitMatrix))
            } catch (e: Exception) {
                Log.e(TAG, "Error generating QR code: ${e.message}", e)
                callback(null)
            }
        }
    }

    // Get Device info using wifiP2pManager that will used to connect peer

    fun getWifiDirectDeviceInfo(
        callback: (String, String) -> Unit
    ) {
        if (!hasRequiredPermissions(context)) {
            Log.e(TAG, "Missing required permissions")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For API 29 and above, use requestDeviceInfo()
            wifiP2pManager.requestDeviceInfo(wifiP2pChannel) { wifiP2pDevice ->
                if (wifiP2pDevice != null) {
                    callback(wifiP2pDevice.deviceName, wifiP2pDevice.deviceAddress)
                } else {
                    Log.e(TAG, "Failed to get device info using requestDeviceInfo()")
                }
            }
        } else {
            // For API 28,  // Get Device info using wifiP2pManager that will used to connect peer
            wifiP2pManager.requestGroupInfo(wifiP2pChannel) { group ->
                if (group != null && group.isGroupOwner) {
                    val localDevice = group.owner
                    callback(localDevice.deviceName, localDevice.deviceAddress)
                } else {
                    Log.e(TAG, "Failed to get local device info using requestGroupInfo()")
                }
            }
        }

    }

    // Check if the required permissions are granted
    private fun hasRequiredPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                        ) == PackageManager.PERMISSION_GRANTED)
    }

    // Connect to Specific Device after scan with QR Code
    fun proceedToConnect(
        device: WifiP2pDevice,
        onSuccess: () -> Unit,
        onFailure: (reason: Int) -> Unit
    ) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mavi", "NEARBY_WIFI_DEVICES permission not granted")
                return
            }
        } else { // Android 12 and below
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mavi", "ACCESS_FINE_LOCATION permission not granted")
                return
            }
        }

        Log.d("mavi", "Try to connect")

        wifiP2pManager.connect(
       wifiP2pChannel,
            config,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("mavi", "Connection successful to device: ${device.deviceName}")
                    Toast.makeText(context, "Connected to ${device.deviceName}!", Toast.LENGTH_SHORT).show()
                    onSuccess.invoke()
                }

                override fun onFailure(reason: Int) {
                    Log.e("mavi", "Connection failed with reason: $reason")
                    Toast.makeText(context, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
                    onFailure.invoke(reason)
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
