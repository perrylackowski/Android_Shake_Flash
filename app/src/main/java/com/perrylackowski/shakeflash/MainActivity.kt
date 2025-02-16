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
import androidx.compose.ui.tooling.preview.Preview

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
                // State variables
                var flashlightState by remember { mutableStateOf(false) }
                var offDelay by remember { mutableFloatStateOf(sharedPreferences.getFloat("offDelay", 10f)) }
                var cooldown by remember { mutableFloatStateOf(sharedPreferences.getFloat("cooldown", 1f)) }
                var sensitivity by remember { mutableFloatStateOf(sharedPreferences.getFloat("sensitivity", 10f)) }

                // Register sensors
                LaunchedEffect(Unit) {
                    sensorManager.registerListener(
                        shakeDetector,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI
                    )

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

                // Pass state & handlers to the UI
                FlashlightSettingsScreen(
                    flashlightState = flashlightState,
                    offDelay = offDelay,
                    cooldown = cooldown,
                    sensitivity = sensitivity,
                    onFlashlightToggle = {
                        flashlightState = flashlightUtils.toggleFlashlight(flashlightState)
                    },
                    onOffDelayChange = {
                        offDelay = it
                        sharedPreferences.edit().putFloat("offDelay", it).apply()
                    },
                    onCooldownChange = {
                        cooldown = it
                        sharedPreferences.edit().putFloat("cooldown", it).apply()
                        shakeDetector.setCooldown((it * 1000).toLong())
                    },
                    onSensitivityChange = {
                        sensitivity = it
                        sharedPreferences.edit().putFloat("sensitivity", it).apply()
                        shakeDetector.setSensitivity(it)
                    },
                    onResetDefaults = {
                        offDelay = 10f
                        cooldown = 1f
                        sensitivity = 10f
                        sharedPreferences.edit().clear().apply()
                    }
                )
            }
        }
    }
}

// User Interface
@Composable
fun FlashlightSettingsScreen(
    flashlightState: Boolean,
    offDelay: Float,
    cooldown: Float,
    sensitivity: Float,
    onFlashlightToggle: () -> Unit,
    onOffDelayChange: (Float) -> Unit,
    onCooldownChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onResetDefaults: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (flashlightState) "Flashlight is ON" else "Flashlight is OFF",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onFlashlightToggle) {
                Text(text = "Toggle Flashlight")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sliders for settings
            SettingSlider("Off Delay (minutes)", offDelay, 0.5f, 600f, onOffDelayChange)
            SettingSlider("Cooldown (seconds)", cooldown, 0.25f, 2f, onCooldownChange)
            SettingSlider("Sensitivity", sensitivity, 5f, 25f, onSensitivityChange)

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onResetDefaults) {
                Text(text = "Reset to Defaults")
            }
        }
    }
}

//User Interface (repeated slider component)
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

//// This is an empty sample of the UI so that you can view it in the previewer
//@Preview(showBackground = true)
//@Composable
//fun PreviewFlashlightSettingsScreen() {
//    ShakeFlashTheme {
//        FlashlightSettingsScreen(
//            flashlightState = false,
//            offDelay = 10f,
//            cooldown = 1f,
//            sensitivity = 10f,
//            onFlashlightToggle = {},
//            onOffDelayChange = {},
//            onCooldownChange = {},
//            onSensitivityChange = {},
//            onResetDefaults = {}
//        )
//    }
//}