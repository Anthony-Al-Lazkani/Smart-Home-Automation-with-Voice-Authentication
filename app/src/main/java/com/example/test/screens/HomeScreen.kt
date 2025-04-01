package com.example.test.screens
import com.example.test.objects.TokenManager

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isAuthenticated = TokenManager.isAuthenticated(context)

    if (isAuthenticated) {
        Text(text = "authenticated")
    } else {
        Text(text = "unauthenticated")
    }
}