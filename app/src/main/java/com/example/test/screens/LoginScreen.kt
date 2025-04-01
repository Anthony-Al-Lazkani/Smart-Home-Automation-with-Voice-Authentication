package com.example.test.screens
import com.example.test.R

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.objects.TokenManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

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

val retrofit2 = Retrofit.Builder()
//    .baseUrl("http://192.168.1.12:8000/")
    .baseUrl("http://192.168.1.120:8000/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val authApiLogin = retrofit2.create(AuthApiLogin::class.java)


@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            // Logo Placeholder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "EchoControl", fontSize = 25.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Email Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = authApiLogin.signIn(
                                SignInRequest(username, password)
                            )

                            TokenManager.saveToken(context = context, token = response.token)

                            navController.navigate("main screen")
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "An error occurred"
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48F19)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Login", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Forgot your password?",
                fontSize = 12.sp,
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "or", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(15.dp))

            // Google Login Button
            Button(
                onClick = { /* Handle Google Sign-In */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48F19)),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                shape = RectangleShape,
                modifier = Modifier
                    .border(1.dp, Color(0xFFF48F19))
                    .height(50.dp)
                    .width(230.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF48F19)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google Sign-In",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Login with Google",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Signup Link
            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(style = SpanStyle(color = Color(0xFFFF9800))) {
                        append("Sign Up")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("signup") // Replace with your actual route
                }
            )
        }
    }
}
