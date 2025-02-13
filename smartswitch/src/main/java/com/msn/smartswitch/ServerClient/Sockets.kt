package com.ft.features.local_transfer.smart_switch.connection

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
