package com.example.test.screens

import com.example.test.R
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.objects.TokenManager
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.test.api.RetrofitInstance
import retrofit2.HttpException
import org.json.JSONObject
import com.example.test.api.ResetPasswordRequest


@Composable
fun ResetPasswordScreen(navController: NavController) {
    var old_password by remember { mutableStateOf("") }
    var new_password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Api Instance
    val resetPasswordAPI = RetrofitInstance.getResetPasswordApi()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(Color(0xFF05103A)),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Reset Password",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )

            // Logo Placeholder
            Image(
                painter = painterResource(id = R.drawable.home_logo),
                contentDescription = "Home Logo",
                modifier = Modifier.size(200.dp)
                    .padding(top=15.dp),
            )
            Text(
                text = "Vira",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Old Password Field
            OutlinedTextField(
                value = old_password,
                onValueChange = { old_password = it },
                label = { Text("Old Password") },
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password Field
            OutlinedTextField(
                value = new_password,
                onValueChange = { new_password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        loading = true
                        try {
                            val token = TokenManager.getToken(context)
                            if (token == null) {
                                Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            val response = resetPasswordAPI.resetPassword(
                                ResetPasswordRequest(token, old_password, new_password)
                            )
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                            navController.navigate("settings")
                        }catch (e: HttpException) {
                            try {
                                // Extract the error message from the error body
                                val errorBody = e.response()?.errorBody()?.string()
                                val jsonObject = errorBody?.let {
                                    JSONObject(it)
                                } ?: JSONObject() // Fallback to an empty JSONObject if errorBody is null

                                val errorMessage = jsonObject.optString("detail", "An error occurred")

                                // Show error as a toast message, ensure errorMessage is non-null
                                Toast.makeText(context, errorMessage ?: "An error occurred", Toast.LENGTH_LONG).show()
                            } catch (jsonException: Exception) {
                                // If JSON parsing fails, show a generic message
                                Toast.makeText(context, "An error occurred while parsing the response.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "An error occurred"
                            Toast.makeText(context, "An unknown error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            loading = false
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FCF)),
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Change Password", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

