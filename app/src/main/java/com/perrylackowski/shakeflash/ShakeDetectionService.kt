package com.perrylackowski.shakeflash

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class ShakeDetectionService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sharedPreferences: SharedPreferences
    private var shakeDetector: ShakeDetector? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        Log.d("ShakeFlash", "Creating ShakeDetectionService")
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sharedPreferences = getSharedPreferences("ShakeFlashPrefs", Context.MODE_PRIVATE)

        // Use the single instances from SingletonRepository
        val flashlightUtils = SingletonRepository.flashlightUtils
        val shakeDetector = SingletonRepository.shakeDetector

        // Since we can't get the flashlight state unless I upgrade the API, starting the service
        // will force the flashlight into an off state (which it is likely already in when you
        // start the app). Going forward, we can use this knowledge to track the flashlight state
        // starting from off or false.
        flashlightUtils.turnOffFlashlight()

        // Register the singleton ShakeDetector with the SensorManager
        sensorManager.registerListener(
            shakeDetector,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )

        startForeground(NOTIFICATION_ID, createNotification())
        // Acquire a wake lock to keep sensors active
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ShakeFlash:WakeLock")
        wakeLock?.acquire() //No timeout since it should run indefinitely, until the app is deleted.
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector) // Unregister sensor to free resources
        wakeLock?.release()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Ensures service restarts if killed
    }

    private fun createNotification(): Notification {
        val channelId = "ShakeFlashServiceChannel"
        val channelName = "Shake Flashlight Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Shake Flashlight")
            .setContentText("Shake detection is running in the background")
            .setSmallIcon(R.drawable.flashlight)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}
