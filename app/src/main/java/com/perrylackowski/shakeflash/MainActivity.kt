package com.perrylackowski.shakeflash

//TODO: Pick better default ranges for the timer?
//TODO: Figure out how to exclude .idea folder from github?

//TODO: Set up a class that stores the default states for the settings variables. They are currently set in 5-6 different places.
//TODO: Add an additional setting for maxTimeBetweenConsecutiveShakes

//TODO: App fails to launch if permissions are initially granted but then taken away through the settings.


import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
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
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.app.AlertDialog
import android.content.DialogInterface

class MainActivity : ComponentActivity() {

    private val sharedPreferences = ShakeFlashApp.sharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ShakeFlash", "onCreate")
        super.onCreate(savedInstanceState)

        // Use the single instances from SingletonRepository
        val shakeDetector = SingletonRepository.shakeDetector
        val flashlightUtils = SingletonRepository.flashlightUtils
        val stateModel = SingletonRepository.StateModel



        if (checkCameraPermission()) {
            startShakeService()
        } else {
            requestCameraPermission()
        }

        // Initialize UI by passing variables and setting up listener functions.
        setContent {
            ShakeFlashTheme {
                // State variables
                val flashlightState by flashlightUtils.isFlashlightOn.collectAsState()
                val maxTimeBetweenConsecutiveShakes by shakeDetector.maxTimeBetweenConsecutiveShakes.collectAsState()
                var offDelay by remember { mutableFloatStateOf((sharedPreferences.getLong("offDelay", 600000)/60000f)) }
                var cooldown by remember { mutableFloatStateOf((sharedPreferences.getLong("cooldown", 1000)/1000f)) }
                var sensitivity by remember { mutableFloatStateOf(sharedPreferences.getFloat("sensitivity", 10f)) }

                // Pass state & handlers to the UI
                FlashlightSettingsScreen(
                    flashlightState = flashlightState,
                    offDelay = offDelay,
                    cooldown = cooldown,
                    sensitivity = sensitivity,
                    maxTimeBetweenConsecutiveShakes = maxTimeBetweenConsecutiveShakes,
                    onFlashlightToggle = {
                        flashlightUtils.toggleFlashlight()
                    },
                    onOffDelayChange = {
                        // Update the UI
                        offDelay = it
                        // Convert from minutes to milliseconds
                        val offDelayInMillSecs = (it * 60000f).toLong()
                        // Update the preferences in storage
                        sharedPreferences.edit().putLong("offDelay", offDelayInMillSecs).apply()
                        // Update the private variable currently used by the running service.
                        flashlightUtils.setOffDelay(offDelayInMillSecs)
                    },
                    onCooldownChange = {
                        cooldown = it
                        //Convert from seconds to milliseconds
                        val cooldownInMillSecs = (it * 1000f).toLong()
                        sharedPreferences.edit().putLong("cooldown", cooldownInMillSecs).apply()
                        shakeDetector.setCooldown(cooldownInMillSecs)
                    },
                    onSensitivityChange = {
                        sensitivity = it
                        sharedPreferences.edit().putFloat("sensitivity", it).apply()
                        shakeDetector.setSensitivity(it)
                    },
                    onMaxTimeBetweenConsecutiveShakesChange = {
                        shakeDetector.setMaxTimeBetweenConsecutiveShakes(it)
                    },
                    onResetDefaults = {
                        sharedPreferences.edit().clear().apply()
                        shakeDetector.setMaxTimeBetweenConsecutiveShakes(0.5f)
                        // Manually update state variables
                        offDelay = 10f
                        cooldown = 1f
                        sensitivity = 10f

                        // Manually update SharedPreferences
                        sharedPreferences.edit()
                            .putLong("offDelay", 600000) //10 min
                            .putLong("cooldown", 1000) //1 sec
                            .putFloat("sensitivity", 10f) //10 G's
                            .apply()

                        // Manually update the service instances
                        flashlightUtils.setOffDelay(600000)
                        shakeDetector.setCooldown(1000)
                        shakeDetector.setSensitivity(10f)

                        Log.d("MainActivity", "Defaults reset: OffDelay=10, Cooldown=1, Sensitivity=10")
                    }
                )
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestCameraPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Needed")
        builder.setMessage("Permissions are needed to use this app. In Android, the flashlight " +
                "is part of the camera's flash mechanism, so the app requires camera permissions " +
                "in order to function. Please choose \"While using the app\" on the next screen."
        )
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // Close the dialog when OK is clicked
            requestPermissionLauncher.launch(Manifest.permission.CAMERA) // Launch the permission request
        }
        builder.setCancelable(false) // Prevent the dialog from being dismissed without clicking OK
        builder.create().show()

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startShakeService()
            } else {
                Toast.makeText(this,
                    "Camera permission is required to use the flashlight.",
                    Toast.LENGTH_LONG).show()
                finish() // Close the app if permission is denied
            }
        }

    private fun startShakeService() {
        val serviceIntent = Intent(this, ShakeDetectionService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopShakeService() {
        val serviceIntent = Intent(this, ShakeDetectionService::class.java)
        this.stopService(serviceIntent)
    }
}

// User Interface
@Composable
fun FlashlightSettingsScreen(
    flashlightState: Boolean,
    offDelay: Float,
    cooldown: Float,
    sensitivity: Float,
    maxTimeBetweenConsecutiveShakes: Float,
    onFlashlightToggle: () -> Unit,
    onOffDelayChange: (Float) -> Unit,
    onCooldownChange: (Float) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onMaxTimeBetweenConsecutiveShakesChange: (Float) -> Unit,
    onResetDefaults: () -> Unit
) {
        val context = LocalContext.current

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shake Flash",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "How to Use",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Holding your phone in your hand, swing your arm in a chopping motion " +
                        "two times in a row to toggle the flashlight.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (flashlightState) "Flashlight is ON" else "Flashlight is OFF",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onFlashlightToggle) {
                Text(text = "Toggle Flashlight")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Flashlight Settings",
                style = MaterialTheme.typography.titleLarge
            )

            // Sliders for settings
            SettingSlider("Automatically turn flashlight off after X minutes", offDelay, 0.5f, 600f, onOffDelayChange)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Chop Detection Settings",
                style = MaterialTheme.typography.titleLarge
            )

            SettingSlider("Cooldown between toggles (seconds)", cooldown, 0.25f, 2f, onCooldownChange)
            SettingSlider("Sensitivity (Gs of shake force)", sensitivity, 5f, 25f, onSensitivityChange)
            SettingSlider("Max time between consecutive shakes (seconds)", maxTimeBetweenConsecutiveShakes, 0.1f, 2f, onMaxTimeBetweenConsecutiveShakesChange)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onResetDefaults) {
                Text(text = "Reset to Defaults")
            }

            Spacer(modifier = Modifier.height(96.dp))

            Text(
                text = "This project is a labor of love. It is open source, and will remain " +
                        "completely ad free. If you would like to show your support, " +
                        "use the link below!",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://buymeacoffee.com/perrylackowski"))
                context.startActivity(intent)
            }) {
                Text(text = "Support me  ☕")
            }

            Spacer(modifier = Modifier.height(32.dp))
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

/*
// This is an empty sample of the UI so that you can view it in the previewer
@Preview(showBackground = true)
@Composable
fun PreviewFlashlightSettingsScreen() {
    ShakeFlashTheme {
        FlashlightSettingsScreen(
            flashlightState = false,
            offDelay = 10f,
            cooldown = 1f,
            sensitivity = 10f,
            onFlashlightToggle = {},
            onOffDelayChange = {},
            onCooldownChange = {},
            onSensitivityChange = {},
            onResetDefaults = {}
        )
    }
}
*/

// Might want to enable these permissions if the app stops working after a few days.
/*
fun requestBatteryOptimizationPermission(activity: Activity) {
    val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = activity.packageName

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            activity.startActivity(intent)
        }
    }
}
*/