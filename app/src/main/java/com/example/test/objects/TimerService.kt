//package com.example.test.objects
//
//import android.app.*
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.example.test.api.RetrofitInstance
//import com.example.test.objects.TimerManager
//import kotlinx.coroutines.*
//import java.time.LocalTime
//
//class TimerService : Service() {
//
//    private var job: Job? = null
//
//    override fun onCreate() {
//        super.onCreate()
//        startForegroundService()
//
//        // Initialize your API once
//        TimerManager.initialize(RetrofitInstance.getManualControlApi())
//        startTimerLoop()
//    }
//
//    private fun startForegroundService() {
//        val channelId = "timer_service_channel"
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Timer Service",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Home Automation Timer Running")
//            .setContentText("Your device timers are active.")
//            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
//            .build()
//
//        startForeground(1, notification)
//    }
//
//    private fun startTimerLoop() {
//        job = CoroutineScope(Dispatchers.IO).launch {
//            while (true) {
//                val now = LocalTime.now().withSecond(0).withNano(0)
//                println("Current time: $now")
//                val timers = TimerManager.getTimers(applicationContext)
//
//                for (timer in timers) {
//                    println("Current time: $now, Checking timers: $timers")
//
//                    timer.onTime?.let {
//                        val onTime = LocalTime.parse(it).withSecond(0).withNano(0)
//                        if (now == onTime) {
//                            TimerManager.controlApi.controlDevice("${timer.deviceType}_on")
//
//                            TimerManager.clearTimerField(applicationContext, timer.deviceType, clearOnTime = true)
//                        }
//                    }
//                    timer.offTime?.let {
//                        val offTime = LocalTime.parse(it).withSecond(0).withNano(0)
//                        if (now == offTime) {
//                            TimerManager.controlApi.controlDevice("${timer.deviceType}_off")
//
//                            TimerManager.clearTimerField(applicationContext, timer.deviceType, clearOnTime = false)
//                        }
//                    }
//                }
//
//                delay(60 * 1000) // Check every minute
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        job?.cancel()
//        super.onDestroy()
//    }
//
//    override fun onBind(intent: Intent?) = null
//
//    companion object {
//        fun start(context: Context) {
//            val intent = Intent(context, TimerService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
//                context.startService(intent)
//            }
//        }
//    }
//}
