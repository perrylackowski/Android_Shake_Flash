package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

class FlashlightUtils(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0] // Use the first camera (usually the back camera)

    fun turnOnFlashlight(): Boolean {
        try {
            cameraManager.setTorchMode(cameraId, true)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return true
    }

    fun turnOffFlashlight(): Boolean {
        try {
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    fun toggleFlashlight(isFlashlightOn: Boolean): Boolean {
        return if (isFlashlightOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }
}
