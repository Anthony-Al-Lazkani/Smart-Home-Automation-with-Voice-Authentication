package com.example.test.screens


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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.R
import com.example.test.objects.PinManager
import com.example.test.objects.ThemeMode
import com.example.test.objects.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val backgroundColor = Color(0xFF05103A)
    val cardColor = Color(0xFF1A2A5B)
    val textColor = Color.White
    val context = LocalContext.current
    val themeMode = remember { ThemeMode.getInstance(context) }
    var isLightModeEnabled by remember {
        mutableStateOf(themeMode.isLightMode()) // true = light, false = dark
    }

    var darkModeEnabled by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Mathieu Khoury") }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    val pinExists = PinManager.isPinSet(context)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Profile Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("MK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
                        tint = Color.White,
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
                    Text(userName, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                isEditing = true
                                editedName = userName
                            }
                    )
                }
            }

            Text("matt@gmail.com", color = Color.LightGray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Change Credentials Section
        Text(
            text = "Account",
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Change Password (top rounded only)
        SettingItem(
            label = "Reset Password",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            onClick = {
                navController.navigate("reset password")
            },
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        )

        // Divider
        Divider(color = Color(0xFF9F9F9F), thickness = 0.3.dp)

        // Change Voice (bottom rounded only)
        SettingItem(
            label = "Setup Pin",
            cardColor = cardColor,
            textColor = textColor,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            onClick = {

            },
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
                                PinManager.savePin(context, newPin)
                                showPinDialog = false
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
            trailingContent = {
                Switch(
                    checked = isLightModeEnabled,
                    onCheckedChange = {
                        isLightModeEnabled = it
                        themeMode.setIsLightMode(it)
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
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            //Go to About page
                        },
                    colorFilter = ColorFilter.tint(Color.White)
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
            trailingContent = {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Toggle",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            //Go to Contact page
                        },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Account Actions
        SettingItem(
            label = "Log Out",
            cardColor = cardColor,
            textColor = Color.White,
            centerText = true,
            shape = RoundedCornerShape(12.dp),
            onClick = {
                TokenManager.removeToken(context)
                navController.navigate("login")
            },



            )
    }
}

@Composable
fun SettingItem(
    label: String,
    cardColor: Color,
    textColor: Color,
    shape: RoundedCornerShape,
    trailingContent: @Composable (() -> Unit)? = null,
    centerText: Boolean = false,
    onClick: () -> Unit, // Accept onClick parameter
    modifier: Modifier = Modifier // Make modifier optional
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, shape)
            .padding(16.dp)
            .clickable { onClick() }, // Use the onClick lambda here
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centerText) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            textAlign = if (centerText) TextAlign.Center else TextAlign.Start
        )
        trailingContent?.invoke()
    }
}