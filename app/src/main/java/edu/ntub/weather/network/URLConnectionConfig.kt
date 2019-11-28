package edu.ntub.weather.network

import com.google.gson.Gson
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

public class URLConnectionConfig {
    private val baseUrl = "http://127.0.0.1:8000"

    fun <T> get(apiUrl: String, returnType: Class<T>): T? {
        val url = URL(baseUrl + apiUrl)
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            val inputStream = urlConnection.inputStream
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            return gson.fromJson<T>(reader, returnType)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            urlConnection.disconnect()
        }

        return null
    }
}
