package com.msn.dataselectionviewpager.Utils


import android.Manifest
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
 import androidx.core.content.ContextCompat

class PermissionHelper(
    private val context: Context,
    private val requestStoragePermissionLauncher: ActivityResultLauncher<Array<String>>,
    private val requestManageStorageLauncher: ActivityResultLauncher<Intent>,
    private val onPermissionGranted: () -> Unit
)
{

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                requestManageStorageLauncher.launch(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "Error opening settings", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestStoragePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.entries.all { it.value }) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }
    }

    fun handleManageStorageResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && android.os.Environment.isExternalStorageManager()) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Manage Storage permission required", Toast.LENGTH_LONG).show()
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}
