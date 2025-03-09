package com.perrylackowski.shakeflash

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class ShakeDetector : SensorEventListener {
    // Settings variables
    private var shakeThreshold: Float = 10.0f // Default sensitivity
    private var cooldownTime: Long = 1000 // Default cooldown in milliseconds

    // Functional variables
    private var lastShakeTime: Long = 0
    private var lastDirection: Int = 0 // -1 for down, 1 for up
    private var shakePattern = mutableListOf<Int>()
    private var shakeStartTime: Long = 0

    // Class references
    private val flashlightUtils = SingletonRepository.flashlightUtils

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()

            resetShakePatternIfNeeded(currentTime)

            if (currentTime - lastShakeTime < cooldownTime) return // Ensure cooldown period

            val direction = detectShakeDirection(event.values[0])
            if (direction != 0) {
                Log.d("ShakeDetector", "Shake detected! Looking for pattern.")
                processShakeDirection(direction, currentTime)
            }

            if (isChopPatternDetected()) {
                lastShakeTime = currentTime
                shakePattern.clear()
                Log.d("ShakeDetector", "Shake pattern detected! Triggering flashlight.")
                flashlightUtils.toggleFlashlight()
            }
        }
    }

    // Resets the shake pattern if the shake window has been exceeded
    private fun resetShakePatternIfNeeded(currentTime: Long) {
//        if (shakePattern.isNotEmpty() && (currentTime - shakeStartTime > 2000)) {
        if (currentTime - shakeStartTime > 500) {
            shakePattern.clear()
            Log.d("ShakeDetector", "Shake window exceeded, resetting pattern.")
        }
    }

    // Determines movement direction based on accelerometer values
    private fun detectShakeDirection(x: Float): Int {
        return when {
            x < -shakeThreshold -> -1
            x > shakeThreshold -> 1
            else -> 0
        }
    }

    // Handles tracking of shake motion and storing the pattern
    private fun processShakeDirection(direction: Int, currentTime: Long) {
        if (direction != lastDirection) { // Detects direction change
            lastDirection = direction

//            if (shakePattern.isEmpty()) {
//                shakeStartTime = currentTime // Set shake start time when first movement is detected
//            }

            // Reset the shake timer each time a shake is successfully detected.
            // If the time elapsed gets above 0.5s, reset the pattern tracker.
            // This ensures the shakes are in quick succession.
            shakeStartTime = currentTime

            shakePattern.add(direction)
            if (shakePattern.size > 4) shakePattern.removeAt(0) // Keep last 4 movements
        }
    }

    // Checks if the chop pattern (Right, Left, Right, Left) has been completed.
    // The opposite pattern is valid as well, in case your phone is facing the opposite
    // direction in the user's hand.
    private fun isChopPatternDetected(): Boolean {
        return shakePattern == listOf(1, -1, 1, -1) || shakePattern == listOf(-1, 1, -1, 1)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setSensitivity(value: Float) {
        shakeThreshold = value
    }

    fun setCooldown(value: Long) {
        cooldownTime = value
    }
}