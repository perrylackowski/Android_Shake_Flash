package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class ShakeDetector(private val context: Context) : SensorEventListener {

    var onShakeDetected: (() -> Unit)? = null
    private var lastUpdateTime: Long = 0
    private var lastShakeTime: Long = 0
    private var cooldownTime: Long = 1000 // Default cooldown in milliseconds
    private var shakeThreshold: Float = 10.0f // Default sensitivity
    private var lastX: Float = 0f
    private var lastDirection: Int = 0 // -1 for down, 1 for up

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime < cooldownTime) return // Cooldown to avoid multiple shakes
            lastUpdateTime = currentTime

            val x = event.values[0]
            val direction = if (x < -shakeThreshold) -1 else if (x > shakeThreshold) 1 else 0

            if (direction != 0 && direction != lastDirection) { // Detects a change in direction (chop motion)
                lastDirection = direction
                if (currentTime - lastShakeTime > cooldownTime) { // Enforce cooldown
                    lastShakeTime = currentTime
                    onShakeDetected?.invoke()
                }
            }

            lastX = x
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