package com.example.test.screens


import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.R
import com.example.test.api.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import retrofit2.HttpException

val deviceApi = RetrofitInstance.getDeviceApi()

class DeviceViewModel : ViewModel() {
    var devices by mutableStateOf<List<com.example.test.api.Device>>(emptyList())
        private set

    init {
        fetchDevices()
    }

    private fun fetchDevices() {
        viewModelScope.launch {
            while(true) {
                try {
                    val response = deviceApi.getAllDevices()
                    devices = response.devices
                } catch (e: Exception) {
                    // Optional: handle error here
                }
                delay(5000)
            }

        }
    }
}




val DarkBlue = Color(0xFF0A1A3D)
data class DeviceStatus(val label: String, val percentage: Float, val color: Color)


@Serializable
data class DeviceJson(
    val status: Boolean,
    val device_name: String,
    val id: Int,
    val last_updated: String
)

@Serializable
data class DeviceResponse(val devices: List<DeviceJson>)

data class Device(
    val name: String,
    val location: String,
    val isOn: Boolean,
    val color: Color,
    val textColor: Color,
    val hasToggle: Boolean,
    val showButtons: Boolean
)


//get devices from json
//@Composable
//fun getDevicesWithStateFromJson(jsonElement: JsonElement): MutableState<List<Pair<Device, Int>>> {
//    // Extract the 'devices' array from the JSON element
//    val devices = remember(jsonElement) {
//        val deviceList = mutableListOf<DeviceJson>()
//        val devicesJsonArray = jsonElement.jsonObject["devices"]?.jsonArray
//
//        devicesJsonArray?.forEach { deviceJsonElement ->
//            val deviceJson = deviceJsonElement.jsonObject
//            val device = DeviceJson(
//                status = deviceJson["status"]?.jsonPrimitive?.boolean ?: false,
//                device_name = deviceJson["device_name"]?.jsonPrimitive?.content ?: "",
//                id = deviceJson["id"]?.jsonPrimitive?.int ?: 0,
//                last_updated = deviceJson["last_updated"]?.jsonPrimitive?.content ?: ""
//            )
//            deviceList.add(device)
//        }
//        deviceList
//    }
//
//    val devicesState = remember(devices) {
//        mutableStateOf(
//            listOf(
//                Device("Smart Light", "Bedroom", devices.find { it.device_name == "lights" }?.status ?: false, Color(0xFF101C43), Color.White, true, showButtons = false) to R.drawable.hang_lamp,
//                Device("Smart Fan", "Living Room", devices.find { it.device_name == "fan" }?.status ?: false, Color(0xFF101C43), Color.White, false, showButtons = true) to R.drawable.fan,
//                Device("Smart Heater", "Living Room", devices.find { it.device_name == "heater" }?.status ?: false, Color(0xFF101C43), Color.White, false, showButtons = false) to R.drawable.heater_icon,
//            )
//        )
//    }
//
//    return devicesState
//}

@Composable
fun getDevicesWithState(devices: List<com.example.test.api.Device>): MutableState<List<Pair<Device, Int>>> {
    val devicesState = remember(devices) {
        mutableStateOf(
            listOf(
                Device("Smart Light", "Bedroom", devices.find { it.device_name == "lights" }?.status ?: false, Color(0xFF101C43), Color.White, true, showButtons = false) to R.drawable.hang_lamp,
                Device("Smart Fan", "Living Room", devices.find { it.device_name == "fan" }?.status ?: false, Color(0xFF101C43), Color.White, false, showButtons = true) to R.drawable.fan,
                Device("Smart Heater", "Living Room", devices.find { it.device_name == "heater" }?.status ?: false, Color(0xFF101C43), Color.White, false, showButtons = false) to R.drawable.heater_icon,
            )
        )
    }

    return devicesState
}


@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: DeviceViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val devices = viewModel.devices
//    val devicesState = getDevicesWithStateFromJson(devices)
    val devicesState = getDevicesWithState(devices)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05103A))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Hey, User!", fontSize = 24.sp, fontWeight = FontWeight.Normal, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Weather Card
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.padding(end = 16.dp)) {
                                Text(text = "Cloudy", fontSize = 21.sp, color = Color.Black)
                                Text(text = " 30°", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color.Black)
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(text = "Monday, 2 May 2025", fontSize = 14.sp, color = Color.Black)
                                Text(text = "Beirut, Metn", fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.weather_icon),
                    contentDescription = "Weather Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = (-40).dp, x = 20.dp)
                        .padding(end = 4.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Device Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            ) {
                Button(
                    onClick = { /* All Devices */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FCF)),
                    modifier = Modifier.widthIn(min = 0.dp)
                ) {
                    Text(text = "All devices", color = Color.White)
                }

                Button(
                    onClick = { /* Bedroom */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.widthIn(min = 0.dp)
                ) {
                    Text(text = "Bedroom", color = Color.Black)
                }

                Button(
                    onClick = { /* Living Room */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.widthIn(min = 0.dp)
                ) {
                    Text(text = "Living", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            DoorUnlockScreen()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                items(devicesState.value) { (device, iconRes) ->

                    DeviceCard(
                        device,
                        iconRes,
                        onToggle = { isChecked ->
                            val index = devicesState.value.indexOfFirst { it.first.name == device.name }
                            if (index != -1) {
                                val updatedList = devicesState.value.toMutableList()
                                updatedList[index] = updatedList[index].copy(first = device.copy(isOn = isChecked))
                                devicesState.value = updatedList
                            }
                        },
                        onArrowClick = {
                            println("${device.name} clicked for navigation")
                        },
                        showButtons = true,
                        isButtonEnabled = device.isOn
                    )
                }
            }

            SecurityModeWidget()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // prevent it from overflowing and allows scroll
                contentAlignment = Alignment.Center
            ) {
                val smartHomeData = listOf(
                    DeviceStatus("Lights", 40f, Color(0xFFE91E63)),
                    DeviceStatus("Fans", 25f, Color(0xFFFFC107)),
                    DeviceStatus("AC", 15f, Color(0xFF4CAF50)),
                    DeviceStatus("Heater", 10f, Color(0xFF2196F3)),
                    DeviceStatus("Others", 10f, Color(0xFF9C27B0))
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SmartHomeDonutChart(data = smartHomeData)
                }
            }
        }
    }
}




@Composable
fun DeviceCard(
    device: Device,
    iconRes: Int,
    onToggle: (Boolean) -> Unit,
    onArrowClick: () -> Unit,
    showButtons: Boolean,
    isButtonEnabled: Boolean
) {
    var isChecked by remember { mutableStateOf(device.isOn) }
//    var isButtonEnabled by remember { mutableStateOf(false) }  // Track if the buttons should be enabled or not
    val dotColor = if (isChecked) Color.Green else Color.Transparent
    val controlApi = RetrofitInstance.getManualControlApi()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 180.dp)
            .padding(3.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = device.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = device.textColor,
                    modifier = Modifier.size(28.dp)
                )

                // Only show the green dot if the device has a toggle and the button is enabled
                if (device.hasToggle && isButtonEnabled) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Green, CircleShape)
                            .offset(x = 30.dp, y = 3.dp)  // Apply offset to position the dot
                    )
                }
            }


            Spacer(modifier = Modifier.height(8.dp))
            Text(text = device.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = device.textColor)
            Text(text = device.location, fontSize = 14.sp, color = device.textColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(19.dp))
            if (device.hasToggle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First button: toggles ON/OFF and enables/disables the other buttons
                    Box(
                        modifier = Modifier
                            .size(36.dp) // Size of the button
                    ) {
                        Button(
                            onClick = {
                                // Toggle button state light
                                coroutineScope.launch {
                                    loading = true
                                    try {
                                        if (device.isOn) {
                                            val response = controlApi.controlDevice("lights_off")
                                        } else {
                                            val response = controlApi.controlDevice("lights_on")
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
                                        Toast.makeText(context, "An unknown error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        loading = false
                                    }
                                }

                            },
                            enabled = !loading,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxSize(), // Fill the box size to ensure the button takes full space
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (isButtonEnabled) "ON" else "OFF",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                        }
                    }
                }


            } else if (device.showButtons) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First button: toggles ON/OFF and enables/disables the other buttons
                    Button(
                        onClick = {
                            onToggle(!isButtonEnabled)
                        },
                        enabled = true,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(
                            text = if (isButtonEnabled) "ON" else "OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }


                    // Second button: only enabled if the first button is ON
                    Button(
                        onClick = {
                            println("setting speed to level 2")
                        },
                        enabled = isButtonEnabled,  // Enabled only if the first button is ON
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.speed2),
                            contentDescription = "Navigate",
                            tint = device.color
                        )
                    }

                    // Third button: only enabled if the first button is ON
                    Button(
                        onClick = {
                            println("setting speed to level 3")
                        },
                        enabled = isButtonEnabled,  // Enabled only if the first button is ON
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.speed3),
                            contentDescription = "Navigate",
                            tint = device.color
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SecurityModeWidget() {
    var isEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(color = Color(0xFF101C43), shape = RoundedCornerShape(12.dp))
            .padding(16.dp) // Internal padding
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.security_icon), // Your image
                    contentDescription = "Security Mode Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Security Mode", color = Color.White, fontSize = 18.sp)
                    Text(
                        if (isEnabled) "Enabled" else "Disabled",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                    checkedTrackColor = Color.Gray,
                    uncheckedTrackColor = DarkBlue
                )
            )
        }
    }
}

@Composable
fun DoorUnlockScreen() {
    var pin by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Main content - Unlock Button
        Button(
            onClick = { isDialogOpen = true }, // Open the dialog when clicked
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101C43)),
            modifier = Modifier.height(70.dp)
                .padding(start = 3.dp)
                .fillMaxWidth(),

            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.door_opened), // Door icon
                    contentDescription = "Door Icon",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Unlock Door", color = Color.White,fontSize = 24.sp)
            }
        }

        // Dialog for PIN entry
        if (isDialogOpen) {
            AlertDialog(
                onDismissRequest = { isDialogOpen = false }, // Close the dialog when dismissed
                title = {
                    Text(text = "Enter Pin to Unlock", color = Color.Black, fontSize = 18.sp,modifier = Modifier.padding(start = 8.dp))
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { if (it.length <= 4) pin = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Pin") },
                            modifier = Modifier
                                .padding(bottom = 5.dp)
                                .width(200.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },

                confirmButton = {
                    Button(
                        onClick = {
                            // Handle pin submission logic
                            println("Pin entered: $pin")
                            isDialogOpen = false // Close the dialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A6FCF)),
                        modifier = Modifier.padding(start = 38.dp)
                            .padding(end = 20.dp),

                        ) {
                        Text(text = "Enter", color = Color.White)
                    }

                },

                dismissButton = {
                    Button(
                        onClick = { isDialogOpen = false }, // Close dialog without action
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),

                        ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                },

                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun SmartHomeDonutChart(
    data: List<DeviceStatus>,
    modifier: Modifier = Modifier
        .size(300.dp)
        .padding(16.dp)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension / 6
            val radius = size.minDimension / 2
            val innerRadius = radius - strokeWidth
            val rect = Rect(0f, 0f, size.width, size.height)

            var startAngle = -90f

            data.forEach { item ->
                val sweepAngle = 360 * (item.percentage / 100f)
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = size,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }

        // Center Label
        Text(
            text = "Smart\nDevices",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}