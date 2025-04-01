package com.example.test.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
//    private const val baseURL = "http://192.168.1.39/"
//    private const val baseURL = "http://192.168.0.122/"
//    private const val baseURL = "http://192.168.1.12:8000/"
    private const val baseURL = "http://192.168.1.120:8000/"

    private fun getInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val dataAPI : NodeMCUAPI = getInstance().create(NodeMCUAPI::class.java)
}