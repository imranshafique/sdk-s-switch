package com.msn.dataselectionviewpager.Activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.DashboardActivity
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.databinding.ActivitySendMultiFilesBinding
 import com.msn.smartswitch.Models.AppConstant.selectedPath
import com.msn.smartswitch.Models.Utilities.Companion.formatSize
import com.msn.smartswitch.SenderConnectionClasses.FileTransferSDK
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DataTransferActivity : AppCompatActivity() {
    lateinit var binding: ActivitySendMultiFilesBinding
    // SDK instance
    private lateinit var fileTransferSDK: FileTransferSDK
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendMultiFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize the SDK
        fileTransferSDK = FileTransferSDK(
            context = this,
            activity = this
        )
        var totalFilesSizes: Long = 0L // Ensure total size is a Long

        totalFilesSizes = getTotalSizeInBytes(selectedPath)
        binding.tvTotalFilesSizes.text = getString(R.string.totalFilesSize) + " " + formatSize(totalFilesSizes)
        binding.tvTotalFiles.text = "${resources?.getString(R.string.totalFiles)} ${selectedPath.size}"
        fileTransferSDK.sendFiles()
        binding.btnDisconnect.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
         }
        // Set up the listener for file transfer progress
        fileTransferSDK.setListener(object : FileTransferSDK.FileTransferListener {
            override fun onFileSendSuccess(progress: Int, totalBytesSent: Long) {
                runOnUiThread {
//                    Log.d(TAG, "onFileSendSuccess: ")
                    binding.progressBar.progress = progress
                    binding.tvPercentage.text = progress.toString() +"%"
                    binding.tvBytesSend.text = formatSize(totalBytesSent)
                }

            }

            override fun onAllFilesSentSuccessfully() {
                runOnUiThread {
                    Log.d(TAG, "onAllFilesSentSuccessfully: ")
                    Toast.makeText(
                        this@DataTransferActivity,
                        "all Files sent successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnDisconnect.visibility = View.VISIBLE
                }
            }

            override fun onFileSendFailure(errorMessage: String) {
                runOnUiThread {
                    Log.d(TAG, "onFileSendFailure: ")
                    Toast.makeText(
                        this@DataTransferActivity,
                        "Error: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onConnectionError() {
                runOnUiThread {
                    Log.d(TAG, "onConnectionError: ")
                    Toast.makeText(
                        this@DataTransferActivity,
                        "Connection error. Server address is null.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onConnectionClosed() {
                Log.d(TAG, "onConnectionClosed: ")
            }

            override fun onFileSendingProgress(progress: Float) {
                runOnUiThread {
                    Log.d(TAG, "onFileSendingProgress: ")
                }
                // Update the progress bar as files are being sent
            }
        })
    }

    private fun getTotalSizeInBytes(filePaths: ArrayList<String>): Long {
        var totalSize = 0L
        for (path in filePaths) {
            val file = File(path)
            if (file.exists()) {
                totalSize += file.length()
            }
        }
        return totalSize
    }
}


