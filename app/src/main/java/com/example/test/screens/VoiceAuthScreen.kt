package com.example.test.screens

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.R
import com.example.test.objects.TokenManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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


@Composable
fun VoiceAuthScreen(navController: NavController,modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val token = TokenManager.getToken(context)
    var progress by remember { mutableFloatStateOf(0f) }
    var isComplete by remember { mutableStateOf(false) }

    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    val tempFile = remember { File.createTempFile("temp_audio", ".aac", context.cacheDir) }

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var cancelSignal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Start recording
    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempFile.absolutePath)
            setAudioSamplingRate(16000) // Use 16kHz or higher
            setAudioEncodingBitRate(128000) // Higher bitrate for better quality
            prepare()
            start()
        }
        isRecording = true
        isPaused = false
    }

    // Stop recording
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        isPaused = false
    }

    // Toggle recording
    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    // Pause recording
    fun pauseRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            isPaused = true
        }
    }

    // Resume recording
    fun resumeRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            isPaused = false
        }
    }

    // Cancel recording and delete temp file
    fun cancelRecording() {
        stopRecording()
        tempFile.delete()
        Log.d("Audio", "Recording canceled")
        isPaused = false
        isRecording = false
        isComplete = false
        isPlaying = false
        progress = 0f
        cancelSignal = !cancelSignal
    }

    // Play recording
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

    // Stop playback
    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }

    // Send recording to server
    fun sendRecording() {
        stopRecording()
        coroutineScope.launch {
            val requestFile = tempFile.asRequestBody("audio/aac".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", "recorded.aac", requestFile)
//            val tokenPart = token.toRequestBody("text/plain".toMediaTypeOrNull())

            val tokenPart = token?.toRequestBody("text/plain".toMediaTypeOrNull())
                ?: // Handle the null case (e.g., show an error, or use a default token)
                "".toRequestBody("text/plain".toMediaTypeOrNull()) // Example fallback

            val response = try {
                VoiceAuthApiService.create().uploadVoice(tokenPart, audioPart)
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

    // Box layout for the VoiceAuthScreen
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF05103A)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Auth", color = Color.White, fontSize = 32.sp,
                modifier = Modifier.padding(top = 40.dp)
            )
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = "Please say a phrase clearly after clicking the microphone. For example 'Hello, my name is john and I am a student at USJ.'",
                color = Color.White, fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(50.dp))
            Box(contentAlignment = Alignment.Center) {
                MicButton(isRecording, isPaused, progress) {
                    if (isRecording) {
                        pauseRecording() // Pause if recording
                    } else {
                        toggleRecording() // Start or stop recording
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(Color(0xFFFF0000), R.drawable.xsymbole, isRecording || isPaused || isComplete) { cancelRecording() }
                ActionButton(Color(0xFF3B3B3B), if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon, isComplete) {
                    if (isPlaying) stopPlayback() else playRecording()
                }
                ActionButton(Color.Green, R.drawable.tick_icon, isComplete) {
                    sendRecording()
                    navController.navigate("main screen")
                }
            }
        }
    }

    LaunchedEffect(isRecording, cancelSignal) {
        if (isRecording) {
            for (i in 0..100) {
                progress = i / 100f
                delay(100)

                // new lines added
                if (!isRecording) {
                    progress = 0f
                    return@LaunchedEffect
                }
            }
            stopRecording()
            isComplete = true
        }
    }
}


@Composable
fun ActionButton(bgColor: Color, icon: Int, isEnabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(55.dp)
            .background(if (isEnabled) bgColor else bgColor.copy(alpha = 0.3f), shape = CircleShape)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(30.dp))
    }
}


@Composable
fun MicButton(isRecording: Boolean, isPaused: Boolean, progress: Float, onStartRecording: () -> Unit) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .background(Color(0xFF101C43), shape = CircleShape)
            .clickable { onStartRecording() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            drawArc(
                color = Color.Blue,
                startAngle = -90f,
                sweepAngle = progress * 360f, // Progress controls the sweep of the arc
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Image(painter = painterResource(id = R.drawable.mic_record), contentDescription = "Microphone", modifier = Modifier.size(50.dp))
    }
}

interface VoiceAuthApiService {
    @Multipart
    @POST("/voice-auth/voice-upload")
    suspend fun uploadVoice(
        @Part("token") token: RequestBody,
        @Part audio: MultipartBody.Part
    ): retrofit2.Response<ResponseBody>

    companion object {
        fun create(): VoiceAuthApiService {
            return Retrofit.Builder()
//                .baseUrl("http://192.168.1.12:8000")
                .baseUrl("http://192.168.1.120:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VoiceAuthApiService::class.java)
        }
    }
}