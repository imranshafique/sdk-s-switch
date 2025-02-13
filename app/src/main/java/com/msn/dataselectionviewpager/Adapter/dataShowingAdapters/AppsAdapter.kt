package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R
import com.msn.smartswitch.AppPrefs.SelectedItemsUriManager


import android.content.Context
import android.widget.ImageView
import com.msn.dataselectionviewpager.dataClass.AppInfo
import com.msn.smartswitch.Models.AppConstant.listOfSelectedItemsFileNames
import com.msn.smartswitch.Models.AppConstant.listOfSelectedItemsFilesLength
import com.msn.smartswitch.Models.AppConstant.selectedPath
import java.io.File

class AppsAdapter(
    private val context: Context,
    private val appsList: List<AppInfo>
) : RecyclerView.Adapter<AppsAdapter.AppsViewHolder>()
{

    inner class AppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.appName)
        val appIconImageView: ImageView = itemView.findViewById(R.id.appIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.appCheckBox)

        fun bind(appInfo: AppInfo) {
            appNameTextView.text = appInfo.appName
            appIconImageView.setImageDrawable(appInfo.appIcon)

            val appName = appInfo.appName // The human-readable app name
            val appUri = appInfo.appUri
            val appPath = appInfo.appPath

            itemView.setOnClickListener {
                Log.d("AppsAdapter", "App Path: $appPath")
                Log.d("AppsAdapter", "App Name: $appName")
            }

            // Set checkbox state based on whether the URI is selected
            checkBox.isChecked = selectedPath.contains(appPath)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    SelectedItemsUriManager.addUri(appUri)
                    Log.d("Selected URIs", "Added URI: $appUri")

                    // Get the file size
                    val appFile = File(appInfo.appPath ?: "")
                    val fileSize = appFile.length() // File size in bytes

                    // Get the original file name (not just base.apk)
                    val originalFileName = appFile.name // This will give the actual APK file name
                    val appFileName = originalFileName // APK file name, not "base.apk"

                    // Log the app name and APK info
                    Log.d("AppsAdapter", "App Name (Human-readable): $appName")
                    Log.d("AppsAdapter", "App File Path: $appPath")
                    Log.d("AppsAdapter", "App File Name (APK): $appFileName")
                    Log.d("AppsAdapter", "App File Size: $fileSize bytes")

                    // Add the file name (with original name) to the respective lists
                    listOfSelectedItemsFilesLength.add(fileSize)
                    listOfSelectedItemsFileNames.add(appName) // Store human-readable name if needed
                    if (appPath != null) {
                        selectedPath.add(appPath)
                    }
                } else {
                    SelectedItemsUriManager.removeUri(appUri)
                    Log.d("Selected URIs", "Removed URI: $appUri")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        val appInfo = appsList[position]
        holder.bind(appInfo)
    }

    override fun getItemCount(): Int = appsList.size
}