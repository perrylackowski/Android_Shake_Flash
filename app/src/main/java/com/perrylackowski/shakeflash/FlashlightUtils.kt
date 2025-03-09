package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FlashlightUtils(context: Context) {
    // Settings variables
    private var offDelay: Long = 600000 // Default delay: 10 minutes
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn

    // Functional Variables
    private val handler = Handler(Looper.getMainLooper())
    private var offRunnable: Runnable? = null

    // References
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0] // Use the first camera (usually the back camera)

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
        handler.postDelayed(offRunnable!!, offDelay)
    }

    private fun cancelOffTimer() {
        offRunnable?.let { handler.removeCallbacks(it) }
        offRunnable = null
    }

    fun setOffDelay(delay: Long) {
        offDelay = delay
    }
}
