package com.example.websocketserver

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * Websocketテストサーバー
 */
class TestServer(address: InetSocketAddress) : WebSocketServer(address) {

    interface WebsocketCallback {
        fun onMessageReceived(message: String)
    }

    private var callback: WebsocketCallback? = null

    fun setWebSocketCallback(callback: WebsocketCallback?) {
        this.callback = callback
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.i("TestServer", "### onOpen ###")
        Log.i("TestServer", "new connection = ${conn.remoteSocketAddress}")

        // TODO: クライアント識別子を取得or生成する
        conn.send("接続しました")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        try {
            Log.i("TestServer", "### onClose ###")
            Log.i("TestServer", "close connection = ${conn?.remoteSocketAddress}")
            Log.i("TestServer", "code = $code")
            Log.i("TestServer", "reason = $reason")
            Log.i("TestServer", "remote = $remote")
        } catch (e: Exception) {
            Log.e("TestServer", "onClose error", e)
        }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        try {
            Log.i("TestServer", "### onMessage ###")
            Log.i("TestServer", "message = $message")
            callback?.onMessageReceived(message ?: "")
            conn?.send("メッセージを受け取りました = ${message ?: ""}")
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
        Log.i("TestServer", "### onStart ###")
        Log.i("TestServer", "server started")
    }
}
