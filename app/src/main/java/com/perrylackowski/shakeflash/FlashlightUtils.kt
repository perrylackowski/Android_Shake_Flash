package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FlashlightUtils(context: Context) {
    // References
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0] // Use the first camera (usually the back camera)

    // Settings variables
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn

    private val flashlightTimeout = SliderSetting(
        key = "flashlightTimeout",
        label = "How long before flashlight turns off automatically to conserve battery (minutes)",
        min = 1f,
        max = 120f,
        default = 15f,
        unitConversionFactor = 60000f //minutes to milliseconds
    )

    // Put all the settings in a list that can be passed to the composable
    val sliderSettings: List<SliderSetting<out Number>> = listOf(
        flashlightTimeout,
    )

    // Functional Variables
    private val handler = Handler(Looper.getMainLooper())
    private var offRunnable: Runnable? = null


    fun turnOnFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, true)
            _isFlashlightOn.value = true
            scheduleOffTimer()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false)
            _isFlashlightOn.value = false
            cancelOffTimer()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun toggleFlashlight() {
        if (_isFlashlightOn.value) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    private fun scheduleOffTimer() {
        cancelOffTimer() // Reset any existing timer
        offRunnable = Runnable { turnOffFlashlight() }
        handler.postDelayed(offRunnable!!, flashlightTimeout.toCodeUnits().toLong())
    }

    private fun cancelOffTimer() {
        offRunnable?.let { handler.removeCallbacks(it) }
        offRunnable = null
    }
}
