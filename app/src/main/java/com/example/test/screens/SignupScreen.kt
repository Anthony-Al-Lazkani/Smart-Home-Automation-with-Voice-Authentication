package com.example.test.screens
import com.example.test.objects.TokenManager

import com.example.test.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST



data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirm_password: String
)


data class SignUpResponse(val message: String, val token: String)


interface AuthApi {
    @POST("auth/register")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse
}


val retrofit = Retrofit.Builder()
//    .baseUrl("http://192.168.1.12:8000/")
    .baseUrl("http://192.168.1.120:8000/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val authApi = retrofit.create(AuthApi::class.java)

@Composable
fun SignupScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm_password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val apiResponse by remember { mutableStateOf("") }
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
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirm_password,
                onValueChange = { confirm_password = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

//            if (errorMessage.isNotEmpty()) {
//                Text(text = errorMessage, color = Color.Red)
//            }
//
//            if (apiResponse.isNotEmpty()) {
//                Text(text = apiResponse)
//            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = authApi.signUp(
                                SignUpRequest(username, email, password, confirm_password)
                            )

                            TokenManager.saveToken(context = context, token = response.token)

                            navController.navigate("voice upload")
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "An error occurred"
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48F19)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign Up", fontSize = 18.sp)
            }
        }
    }
}
