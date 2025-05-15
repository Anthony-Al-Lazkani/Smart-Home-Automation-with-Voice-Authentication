package com.example.test.screens
import android.widget.Toast
import com.example.test.objects.TokenManager

import com.example.test.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import com.example.test.api.RetrofitInstance
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import com.example.test.api.SignUpRequest


@Composable
fun SignupScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm_password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authApiRegister = RetrofitInstance.getAuthAPIRegister()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05103A)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { navController.popBackStack() }, // Navigate back
            modifier = Modifier
                .align(Alignment.TopStart) // Align to top left
                .padding(16.dp) // Padding for spacing
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, // Use Material Back Arrow
                contentDescription = "Back",
                tint = Color.White // Make it white
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
                .fillMaxWidth()
                .fillMaxHeight()

        ) {
            Text(
                text = "Sign Up & Step Into the Future",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()  // Ensures the text block takes the full width
                    .wrapContentWidth(Alignment.CenterHorizontally) // Centers the text
                    .padding(start=35.dp, top=20.dp)
            )

//            Text(
//                text = "Unlock the Future of Living 🚀",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color(0xFF2A6FCF), // A cool accent color
//                modifier = Modifier
//                    .padding(top = 80.dp, bottom = 16.dp)
//            )


//            Image(
//                painter = painterResource(id = R.drawable.home_logo),
//                contentDescription = "Home Logo",
//                modifier = Modifier.size(200.dp)
//                    .padding(top=15.dp),
//            )
//            Text(
//                text = "Vira",
//                fontSize = 42.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                modifier = Modifier.padding(bottom = 15.dp)
//            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 15.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_logo),
                    contentDescription = "Home Logo",
                    modifier = Modifier.size(100.dp) // Adjust size if needed
                )
                Spacer(modifier = Modifier.width(8.dp)) // Adds spacing between image and text
                Text(
                    text = "Vira",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(25.dp))



            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                textStyle = TextStyle(color = Color.White),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = confirm_password,
                onValueChange = { confirm_password = it },
                label = { Text("Confirm Password") },
                textStyle = TextStyle(color = Color.White),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        loading = true
                        try {
                            val response = authApiRegister.signUp(
                                SignUpRequest(username, email, password, confirm_password)
                            )

                            TokenManager.saveToken(context = context, token = response.token)

                            navController.navigate("voice upload") {
                                popUpTo("signup") { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: HttpException) {
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
                    Text(text = "Sign Up", fontSize = 18.sp)
                }
            }
            Text(
                text = "By signing up, you agree to our Terms & Privacy Policy.",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { /* Handle terms click if needed */ }
            )
        }
    }
}
