package com.perrylackowski.shakeflash

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.SharedPreferences

class FlashlightViewModel(application: Application, context: Context) : AndroidViewModel(application) {
//    private val flashlightUtils = FlashlightUtils(context)
//    private val sharedPreferences: SharedPreferences =
//        context.getSharedPreferences("ShakeFlashPrefs", Context.MODE_PRIVATE)
//
//    private val _flashlightState = MutableStateFlow(sharedPreferences.getBoolean("flashlightState", false))
//    val flashlightState: StateFlow<Boolean> = _flashlightState.asStateFlow()
//
//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == "FLASHLIGHT_STATE_UPDATED") {
//                val isOn = intent.getBooleanExtra("flashlightState", false)
//                _flashlightState.value = isOn
//
//                // Save flashlight state to SharedPreferences
//                sharedPreferences.edit().putBoolean("flashlightState", isOn).apply()
//            }
//        }
//    }
//
//    init {
//        val filter = IntentFilter("FLASHLIGHT_STATE_UPDATED")
//        ContextCompat.registerReceiver(
//            context,
//            broadcastReceiver,
//            filter,
//            ContextCompat.RECEIVER_NOT_EXPORTED
//        )
//    }
//
//    fun toggleFlashlight(state: Boolean) {
//        flashlightUtils.toggleFlashlight()
//        _flashlightState.value = state
//
//        // Save flashlight state to SharedPreferences
//        sharedPreferences.edit().putBoolean("flashlightState", state).apply()
//    }
}
