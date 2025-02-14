package com.msn.dataselectionviewpager.dataClass

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.File

data class FolderWithImageCount(
    val folder: File,
    val imageCount: Int,
    val thumbnail: Bitmap?,

)
data class FolderWithVideoCount(
    val folder: File,
    val videoCount: Int,
    val thumbnail: Bitmap?
)
data class FolderWithAudioCount(
    val folder: File,
    val audioCount: Int
)
data class AppInfo(
    val appName: String,
    val appUri: Uri,
    val appIcon: Drawable, // For app icon
    val appPath: String? = null,
    val originalApkName: String? = null
)
data class DocumentModel(
    val name: String,
    val filePath: String,
    val mimeType: String
)
data class AudioModel(
    val name: String,
    val filePath: String
)
data class FolderWithDocumentCount(
    val folder: File,             // The folder itself
    val documentCount: Int,       // The count of documents in the folder
    val thumbnail: Bitmap? = null // Optional thumbnail for the folder, if available
)
data class DocumentFile(
    val fileName: String,
    val filePath: String
)
 data class Contact(
    val name: String,
    val phoneNumber: String,
    val contactUri: Uri // Added field to store contact URI
)