package com.example.websocketserver

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val serverStartButton by lazy { findViewById<Button>(R.id.button_start_server) }
    private val sendMessageButton by lazy { findViewById<Button>(R.id.btn_send_message) }
    private val scrollView by lazy { findViewById<ScrollView>(R.id.scrollView) }
    private val logTextView by lazy { findViewById<TextView>(R.id.logTextView) }

    private var server: TestServer? = null
    private var isServerStarted = false
    private val host = ""
    private val port = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setServer()

        serverStartButton.setOnClickListener {
            if (!isServerStarted) {
                Log.d("MainActivity", "onClick start server")
                startServer()
                isServerStarted = true
                serverStartButton.text = "stop"
            } else {
                Log.d("MainActivity", "onClick stop server")
                stopServer()
                isServerStarted = false
                serverStartButton.text = "start"
            }
        }

        sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun setServer() {
        server = TestServer(InetSocketAddress(host, port))
        server?.isReuseAddr = true
        server?.connectionLostTimeout = 10
        server?.setWebSocketCallback(object : TestServer.WebsocketCallback {
            override fun onMessageReceived(message: String) {
                // メッセージ受信時の処理
                Log.d("MainActivity", "received message = $message")
                printLog(message)
            }
        })
    }

    private fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            thread {
                try {
                    server?.start()
                } catch (e: Exception) {
                    Log.e("MainActivity", "server start error", e)
                }

                Log.d("MainActivity", "server started")
            }
        }
    }

    private fun stopServer() {
        try {
            server?.stop()
            server?.setWebSocketCallback(null)
        } catch (e: Exception) {
            Log.e("MainActivity", "server stop error", e)
        }

        Log.d("MainActivity", "server stopped")
    }

    private fun sendMessage() {
        val message = findViewById<EditText>(R.id.send_message).text.toString()
        server?.broadcast("[server]$message")
        Log.d("MainActivity", "send message = $message")
    }

    fun printLog(message: String) {
        Log.d("MainActivity", message)
        CoroutineScope(Dispatchers.Main).launch {
            logTextView.append(message)
            logTextView.append("\n")
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}