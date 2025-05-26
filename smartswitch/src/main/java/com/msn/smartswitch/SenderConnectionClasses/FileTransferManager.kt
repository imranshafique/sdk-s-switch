package com.msn.smartswitch.SenderConnectionClasses

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import com.msn.smartswitch.ServerClient.Sockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
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

    fun sendFiles(selectedPath: ArrayList<String>,activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        CoroutineScope(Dispatchers.IO).launch {
            val socket = Sockets.getSocket()
            if (socket == null || socket.isClosed) {
                Log.e(TAG, "sendFiles: Socket is null or closed")
                listener?.onConnectionError()
                return@launch
            }
            Log.d(TAG, "sendFiles: socket: $socket")
            totalBytesToSend = getTotalSizeInBytes(selectedPath)
            Log.d(TAG, "sendFiles: totalSize: $totalBytesToSend")

            val dataOutputStream = DataOutputStream(socket.getOutputStream())

            Log.d(TAG, "sendFiles: Sending file count: ${selectedPath.size}")
            Log.d(TAG, "sendFiles: Sending file count: ${selectedPath}")
            totalData = selectedPath.size
            dataOutputStream.writeInt(selectedPath.size)
            dataOutputStream.writeLong(totalBytesToSend)
            dataOutputStream.flush()

            // **Create a copy of the list to prevent modification issues**
            val selectedPathCopy = ArrayList(selectedPath)

            for (path in selectedPathCopy) {
                Log.d(TAG, "sendFiles: Sending file: $path")
                sendData(File(path), dataOutputStream, socket)
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
