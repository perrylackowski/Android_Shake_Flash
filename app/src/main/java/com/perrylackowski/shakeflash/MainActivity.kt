package com.perrylackowski.shakeflash

import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.perrylackowski.shakeflash.ui.theme.ShakeFlashTheme
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
//import androidx.compose.ui.tooling.preview.Preview
//import android.os.Build
//import android.provider.Settings
//import android.app.Activity
//import android.net.Uri
//import android.os.PowerManager

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var flashlightUtils: FlashlightUtils
    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var flashlightState: MutableState<Boolean>

    private val autoOffHandler = Handler(Looper.getMainLooper())
    private var autoOffRunnable: Runnable? = null

//    private var flashlightState: MutableState<Boolean> = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ShakeFlash", "onCreate")
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        flashlightUtils = FlashlightUtils(this)
        shakeDetector = ShakeDetector(this)
        sharedPreferences = getSharedPreferences("ShakeFlashPrefs", Context.MODE_PRIVATE)

        if (checkCameraPermission()) {
            startShakeService(this)
        } else {
            requestCameraPermission()
        }

        setContent {
            ShakeFlashTheme {
                // State variables
                var flashlightState = remember { mutableStateOf(flashlightUtils.isFlashlightOn) }
//                flashlightState.value = flashlightUtils.isFlashlightOn
                var offDelay by remember { mutableFloatStateOf(sharedPreferences.getFloat("offDelay", 10f)) }
                var cooldown by remember { mutableFloatStateOf(sharedPreferences.getFloat("cooldown", 1f)) }
                var sensitivity by remember { mutableFloatStateOf(sharedPreferences.getFloat("sensitivity", 10f)) }

                // Pass state & handlers to the UI
                FlashlightSettingsScreen(
                    flashlightState = flashlightState,
                    offDelay = offDelay,
                    cooldown = cooldown,
                    sensitivity = sensitivity,
                    onFlashlightToggle = {
                        flashlightUtils.toggleFlashlight()
                        Log.d("MainActivity", "Flashlight toggled: ${flashlightUtils.isFlashlightOn}")
                        flashlightState.value = flashlightUtils.isFlashlightOn
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
    override fun onStart() {
        super.onStart()
        Log.d("ShakeFlash", "onStart")
//        flashlightState.value = flashlightUtils.isFlashlightOn

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startShakeService(this)
            } else {
                Toast.makeText(this, "Camera permission is required to use the flashlight.", Toast.LENGTH_LONG).show()
                finish() // Close the app if permission is denied
            }
        }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        Toast.makeText(this, "Permissions needed to use this app. Internally, the flashlight is part of the Camera system, so it needs full camera permissions to work properly.", Toast.LENGTH_LONG).show()
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

}



// User Interface
@Composable
fun FlashlightSettingsScreen(
    flashlightState: State<Boolean>,
    offDelay: Float,
    cooldown: Float,
    sensitivity: Float,
    onFlashlightToggle: () -> Unit,
    onOffDelayChange: (Float) -> Unit,
    onCooldownChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onResetDefaults: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (flashlightState.value) "Flashlight is ON" else "Flashlight is OFF",
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

fun startShakeService(context: Context) {
    val serviceIntent = Intent(context, ShakeDetectionService::class.java)
    ContextCompat.startForegroundService(context, serviceIntent)
}

fun stopShakeService(context: Context) {
    val serviceIntent = Intent(context, ShakeDetectionService::class.java)
    context.stopService(serviceIntent)
}

//fun requestBatteryOptimizationPermission(activity: Activity) {
//    val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
//    val packageName = activity.packageName
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
//            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
//                data = Uri.parse("package:$packageName")
//            }
//            activity.startActivity(intent)
//        }
//    }
//}

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