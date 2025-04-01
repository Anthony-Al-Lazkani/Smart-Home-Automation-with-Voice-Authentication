package com.example.test.api

import retrofit2.http.Body
import retrofit2.http.POST

data class CommandRequest (
        val command : String
        )

interface NodeMCUAPI {
    @POST("nlp/command")
    suspend fun sendDataToNodeMCU(@Body request: CommandRequest)
}