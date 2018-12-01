package jp.qais.coinz

import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFileTask(private val caller: DownloadCompleteListener): AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg urls: String): String = try {
        loadFileFromNetwork(urls[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection."
    }

    private fun loadFileFromNetwork(url: String): String {
        val stream: InputStream = downloadUrl(url)

        // Read input from stream, build result as a string
        stream.bufferedReader().use {
            return it.readText()
        }
    }

    // Given a string representation of a URL, sets up a connection and gets an input stream
    @Throws(IOException::class)
    private fun downloadUrl(url: String): InputStream {
        val conn = URL(url).openConnection() as HttpURLConnection

        conn.readTimeout = 10 * 1000 // 10s
        conn.connectTimeout = 15 * 1000 // 15s
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect() // perform the request

        return conn.inputStream
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        caller.downloadComplete(result)
    }
}