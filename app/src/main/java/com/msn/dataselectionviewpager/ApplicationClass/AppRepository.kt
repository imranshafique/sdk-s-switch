/*
package com.msn.dataselectionviewpager.ApplicationClass

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.futuremind.recyclerviewfastscroll.Utils
import com.msn.dataselectionviewpager.R
import com.msn.smartswitch.AppPrefs.SelectedItemsUriManager.showToast
import com.msn.smartswitch.data.app_prefs.AppPref
import com.msn.smartswitch.data.constant.AppConstant
import com.msn.smartswitch.data.constant.AppConstant.OwnerPort
import com.msn.smartswitch.data.constant.AppConstant.listOfSelectedItemsFileNames
import com.msn.smartswitch.data.constant.AppConstant.listOfSelectedItemsFilesLength
import com.msn.smartswitch.data.constant.AppConstant.listOfSelectedItemsUris
 import com.msn.smartswitch.data.domain.local.db.sql.AppDatabase
import com.msn.smartswitch.data.domain.local.models.DataSentDto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject


class AppRepository @Inject constructor(val prefs: AppPref, val appDatabase: AppDatabase) {
    fun isPinLockActivate(value: Boolean) = prefs.isPinLockActivate(value)
    fun savePinLock(value: String) = prefs.savePinLock(value)
    fun getPinLock() = prefs.getPinLock()
    fun getIsPinLockActivate() = prefs.getIsPinLockActivate()
    fun saveUserPermissions(value: Boolean) = prefs.saveUserPermissions(value)
    fun saveIsLanguageSelection(value: Boolean) = prefs.saveIsLanguageSelection(value)
    fun getUserPermissions() = prefs.getUserPermissions()
    fun getIsLanguageSelection() = prefs.getIsLanguageSelection()
    fun saveUserConsent(value: Boolean) = prefs.saveUserConsent(value)
    fun saveFileSendSuccess(value: String) = prefs.saveFileSendSuccess(value)
    fun saveReceiveFile(value: String) = prefs.saveReceiveFile(value)
    fun saveTotalReceiveFile(value: String) = prefs.saveTotalReceiveFile(value)
    fun saveTypeImage(value: String) = prefs.saveTypeImage(value)
    fun saveTypeAudio(value: String) = prefs.saveTypeAudio(value)
    fun saveTypeVideo(value: String) = prefs.saveTypeVideo(value)
    fun saveTypeDocuments(value: String) = prefs.saveTypeDocuments(value)
    fun saveTypeApps(value: String) = prefs.saveTypeApps(value)
    fun saveSelectedPositionCheckBox(value: Int) = prefs.saveSelectedPositionCheckBox(value)
    fun saveSelectedLanguage(value: String) = prefs.saveSelectedLanguage(value)
    fun saveHistoryClearedTime(value: Long) = prefs.saveClearedHistoryTime(value)
    fun saveEstimatedTime(value: String) = prefs.saveEstimatedTime(value)


    fun getSelectedPositionCheckBox() = prefs.getSelectedPositionCheckBox()
    fun getTypeImage() = prefs.getTypeImage()
    fun getTypeVideo() = prefs.getTypeVideo()
    fun getTypeAudio() = prefs.getTypeAudio()
    fun getTypeDocuments() = prefs.getTypeDocuments()
    fun getTypeApps() = prefs.getTypeApps()
    fun getSelectedLanguage() = prefs.getSelectedLanguage()
    fun getUserConsent() = prefs.getUserConsent()
    fun getSendingFile() = prefs.getSendingFile()
    fun getFileSendSuccess() = prefs.getFileSendSuccess()
    fun getReceiveFile() = prefs.getReceiveFile()
    fun getTotalReceiveFile() = prefs.getTotalReceiveFile()
    fun getHistoryClearedTime() = prefs.getHistoryClearedTime()
    fun getEstimatedTime() = prefs.getEstimatedTime()




    private val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )



    fun getFilePath(context: Context, folderName: String): File {
        val cw = ContextWrapper(context)
        val path = cw.getExternalFilesDir(null).toString()
        val file = File(path, folderName)
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }

    fun getBackupFiles(context: Context, folderName: String): ArrayList<File> {
        val fileList: ArrayList<File> = ArrayList()
        getFilePath(context, folderName).walkTopDown().forEach {
            if (!it.isDirectory) {
                fileList.add(it)
            }
        }
        return fileList
    }

    private fun isDuplicate(lkKey: String): Boolean {
        return saveLookupKey.contains(lkKey)
    }

    private var saveLookupKey = java.util.ArrayList<String>()

    private var job: Deferred<Unit>? = null

    @SuppressLint("Range")
    fun getVCF(mContext: Context) {
        var phones: Cursor? = null
        val path = getFilePath(mContext, AppConstant.contactFolderName)
        val file = File(path, AppConstant.contactFolderName + ".vcf")

        phones = mContext.contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
        phones?.moveToFirst()
        //initilizating zero
        saveLookupKey = arrayListOf()
        try {
            phones?.count?.let { phone ->
                for (i in 0 until phone) {
                    if (i < phone) {
                        Log.e("i", "getVCF: $i")
                        var lookupKey =
                            phones.getString(phones.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                        lookupKey = lookupKey?.trim { it <= ' ' }
                        if (saveLookupKey.isNullOrEmpty()) {
                            saveLookupKey = arrayListOf<String>()
                            lookupKey?.let { saveLookupKey.add(it) }
                        } else {
                            if (lookupKey?.let { isDuplicate(it) } == true) {
                                phones.moveToNext()
                                continue
                            } else {
                                lookupKey?.let { saveLookupKey.add(it) }
                            }
                        }
                        val uri =
                            Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_VCARD_URI,
                                lookupKey
                            )
                        var fd: AssetFileDescriptor? = null
                        try {
                            mContext.contentResolver.openAssetFileDescriptor(uri, "r")?.let {
                                fd = it
                            }
                            val buf = fd?.createInputStream()?.let { readBytes(it) }
                            val VCard = buf?.let { String(it) }
                            val mFileOutputStream = FileOutputStream(file, true)
                            mFileOutputStream.write(VCard?.toByteArray())
                            phones.moveToNext()
                            mFileOutputStream.close()
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }
                    }
                }
            }
        } catch (ex: ArrayIndexOutOfBoundsException) {

        }
    }

    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        byteBuffer.write(buffer)
        var length = 0
        while (inputStream.read(buffer).also { length = it } !== -1) {
            byteBuffer.write(buffer, 0, length)
        }
        return byteBuffer.toByteArray()
    }

    suspend fun sendFile(
        context: Context, activity: Activity, uris: ArrayList<Uri>,
        filesLength: ArrayList<Long>, fileNames: ArrayList<String>,
        serverAddress: InetAddress,
    ) = withContext(Dispatchers.IO) {
        var socket: Socket = Socket()
        var len = 0
        var totalTransferred = 0L // Tracks total bytes transferred

        try {
                socket.connect(InetSocketAddress(serverAddress, OwnerPort), 10000)

            if (socket.isConnected) {
                socket.reuseAddress = true
            }

            val outputStream = socket.getOutputStream()
            val objectOutputStream = ObjectOutputStream(outputStream).apply {
                writeInt(uris.size) // Send number of files
                writeObject(fileNames) // Send file names
                flush()
                writeObject(filesLength) // Send file lengths
                flush()
            }

            // Calculate total size of all files
            val totalSize = filesLength.sum()

            // Start time for calculating transfer speed
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = System.currentTimeMillis()

            for (i in uris.indices) {
                val uri = uris[i]
                val fileSize = filesLength[i]
                var fileTransferred = 0L // Tracks bytes transferred for the current file

                // Set dynamic buffer size based on file size
                val buf = when {
                    fileSize > 500 * 1024 * 1024 -> ByteArray(5 * 1024 * 1024) // 5 MB for very large files
                    fileSize > 50 * 1024 * 1024 -> ByteArray(2 * 1024 * 1024)  // 2 MB for medium files
                    else -> ByteArray(256 * 1024)                              // 256 KB for small files
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    while (inputStream.read(buf).also { len = it } != -1) {
                        if (socket.isConnected) {
                            objectOutputStream.write(buf, 0, len)
                            objectOutputStream.flush()

                            totalTransferred += len
                            fileTransferred += len

                            // Update progress logs every second
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastUpdateTime > 1000) {
                                lastUpdateTime = currentTime

                                // Log current progress
                                val percentage =
                                    (totalTransferred.toDouble() / totalSize * 100).toInt()
                                val elapsedTime = currentTime - startTime
                                val transferSpeed =
                                    if (elapsedTime > 0) (totalTransferred / elapsedTime) / 1024 else 0L // KB/ms
                                val remainingTime =
                                    if (transferSpeed > 0) ((totalSize - totalTransferred) / transferSpeed) / 1024 else 0L // seconds

                                Log.d(
                                    "FileTransfer",
                                    "Speed: ${transferSpeed * 1000 / 1024} MB/s | Estimated time remaining: ${remainingTime}s"
                                )
                            }
                        } else {
                            throw IOException("Socket disconnected")
                        }
                    }
                }

                Log.d("FileTransfer", "Completed transfer of file: ${fileNames[i]}")
            }

        } catch (e: Exception) {
            Log.e("FileTransfer", "Error during file transfer: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                socket.close()
                withContext(Dispatchers.Main) {
                   showToast(context, context.getString(R.string.successFile))
                    clearDataAfterSend()
                    prefs.saveFileSendSuccess(context.getString(R.string.success))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    clearDataAfterSend()
                }
            }
        }
    }

    fun clearDataAfterSend() {

        try {
            CoroutineScope(Dispatchers.Default).launch {
                if (listOfSelectedItemsUris.isNotEmpty() && listOfSelectedItemsFilesLength.isNotEmpty() && listOfSelectedItemsFileNames.isNotEmpty()) {

                    if (job?.isActive == false) {
                        job = CoroutineScope(Dispatchers.IO).async {
                            val urisToString: ArrayList<String> = ArrayList()
                            val copyOfListOfSelectedItemsUris =
                                ArrayList<Uri>(listOfSelectedItemsUris)
                            try {
                                copyOfListOfSelectedItemsUris.forEach { it ->
                                    urisToString.add(
                                        it.toString()
                                    )
                                }
                            } catch (e: java.lang.NullPointerException) {
                                e.printStackTrace()
                            }

                            val copyOfListOfSelectedItemsFileNames =
                                ArrayList<String>(listOfSelectedItemsFileNames)
                            val copyOfListOfSelectedItemsFilesLength =
                                ArrayList<Long>(listOfSelectedItemsFilesLength)

                            val dataSent = DataSentDto(
                                dateAndTime = System.currentTimeMillis(),
                                uris = urisToString,
                                names = copyOfListOfSelectedItemsFileNames,
                                sizes = copyOfListOfSelectedItemsFilesLength
                            )
                            appDatabase.DataSentDao().insertSentDataHistory(dataSent)
                        }

                        job?.await()
                    }
                } else {
                    val i = 0
                }

                listOfSelectedItemsUris.clear()
                listOfSelectedItemsFileNames.clear()
                listOfSelectedItemsFilesLength.clear()

                prefs.saveTypeImage("")
                prefs.saveTypeAudio("")
                prefs.saveTypeVideo("")
                prefs.saveTypeDocuments("")
                prefs.saveTypeApps("")

                // in below clear all listes values

               */
/* listOfImage.value?.let { images ->
                    if (images.isNotEmpty()) {
                        repeat(images.size)
                        {
                            listOfImage.value?.get(it)?.isItemChecked = false
                        }
                    }
                }
                listOfDoc.value?.let { doc ->
                    if (doc.isNotEmpty()) {
                        repeat(doc.size)
                        {
                            listOfDoc.value?.get(it)?.isItemChecked = false
                        }
                    }

                }

                listOfAudio.value?.let { audio ->
                    if (audio.isNotEmpty()) {
                        repeat(audio.size)
                        {
                            listOfAudio.value?.get(it)?.isItemChecked = false
                        }
                    }

                }


                listOfVideo.value?.let { video ->
                    if (video.isNotEmpty()) {
                        repeat(video.size)
                        {
                            listOfVideo.value?.get(it)?.isItemChecked = false
                        }
                    }

                }
                listOfApps.value?.let { app ->
                    if (app.isNotEmpty()) {
                        repeat(app.size)
                        {
                            listOfApps.value?.get(it)?.isItemChecked = false
                        }
                    }

                }*//*


            }
        } catch (e: ConcurrentModificationException) {
            e.printStackTrace()
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
    }
}*/
