package com.msn.dataselectionviewpager.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.databinding.ActivityReceiverSelectionMethodBinding

class ReceiverConnectioTypeActivity : AppCompatActivity() {
    var binding:ActivityReceiverSelectionMethodBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityReceiverSelectionMethodBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.apply {
            btnWifi.setOnClickListener {
                startActivity(Intent(this@ReceiverConnectioTypeActivity, WifiReceiverActivity::class.java))
            }
            btnQrCode.setOnClickListener {
                startActivity(Intent(this@ReceiverConnectioTypeActivity, QrCodeScannerActivity::class.java))
            }
        }

    }
}