package com.perrylackowski.shakeflash

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

class FlashlightUtils(context: Context) {
    var isFlashlightOn = false
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0] // Use the first camera (usually the back camera)

    fun turnOnFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, true)
            isFlashlightOn = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false)
            isFlashlightOn = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

}
