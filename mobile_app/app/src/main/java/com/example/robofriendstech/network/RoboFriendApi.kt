package com.example.robofriendstech.network

import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ScannedTarget(
    val scheme: String,
    val host: String,
    val port: Int,
    val token: String,
) {
    val id: String get() = "$host:$port"
}

data class ConnectionInfo(
    val robotName: String,
    val jetsonIp: String,
    val connectedAt: String,
    val clientIp: String,
)

/** The QR code encodes the robot's welcome page (`http://<jetson-ip>:<port>/?token=...`). */
fun parseScannedUrl(rawValue: String): ScannedTarget {
    val uri = Uri.parse(rawValue)
    val token = uri.getQueryParameter("token")
        ?: throw IllegalArgumentException("Kod QR nie zawiera tokenu połączenia.")
    val host = uri.host
        ?: throw IllegalArgumentException("Kod QR nie zawiera prawidłowego adresu.")
    val scheme = uri.scheme ?: "http"
    val port = if (uri.port != -1) uri.port else 80
    return ScannedTarget(scheme, host, port, token)
}

/**
 * Hits the JSON sibling of the page the QR code points at, so the app gets
 * structured connection info back instead of an HTML page meant for a browser.
 */
suspend fun fetchConnectionInfo(target: ScannedTarget): ConnectionInfo = withContext(Dispatchers.IO) {
    val apiUrl = URL("${target.scheme}://${target.host}:${target.port}/api/connect?token=${target.token}")
    val connection = apiUrl.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 5000
    connection.readTimeout = 5000
    try {
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("Robot odrzucił połączenie (błąd ${connection.responseCode}).")
        }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        ConnectionInfo(
            robotName = json.optString("robot_name", "RoboFriend"),
            jetsonIp = json.optString("jetson_ip", target.host),
            connectedAt = json.optString("connected_at", ""),
            clientIp = json.optString("client_ip", ""),
        )
    } finally {
        connection.disconnect()
    }
}
