package com.perrylackowski.shakeflash

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.perrylackowski.shakeflash.ui.theme.ShakeFlashTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var flashlightUtils: FlashlightUtils
    private lateinit var sharedPreferences: SharedPreferences

    private val autoOffHandler = Handler(Looper.getMainLooper())
    private var autoOffRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        flashlightUtils = FlashlightUtils(this)
        shakeDetector = ShakeDetector(this)
        sharedPreferences = getSharedPreferences("ShakeFlashPrefs", Context.MODE_PRIVATE)

        setContent {
            ShakeFlashTheme {
                var flashlightState by remember { mutableStateOf(false) }
                var offDelay by remember { mutableStateOf(sharedPreferences.getFloat("offDelay", 10f)) }
                var cooldown by remember { mutableStateOf(sharedPreferences.getFloat("cooldown", 1f)) }
                var sensitivity by remember { mutableStateOf(sharedPreferences.getFloat("sensitivity", 10f)) }

                LaunchedEffect(Unit) {
                    sensorManager.registerListener(shakeDetector,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI)

                    shakeDetector.setSensitivity(sensitivity)
                    shakeDetector.setCooldown((cooldown * 1000).toLong())

                    shakeDetector.onShakeDetected = {
                        flashlightState = !flashlightState
                        if (flashlightState) {
                            flashlightUtils.turnOnFlashlight()
                            autoOffRunnable?.let { autoOffHandler.removeCallbacks(it) }
                            autoOffRunnable = Runnable {
                                flashlightState = false
                                flashlightUtils.turnOffFlashlight()
                            }
                            autoOffHandler.postDelayed(autoOffRunnable!!, (offDelay * 60 * 1000).toLong())
                        } else {
                            flashlightUtils.turnOffFlashlight()
                            autoOffRunnable?.let { autoOffHandler.removeCallbacks(it) }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        Text(
                            text = if (flashlightState) "Flashlight is ON" else "Flashlight is OFF",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            flashlightState = flashlightUtils.toggleFlashlight(flashlightState)
                        }) {
                            Text(text = "Toggle Flashlight")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Sliders for settings
                        SettingSlider("Off Delay (minutes)", offDelay, 2.5f, 20f) {
                            offDelay = it
                            sharedPreferences.edit().putFloat("offDelay", it).apply()
                        }
                        SettingSlider("Cooldown (seconds)", cooldown, 0.25f, 2f) {
                            cooldown = it
                            sharedPreferences.edit().putFloat("cooldown", it).apply()
                            shakeDetector.setCooldown((it * 1000).toLong())
                        }
                        SettingSlider("Sensitivity", sensitivity, 2.5f, 20f) {
                            sensitivity = it
                            sharedPreferences.edit().putFloat("sensitivity", it).apply()
                            shakeDetector.setSensitivity(it)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            offDelay = 10f
                            cooldown = 1f
                            sensitivity = 10f
                            sharedPreferences.edit().clear().apply()
                        }) {
                            Text(text = "Reset to Defaults")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSlider(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "$label: ${"%.2f".format(value)}")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth()
        )
    }
}