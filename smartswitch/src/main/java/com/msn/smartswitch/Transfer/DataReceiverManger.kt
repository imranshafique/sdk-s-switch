package com.msn.smartswitch.Transfer

import android.app.Activity
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import com.msn.smartswitch.ServerClient.Sockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class DataReceiverManger {

    interface ReceiverListener {
        fun onFileReceiveSuccess()
        fun onAllFileReceiveSuccess()
        fun onFileReceiveFailure(errorMessage: String)
        fun onConnectionError()
        fun onConnectionClosed()
        fun onFileReceiveProgress(progress: Int, totalBytesReceived: Long)
        fun onTotalCountReceived(count: Int, size: Long)
    }

    private val TAG = javaClass.simpleName
    private var listener: ReceiverListener? = null

    fun setListener(listener: ReceiverListener) {
        this.listener = listener
    }

    private var dataOutputStream: DataOutputStream? = null
    private var dataInputStream: DataInputStream? = null
    private var filesReceived = 0
    private val existingSocket = Sockets.getSocket()
    private var totalBytesToReceive: Long = 0L
    private var totalBytesReceived: Long = 0L
    private var currentActivity: Activity? = null

    fun startReceive(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "startReceive: Attempting to receive files")
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = existingSocket
                Log.d(TAG, "startReceive: existingSocket $existingSocket")
                if (socket == null || socket.isClosed) {
                    Log.e(TAG, "startReceive: Socket is null or closed")
                    listener?.onConnectionError()
                    return@launch
                }

                dataOutputStream = DataOutputStream(socket.getOutputStream())
                dataInputStream = DataInputStream(socket.getInputStream())

                if (dataInputStream == null) {
                    Log.e(TAG, "startReceive: DataInputStream is null")
                    listener?.onConnectionError()
                    return@launch
                }

                val count = dataInputStream?.readInt() ?: 0
                totalBytesToReceive = dataInputStream?.readLong() ?: 0
                Log.d(TAG, "startReceive: Expected file count: $count")
                listener?.onTotalCountReceived(count, totalBytesToReceive)
                Log.d(TAG, "startReceive: Expected file length: $totalBytesToReceive")

                for (i in 0 until count) {
                    Log.d(TAG, "startReceive: Receiving file $i/$count")
                    receiveFile()
                }


            } catch (e: IOException) {
                Log.e(TAG, "startReceive: Exception: ${e.localizedMessage}")
                listener?.onConnectionError()
            }
        }
    }


    private fun receiveFile() {
        try {
            val socket = existingSocket
            var progress: Int = 0
            if (socket == null || socket.isClosed) {
                Log.e(TAG, "receiveFile: Socket is closed or null")
                listener?.onConnectionError()
                return
            }

            // Initialize DataInputStream if it's null
            if (dataInputStream == null) {
                dataInputStream = DataInputStream(socket.getInputStream())
            }

            // Read file length and path
            val fileLength = dataInputStream?.readLong() ?: 0
            if (fileLength <= 0) {
                Log.e(TAG, "receiveFile: Invalid file length, skipping file.")
                listener?.onFileReceiveFailure("Invalid file length")
                return  // Skip the corrupt file
            }

            val filePath = dataInputStream?.readUTF() ?: ""
            if (filePath.isEmpty()) {
                Log.e(TAG, "receiveFile: File path is empty, skipping file.")
                listener?.onFileReceiveFailure("Invalid file path")
                return  // Skip the corrupt file
            }

            val fileName = filePath.substringAfterLast('/').substringAfterLast('\\')
            if (fileName.isBlank() || fileName.contains("..")) {
                listener?.onFileReceiveFailure("Invalid file name")
                return
            }

            if (fileName.isNotEmpty()) {
                val output = createDestination(fileName)
                if (output == null) {
                    listener?.onFileReceiveFailure("Could not create destination")
                    return
                }

                val buffer = ByteArray(1024 * 256) // 256KB buffer (same as sender)
                var bytesRead: Int
                var totalBytesRead = 0L

                try {
                    // Read file data in chunks until the full file length is received
                    while (totalBytesRead < fileLength) {
                        val bytesToRead =
                            minOf(buffer.size.toLong(), fileLength - totalBytesRead).toInt()
                        bytesRead = dataInputStream?.read(buffer, 0, bytesToRead) ?: -1
                        if (bytesRead == -1) throw IOException("Corrupt file detected, bytesRead == -1")

                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        totalBytesReceived += bytesRead

                        // Progress calculation
                        progress =
                            (totalBytesReceived.toFloat() / totalBytesToReceive * 100).toInt()
                        listener?.onFileReceiveProgress(progress, totalBytesReceived)
                        Log.d(TAG, "receiveFile: Receiving $fileName - $progress%")
                    }

                    output.flush()
                    output.close()

                    Log.d(
                        TAG,
                        "receiveFile: File received successfully: $fileName, Size: $totalBytesRead bytes"
                    )
                    listener?.onFileReceiveSuccess()
                    filesReceived++
                } catch (e: IOException) {
                    Log.e(TAG, "receiveFile: Corrupt file detected ($fileName), skipping.")
                    listener?.onFileReceiveFailure("File $fileName is corrupt and was skipped.")
                    output.close()
                }

                if (progress == 100) {
                    listener?.onAllFileReceiveSuccess()
                    Sockets.getSocket()?.let {
                        Log.d(TAG, "closeSocket: ${it.isConnected}")
                        if (it.isConnected) {
                            it.close()
                            Log.d(TAG, "closeSocket: ${it.isClosed}")
                        }
                    }
                }
            } else {
                Log.e(TAG, "receiveFile: Invalid file name, skipping file.")
                listener?.onFileReceiveFailure("Invalid file name")
            }
        } catch (e: IOException) {
            Log.e(TAG, "receiveFile: IOException: ${e.localizedMessage}")
            listener?.onFileReceiveFailure("IOException: ${e.message}, skipping file.")
        } catch (e: Exception) {
            Log.e(TAG, "receiveFile: Exception: ${e.localizedMessage}")
            listener?.onFileReceiveFailure("Exception: ${e.message}, skipping file.")
        }
    }

    private fun createDestination(fileName: String): OutputStream? {
        val activity = currentActivity ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/SmartSwitchSDK")
            }
            activity.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?.let(activity.contentResolver::openOutputStream)
        } else {
            FileOutputStream(java.io.File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName))
        }
    }
}
