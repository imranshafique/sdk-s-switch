package com.msn.dataselectionviewpager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.msn.dataselectionviewpager.Activities.QrCodeGeneratorActivity
import com.msn.dataselectionviewpager.Activities.QrCodeScannerActivity
import com.msn.dataselectionviewpager.Activities.ReceiverConnectioTypeActivity
import com.msn.dataselectionviewpager.Activities.SenderConnectionTypeActivity
import com.msn.dataselectionviewpager.Utils.AppUtils
import com.msn.dataselectionviewpager.Activities.WifiReceiverActivity
import com.msn.dataselectionviewpager.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val sender_permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION

        )
    }
    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
        binding.run {
            sendData.setOnClickListener {
                if (AppUtils.hasPermission(this@DashboardActivity)){
                    startActivity(Intent(this@DashboardActivity, MainActivity::class.java))

                }else{
                    Toast.makeText(this@DashboardActivity,"Permission not Granted Go to Settings", Toast.LENGTH_SHORT).show()

                }
            }
            receiveData.setOnClickListener {
                if(arePermissionsGranted(sender_permissions)&& isLocationEnabled()){

                    startActivity(Intent(this@DashboardActivity, ReceiverConnectioTypeActivity::class.java))

                }else{
                    Toast.makeText(this@DashboardActivity,"Permission not Granted Go to Settings", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun arePermissionsGranted(permissionList: Array<String> ): Boolean {
        return permissionList.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}