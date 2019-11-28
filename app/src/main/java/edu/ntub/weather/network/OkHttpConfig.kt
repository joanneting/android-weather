package edu.ntub.weather.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStreamReader


public class OkHttpConfig {
    private val client = OkHttpClient()
    private val baseUrl = "http://127.0.0.1:8000"

    fun <T> get(apiUrl: String, returnType: Class<T>): T? {
        val request = Request.Builder()
                .url(baseUrl + apiUrl)
                .build()

        client.newCall(request).execute().use { response ->
            try {
                val inputStream = response.body!!.byteStream()
                val reader = InputStreamReader(inputStream)
                val gson = Gson()

                return gson.fromJson<T>(reader, returnType)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }
}