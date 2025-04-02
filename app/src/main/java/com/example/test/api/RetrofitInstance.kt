package com.example.test.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val baseURL = "http://192.168.1.12:8000/"

    private fun getInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val dataAPI : NodeMCUAPI = getInstance().create(NodeMCUAPI::class.java)

    fun getAuthAPILogin(): AuthApiLogin {
        return getInstance().create(AuthApiLogin::class.java)
    }

    fun getAuthAPIRegister(): AuthApiRegister {
        return getInstance().create(AuthApiRegister::class.java)
    }

    fun getVoiceAuthUpload(): VoiceAuthApi {
        return getInstance().create(VoiceAuthApi::class.java)
    }

    fun getVoiceAuthentication(): VoiceAuthenticationApi {
        return getInstance().create(VoiceAuthenticationApi::class.java)
    }
}