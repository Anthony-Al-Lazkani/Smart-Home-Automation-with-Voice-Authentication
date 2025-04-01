package com.example.test.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.test.api.CommandRequest
import com.example.test.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("RememberReturnType")
@Composable
fun SpeechToTextScreen() {
    val dataAPI = RetrofitInstance.dataAPI
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
    }

    var recognizedText by remember { mutableStateOf("Tap the button and start speaking...") }
    var isListening by remember { mutableStateOf(false) }

    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        recognizedText = "Speech Recognition is not available on this device"
    }

    val speechListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val command = matches?.firstOrNull() ?: "Couldn't recognize speech"
            recognizedText = command
            isListening = false

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val request = CommandRequest(command = command)
                    dataAPI.sendDataToNodeMCU(request)
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Failed to send request: ${e.message}")
                }
            }
        }

        override fun onError(error: Int) {
            recognizedText = "Error: $error"
            isListening = false
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() { isListening = true }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { isListening = false }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(speechListener)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = recognizedText, modifier = Modifier.padding(16.dp))

        Button(
            onClick = { speechRecognizer.startListening(speechIntent) },
            enabled = !isListening
        ) {
            Text(if (isListening) "Listening..." else "Start Speech Recognition")
        }
    }
}
