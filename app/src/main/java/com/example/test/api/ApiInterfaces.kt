package com.example.test.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Login
data class SignInRequest(
    val username: String,
    val password: String
)

data class SignInResponse(
    val message : String,
    val token : String
)

interface AuthApiLogin {
    @POST("auth/login")
    suspend fun signIn(@Body request: SignInRequest): SignInResponse
}


// Register
data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirm_password: String
)


data class SignUpResponse(val message: String, val token: String)


interface AuthApiRegister {
    @POST("auth/register")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse
}



// Voice Upload
interface VoiceAuthApi {
    @Multipart
    @POST("/voice-auth/voice-upload")
    suspend fun uploadVoice(
        @Part("token") token: RequestBody,
        @Part audio: MultipartBody.Part
    ): retrofit2.Response<ResponseBody>
}


// Voice Authentication
data class SpeechRecognitionApiResponse (
    val message : String,
    val isAuthenticated : Boolean
)

interface VoiceAuthenticationApi {
    @Multipart
    @POST("/voice-auth/voice-authentication")
    suspend fun authenticateVoice(
        @Part("token") token: RequestBody,
        @Part audio: MultipartBody.Part
    ): SpeechRecognitionApiResponse
}