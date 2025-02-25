package com.msn.dataselectionviewpager.ViewModel

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msn.dataselectionviewpager.dataClass.AudioModel
import com.msn.dataselectionviewpager.dataClass.DocumentModel
import com.msn.dataselectionviewpager.dataClass.FolderWithAudioCount
import com.msn.dataselectionviewpager.dataClass.FolderWithImageCount
import com.msn.dataselectionviewpager.dataClass.FolderWithVideoCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MediaViewModel : ViewModel() {

    private val _imageFolders = MutableLiveData<List<FolderWithImageCount>>()
    val imageFolders: LiveData<List<FolderWithImageCount>> = _imageFolders
    // Load image folders in the background
    fun loadImageFolders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageDirs = getMediaDirectories(context)

            for (folder in imageDirs) {
                val images = folder.listFiles { file -> file.isFile && file.extension in listOf("jpg", "jpeg", "png", "gif") }
                val imageCount = images?.size ?: 0
                val thumbnail = if (imageCount > 0) {
                    BitmapFactory.decodeFile(images?.get(0)?.absolutePath)
                } else {
                    null
                }

                val folderWithImageCount = FolderWithImageCount(folder, imageCount, thumbnail)

                // Post each folder update to LiveData immediately
                withContext(Dispatchers.Main) {
                    _imageFolders.value = (_imageFolders.value ?: emptyList()) + folderWithImageCount
                }
            }
        }
    }




    private val _categorizedDocuments = MutableLiveData<Map<String, List<DocumentModel>>>()
    val categorizedDocuments: LiveData<Map<String, List<DocumentModel>>> get() = _categorizedDocuments

    private val _videoFolders = MutableLiveData<List<FolderWithVideoCount>>()
    val videoFolders: LiveData<List<FolderWithVideoCount>> get() = _videoFolders



    // Load video folders in the background
    fun loadVideoFolders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val videoDirs = getVideoDirectories(context)

            val newFolderList = mutableListOf<FolderWithVideoCount>()

            for (folder in videoDirs) {
                Log.d("VideoFolders", "Folder Path: ${folder.absolutePath}")

                val videos = folder.listFiles { file ->
                    file.isFile && file.extension in listOf("mp4", "mkv", "avi", "mov")
                }
                val videoCount = videos?.size ?: 0
                val thumbnail = if (videoCount > 0) {
                    getVideoThumbnail(videos[0].absolutePath)
                } else {
                    null
                }

                newFolderList.add(FolderWithVideoCount(folder, videoCount, thumbnail))

                // Update LiveData incrementally without duplication
                withContext(Dispatchers.Main) {
                    _videoFolders.value = newFolderList.toList() // Assign a fresh list
                }
            }
        }
    }

    private fun getVideoThumbnail(videoPath: String?): Bitmap? {
        if (videoPath.isNullOrEmpty()) return null
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private val _audioFolders = MutableLiveData<List<FolderWithAudioCount>>()
    val audioFolders: LiveData<List<FolderWithAudioCount>> get() = _audioFolders

    private val _audiosInFolder = MutableLiveData<List<AudioModel>>()
    val audiosInFolder: LiveData<List<AudioModel>> get() = _audiosInFolder

    private val _documentFolders = MutableLiveData<List<File>>()
    val documentFolders: LiveData<List<File>> get() = _documentFolders








    // Load audio folders in the background
    fun loadAudioFolders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val audioDirs = getAudioDirectories(context)

            val newFolderList = mutableListOf<FolderWithAudioCount>()

            for (folder in audioDirs) {
                val audioFiles = folder.listFiles { file ->
                    file.isFile && file.extension in listOf("mp3", "wav", "flac", "aac")
                }

                val audioCount = audioFiles?.size ?: 0

                Log.d("AudioFile", "Folder: ${folder.absolutePath}, Count: $audioCount")

                val folderWithAudioCount = FolderWithAudioCount(folder, audioCount)

                newFolderList.add(folderWithAudioCount)

                // Update LiveData incrementally without duplication
                withContext(Dispatchers.Main) {
                    _audioFolders.value = newFolderList.toList() // Assign a fresh list
                }
            }
        }
    }



    // Load audios from a specific folder in the background
    fun loadAudiosFromFolder(folder: File) {
        CoroutineScope(Dispatchers.IO).launch {
            val audios = folder.listFiles { file -> file.isFile && file.extension in listOf("mp3", "wav", "flac", "aac") }
            audios?.forEach { file ->
                // Print or log the file path
                Log.d("AudioFile", "Audio File absolutePath: ${file.absolutePath}")
                Log.d("AudioFile", "Audio File Path: ${file.path}")
            }
            val audioModels = audios?.map { file ->
                AudioModel(name = file.name, filePath = file.absolutePath)
            } ?: emptyList()
            withContext(Dispatchers.Main) {
                _audiosInFolder.value = audioModels
            }
        }
    }

    // Load document folders in the background
    fun loadDocumentFolders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val documentDirs = getMediaDirectories(context)
            withContext(Dispatchers.Main) {
                _documentFolders.value = documentDirs
            }
        }
    }

    // Load and categorize documents in the background
    fun fetchAndCategorizeDocuments(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val mimeCategories = mapOf(
                "PDF" to listOf("application/pdf"),
                "PowerPoint" to listOf("application/vnd.ms-powerpoint"),
                "Word" to listOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                "Text" to listOf("text/plain"),
                "Excel" to listOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )

            val categorizedDocuments = mutableMapOf<String, MutableList<DocumentModel>>()

            val documentUri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DISPLAY_NAME
            )

            val selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (" +
                    mimeCategories.flatMap { it.value }.joinToString(",") { "'${it}'" } + ")"

            val cursor: Cursor? = context.contentResolver.query(
                documentUri, projection, selection, null, null
            )

            cursor?.use {
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val data = cursor.getString(dataIndex)
                    val mimeType = cursor.getString(mimeIndex)
                    val name = cursor.getString(nameIndex)
                    val path = cursor.getString(dataIndex)
                    Log.d("docType", "fetchAndCategorizeDocuments: name: $name")
                    Log.d("docType", "fetchAndCategorizeDocuments: path: $path")

                    mimeCategories.forEach { (category, mimeTypes) ->
                        if (mimeTypes.contains(mimeType)) {
                            val document = DocumentModel(data, path, mimeType)
                            categorizedDocuments.getOrPut(category) { mutableListOf() }.add(document)
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _categorizedDocuments.value = categorizedDocuments
            }
        }
    }

    // Helper function to get media directories (images)
    private fun getMediaDirectories(context: Context): List<File> {
        val directories = mutableListOf<File>()
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = resolver.query(collection, projection, null, null, null)

        cursor?.use {
            val dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumnIndex)
                val file = File(filePath)
                val parentDir = file.parentFile
                if (parentDir != null && !directories.contains(parentDir)) {
                    directories.add(parentDir)
                }
            }
        }

        return directories
    }

    // Helper function to get audio directories
    private fun getAudioDirectories(context: Context): List<File> {
        val directories = mutableListOf<File>()
        val resolver = context.contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)

        val cursor = resolver.query(collection, projection, null, null, null)

        cursor?.use {
            val dataColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumnIndex)
                val file = File(filePath)
                val parentDir = file.parentFile
                if (parentDir != null && !directories.contains(parentDir)) {
                    directories.add(parentDir)
                }
            }
        }

        return directories
    }

    // Helper function to get video directories
    private fun getVideoDirectories(context: Context): List<File> {
        val directories = mutableListOf<File>()
        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media.DATA)

        val cursor = resolver.query(collection, projection, null, null, null)

        cursor?.use {
            val dataColumnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumnIndex)
                val file = File(filePath)
                val parentDir = file.parentFile
                if (parentDir != null && !directories.contains(parentDir)) {
                    directories.add(parentDir)
                }
            }
        }

        return directories
    }
}
