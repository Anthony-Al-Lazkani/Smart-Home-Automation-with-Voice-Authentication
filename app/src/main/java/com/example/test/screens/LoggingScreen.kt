package com.example.test.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.api.LogsResponse
import com.example.test.api.RetrofitInstance
import com.example.test.objects.ThemeMode
import com.example.test.objects.TokenManager
import kotlinx.coroutines.launch

class LogsViewModel : ViewModel() {
    val logsApi = RetrofitInstance.getLogsApi()
    var logs by mutableStateOf<List<LogsResponse>>(emptyList())
        private set

    var errorMessage by mutableStateOf("")

    fun getAllLogs(token: String) {
        viewModelScope.launch {
            try {
                val response = logsApi.getLogs("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    logs = response.body()!!
                } else {
                    errorMessage = "Failed to fetch users: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage = ""
    }
}


@Composable
fun LoggingScreen(navController: NavController) {
    val viewModel: LogsViewModel = viewModel()
    val logs = viewModel.logs
    val errorMessage = viewModel.errorMessage
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val token = TokenManager.getToken(context)
        if (token != null) {
            viewModel.getAllLogs(token)
        } else {
            viewModel.clearError()
            Toast.makeText(context, "Missing token", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack()
                    },
                tint = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logs",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
        }

        LazyColumn {
            items(logs.size) { index ->
                val entry = logs[index]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp) // Small space between blocks
                ) {
                    CommandCard(entry)
                }
            }
        }

    }
}

@Composable
fun CommandCard(entry: LogsResponse) {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Remove shadow
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRowWithIcon(Icons.Default.Person, "User", entry.user)

            InfoRowWithIcon(
                Icons.Default.Settings, // or Icons.Default.Code
                "Command",
                entry.command.replace("_", " ").replaceFirstChar { it.uppercase() }
            )

            InfoRowWithIcon(
                when (entry.source.lowercase()) {
                    "manual" -> Icons.Default.TouchApp
                    "voice" -> Icons.Default.Mic
                    else -> Icons.Default.Input
                },
                "Source",
                entry.source.replaceFirstChar { it.uppercase() }
            )

            InfoRowWithIcon(Icons.Default.Schedule, "Time", entry.issued_at.toString())
        }
    }
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp), // Reduced space above and below the line
        color = Color.LightGray,
        thickness = 1.dp // Optional: thinner divider
    )
}

@Composable
fun InfoRowWithIcon(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF1976D2),
            modifier = Modifier
                .size(20.dp)
                .padding(end = 8.dp)
        )
        Text(
            text = "$label:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = Color.DarkGray,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 60.dp)
        )
    }
}