//package com.example.test.objects
//
//import android.content.Context
//import com.example.test.api.ManualControlApi
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//
//data class DeviceTimer(
//    val deviceType: String,
//    val onTime: String? = null,
//    val offTime: String? = null
//)
//
//object TimerManager {
//
//    private const val PREF_NAME = "device_timers"
//    private const val KEY_TIMERS = "timers"
//    private val gson = Gson()
//
//    lateinit var controlApi: ManualControlApi
//
//    private val defaultTimers = listOf(
//        DeviceTimer("lights", null, null),
//        DeviceTimer("heater", null, null),
//        DeviceTimer("fan", null, null)
//    )
//
//    fun initialize(api: ManualControlApi) {
//        controlApi = api
//    }
//
//
//    fun saveTimers(context: Context, timers: List<DeviceTimer>) {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val existingTimers = getTimers(context).toMutableList()
//
//        // Update or add the new timer to the list
//        timers.forEach { newTimer ->
//            val index = existingTimers.indexOfFirst { it.deviceType == newTimer.deviceType }
//            if (index != -1) {
//                // If the timer already exists, update it
//                val current = existingTimers[index]
//                existingTimers[index] = DeviceTimer(
//                    deviceType = newTimer.deviceType,
//                    onTime = newTimer.onTime ?: current.onTime,
//                    offTime = newTimer.offTime ?: current.offTime
//                )
//            } else {
//                // If the timer does not exist, add it
//                existingTimers.add(newTimer)
//            }
//        }
//
//        // Save the updated timers to shared preferences
//        val json = gson.toJson(existingTimers)
//        prefs.edit().putString(KEY_TIMERS, json).apply()
//        println("Timers saved: $json")
//    }
//
//    fun getTimers(context: Context): List<DeviceTimer> {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val json = prefs.getString(KEY_TIMERS, null)
//
//        return if (json.isNullOrEmpty()) {
//            // First time: save and return default timers
//            saveTimers(context, defaultTimers)
//            defaultTimers
//        } else {
//            gson.fromJson(json, object : TypeToken<List<DeviceTimer>>() {}.type)
//        }
//    }
//
//
//
//
//    fun clearTimerField(context: Context, deviceType: String, clearOnTime: Boolean) {
//        val timers = getTimers(context).toMutableList()
//        val index = timers.indexOfFirst { it.deviceType == deviceType }
//
//        if (index != -1) {
//            val current = timers[index]
//            val updated = DeviceTimer(
//                deviceType = current.deviceType,
//                onTime = if (clearOnTime) null else current.onTime,
//                offTime = if (!clearOnTime) null else current.offTime
//            )
//            timers[index] = updated
//            saveTimers(context, timers)
//        }
//    }
//
//    fun resetTimers(context: Context) {
//        saveTimers(context, defaultTimers)
//    }
//
//
//}
