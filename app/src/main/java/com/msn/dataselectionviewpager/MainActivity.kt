package com.msn.dataselectionviewpager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.viewpager2.widget.ViewPager2
import com.msn.dataselectionviewpager.Activities.QrCodeGeneratorActivity
import com.msn.dataselectionviewpager.Activities.QrCodeScannerActivity
import com.msn.dataselectionviewpager.Activities.SenderConnectionTypeActivity
import com.msn.dataselectionviewpager.Activities.WifiSenderActivity
import com.msn.dataselectionviewpager.Adapter.ViewPagerAdapter.FragmentAdapter
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import com.msn.dataselectionviewpager.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
     private lateinit var adapter: FragmentAdapter
     private var TAG = javaClass.simpleName
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
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
        Log.d(TAG, "onCreate: selectedPath before: ${selectedPath.size}")

        selectedPath.clear()
        Log.d(TAG, "onCreate: selectedPath: ${selectedPath.size}")
        // Set the adapter to ViewPager2
        adapter = FragmentAdapter(this)
        binding.viewPager.adapter = adapter

        // Create custom tabs and add them to the tab container
        setupTabs()

        // Add ViewPager2 listener to update tab selection
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectTab(position) // Select tab based on ViewPager2 page
            }
        })
    }

    private fun setupTabs() {
        // Define tab names and their respective icons
        val tabNames = listOf("Images", "Videos", "Audios", "Documents", "Contacts", "Apps")
        val tabIcons = listOf(
            R.drawable.imagesicon, R.drawable.videosicon, R.drawable.musicicon,
            R.drawable.documentsicon, R.drawable.contactsicon, R.drawable.appsicon
        )

        // Create custom tabs for each fragment
        tabNames.forEachIndexed { index, tabName ->
            val tabView = layoutInflater.inflate(R.layout.custom_tab, binding.customTabContainer, false)
            val tabIcon: ImageView = tabView.findViewById(R.id.tabIcon)
            val tabText: TextView = tabView.findViewById(R.id.tabText)
            val tabIndicator: View = tabView.findViewById(R.id.tabIndicator)

            // Set icon and text for each tab
            tabIcon.setImageResource(tabIcons[index])
            tabText.text = tabName

            // Set click listener to change ViewPager2 page
            tabView.setOnClickListener {
                binding.viewPager.currentItem = index
            }

            // Adjust layout parameters for tabs
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            // First tab on the left
            if (index == 0) {
                params.gravity = android.view.Gravity.START
            }
            // Last tab on the right
            if (index == tabNames.size - 1) {
                params.gravity = android.view.Gravity.END
            }

            // Add the tab to the container
            binding.customTabContainer.addView(tabView, params)
        }

        // Set the first tab as selected by default
        selectTab(0)
        clickListeners()
    }

     private fun selectTab(position: Int) {
        for (i in 0 until binding.customTabContainer.childCount) {
            val tab = binding.customTabContainer.getChildAt(i)
            val tabIcon: ImageView = tab.findViewById(R.id.tabIcon)
            val tabText: TextView = tab.findViewById(R.id.tabText)
            val tabIndicator: View = tab.findViewById(R.id.tabIndicator)

            // Highlight selected tab
            if (i == position) {
                tabIcon.setColorFilter(resources.getColor(android.R.color.holo_blue_dark))  // Change color for selected
                tabText.setTextColor(resources.getColor(android.R.color.holo_blue_dark))    // Change text color for selected
                tabIndicator.visibility = View.VISIBLE  // Show indicator
            } else {
                tabIcon.setColorFilter(resources.getColor(android.R.color.darker_gray))    // Default color for unselected
                tabText.setTextColor(resources.getColor(android.R.color.darker_gray))      // Default text color for unselected
                tabIndicator.visibility = View.GONE    // Hide indicator
            }
        }
    }
    private fun clickListeners(){
        binding.run {
            sendData.setOnClickListener {
                if (selectedPath.isNotEmpty()) {
                    if(arePermissionsGranted(sender_permissions)&& isLocationEnabled()){

                        startActivity(Intent(this@MainActivity, SenderConnectionTypeActivity::class.java))


                    }else{
                        Toast.makeText(this@MainActivity,"Permission not Granted Go to Settings", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this@MainActivity, "select some files", Toast.LENGTH_SHORT).show()
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
