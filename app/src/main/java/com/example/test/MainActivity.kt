package com.example.test
import com.example.test.screens.*


import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.test.api.RetrofitInstance
import com.example.test.notification.NotificationHandler
import com.example.test.objects.TokenManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request Microphone Permission
//        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
//
//        // Notification Initialization
//        NotificationHandler.createNotificationChannel(this)
//
//
//
//        setContent {
//            val navController = rememberNavController()
//            NavHost(
//                navController = navController,
//                startDestination = "main screen"
//            ) {
//                composable("login") { LoginScreen(navController) }
//                composable("signup") { SignupScreen(navController) }
//                composable("home") { HomeScreen() }
//                composable("main screen") { MainScreen(navController) }
//                composable("settings") { SettingsScreen(navController) }
//                composable("voice upload") { VoiceAuthScreen(navController) }
//                composable("reset password") { ResetPasswordScreen(navController) }
//                composable(
//                    route = "manage_roles/{userName}",
//                    arguments = listOf(navArgument("userName") { type = NavType.StringType })
//                ) { backStackEntry ->
//                    val username = backStackEntry.arguments?.getString("userName") ?: ""
//                    RoleManager(navController, username)
//                }
//            }
////            SpeechToTextScreen()
//        }
////        TimerService.start(this)
//    }
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Microphone Permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)

        // Notification Initialization
        NotificationHandler.createNotificationChannel(this)

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var isTokenValid by remember { mutableStateOf<Boolean?>(null) }

            val tokenApi = RetrofitInstance.getTokenValidationApi()

            LaunchedEffect(Unit) {
                val token = TokenManager.getToken(context)
                if (token.isNullOrEmpty()) {
                    isTokenValid = false
                } else {
                    try {
                        val response = tokenApi.validateToken("Bearer $token")
                        isTokenValid = response.isSuccessful && response.body()?.valid == true
                    } catch (e: Exception) {
                        isTokenValid = false
                    }
                }
            }

            // Show loading spinner while checking token
            if (isTokenValid == null) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05103A)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val startDestination = if (isTokenValid == true) "main screen" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") { LoginScreen(navController) }
                    composable("signup") { SignupScreen(navController) }
                    composable("home") { HomeScreen() }
                    composable("main screen") { MainScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("voice upload") { VoiceAuthScreen(navController) }
                    composable("reset password") { ResetPasswordScreen(navController) }
                    composable("logs") { LoggingScreen(navController) }
                    composable(
                        route = "manage_roles/{userName}",
                        arguments = listOf(navArgument("userName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("userName") ?: ""
                        RoleManager(navController, username)
                    }
                }
            }
        }
    }
}



