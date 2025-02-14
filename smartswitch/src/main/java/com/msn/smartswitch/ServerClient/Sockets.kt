package com.msn.smartswitch.ServerClient

import java.net.Socket

object Sockets {
    private var socket: Socket? = null

    @Synchronized
    fun getSocket(): Socket? {
        return socket
    }

    @Synchronized
    fun setSocket(socket: Socket) {
        Sockets.socket = socket
    }
}
