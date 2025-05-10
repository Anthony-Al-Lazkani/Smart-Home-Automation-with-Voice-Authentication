package com.example.test.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.test.api.RetrofitInstance
import com.example.test.api.TokenBody
import com.example.test.objects.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.example.test.ACTION_YES" -> {
                Toast.makeText(context, "YES clicked", Toast.LENGTH_SHORT).show()
                val token = TokenManager.getToken(context)
                if (token != null) {
                    val controlApi = RetrofitInstance.getManualControlApi()
                    val tokenBody = TokenBody(token)

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                controlApi.controlDevice("door_unlock", TokenBody(token))
                            }
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "API error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            "com.example.test.ACTION_NO" -> {
                Toast.makeText(context, "NO clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}