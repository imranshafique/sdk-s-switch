package com.msn.dataselectionviewpager.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.msn.dataselectionviewpager.MainActivity
import com.msn.dataselectionviewpager.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.receiveData.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, WifiReceiverActivity::class.java))

        }
        binding.sendData.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, MainActivity::class.java))

        }

    }
}