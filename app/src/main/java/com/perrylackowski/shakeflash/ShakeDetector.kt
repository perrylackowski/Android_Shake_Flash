package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class ShakeDetector(private val context: Context) : SensorEventListener {

    var onShakeDetected: (() -> Unit)? = null
    private var lastShakeTime: Long = 0
    private var cooldownTime: Long = 1000 // Default cooldown in milliseconds
    private var shakeThreshold: Float = 10.0f // Default sensitivity
    private var lastDirection: Int = 0 // -1 for down, 1 for up
    private var shakePattern = mutableListOf<Int>()

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime < cooldownTime) return // Ensure enough time has passed
            val x = event.values[0]
            val direction = if (x < -shakeThreshold) -1 else if (x > shakeThreshold) 1 else 0

            if (direction != 0 && direction != lastDirection) { // Detects direction change (chop motion)
                lastDirection = direction
                shakePattern.add(direction)
                if (shakePattern.size > 3) shakePattern.removeAt(0) // Keep last 3 movements
            }

            // Check if the chop pattern is complete: Right > Left > Right
            if (shakePattern == listOf(1, -1, 1)) {
                lastShakeTime = currentTime
                shakePattern.clear()
                onShakeDetected?.invoke() // Trigger flashlight toggle
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setSensitivity(value: Float) {
        shakeThreshold = value
    }

    fun setCooldown(value: Long) {
        cooldownTime = value
    }
}