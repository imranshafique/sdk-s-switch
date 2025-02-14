package com.msn.smartswitch.ServerClient

import android.util.Log
import com.msn.smartswitch.Models.Utilities.Companion.PORT
import java.net.ServerSocket
import java.net.Socket

class ServerClass(private val connectionCallBack: ConnectionCallBack) : Thread() {
    private val TAG = javaClass.simpleName
    private var socket: Socket? = null
    private var serverSocket: ServerSocket? = null

    override fun run() {
        try {
            Log.d(TAG, "Server starting...")
            socket?.let {
                Log.d(TAG, "socket: ${it.isConnected}")
                if (it.isConnected)
                    it.close()
                Log.d(TAG, "close: ${it.isClosed}")

            }
            if (serverSocket != null)
            {
                if (serverSocket!!.isBound)
                {
                    serverSocket!!.close()
                }
            }
            serverSocket = ServerSocket(PORT)
            serverSocket?.reuseAddress = true

            socket = serverSocket?.accept()
            Log.d(TAG, "Server started and connected to ${socket?.inetAddress}")

            // Set socket for communication
            Sockets.setSocket(socket!!)
            Log.d(TAG, "run: server started")
            connectionCallBack.onSuccess()


        } catch (e: Exception) {
            Log.d(TAG, "Server exception: ${e.message}")
        }
    }
}
