package com.example.websocketserver

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.UUID

/**
 * Websocketテストサーバー
 */
class TestServer(address: InetSocketAddress) : WebSocketServer(address) {

    interface WebsocketCallback {
        fun onMessageReceived(message: String)
    }

    private var callback: WebsocketCallback? = null
    private val connectionMap = mutableMapOf<InetSocketAddress, String>()

    fun setWebSocketCallback(callback: WebsocketCallback?) {
        this.callback = callback
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        callback?.onMessageReceived("new connection = ${conn.remoteSocketAddress}")
        Log.i("TestServer", "### onOpen ###")
        Log.i("TestServer", "new connection = ${conn.remoteSocketAddress}")

        val uuid = UUID.randomUUID().toString()
        connectionMap[conn.remoteSocketAddress] = uuid
        Log.i("TestServer", "connection = $connectionMap")
        broadcast("${uuid}が接続しました")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        try {
            callback?.onMessageReceived("close connection = ${conn?.remoteSocketAddress}")
            Log.i("TestServer", "### onClose ###")
            Log.i("TestServer", "close connection = ${conn?.remoteSocketAddress}")
            Log.i("TestServer", "code = $code")
            Log.i("TestServer", "reason = $reason")
            Log.i("TestServer", "remote = $remote")

            val uuid = conn?.remoteSocketAddress?.let { getUUID(it) }
            broadcast("${uuid}が切断しました")
            
            connectionMap.remove(conn?.remoteSocketAddress)
            Log.i("TestServer", "connection = $connectionMap")

        } catch (e: Exception) {
            Log.e("TestServer", "onClose error", e)
        }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        try {
            val uuid = conn?.remoteSocketAddress?.let { getUUID(it) }
            val receivedMessage = "$message ($uuid)"
            Log.i("TestServer", "### onMessage ###")
            Log.i("TestServer", receivedMessage)
            broadcast(receivedMessage)
            callback?.onMessageReceived(receivedMessage)
        } catch (e: Exception) {
            Log.e("TestServer", "onMessage error", e)
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        try {
            Log.i("TestServer", "### onError ###")
            Log.i("TestServer", ex?.stackTraceToString() ?: "stack trace is null")
            Log.i("TestServer", "error message = ${ex?.message ?: "error message is null"}")
        } catch (e: Exception) {
            Log.e("TestServer", "onError error", e)
        }
    }

    override fun onStart() {
        callback?.onMessageReceived("server started")
        Log.i("TestServer", "### onStart ###")
        Log.i("TestServer", "server started")
    }

    private fun getUUID(address: InetSocketAddress): String {
        return connectionMap[address] ?: ""
    }
}
