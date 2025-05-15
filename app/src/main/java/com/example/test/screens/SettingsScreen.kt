package com.example.test.screens


import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.test.R
import com.example.test.api.RetrofitInstance
import com.example.test.objects.PinManager
import com.example.test.objects.ThemeMode
import com.example.test.objects.TokenManager
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {


    val context = LocalContext.current
    var themeToggleKey by remember { mutableStateOf(0) }
    val themeMode = remember(themeToggleKey) { ThemeMode.getInstance(context) }

    val textColor = Color(themeMode.fontColor)
    val cardColor = Color(themeMode.secondary)
    val colorFont = themeMode.fontColor
    var isLightModeEnabled by remember {
        mutableStateOf(themeMode.isLightMode()) // true = light, false = dark
    }

    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userRole by remember { mutableStateOf("Loading...") }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }
    var showPinDialog by remember { mutableStateOf(false) }
    val pinExists = PinManager.isPinSet(context)
    val coroutineScope = rememberCoroutineScope()
    val token = TokenManager.getToken(context)
    val apiToken = "Bearer $token"
    var isProfileLoading by remember { mutableStateOf(true) }
    val userApi = RetrofitInstance.getUserApi()

    val firstLetter = userName.firstOrNull()?.uppercase() ?: "?"


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Make the API call and wrap it in a Response object
                val response = userApi.getCurrentUser(apiToken)

                if (response.isSuccessful) {
                    // If the request is successful, get the UserResponse object
                    val user = response.body()
                    if (user != null) {
                        userName = user.username
                        userEmail = user.email
                        userRole = user.role

                        isProfileLoading = false
                    } else {
                        Toast.makeText(context, "User data is missing", Toast.LENGTH_SHORT).show()
                        isProfileLoading = false
                    }
                } else {
                    Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    isProfileLoading = false
                }
            } catch (e: Exception) {
                // Handle any exception that occurs
                Toast.makeText(context, "An unknown error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                isProfileLoading = false
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(themeMode.primary))
            .padding(16.dp)
    ) {
        // Profile Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .background(Color(themeMode.secondary), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(firstLetter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = textColor),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color(colorFont),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                userName = editedName
                                isEditing = false
                            }
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, color = Color(colorFont), fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
                }
            }

            Text(userEmail, color = Color.LightGray, fontSize = 14.sp)
            Text(userRole, color = Color.LightGray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Change Credentials Section
        Text(
            text = "Account",
            color = Color(colorFont),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Change Password (top rounded only)
        SettingItem(
            label = "Reset Password",
            cardColor = Color(themeMode.secondary),
            textColor = Color(themeMode.fontColor),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            onClick = {
                navController.navigate("reset password")
            },
            leadingPainter = painterResource(id = R.drawable.resetpass),
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp),
                    colorFilter = ColorFilter.tint(Color(themeMode.fontColor))
                )
            }
        )

        // Divider
        Divider(color = Color(0xFF9F9F9F), thickness = 0.3.dp)

        if (userRole == "admin"){
            SettingItem(
                label = "Manage roles",
                cardColor = Color(themeMode.secondary),
                textColor = Color(themeMode.fontColor),
                shape = RoundedCornerShape(topStart = 0.dp),
                onClick = {
                    if (!isProfileLoading && userName != "Loading") {
                        navController.navigate("manage_roles/${Uri.encode(userName)}")
                    } else {
                        Toast.makeText(context, "User is currently loading or username is unavailable.", Toast.LENGTH_SHORT).show()
                    }
                },
                leadingIcon = Icons.Filled.Security,  // Using Android material icon
                trailingContent = {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = "Toggle",
                        modifier = Modifier
                            .size(24.dp),
                        colorFilter = ColorFilter.tint(Color(themeMode.fontColor))
                    )
                }
            )
            Divider(color = Color(0xFF9F9F9F), thickness = 0.3.dp)

            SettingItem(
                label = "Logs",
                cardColor = Color(themeMode.secondary),
                textColor = Color(themeMode.fontColor),
                shape = RoundedCornerShape(topStart = 0.dp),
                onClick = {
                        navController.navigate("logs")
                },
                leadingIcon = Icons.Filled.Book,  // Using Android material icon
                trailingContent = {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = "Toggle",
                        modifier = Modifier
                            .size(24.dp),
                        colorFilter = ColorFilter.tint(Color(themeMode.fontColor))
                    )
                }
            )
            Divider(color = Color(0xFF9F9F9F), thickness = 0.3.dp)
        }

        // Change Voice (bottom rounded only)
        SettingItem(
            label = "Setup Pin",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            onClick = {

            },
            leadingPainter = painterResource(id = R.drawable.setpin),
            trailingContent = {
                if (!pinExists) {
                    // Show red X icon and make it clickable
                    Icon(
                        imageVector = Icons.Default.Close, // ❌ icon
                        contentDescription = "Set PIN",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                showPinDialog = true
                            }
                    )
                } else {
                    // Show green check and NOT clickable
                    Icon(
                        imageVector = Icons.Default.Check, // ✅ icon
                        contentDescription = "PIN Set",
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)

                        //.clickable {
                        //                                showPinDialog = true
                        //                            }
                    )
                }
            }
        )

        if (showPinDialog) {
            var newPin by remember { mutableStateOf("") }
            var confirmPin by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = {
                    Text("Setup Pin", fontWeight = FontWeight.Bold, color = Color.White)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = {
                                // Only allow up to 4 digits
                                if (it.length <= 4) {
                                    newPin = it
                                }
                            },
                            label = { Text("New Pin", color = Color.White) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                // Only allow up to 4 digits
                                if (it.length <= 4) {
                                    confirmPin = it
                                }
                            },
                            label = { Text("Confirm Pin", color = Color.White) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPin == confirmPin && newPin.isNotEmpty()) {
                                // Save pin logic
                                //hone bta3ml save lal pin
                                coroutineScope.launch {
                                    PinManager.savePin(context, newPin)
                                    showPinDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FCF))
                    ) {
                        Text("Enter", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showPinDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FCF))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = Color(0xFF05103A)
            )
        }





        Spacer(modifier = Modifier.height(16.dp))

        // Appearance & Info Section
        Text(
            text = "Appearance & Info",
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Dark/Light Mode (top only)
        SettingItem(
            label = "Light Mode",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            onClick = {
                // TODO
            },
            leadingPainter = painterResource(id = R.drawable.darklight),
            trailingContent = {
                Switch(
                    checked = isLightModeEnabled,
                    onCheckedChange = {
                        isLightModeEnabled = it
                        coroutineScope.launch {
                            themeMode.setIsLightMode(it)
                            themeToggleKey++
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }
        )

        Divider(color = Color(0xFF9F9F9F),thickness = 0.3.dp)

        // About App & Version Info (no rounding)
        SettingItem(
            label = "About App & Version Info",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(0.dp),
            onClick = {
                //Go to About page
            },
            leadingIcon = Icons.Filled.Info,
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            //Go to About page
                        },
                    colorFilter = ColorFilter.tint(textColor)
                )
            }
        )
        Divider(color = Color(0xFFB0B0B0),thickness = 0.3.dp)

        // Contact Support (bottom only)
        SettingItem(
            label = "Contact Support",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            onClick = {
                // Go to contact page
            },
            leadingPainter = painterResource(id = R.drawable.contact),
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            //Go to Contact page
                        },
                    colorFilter = ColorFilter.tint(textColor)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Account Actions
        SettingItem(
            label = "Log Out",
            cardColor = cardColor,
            textColor = textColor,
            centerText = true,
            shape = RoundedCornerShape(12.dp),
            onClick = {
                TokenManager.removeToken(context)
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(12.dp), clip = true)
        )
        if (userRole == "admin") {
            Spacer(modifier = Modifier.height(40.dp))
        }

    }
}

@Composable
fun SettingItem(
    label: String,
    cardColor: Color,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingPainter: Painter? = null,   // <-- for PNG images
    leadingIcon: ImageVector? = null,  // <-- for Material Icons
    leadingIconSize: Dp = 27.dp,
    leadingIconTint: Color = Color.Unspecified, // allow tinting icons if needed
    trailingContent: @Composable (() -> Unit)? = null,
    centerText: Boolean = false,
) {
    val context = LocalContext.current
    val themeMode = remember { ThemeMode.getInstance(context) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centerText) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                leadingPainter != null -> {
                    Icon(
                        painter = leadingPainter,
                        contentDescription = null,
                        modifier = Modifier.size(leadingIconSize),
                        tint = Color(themeMode.fontColor)
                    )
                }
                leadingIcon != null -> {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(leadingIconSize),
                        tint = Color(themeMode.fontColor)
                    )
                }
            }

            if (leadingPainter != null || leadingIcon != null) {
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = label,
                color = textColor,
                fontSize = 16.sp,
                textAlign = if (centerText) TextAlign.Center else TextAlign.Start
            )
        }

        trailingContent?.invoke()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(navController = rememberNavController())
}