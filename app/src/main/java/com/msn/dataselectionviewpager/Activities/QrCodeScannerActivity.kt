package com.msn.dataselectionviewpager.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.Utils.AppUtils.isLocationEnabled
import com.msn.dataselectionviewpager.databinding.ActivityQrCodeScannerBinding
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionListener
import com.msn.smartswitch.SenderConnectionClasses.P2PConnectionManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class QrCodeScannerActivity : AppCompatActivity(), P2PConnectionListener {

    lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var p2pConnectionManager: P2PConnectionManager
    private val TAG = "mavi"
    private lateinit var locationManager: LocationManager
    private var scannedDeviceName: String? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isConnecting = false // Flag to prevent multiple calls
    var isDiscoveryStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        p2pConnectionManager = P2PConnectionManager(this)
        p2pConnectionManager.setListener(this)
        p2pConnectionManager.registerReceiver()

        startCamera() // Start CameraX with ML Kit
        cameraExecutor = Executors.newSingleThreadExecutor()


    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.surfaceProvider = binding.previewView.surfaceProvider }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analyzer ->
                    analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                        processBarcodeImage(imageProxy)

                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processBarcodeImage(imageProxy: ImageProxy) {
        Log.d("scandata", "Image received for processing") // Add this log

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            Log.e("scandata", "No media image available")
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isEmpty()) {
                    Log.d("scandata", "No barcodes detected")
                } else {
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            Log.d("scandata", "Barcode detected: $it")
                            processScannedData(it)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("scandata", "Error scanning QR code: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processScannedData(qrContent: String) {
        Log.d("scandata", "processScannedData")

        val deviceInfo = qrContent.split(":")
        if (deviceInfo[0] == "WIFI_DIRECT") {
            val deviceName = deviceInfo[1]
            scannedDeviceName = deviceName
            val deviceAddress = deviceInfo.subList(2, deviceInfo.size).joinToString(":")

            val device = WifiP2pDevice().apply {
                this.deviceName = deviceName
                this.deviceAddress = deviceAddress // Set the MAC address correctly
            }
            Log.d("mavirock", "deviceName: ${device.deviceName}, deviceAddress: ${device.deviceAddress}")

            if (AppUtils.isInternetAvailable(this) && isLocationEnabled(this)&& (!isDiscoveryStarted)) { // Check if discovery has already started
                isDiscoveryStarted = true
                p2pConnectionManager.disconnectWifiDirectIfConnected()
                p2pConnectionManager.startDiscovery()
            } else {
                Log.d("mavirock", "Something went wrong. Check internet and GPS")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        p2pConnectionManager.unregisterReceiver()
        cameraExecutor.shutdown()
    }

    override fun onDiscoveryStarted() {
        Log.d(TAG, "Discovery started")
    }

    override fun onPeersAvailable(peers: List<WifiP2pDevice>) {
        Log.d("peers", "Available peers: $peers")
        Log.d(TAG, "Available scannedDeviceName: $scannedDeviceName")
        if (isConnecting) {
            Log.d(TAG, "Already attempting to connect. Skipping redundant calls.")
            return // Prevent duplicate connection attempts
        }
        scannedDeviceName = scannedDeviceName ?: return
        val targetDevice = peers.find { it.deviceName == scannedDeviceName }

        if (targetDevice != null) {
            Log.d(
                TAG,
                "Found scanned device: ${targetDevice.deviceName} - ${targetDevice.deviceAddress}"
            )
            isConnecting = true // Set flag before connecting
            p2pConnectionManager.proceedToConnect(targetDevice,
                onSuccess = { Log.d("mavi", "Connection established successfully!") },
                onFailure = { isConnecting = false // Reset flag on failure
                    Log.e("mavi", "Failed to connect. Reason: $it") }
            )
        } else {
            Log.d(TAG, "Scanned device not found in available peers. Retrying discovery...")
            p2pConnectionManager.startDiscovery()
        }
    }

    override fun onGroupOwnerConnected() {
        Log.d(TAG, "Connected as Group Owner")
        startActivity(Intent(this, DataReceiveActivity::class.java))
    }

    override fun onClientConnected() {
        Log.d(TAG, "Connected as Client")
        startActivity(Intent(this, DataReceiveActivity::class.java))
    }

    override fun onManualConnectionSuccess() {
        Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Log.d(TAG, "Error: $message")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}



