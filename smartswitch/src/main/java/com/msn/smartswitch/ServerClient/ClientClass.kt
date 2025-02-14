package com.msn.smartswitch.ServerClient

import android.util.Log
import com.msn.smartswitch.Models.Utilities.Companion.PORT
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientClass internal constructor(
    private val address: InetAddress,
    private val connectionCallBack: ConnectionCallBack
) : Thread() {
    private val TAG = javaClass.simpleName
    private var socket: Socket = Socket()

    override fun run() {
        try {
            socket.let {
                if (it.isConnected) {
                    it.close()
                    Log.d(TAG, "run: ${it.close()}")
                }
            }

            val socketAddress = InetSocketAddress(address.hostAddress, PORT)
            socket.connect(socketAddress, 100000)  // 10-second timeout
            socket.reuseAddress = true

            Log.d(TAG, "Connected to ${address.hostAddress}")

            // Set the socket for communication
            Sockets.setSocket(socket)
            connectionCallBack.onSuccess()
            Log.d(TAG, "run: client started")

        } catch (e: Exception) {
            Log.d(TAG, "Client connection error: ${e.message}")
        }
    }
}
