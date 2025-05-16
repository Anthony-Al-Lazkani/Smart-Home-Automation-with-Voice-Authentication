package com.example.test.screens

import android.media.MediaRecorder
import android.widget.Toast
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.api.RetrofitInstance
import com.example.test.objects.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
)

@Composable
fun MainScreen(navController: NavController) {
    val items = listOf(
        BottomNavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = true,
        )
    )

    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    var isMicFilled by rememberSaveable { mutableStateOf(false) }


    val context = LocalContext.current
    val token = TokenManager.getToken(context)
    val coroutineScope = rememberCoroutineScope()

    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    val tempFile = remember { File.createTempFile("temp_audio", ".aac", context.cacheDir) }
    var isRecording by remember { mutableStateOf(false) }

    // Api Instance
    val voiceAuthentication = RetrofitInstance.getVoiceAuthentication()

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
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    fun sendRecording() {
        stopRecording()
        coroutineScope.launch {
            val requestFile = tempFile.asRequestBody("audio/aac".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", "recorded.aac", requestFile)

            val tokenPart = token?.toRequestBody("text/plain".toMediaTypeOrNull())
                ?: // Handle the null case (e.g., show an error, or use a default token)
                "".toRequestBody("text/plain".toMediaTypeOrNull()) // Example fallback

            try {
                val response = voiceAuthentication.authenticateVoice(tokenPart, audioPart)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Authentication Successful", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val jsonObject = errorBody?.let { JSONObject(it) } ?: JSONObject()
                    val errorMessage = jsonObject.optString("detail", "Authentication failed")

                    Toast.makeText(context, "Authentication Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val jsonObject = errorBody?.let { JSONObject(it) } ?: JSONObject()
                    val errorMessage = jsonObject.optString("detail", "Authentication failed")

                    Toast.makeText(context, "Authentication Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                } catch (jsonException: Exception) {
                    Toast.makeText(context, "An error occurred while parsing the error response.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "An unknown error occurred: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
//                    containerColor = Color(0xFF2A6FCF)
                    containerColor = Color.White
                ) {
                    val selectedColor = Color(0xFF2A6FCF)
                    val unselectedColor = Color(0xFF05103A)
                    NavigationBarItem(
                        selected = selectedItemIndex == 0,
                        onClick = { selectedItemIndex = 0 },
                        label = { Text(text = items[0].title) },
                        icon = {
                            Icon(
                                imageVector = if (selectedItemIndex == 0) items[0].selectedIcon else items[0].unselectedIcon,
                                contentDescription = items[0].title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Middle Mic Button (properly centered)
//                    Box(
//                        modifier = Modifier
//                            .weight(1f),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        IconButton(
//                            onClick = {
//                                // Toggle recording state
//                                toggleRecording()
//
//                                // Send the recording if recording is stopped
//                                if (!isRecording) {
//                                    sendRecording()
//                                }
//                            },
//                            modifier = Modifier
//                                .size(56.dp)
//                                .clip(CircleShape)
//                        ) {
//                            Icon(
//                                imageVector = if (isMicFilled) Icons.Filled.Mic else Icons.Outlined.Mic,
//                                contentDescription = "Mic"
//                            )
//                        }
//                    }

                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Wave animation
                        if (isRecording) {
                            val infiniteTransition = rememberInfiniteTransition(label = "mic_wave")

                            val ripples = listOf(0, 300, 600) // staggered delays in ms
                            ripples.forEach { delayMillis ->
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 2.5f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, delayMillis = delayMillis, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ), label = "ripple_scale_$delayMillis"
                                )

                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, delayMillis = delayMillis, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ), label = "ripple_alpha_$delayMillis"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .scale(scale)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2A6FCF).copy(alpha = alpha))
                                        .align(Alignment.Center)
                                )
                            }
                        }


                        IconButton(
                            onClick = {
                                toggleRecording()
                                isMicFilled = !isMicFilled
                                if (!isRecording) {
                                    sendRecording()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMicFilled || isRecording) Icons.Filled.Mic else Icons.Outlined.Mic,
                                contentDescription = "Mic",
                                tint = if (isRecording) Color.White else Color.Unspecified
                            )

                        }
                    }

                    NavigationBarItem(
                        selected = selectedItemIndex == 1,
                        onClick = { selectedItemIndex = 1 },
                        label = { Text(text = items[1].title) },
                        icon = {
                            Icon(
                                imageVector = if (selectedItemIndex == 1) items[1].selectedIcon else items[1].unselectedIcon,
                                contentDescription = items[1].title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        ) { innerPadding ->
            navController
            ContentScreen(modifier = Modifier.padding(innerPadding), selectedItemIndex, navController)
        }
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedItemIndex: Int, navController: NavController) {
    when (selectedItemIndex) {
        0 -> HomeScreen()
        1 -> SettingsScreen(navController)
    }
}
