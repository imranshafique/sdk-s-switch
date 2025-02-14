package com.msn.smartswitch.SenderConnectionClasses

import android.app.Activity
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.ft.features.local_transfer.smart_switch.connection.Sockets
 import com.msn.smartswitch.Models.AppConstant.selectedPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import javax.inject.Inject

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

    // TODO: Remove  these injections
    @Inject
    lateinit var wifiP2pManager: WifiP2pManager

    @Inject
    lateinit var wifiP2pChannel: WifiP2pManager.Channel

     private var listener: FileTransferListener? = null
    private val TAG = javaClass.simpleName
    var totalData: Int = 0
    var transferredData:Int = 0
    var totalBytesSent = 0L
    var totalBytesToSend: Long = 0L  // Total bytes of all files

    fun setListener(listener: FileTransferListener) {
        this.listener = listener
    }

    fun sendFiles() {
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
            Log.d(TAG, "")

            for (path in selectedPath) {

                Log.d(TAG, "sendFiles: Sending file: $path")
                sendData(File(path), dataOutputStream, socket)
            }
        }
    }
    fun getTotalSizeInBytes(filePaths: ArrayList<String>): Long {
        var totalSize = 0L
        for (path in filePaths) {
            val file = File(path)
            if (file.exists()) {
                totalSize += file.length()
            }
        }
        return totalSize
    }

    // TODO: make suspend 
    private fun sendData(file: File, dataOutputStream: DataOutputStream, socket: Socket) {
        try {
            if (!socket.isClosed) {
//                Log.d(TAG, "sendData: Sending file: ${file.name}")

                dataOutputStream.writeLong(file.length())
                dataOutputStream.writeUTF(file.path)
                dataOutputStream.flush()

                val buffer = ByteArray(1024 * 4) // 4KB buffer for efficient streaming
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
    // TODO: Remove commented Code 


    /*   private fun sendData(file: File, dataOutputStream: DataOutputStream, socket: Socket) {
        try {
            if (!socket.isClosed) {
                Log.d(TAG, "sendData: Sending file: ${file.name}")

                dataOutputStream.writeLong(file.length())
                dataOutputStream.writeUTF(file.path)
                dataOutputStream.flush()

                val objectOutputStream = ObjectOutputStream(socket.getOutputStream())
                val bytes = ByteArray(file.length().toInt())

                BufferedInputStream(FileInputStream(file)).use { bis ->
                    bis.read(bytes, 0, bytes.size)
                }

                objectOutputStream.writeObject(bytes)
                objectOutputStream.flush()
                transferredData++
                val progress = (transferredData.toFloat() / totalData.toFloat() * 100).toInt()
                listener?.onFileSendSuccess(progress)

                if (progress == 100) {
                    Sockets.getSocket()?.let {
                        Log.d(TAG, "closeSocket: ${it.isConnected}")
                        if (it.isConnected)
                        {
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
    }*/
}
