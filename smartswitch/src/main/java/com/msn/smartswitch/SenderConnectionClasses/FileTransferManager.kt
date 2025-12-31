package com.msn.smartswitch.SenderConnectionClasses

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import com.msn.smartswitch.ServerClient.Sockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.Socket

class FileTransferSDK(
    private val context: Context,
    private val activity: Activity
) {

    interface FileTransferListener {
        fun onFileSendSuccess(progress: Int, totalBytesSent: Long)
        fun onAllFilesSentSuccessfully()
        fun onFileSendFailure(errorMessage: String)
        fun onConnectionError()
        fun onConnectionClosed()
        fun onFileSendingProgress(progress: Float)
    }


    private var listener: FileTransferListener? = null
    private val TAG = javaClass.simpleName
    private var totalData: Int = 0
    private var transferredData:Int = 0
    private var totalBytesSent = 0L
    private var totalBytesToSend: Long = 0L  // Total bytes of all files

    fun setListener(listener: FileTransferListener) {
        this.listener = listener
    }

    fun sendFiles(selectedPath: ArrayList<String>, activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        CoroutineScope(Dispatchers.IO).launch {
            val socket = Sockets.getSocket()
            if (socket == null || socket.isClosed) {
                Log.e(TAG, "sendFiles: Socket is null or closed")
                withContext(Dispatchers.Main) {
                    listener?.onConnectionError()
                }
                return@launch
            }

            Log.d(TAG, "sendFiles: socket: $socket")

            // Safely calculate total size with try-catch for invalid paths
            var totalBytesToSend: Long = 0
            try {
                totalBytesToSend = selectedPath.sumOf { path ->
                    val file = File(path)
                    if (file.exists() && file.isFile) file.length() else 0L
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating total size", e)
                withContext(Dispatchers.Main) {
                    listener?.onConnectionError()
                }
                return@launch
            }

            Log.d(TAG, "sendFiles: totalSize: $totalBytesToSend")

            try {
                val dataOutputStream = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))

                Log.d(TAG, "sendFiles: Sending file count: ${selectedPath.size}")
                dataOutputStream.writeInt(selectedPath.size)
                dataOutputStream.writeLong(totalBytesToSend)
                dataOutputStream.flush()

                totalData = selectedPath.size

                // Create a snapshot copy to avoid any potential concurrent modification
                val pathsToSend = ArrayList(selectedPath)

                for (path in pathsToSend) {
                    val file = File(path)
                    if (!file.exists() || !file.isFile) {
                        Log.w(TAG, "Skipping invalid file: $path")
                        continue
                    }

                    Log.d(TAG, "sendFiles: Sending file: $path")
                    try {
                        sendData(file, dataOutputStream, socket)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending file: $path", e)
                        // Optionally notify listener of partial failure
                        break // or continue depending on requirements
                    }
                }

                dataOutputStream.flush()
                // Do not close the stream here if the socket is reused elsewhere

            } catch (e: IOException) {
                Log.e(TAG, "IO error during header/sendFiles", e)
                withContext(Dispatchers.Main) {
                    listener?.onConnectionError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in sendFiles", e)
                withContext(Dispatchers.Main) {
                    listener?.onConnectionError()
                }
            }
        }
    }


    private fun getTotalSizeInBytes(filePaths: ArrayList<String>): Long {
        var totalSize = 0L
        val pathsCopy = ArrayList(filePaths) // Create a copy
        for (path in pathsCopy) {
            val file = File(path)
            if (file.exists()) {
                totalSize += file.length()
            }
        }
        return totalSize
    }

    private fun sendData(file: File, dataOutputStream: DataOutputStream, socket: Socket) {
        try {
            if (!socket.isClosed) {

                dataOutputStream.writeLong(file.length())
                dataOutputStream.writeUTF(file.path)
                dataOutputStream.flush()

                val buffer = ByteArray(1024 * 256) // 256KB buffer for efficient streaming
                var bytesRead: Int

                BufferedInputStream(FileInputStream(file)).use { bis ->
                    while (bis.read(buffer).also { bytesRead = it } != -1) {
                        dataOutputStream.write(buffer, 0, bytesRead)
                        dataOutputStream.flush()
                        totalBytesSent += bytesRead

                        // Progress calculation
                        val progress = (totalBytesSent.toFloat() / totalBytesToSend.toFloat() * 100).toInt()
                        listener?.onFileSendSuccess(progress,totalBytesSent)
                    }
                }

                transferredData++
                if (transferredData == totalData) {
                    Sockets.getSocket()?.let {
                        if (it.isConnected) {
                            it.close()
                            Log.d(TAG, "closeSocket: ${it.isClosed}")
                        }
                    }
                    listener?.onAllFilesSentSuccessfully()
                }

                Log.d(TAG, "sendData: Successfully sent ${file.name}")

            } else {
                Log.e(TAG, "sendData: Socket closed before sending file")
                listener?.onConnectionError()
            }
        } catch (e: IOException) {
            Log.e(TAG, "sendData: IOException: ${e.message}")
            listener?.onFileSendFailure("IOException: ${e.message}")
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "sendData: OutOfMemoryError: ${e.message}")
            listener?.onFileSendFailure("OutOfMemoryError: ${e.message}")
        }
    }
}
