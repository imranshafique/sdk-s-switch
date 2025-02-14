package com.msn.smartswitch.Transfer

import android.os.Environment
import android.util.Log
import com.msn.smartswitch.ServerClient.Sockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DataReceiverManger {

    interface ReceiverListener {
        fun onFileReceiveSuccess()
        fun onAllFileReceiveSuccess()
        fun onFileReceiveFailure(errorMessage: String)
        fun onConnectionError()
        fun onConnectionClosed()
        fun onFileReceiveProgress(progress: Int, totalBytesReceived: Long)
        fun onTotalCountReceived(count:Int, size:Long)
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
    private  var totalBytesToReceive: Long = 0L
    private var totalBytesReceived: Long = 0L

   fun startReceive() {
        Log.d(TAG, "startReceive: Attempting to receive files")

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
                totalBytesToReceive= dataInputStream?.readLong() ?: 0
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
            var progress:Int = 0
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
                Log.e(TAG, "receiveFile: Invalid file length")
                return
            }

            val filePath = dataInputStream?.readUTF() ?: ""
            if (filePath.isEmpty()) {
                Log.e(TAG, "receiveFile: File path is empty")
                return
            }

            val fileName = File(filePath).name
            val folderName = "SmartSwitchSDK"
            val folderPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                folderName
            )
            if (!folderPath.exists()) folderPath.mkdirs()

            if (fileName.isNotEmpty()) {
                var receivedFile = File(folderPath, fileName)
                var fileCounter = 1
                val baseFileName = fileName.substringBeforeLast(".") // Get file name without extension
                val fileExtension = fileName.substringAfterLast(".", "") // Get file extension

                while (receivedFile.exists()) {
                    val newFileName = "$baseFileName($fileCounter).$fileExtension"
                    receivedFile = File(folderPath, newFileName)
                    fileCounter++
                }
                val fileOutputStream = FileOutputStream(receivedFile)
                val bufferedOutputStream = BufferedOutputStream(fileOutputStream)

                val buffer = ByteArray(1024 * 4) // 4KB buffer (same as sender)
                var bytesRead: Int
                var totalBytesRead = 0L

                // Read file data in chunks until the full file length is received
                while (totalBytesRead < fileLength) {
                    val bytesToRead = minOf(buffer.size.toLong(), fileLength - totalBytesRead).toInt()
                    bytesRead = dataInputStream?.read(buffer, 0, bytesToRead) ?: -1
                    if (bytesRead == -1) break

                    bufferedOutputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    totalBytesReceived += bytesRead

                    // Progress calculation
                    progress = (totalBytesReceived.toFloat() / totalBytesToReceive * 100).toInt()
                    listener?.onFileReceiveProgress(progress,totalBytesReceived)
                    Log.d(TAG, "receiveFile: Receiving $fileName - $progress%")
                }

                bufferedOutputStream.flush()
                bufferedOutputStream.close()

                Log.d(TAG, "receiveFile: File received: $fileName, Size: $totalBytesRead bytes")

                listener?.onFileReceiveSuccess()
                filesReceived++

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
                Log.e(TAG, "receiveFile: Invalid file name")
                listener?.onFileReceiveFailure("Invalid file name")
            }
        } catch (e: IOException) {
            Log.e(TAG, "receiveFile: IOException: ${e.localizedMessage}")
            e.printStackTrace()
            listener?.onFileReceiveFailure("IOException: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "receiveFile: Exception: ${e.localizedMessage}")
            e.printStackTrace()
            listener?.onFileReceiveFailure("Exception: ${e.message}")
        }
    }
}
