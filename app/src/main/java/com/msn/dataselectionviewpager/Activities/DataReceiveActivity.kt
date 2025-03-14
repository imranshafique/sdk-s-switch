package com.msn.dataselectionviewpager.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.DashboardActivity
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.databinding.ActivityReceiverBinding
import com.msn.smartswitch.Models.Utilities
import com.msn.smartswitch.Models.Utilities.Companion.formatSize
import com.msn.smartswitch.Transfer.DataReceiverManger

class DataReceiveActivity : AppCompatActivity(), DataReceiverManger.ReceiverListener {
    private lateinit var dataReceiverManager: DataReceiverManger
    private lateinit var binding: ActivityReceiverBinding
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataReceiverManager = DataReceiverManger()
        dataReceiverManager.setListener(this)
        dataReceiverManager.startReceive()
        binding.btnDisconnect.setOnClickListener {
            finish()
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
       onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    startActivity(Intent(this@DataReceiveActivity, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
        )

    }

    override fun onFileReceiveSuccess() {
        runOnUiThread {  }
        Log.d(TAG, "onFileReceiveSuccess: ")
    }

    override fun onAllFileReceiveSuccess() {
        runOnUiThread {
            Log.d(TAG, "onAllFileReceiveSuccess: ")
            binding.btnDisconnect.visibility = View.VISIBLE
            
        }

    }

    override fun onFileReceiveFailure(errorMessage: String) {
        runOnUiThread {
            Log.d(TAG, "onFileReceiveFailure:  $errorMessage ")
            
        }
    }

    override fun onConnectionError() {
        runOnUiThread {
            Log.d(TAG, "onConnectionError: ")
        }
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "onConnectionClosed: ")
    }

    override fun onFileReceiveProgress(progress: Int, totalBytesReceived: Long) {
        runOnUiThread {
            Log.d(TAG, "onFileReceiveProgress: progress $progress")
            binding.progressBar.progress = progress
            binding.tvPercentage.text = progress.toString() +"%"
            binding.tvReceivedBytes.text = formatSize(totalBytesReceived)
        }
       
    }

    override fun onTotalCountReceived(count: Int, size: Long) {
        runOnUiThread {
            binding.tvTotalFilesSizes.text = getString(R.string.totalFilesSize) + " " + Utilities.formatSize(size)
            binding.tvTotalFiles.text = "${resources?.getString(R.string.totalFiles)} ${count}"
        }

    }

}