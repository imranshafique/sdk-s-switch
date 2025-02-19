package com.msn.dataselectionviewpager.Activities

import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeScannerBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import org.json.JSONException
import org.json.JSONObject

class QrCodeScannerActivity : AppCompatActivity(), P2PConnectionListener {
    private lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        binding.barcodeView.decodeContinuous { result ->
            result?.let {
                runOnUiThread {

                    Log.d("mavi", "ipAddress , Scanned Text : ${it.text}")

                    Toast.makeText(this, "Scanned: ${it.text}", Toast.LENGTH_LONG).show()
                    binding.barcodeView.pause() // Pause after scan




                    connectToReceiver(it.text) // Process scanned QR
                }
            }
        }
    }
    private fun connectToReceiver(qrData: String) {
        Log.d("mavi", "ipAddress , connectToReceiver called2 ")
        try {
            val jsonObject = JSONObject(qrData)  // Parse JSON from QR
            val ipAddress = jsonObject.getString("ip")
            val port = jsonObject.getInt("port")

            Log.d("mavi", "ipAddress , port: $ipAddress,$port")

            p2pConnectionManager.disconnectWifiDirectIfConnected()
            p2pConnectionManager.connectToPeer(ipAddress)

            // ClientClass(ipAddress, port).start()
        } catch (e: JSONException) {
            Log.e("mavi", "Invalid QR Code format", e)
        }
    }
    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pause()
    }

    override fun onClientConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Client!", Toast.LENGTH_SHORT).show()
            startDataTransfer()
        }
    }

    override fun onManualConnectionSuccess() = Unit

    override fun onError(message: String) = Unit

    override fun onDiscoveryStarted() = Unit

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) = Unit

    override fun onGroupOwnerConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected as Group Owner!", Toast.LENGTH_SHORT).show()
            startDataTransfer()
        }
    }

    private fun startDataTransfer() {
        val intent = Intent(this, DataTransferActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
    }



}