package com.msn.dataselectionviewpager.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.databinding.ActivitySenderSelectionMethodBinding

class SenderConnectionTypeActivity : AppCompatActivity() {
    var binding:ActivitySenderSelectionMethodBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySenderSelectionMethodBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.apply {
            btnWifi.setOnClickListener {
                                        startActivity(Intent(this@SenderConnectionTypeActivity, WifiSenderActivity::class.java))
            }
            btnQrCode.setOnClickListener {

                startActivity(Intent(this@SenderConnectionTypeActivity, QrCodeGeneratorActivity::class.java))

            }

        }

    }
}