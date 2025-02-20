package com.perrylackowski.shakeflash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.livedata.observeAsState
import com.perrylackowski.shakeflash.ui.theme.ShakeFlashTheme

// Model class representing the flashlight state
data class FlashlightModel(var isOn: Boolean = false,
                           var brightness: Float = 50.0f,
    )

class FlashlightViewModel3 : ViewModel() {
    private val _flashlight = MutableLiveData(FlashlightModel())
    val flashlight: LiveData<FlashlightModel> get() = _flashlight

    fun toggleFlashlight() {
        _flashlight.value = _flashlight.value?.copy(isOn = _flashlight.value?.isOn?.not() ?: false)
//        FlashlightService.setFlashlightState(_flashlight.value?.isOn ?: false)
    }

    fun setBrightness(value: Float) {
        val clampedValue = value.coerceIn(0.50f, 100.0f)
        _flashlight.value = _flashlight.value?.copy(brightness = clampedValue)
//        FlashlightService.setFlashlightBrightness(clampedValue)
    }
}

class MainActivity3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: FlashlightViewModel3 by viewModels()
        setContent {
            ShakeFlashTheme {
                FlashlightScreen(viewModel)
            }
        }
    }
}

@Composable
fun FlashlightScreen(viewModel: FlashlightViewModel3) {
    val flashlightState by viewModel.flashlight.observeAsState(initial = FlashlightModel())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (flashlightState.isOn) "Flashlight is ON" else "Flashlight is OFF")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.toggleFlashlight() }) {
            Text(text = "Toggle Flashlight")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = flashlightState.brightness,
            onValueChange = { viewModel.setBrightness(it) },
            valueRange = 0.50f..100.0f
        )
        Slider(
            value = flashlightState.brightness,
            onValueChange = { viewModel.setBrightness(it) },
            valueRange = 0.50f..100.0f
        )
    }
}
