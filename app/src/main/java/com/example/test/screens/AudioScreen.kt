package com.example.test.screens

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.test.objects.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File



//@Composable
//fun AudioScreen() {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val token = TokenManager.getToken(context)
//
//    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
//    val tempFile = remember { File.createTempFile("temp_audio", ".aac", context.cacheDir) }
//    var isRecording by remember { mutableStateOf(false) }
//
//    fun startRecording() {
//        mediaRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setOutputFile(tempFile.absolutePath)
//            prepare()
//            start()
//        }
//        isRecording = true
//    }
//
//    fun stopAndSendRecording() {
//        mediaRecorder?.apply {
//            stop()
//            release()
//        }
//        mediaRecorder = null
//        isRecording = false
//
//        // Send file via Retrofit
//        coroutineScope.launch {
//            val requestFile = tempFile.asRequestBody("audio/aac".toMediaTypeOrNull())
//            val audioPart = MultipartBody.Part.createFormData("audio", "recorded.aac", requestFile)
////            val tokenPart = token?.toRequestBody("text/plain".toMediaTypeOrNull())
////                ?: "".toRequestBody("text/plain".toMediaTypeOrNull())
//            val tokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJsYXprYW5pIiwiZXhwIjoxNzQyNjQ0NzcxfQ.D7QMCaNsEU9HY1Ngoxs6K1BiqvkqvVcJrJZ4Kscb6tI"
//                .toRequestBody("text/plain".toMediaTypeOrNull())
//
//
//            val response = try {
//                ApiService.create().uploadVoice(tokenPart, audioPart)
//            } catch (e: Exception) {
//                Log.e("Upload", "Error: ${e.message}")
//                return@launch
//            }
//
//            if (response.isSuccessful) {
//                Log.d("Upload", "Upload successful: ${response.body()?.string()}")
//            } else {
//                Log.e("Upload", "Upload failed: ${response.errorBody()?.string()}")
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(onClick = { startRecording() }, enabled = !isRecording) {
//            Text("Start Recording")
//        }
//        Spacer(modifier = Modifier.height(10.dp))
//
//        Button(onClick = { stopAndSendRecording() }, enabled = isRecording) {
//            Text("Stop & Send")
//        }
//    }
//}

@Composable
fun AudioScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val token = TokenManager.getToken(context)

    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    val tempFile = remember { File.createTempFile("temp_audio", ".aac", context.cacheDir) }

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempFile.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        isPaused = false
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        isPaused = false
    }

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    fun pauseRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            isPaused = true
        }
    }

    fun resumeRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            isPaused = false
        }
    }


    fun togglePauseResume() {
        if (isPaused) {
            resumeRecording()
        } else {
            pauseRecording()
        }
    }



    fun cancelRecording() {
        stopRecording()
        tempFile.delete()
        Log.d("Audio", "Recording canceled")
    }

    fun playRecording() {
        if (!tempFile.exists()) return
        mediaPlayer = MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                isPlaying = false
            }
        }
        isPlaying = true
    }

    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }

    fun sendRecording() {
        stopRecording()
        coroutineScope.launch {
            val requestFile = tempFile.asRequestBody("audio/aac".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", "recorded.aac", requestFile)
            val tokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJsYXprYW5pIiwiZXhwIjoxNzQyNjQ4OTI5fQ.n-EEe6nTSUbY6CA6C_I0VrGZFx6sgOyOXjmGub3rHwI".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = try {
                ApiService.create().uploadVoice(tokenPart, audioPart)
            } catch (e: Exception) {
                Log.e("Upload", "Error: ${e.message}")
                return@launch
            }

            if (response.isSuccessful) {
                Log.d("Upload", "Upload successful: ${response.body()?.string()}")
            } else {
                Log.e("Upload", "Upload failed: ${response.errorBody()?.string()}")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { toggleRecording() }) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { togglePauseResume() }, enabled = isRecording) {
            Text(if (isPaused) "Resume" else "Pause")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { cancelRecording() }, enabled = isRecording || tempFile.exists()) {
            Text("Cancel")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { playRecording() }, enabled = tempFile.exists() && !isPlaying) {
            Text("Play Recording")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { stopPlayback() }, enabled = isPlaying) {
            Text("Stop Playback")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { sendRecording() }, enabled = tempFile.exists()) {
            Text("Send Recording")
        }
    }
}

// Retrofit API Service
interface ApiService {
    @Multipart
    @POST("/voice-auth/voice-upload")
    suspend fun uploadVoice(
        @Part("token") token: RequestBody,
        @Part audio: MultipartBody.Part
    ): retrofit2.Response<ResponseBody>

    companion object {
        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl("http://192.168.1.12:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

