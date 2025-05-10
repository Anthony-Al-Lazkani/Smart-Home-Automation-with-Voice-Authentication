package com.example.test.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.R
import com.example.test.api.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.api.DeviceSummary
import com.example.test.api.DeviceTimer
import com.example.test.api.SetDeviceTimerRequest
import com.example.test.api.TokenBody
//import com.example.test.objects.DeviceTimer
import com.example.test.objects.PinManager
import com.example.test.objects.ThemeMode
import com.example.test.objects.TokenManager
//import com.example.test.objects.TimerManager
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import kotlin.concurrent.timer
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.example.test.notification.NotificationHandler
import android.os.Handler
import android.os.Looper


val deviceApi = RetrofitInstance.getDeviceApi()
val indicatorsApi = RetrofitInstance.getIndicatorsApi()
val devicesSummaryApi = RetrofitInstance.getDevicesSummaryApi()

class DeviceViewModel : ViewModel() {
    var devices by mutableStateOf<List<com.example.test.api.Device>>(emptyList())
        private set

    var indicators by mutableStateOf<List<com.example.test.api.Indicator>>(emptyList())
        private set

    var totalDevices by mutableStateOf(0)
        private set

    var devicesOn by mutableStateOf(0)
        private set

    var percentageOn by mutableStateOf(0f)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        fetchDevices()
    }

    private fun fetchDevices() {
        viewModelScope.launch {
            while(true) {
                try {
                    val response = deviceApi.getAllDevices()
                    val response2 = indicatorsApi.getIndicators()
                    devices = response.devices
                    indicators = response2

                    val responseSummary = devicesSummaryApi.getDeviceSummary()
                    if (responseSummary.isSuccessful) {
                        responseSummary.body()?.let { summary ->
                            totalDevices = summary.total_devices
                            devicesOn = summary.devices_on
                            percentageOn = summary.percentage_on
                        }
                    } else {
                        error = "Failed to fetch summary: ${responseSummary.code()}"
                    }
                } catch (e: Exception) {
                    // Optional: handle error here
                    error = "Could not connect to the server. Please try again later."
                }
                delay(5000)
            }

        }
    }
    fun clearError() {
        error = null
    }
}


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
    val showButtons: Boolean,
    val isDoor: Boolean
)


@Composable
fun getDevicesWithState(devices: List<com.example.test.api.Device>): MutableState<List<Pair<Device, Int>>> {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)
    val devicesState = remember(devices) {
        mutableStateOf(
            listOf(
                Device("Smart Light", "Bedroom", devices.find { it.device_name == "lights" }?.status ?: false, Color(themeMode.secondary), Color(themeMode.fontColor), true, showButtons = false,isDoor = false) to R.drawable.hang_lamp,
                Device("Smart Fan", "Living Room", devices.find { it.device_name == "fan" }?.status ?: false,  Color(themeMode.secondary), Color(themeMode.fontColor), false, showButtons = true,isDoor = false) to R.drawable.fan,
                Device("Smart Heater", "Living Room", devices.find { it.device_name == "heater" }?.status ?: false, Color(themeMode.secondary), Color(themeMode.fontColor), false, showButtons = false,isDoor = false) to R.drawable.heater_icon,
                Device("Smart Door", "House", devices.find { it.device_name == "door" }?.status ?: false,  Color(themeMode.secondary), Color(themeMode.fontColor), false, showButtons = false,isDoor = true) to R.drawable.door_opened,

                )
        )
    }

    return devicesState
}

@Composable
fun SecurityModeWidget(SecurityState: Boolean) {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)
    var security_loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val token = TokenManager.getToken(context) ?: ""
    val tokenBody = TokenBody(token = token)
    val controlApi = RetrofitInstance.getManualControlApi()
    var isEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(color = Color(themeMode.secondary), shape = RoundedCornerShape(12.dp))
            .padding(16.dp) // Internal padding
    )
    {
        val security = false
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (SecurityState){
                    Image(
                        painter = painterResource(id = R.drawable.security_icon),
                        contentDescription = "Security Mode Icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Green)
                    )
                }
                else {
                    Image(
                        painter = painterResource(id = R.drawable.security_icon),
                        contentDescription = "Security Mode Icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Red)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Security Mode", color = Color(themeMode.fontColor), fontSize = 18.sp)

                    Text(
                        if (SecurityState) "Enabled" else "Disabled",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(36.dp)// Size of the button // Size of the button
            ) {
                Button(
                    onClick = {
                        //Security mode logic
                        coroutineScope.launch {
                            security_loading = true
                            try {
                                if (SecurityState) {
                                    val response = controlApi.controlDevice("security_off", tokenBody)
                                } else {
                                    val response = controlApi.controlDevice("security_on", tokenBody)
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
                                security_loading = false
                            }
                        }

                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                    modifier = Modifier.fillMaxWidth(), // Fill the box size to ensure the button takes full space
                    contentPadding = PaddingValues(2.dp)
                ) {
                if (security_loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (SecurityState) "ON" else "OFF",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(themeMode.textButtonColor)
                    )
                }
                }
            }
        }
    }
}

@Composable
fun DashboardCard() {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)
    var isLightMode by remember { mutableStateOf(themeMode.isLightMode()) }
    val viewModel: DeviceViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(Color(themeMode.buttonColor), RoundedCornerShape(12.dp))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent, RoundedCornerShape(20.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Home Appliances",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(themeMode.textButtonColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${viewModel.devicesOn}/${viewModel.totalDevices} devices on",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
                CircularProgressWithText(progress = viewModel.percentageOn)
            }
        }
    }
}

@Composable
fun ApplianceRect(
    icon: Painter,
    applianceName: String,
    activeText: String,
    inactiveText: String,
    isActive: Boolean,
    onButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)

    Box(
        modifier = Modifier
            .width(187.dp)
            .height(90.dp)
            .padding(start = 4.dp, end = 0.dp, top = 3.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12),
                clip = false
            )
            .background(color = Color(themeMode.secondary), shape = RoundedCornerShape(12))
            .clickable {
                // Optional: handle click for the entire card if needed
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxSize()

        ) {
            // Icon
            Image(
                painter = icon,
                contentDescription = "$applianceName Icon",
                modifier = Modifier.size(45.dp)
            )

            // Texts + Button
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(start = 0.dp)
            ) {
                Text(
                    text = applianceName,
                    color = Color(themeMode.fontColor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isActive) activeText else inactiveText,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onButtonClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (isActive) "OFF" else "ON",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(themeMode.textButtonColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircularProgressWithText(progress: Float) {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = 8.dp,
            color = Color(themeMode.textButtonColor), // Green
            modifier = Modifier.size(70.dp)
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            fontWeight = FontWeight.Bold,
            color = Color(themeMode.textButtonColor)
        )
    }
}


@Composable
fun TimerCard() {
    var context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var timerDeviceList by remember { mutableStateOf<List<DeviceTimer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val fetchTimers = RetrofitInstance.getFetchTimerApi()

    val themeMode = ThemeMode.getInstance(context)
    var isLightMode by remember { mutableStateOf(themeMode.isLightMode()) }
    

    // Coroutine scope for async tasks
    val coroutineScope = rememberCoroutineScope()

    // Function to fetch data from the API
    fun fetchTimers() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = fetchTimers.getAllTimers()
                if (response.isSuccessful) {
                    // Get the list of timers from the response
                    timerDeviceList = response.body()?.deviceTimers ?: emptyList()
                } else {
                    // Handle error
                    Toast.makeText(context, "Failed to fetch timers", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not connect to the server. Please try again later.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Call fetchTimers when the composable is first loaded
    LaunchedEffect(Unit) {
        fetchTimers()
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)) // 🔥 2dp shadow
            .clip(RoundedCornerShape(12.dp)) // 👈 Prevent content overflow from shadow
            .background(Color(themeMode.secondary)) // 🎨 Background inside shadow
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row for placing the Set Timer button and Refresh button at top-right corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isLightMode) Color.White else Color(themeMode.dark_secondary)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Icon",
                        tint = if (!isLightMode) Color.Black else Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = "Set Timer",
                        color = if (!isLightMode) Color.Black else Color.White,
                        fontSize = 12.sp
                    )
                }

                // Refresh Button positioned top-right beside Set Timer button
                IconButton(onClick = { fetchTimers() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Timers",
                        tint = Color(themeMode.fontColor),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header row - Columns for deviceType, onTime, offTime
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Device Type", "On Time", "Off Time").forEach { header ->
                    Text(
                        text = header,
                        color = Color(themeMode.fontColor),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                }
            }

            // Data rows - Displaying timer information
            if (isLoading) {
                Text("Loading timers...", color = Color(themeMode.fontColor))
            } else {
                for (timer in timerDeviceList) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = timer.device_type,
                            color = Color(themeMode.fontColor),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                        Text(
                            text = timer.on_time ?: "-",
                            color = Color(themeMode.fontColor),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                        Text(
                            text = timer.off_time ?: "-",
                            color = Color(themeMode.fontColor),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    // Show the timer dialog if needed
    if (showDialog) {
        TimerPopup(onDismiss = { showDialog = false })
    }
}

@Composable
fun TimerPopup(onDismiss: () -> Unit) {
    var selectedDevice by remember { mutableStateOf("Device") }
    var expanded by remember { mutableStateOf(false) }
    var coroutineScope = rememberCoroutineScope()
    var deviceTimerApi = RetrofitInstance.getDeviceTimerApi()
    var setTimerLoading by remember { mutableStateOf(false) }

    var selectedDevicesList by remember { mutableStateOf(listOf("lights", "fan", "heater")) }


    var onTime by remember { mutableStateOf("") }
    var offTime by remember { mutableStateOf("") }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 8.dp)
                        .clickable {
                            onDismiss()
                        }
                )

                Text(
                    text = "Set Timer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(text = "Device", fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF5F7FA), shape = RoundedCornerShape(12.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedDevice)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("lights", "fan", "heater").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                selectedDevice = it
                                expanded = false
                            })
                        }
                    }

                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("On time", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp, top = 4.dp)
                                .background(Color(0xFFF5F7FA), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            onTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        10, 0, true
                                    ).show()
                                }
                                .padding(12.dp)
                        ) {
                            Text(onTime)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Off time", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 4.dp)
                                .background(Color(0xFFF5F7FA), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            offTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        11, 0, true
                                    ).show()
                                }
                                .padding(12.dp)
                        ) {
                            Text(offTime)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        setTimerLoading = true
                        if (onTime.isBlank() && offTime.isBlank()) {
                            Toast.makeText(context, "Please set at least one time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedDevice == "Device") {
                            Toast.makeText(context, "Please select a device", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            try {
                                val token = TokenManager.getToken(context) ?: ""
                                val response = deviceTimerApi.setDeviceTimer(
                                    deviceType = selectedDevice,
                                    timerRequest = SetDeviceTimerRequest(
                                        token = token,
                                        on_time = if (onTime.isBlank()) null else onTime,
                                        off_time = if (offTime.isBlank()) null else offTime
                                    )
                                )

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Timer updated successfully", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    val error = response.errorBody()?.string()
                                    val errorMessage = JSONObject(error ?: "{}").optString("detail", "Something went wrong")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: HttpException) {
                                val error = e.response()?.errorBody()?.string()
                                val errorMessage = JSONObject(error ?: "{}").optString("detail", "An error occurred")
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "An unexpected error occurred: Could not connect to the server. Please try again later.", Toast.LENGTH_SHORT).show()
                            } finally {
                                setTimerLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !setTimerLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF032B91))
                ) {
                    if (setTimerLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Set Timer", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}



@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: DeviceViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val devices = viewModel.devices
    val indicators = viewModel.indicators
    val devicesState = getDevicesWithState(devices)
    val context = LocalContext.current
//    var timers by remember { mutableStateOf(listOf<DeviceTimer>()) }
    val themeMode = ThemeMode.getInstance(context)
    var isLightMode by remember { mutableStateOf(themeMode.isLightMode()) }
    var coroutineScope = rememberCoroutineScope()
    var ldr_loading by remember { mutableStateOf(false) }
    val controlApi = RetrofitInstance.getManualControlApi()
    val token = TokenManager.getToken(context) ?: ""
    val tokenBody = TokenBody(token = token)

    LaunchedEffect(Unit) {
        isLightMode = themeMode.isLightMode()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(Color(0xFF05103A))
            .background(
                Color(themeMode.primary)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Hey, User!", fontSize = 24.sp, fontWeight = FontWeight.Normal, color = Color(themeMode.fontColor))
            Spacer(modifier = Modifier.height(16.dp))

            DashboardCard()

            Spacer(modifier = Modifier.height(4.dp))

            val ledStateSecurity = indicators.find { it.indicator_name == "security" }?.status == true

            if (ledStateSecurity) {
                NotificationHandler.showSecurityDetectionNotification(context)
            }

            val ledStateGas = indicators.find { it.indicator_name == "gas" }?.status == true

            if (ledStateGas) {
                NotificationHandler.showGasDetectionNotification(context)
            }
            val ledStateFire = indicators.find { it.indicator_name == "fire" }?.status == true

            if (ledStateFire) {
                NotificationHandler.showFireDetectionNotification(context)
            }
            val ledStateEarthquake = indicators.find { it.indicator_name == "earthquake" }?.status == true

            if (ledStateEarthquake) {
                NotificationHandler.showEarthquakeDetectionNotification(context)
            }


            //DoorUnlockScreen()
            if (ledStateGas or ledStateFire or ledStateEarthquake or ledStateSecurity){
                SensorWidgetRow(
                    fireDetected = ledStateFire,
                    personDetected = ledStateSecurity,
                    gasDetected = ledStateGas,
                    rainDetected = ledStateEarthquake
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
            TimerCard()
            val ldrState = devices.find { it.device_name == "ldr" }?.status == true

            Row() {
                ApplianceRect(
                    icon = painterResource(id = R.drawable.nightlight),
                    applianceName = "Nightlight",
                    activeText = "LDR ON",
                    inactiveText = "LDR OFF",
                    isActive = ldrState,
                    onButtonClick = {
                        coroutineScope.launch {
                            ldr_loading = true
                            try {
                                if (ldrState) {
                                    val response = controlApi.controlDevice("ldr_off", tokenBody)
                                } else {
                                    val response = controlApi.controlDevice("ldr_on", tokenBody)
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
                                ldr_loading = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(2.dp))

                ApplianceRect(
                    icon = painterResource(id = R.drawable.lamppost),
                    applianceName = "Lamppost",
                    activeText = "Lit Entrance",
                    inactiveText = "No Light",
                    isActive = false,
                    onButtonClick = {// "Lamppost logic"
                    }
                )
            }
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
                        isButtonEnabled = device.isOn,
                        isDoor = false
                    )
                }
            }

            //
            val securityState = devices.find { it.device_name == "security" }?.status ?: false

            SecurityModeWidget(securityState)

            Spacer(modifier = Modifier.height(55.dp))

        }
    }
}


@Composable
fun SensorBox(
    isDetected: Boolean,
    painter: Painter,
    size: Dp,
) {
    val context = LocalContext.current
    val themeMode = ThemeMode.getInstance(context)

    val backgroundColor = Color(themeMode.secondary)

    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(height = 60.dp, width = 86.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isDetected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .alpha(alpha),
                colorFilter = ColorFilter.tint(Color(themeMode.fontColor))
            )
        }
    }
}

@Composable
fun SensorWidgetRow(
    fireDetected: Boolean,
    personDetected: Boolean,
    gasDetected: Boolean,
    rainDetected: Boolean
) {
    val quakeImage = painterResource(id = R.drawable.earthquake)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SensorBox(isDetected = fireDetected, painter = rememberVectorPainter(Icons.Filled.Whatshot), size = 32.dp)
        SensorBox(isDetected = personDetected, painter = rememberVectorPainter(Icons.Filled.Person),size = 32.dp)
        SensorBox(isDetected = gasDetected, painter = rememberVectorPainter(Icons.Filled.Warning),size = 32.dp)
        SensorBox(isDetected = rainDetected, painter = quakeImage,size = 42.dp)
    }
}



@Composable
fun DeviceCard(
    device: Device,
    iconRes: Int,
    onToggle: (Boolean) -> Unit,
    onArrowClick: () -> Unit,
    showButtons: Boolean,
    isButtonEnabled: Boolean,
    isDoor: Boolean
) {
    var isChecked by remember { mutableStateOf(device.isOn) }
//    var isButtonEnabled by remember { mutableStateOf(false) }  // Track if the buttons should be enabled or not
    val dotColor = if (isChecked) Color.Green else Color.Transparent
    val controlApi = RetrofitInstance.getManualControlApi()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var light_loading by remember { mutableStateOf(false) }
    var fan_loading by remember { mutableStateOf(false) }
    var fan_high_loading by remember { mutableStateOf(false) }

    var heater_loading by remember { mutableStateOf(false) }
    var door_loading by remember { mutableStateOf(false) }
    var confirm_door_loading by remember { mutableStateOf(false) }
    val token = TokenManager.getToken(context) ?: ""
    val tokenBody = TokenBody(token = token)
    val themeMode = ThemeMode.getInstance(context)
    var isHighSpeedOn by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 180.dp)
            .padding(3.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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



                Spacer(modifier = Modifier.width(110.dp))

                // Only show the green dot if the device has a toggle and the button is enabled
                if (device.isOn) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Green, CircleShape)
                            .offset(x = 30.dp, y = 3.dp)  // Apply offset to position the dot
                    )
                }
                else{
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red, CircleShape)
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
                            .width(70.dp)
                            .height(36.dp)// Size of the button // Size of the button
                    ) {
                        Button(
                            onClick = {
                                // Toggle button state light
                                coroutineScope.launch {
                                    light_loading = true
                                    try {
                                        if (device.isOn) {
                                            val response = controlApi.controlDevice("lights_off", tokenBody)
                                        } else {
                                            val response = controlApi.controlDevice("lights_on", tokenBody)
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
                                        light_loading = false
                                    }
                                }

                            },
                            enabled = !light_loading,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                            modifier = Modifier.fillMaxSize(), // Fill the box size to ensure the button takes full space
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            if (light_loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (device.isOn) "ON" else "OFF",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(themeMode.textButtonColor)
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
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                fan_loading = true
                                try {
                                    if (device.isOn) {
                                        val response = controlApi.controlDevice("fan_off", tokenBody)
                                    } else {
                                        val response = controlApi.controlDevice("fan_on", tokenBody)
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
                                    fan_loading = false
                                }
                            }
                        },
                        enabled = !fan_loading,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                        modifier = Modifier.width(70.dp)
                            .height(36.dp),// Size of the button
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        if (fan_loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (device.isOn) "ON" else "OFF",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(themeMode.textButtonColor)
                            )
                        }
                    }


                    // Second button: only enabled if the first button is ON
                    if (device.isOn) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    fan_high_loading = true
                                    try {
                                        val response = if (!isHighSpeedOn) {
                                            controlApi.controlDevice("fan_high", tokenBody)
                                        } else {
                                            controlApi.controlDevice("fan_on", tokenBody)
                                        }
                                        isHighSpeedOn = !isHighSpeedOn  // Toggle after successful API call
                                    } catch (e: HttpException) {
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
                                        fan_high_loading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isHighSpeedOn) Color.Green else Color(themeMode.buttonColor)),
                            shape = CircleShape,
                            modifier = Modifier
                                .width(70.dp)
                                .height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (fan_high_loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "HS",   // High Speed
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(themeMode.textButtonColor)
                                )
                            }
                        }
                    }
                }
            }
            else if (device.isDoor) {
                var pin by remember { mutableStateOf("") }
                var isDialogOpen by remember { mutableStateOf(false) }
                var context = LocalContext.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First button: toggles ON/OFF and enables/disables the other buttons
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(36.dp)// Size of the button
                    ) {
                        Button(

                            onClick = {
                                // door
                                door_loading = true
                                val doesPinExist = PinManager.isPinSet(context)
                                if (!doesPinExist) {
                                    Toast.makeText(
                                        context,
                                        "Pin is not Setup ! Please go to settings and setup a pin",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    isDialogOpen = true
                                }
                                door_loading = false

                            },
                            enabled = !door_loading,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                            modifier = Modifier.fillMaxSize(), // Fill the box size to ensure the button takes full space
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            if (door_loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (isButtonEnabled) "LOCK" else "UNLOCK",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(themeMode.textButtonColor)
                                )

                            }
                        }
                    }

                    if (device.isOn) {
                    Image(
                        painter = painterResource(id = R.drawable.unlocked),
                        contentDescription = "Security Mode Icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.locked),
                                contentDescription = "Security Mode Icon",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    if (isDialogOpen) {
                        AlertDialog(
                            onDismissRequest = {
                                isDialogOpen = false
                            }, // Close the dialog when dismissed
                            title = {
                                Text(
                                    text = "Enter Pin to Unlock",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    OutlinedTextField(
                                        value = pin,
                                        onValueChange = { if (it.length <= 4) pin = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        label = { Text("Pin") },
                                        modifier = Modifier
                                            .padding(bottom = 5.dp)
                                            .width(200.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }
                            },

                            confirmButton = {
                                Button(
                                    onClick = {

                                        coroutineScope.launch {
                                            confirm_door_loading = true
                                            try {

                                                val isPinCorrect = PinManager.verifyPin(context, pin)
                                                if (!isPinCorrect) {
                                                    Toast.makeText(
                                                        context,
                                                        "Pin is Incorrect !",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    confirm_door_loading = false
                                                    return@launch
                                                }
                                                if (device.isOn) {
                                                    val response =
                                                        controlApi.controlDevice("door_lock", tokenBody)
                                                } else {
                                                    val response =
                                                        controlApi.controlDevice("door_unlock", tokenBody)
                                                }
                                                isDialogOpen = false
                                                pin = ""
                                            } catch (e: HttpException) {
                                                try {
                                                    // Extract the error message from the error body
                                                    val errorBody =
                                                        e.response()?.errorBody()?.string()
                                                    val jsonObject = errorBody?.let {
                                                        JSONObject(it)
                                                    }
                                                        ?: JSONObject() // Fallback to an empty JSONObject if errorBody is null

                                                    val errorMessage = jsonObject.optString(
                                                        "detail",
                                                        "An error occurred"
                                                    )

                                                    // Show error as a toast message, ensure errorMessage is non-null
                                                    Toast.makeText(
                                                        context,
                                                        errorMessage ?: "An error occurred",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } catch (jsonException: Exception) {
                                                    // If JSON parsing fails, show a generic message
                                                    Toast.makeText(
                                                        context,
                                                        "An error occurred while parsing the response.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "An unknown error occurred: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } finally {
                                                confirm_door_loading = false
                                            }
                                        }
                                    },
                                    enabled = !confirm_door_loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF2A6FCF
                                        )
                                    ),
                                    modifier = Modifier.padding(start = 38.dp)
                                        .padding(end = 20.dp),

                                    ) {

                                    if (confirm_door_loading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(text = "Enter", color = Color.White)
                                    }
                                }
                            },

                            dismissButton = {
                                Button(
                                    onClick = {
                                        isDialogOpen = false
                                    }, // Close dialog without action
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
            else{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First button: toggles ON/OFF and enables/disables the other buttons
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(36.dp)// Size of the button// Size of the button
                    ) {
                        Button(
                            onClick = {
                                // Toggle button state light
                                coroutineScope.launch {
                                    heater_loading = true
                                    try {
                                        if (device.isOn) {
                                            val response = controlApi.controlDevice("heater_off", tokenBody)
                                        } else {
                                            val response = controlApi.controlDevice("heater_on", tokenBody)
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
                                        heater_loading = false
                                    }
                                }

                            },
                            enabled = !heater_loading,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(themeMode.buttonColor)),
                            modifier = Modifier.fillMaxSize(), // Fill the box size to ensure the button takes full space
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            if (heater_loading) {
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
                                    color = Color(themeMode.textButtonColor)
                                )
                            }

                        }
                    }
                }

            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}