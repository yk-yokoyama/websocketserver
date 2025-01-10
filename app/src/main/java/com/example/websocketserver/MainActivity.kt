package com.example.websocketserver

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var server: TestServer? = null
    private var isServerStarted = false
    private val host = ""
    private val port = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val serverStartButton = findViewById<Button>(R.id.button_start_server)

        serverStartButton.setOnClickListener {
            if (!isServerStarted) {
                startServer()
                isServerStarted = true
                serverStartButton.text = "stop"
            } else {
                stopServer()
                isServerStarted = false
                serverStartButton.text = "start"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun startServer() {
        CoroutineScope(Dispatchers.Main).launch {
            server = TestServer(InetSocketAddress(host, port))
            server?.setWebSocketCallback(object : TestServer.WebsocketCallback {
                override fun onMessageReceived(message: String) {
                    // メッセージ受信時の処理
                    Log.d("MainActivity", "received message = $message")
                }
            })

            thread {
                server?.connectionLostTimeout = 10
                server?.start()
                Log.d("MainActivity", "server started")
            }
        }
    }

    private fun stopServer() {
        server?.stop()
        server?.setWebSocketCallback(null)
        Log.d("MainActivity", "server stopped")
    }
}