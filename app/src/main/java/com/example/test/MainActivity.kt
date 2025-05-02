package com.example.test
import com.example.test.screens.*


import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
//import com.example.test.objects.TimerService
import com.example.test.objects.TokenManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Microphone Permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)

        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "main screen"
            ) {
                composable("login") { LoginScreen(navController) }
                composable("signup") { SignupScreen(navController) }
                composable("home") { HomeScreen() }
                composable("main screen") { MainScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("voice upload") { VoiceAuthScreen(navController) }
                composable("reset password") { ResetPasswordScreen(navController) }
//                composable("manage roles") {RoleManager(navController)}
                composable(
                    route = "manage_roles/{userName}",
                    arguments = listOf(navArgument("userName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("userName") ?: ""
                    RoleManager(navController, username)
                }
            }
//            SpeechToTextScreen()
        }
//        TimerService.start(this)
    }
}



