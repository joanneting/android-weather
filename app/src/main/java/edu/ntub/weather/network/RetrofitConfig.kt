package edu.ntub.weather.network

import edu.ntub.weather.dto.Data
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException

object RetrofitConfig {
    private const val API_URL = "http://120.97.27.41"

    interface API {
        @GET("/json/weather.json")
        fun data(): Call<Data>
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getWeather(): Data? {
        // Create a very simple REST adapter which points the API.
        val retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        // Create an instance of our API interface.
        val api = retrofit.create(API::class.java)//建立API類別的物件

        // Create a call instance for looking up Retrofit contributors.
        val call = api.data()

        // Fetch and print a list of the contributors to the library.
        return call.execute().body()
    }//建立連線物件
}