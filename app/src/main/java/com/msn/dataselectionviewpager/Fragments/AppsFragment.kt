package com.msn.dataselectionviewpager.Fragments

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.AppsAdapter
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.dataClass.AppInfo


class AppsFragment : Fragment() {

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_apps, container, false)

        appsRecyclerView = view.findViewById(R.id.appsRecyclerView)
        appsRecyclerView.layoutManager = LinearLayoutManager(context)

        val installedApps = getInstalledApps()
        appsAdapter = AppsAdapter(requireContext(), installedApps)
        appsRecyclerView.adapter = appsAdapter

        return view
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm: PackageManager = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appsList = mutableListOf<AppInfo>()

        for (app in installedApps) {
            val appName = app.loadLabel(pm).toString()  // This retrieves the original app name
            val packageName = app.packageName

            // Check if it's not a system app
            if (!isSystemApp(app)) {
                val appUri = getAppContentUri(packageName) // Generate URI for the app
                val appIcon = app.loadIcon(pm)
                val appPath = app.sourceDir

                Log.d("AppsAdapter", "getInstalledApps: $appPath") // Log the app name
                appsList.add(AppInfo(appName, appUri, appIcon, appPath))
            }
        }

        return appsList
    }


    // Helper function to check if the app is a system app
    private fun isSystemApp(app: ApplicationInfo): Boolean {
        // Return true if the app is a system app, false otherwise
        return app.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    // Helper function to generate a URI for each app
    private fun getAppContentUri(packageName: String): Uri {
        return Uri.parse("content://$packageName") // Example content URI
    }
}
