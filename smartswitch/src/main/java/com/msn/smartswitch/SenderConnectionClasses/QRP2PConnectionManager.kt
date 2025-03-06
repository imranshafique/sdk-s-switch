//package com.msn.smartswitch.SenderConnectionClasses
//
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.graphics.Bitmap
//import android.net.wifi.WifiManager
//import android.net.wifi.WpsInfo
//import android.net.wifi.p2p.WifiP2pConfig
//import android.net.wifi.p2p.WifiP2pDevice
//import android.net.wifi.p2p.WifiP2pManager
//import android.os.Build
//import android.provider.Settings
//import android.util.Log
//import android.widget.Toast
//import com.msn.smartswitch.ServerClient.ClientClass
//import com.msn.smartswitch.ServerClient.ServerClass
//import com.msn.smartswitch.Models.AppConstant.serverAddress
//import com.msn.smartswitch.ServerClient.ConnectionCallBack
//import java.net.NetworkInterface
//
//class QRP2PConnectionManager(private val context: Context) {
//    private var listener: P2PConnectionListener? = null
//    private val TAG = javaClass.simpleName
//
//    val wifiP2pManager: WifiP2pManager by lazy {
//        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//    }
//    val wifiP2pChannel: WifiP2pManager.Channel by lazy {
//        wifiP2pManager.initialize(context, context.mainLooper, null)
//    }
//
//    private var broadcastReceiver: BroadcastReceiver? = null
//
//    fun setListener(listener: P2PConnectionListener) {
//        this.listener = listener
//    }
//
//    fun registerReceiver() {
//        broadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                when (intent.action) {
//                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
//                        wifiP2pManager.requestPeers(wifiP2pChannel) { peerList ->
//                            listener?.onPeersAvailable(peerList.deviceList.toList())
//                        }
//                    }
//
//                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
//                        wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { wifiP2pInfo ->
//                            val goIp = wifiP2pInfo.groupOwnerAddress?.hostAddress
//                            Log.d(TAG, "Group Owner IP: $goIp")
//
//                            if (goIp != null) {
//                                if (wifiP2pInfo.isGroupOwner) {
//                                    Log.d(TAG, "Device is Group Owner. Generating QR Code...")
//                                    generateQRCode(goIp)
//                                } else {
//                                    Log.d(TAG, "Device is a Client. Connecting...")
//                                    connectToGroupOwner(goIp)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        val intentFilter = IntentFilter().apply {
//            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//        }
//        context.registerReceiver(broadcastReceiver, intentFilter)
//    }
//
//    fun unregisterReceiver() {
//        broadcastReceiver?.let {
//            context.unregisterReceiver(it)
//            broadcastReceiver = null
//        }
//    }
//
//    private fun generateQRCode(goIp: String) {
//        val qrData = "WIFI_P2P:$goIp"
//        val multiFormatWriter = MultiFormatWriter()
//        try {
//            val bitMatrix: BitMatrix = multiFormatWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300)
//            val barcodeEncoder = BarcodeEncoder()
//            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
//            listener?.onQRCodeGenerated(bitmap)
//            Log.d(TAG, "QR Code Generated for IP: $goIp")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun connectToGroupOwner(goIp: String) {
//        Log.d(TAG, "Connecting to Group Owner at IP: $goIp")
//
//    }
//
//}
//
//
//
//interface QRP2PConnectionListener {
//    fun onPeersAvailable(peers: List<WifiP2pDevice>)
//    fun onGroupOwnerConnected()
//    fun onClientConnected()
//    fun onQRCodeGenerated(qrBitmap: Bitmap)
//    fun onError(message: String)
//}
