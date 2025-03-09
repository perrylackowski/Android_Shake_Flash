package com.perrylackowski.shakeflash

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class ShakeDetector : SensorEventListener {
    // Class references
    private val flashlightUtils = SingletonRepository.flashlightUtils

    // Settings variables
    private val shakeForceThreshold = SliderSetting(
        key = "shakeForceThreshold",
        label = "Shake force threshold (m/s^2, so 9.8 is one G)",
        min = 1.0f,
        max = 50.0f,
        default = 16.0f
    )

    private val maxTimeBetweenConsecutiveShakes = SliderSetting(
        key = "MaxTimeBetweenConsecutiveShakes",
        label = "Max time between consecutive shakes (seconds)",
        min = 0.1f,
        max = 1.0f,
        default = 0.5f,
        unitConversionFactor = 1000f // Convert seconds to milliseconds
    )

    private val cooldownTime = SliderSetting(
        key = "cooldownTime",
        label = "Cooldown between toggles (seconds)",
        min = 0.1f,
        max = 1f,
        default = 0.5f,
        unitConversionFactor = 1000f // Convert seconds to milliseconds
    )

    // Put all the settings in a list that can be passed to the composable
    val sliderSettings: List<SliderSetting<out Number>> = listOf(
        shakeForceThreshold,
        maxTimeBetweenConsecutiveShakes,
        cooldownTime,
    )

    // Functional variables
    private var lastShakeTime: Long = 0
    private var lastDirection: Int = 0 // -1 for down, 1 for up
    private var shakePattern = mutableListOf<Int>()
    private var shakeStartTime: Long = 0



    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()

            resetShakePatternIfNeeded(currentTime)

            if (currentTime - lastShakeTime < cooldownTime.toCodeUnits()) return // Ensure cooldown period

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
        if (currentTime - shakeStartTime > maxTimeBetweenConsecutiveShakes.toCodeUnits()) { //1000 converts seconds to milliseconds
            shakePattern.clear()
            Log.d("ShakeDetector", "Shake window exceeded, resetting pattern.")
        }
    }

    // Determines movement direction based on accelerometer values
    private fun detectShakeDirection(x: Float): Int {
        return when {
            x < -shakeForceThreshold.state.value -> -1
            x > shakeForceThreshold.state.value -> 1
            else -> 0
        }
    }

    // Handles tracking of shake motion and storing the pattern
    private fun processShakeDirection(direction: Int, currentTime: Long) {
        if (direction != lastDirection) { // Detects direction change
            lastDirection = direction

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

}