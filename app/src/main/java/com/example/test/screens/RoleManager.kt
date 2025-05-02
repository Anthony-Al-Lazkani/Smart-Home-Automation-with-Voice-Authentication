package com.example.test.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.test.api.RetrofitInstance
import com.example.test.api.UserResponse
import com.example.test.objects.ThemeMode
import com.example.test.objects.TokenManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import com.example.test.api.ChangeRoleRequest
import org.json.JSONObject
import retrofit2.HttpException

class UserViewModel : ViewModel() {
    val userApi = RetrofitInstance.getUserApi()
    var users by mutableStateOf<List<UserResponse>>(emptyList())
        private set

    var errorMessage by mutableStateOf("")

    fun getAllUsers(token: String) {
        viewModelScope.launch {
            try {
                val response = userApi.getAllUsers("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    users = response.body()!!
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



//data class User(
//    val username: String,
//    val email: String,
//    val role: String
//)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManager(navController: NavController, userName: String) {
    val context = LocalContext.current
    val themeMode = remember { ThemeMode.getInstance(context) }
//    val users = listOf(
//        User(username = "lazkani",email = "lazkani@gmail.com" ,role = "admin"),
//        User(username = "mathieu",email = "mathieu@gmail.com" ,role = "guest"),
//    )

    val viewModel: UserViewModel = viewModel()
    val users = viewModel.users
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        val token = TokenManager.getToken(context)
        if (token != null) {
            viewModel.getAllUsers(token)
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

    // Function to handle role change
    fun handleRoleChange(userName: String, newRole: String) {
        // Your logic to update the user role
        println("User $userName role changed to $newRole")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(themeMode.primary)) // Dark Blue background
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            IconButton(onClick = { navController.navigate("settings") }) { // Use navController to pop the current screen
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(themeMode.fontColor)  // Set the color to match the theme
                )
            }
        }

        Text(
            text = "Manage Roles",
            fontSize = 24.sp,
            color = Color(themeMode.fontColor),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp), // MORE SPACE BETWEEN CARDS
            contentPadding = PaddingValues(vertical = 16.dp) // ADD SPACE TOP AND BOTTOM
        ) {
            items(users) { user ->
                val isCurrentUser = user.username == userName // from nav argument
//                UserCard(user = user, isCurrentUser, onRoleChange = { newRole ->
//                    handleRoleChange(user.username, newRole)
//                })
                UserCard(
                    user = UserResponse(username = user.username, email = user.email, role = user.role),
                    isCurrentUser,
                    onRoleChange = { newRole ->
                        println("User ${user.username} role changed to $newRole")
                        // TODO: Call API to update role
                    },
                    onSuccess = {
                        val token = TokenManager.getToken(context)
                        if (token != null) {
                            viewModel.getAllUsers(token)
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(user: UserResponse, isCurrentUser: Boolean, onRoleChange: (String) -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val permissionApi = RetrofitInstance.getPermissionApi()
    val themeMode = remember { ThemeMode.getInstance(context) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(user.role) }
    val token = TokenManager.getToken(context) ?: ""
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(themeMode.secondary)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = user.username,
                    fontSize = 18.sp,
                    color = Color(themeMode.fontColor),
                    fontWeight = FontWeight.Bold
                )
                if (isCurrentUser) {
                    Text(
                        text = "Current User",
                        fontSize = 12.sp,
                        color = Color.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Text(
                    text = user.role.replaceFirstChar {it.uppercase()},
                    color = Color.White
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = user.username,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                var expanded by remember { mutableStateOf(false) }
                val roleOptions = listOf("Admin", "Family", "User", "Guest")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roleOptions.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            loading = true
                            try {
                                val role = selectedRole.replaceFirstChar { it.lowercase() }
                                val response = permissionApi.updateUserRole(
                                    username = user.username,
                                    request = ChangeRoleRequest(token = token, role = role)
                                )

                                if (response.isSuccessful) {
                                    val message = response.body()?.message

                                    if (message != null) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    onSuccess()
                                } else {
                                    // Parse backend's error message from the "detail" field
                                    val errorBody = response.errorBody()?.string()
                                    val jsonObject = errorBody?.let { JSONObject(it) }
                                    val errorMessage = jsonObject?.optString("detail", "Unknown error occurred")

                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    selectedRole = user.role
                                }
                            }catch (e: HttpException) {
                                try {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    val jsonObject = errorBody?.let { JSONObject(it) } ?: JSONObject()
                                    val errorMessage = jsonObject.optString("detail", "An error occurred")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                } catch (jsonException: Exception) {
                                    Toast.makeText(context, "An error occurred while parsing the response.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "An unknown error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                loading = false
                            }
                        }
                        onRoleChange(selectedRole)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5) // Blue color
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Change Role", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color =Color.Black )
                }
            }
        )
    }
}


//@Preview(showBackground = true)
//@Composable
//fun RoleManagerPreview() {
//    RoleManager( = )
//}