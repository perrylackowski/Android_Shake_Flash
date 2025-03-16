package com.perrylackowski.shakeflash

//TODO: Figure out how to exclude .idea folder from github?
//TODO: App fails to launch if permissions are initially granted but then taken away through the settings.


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.perrylackowski.shakeflash.ui.theme.ShakeFlashTheme

class MainActivity : ComponentActivity() {

    private val sharedPreferences = ShakeFlashApp.sharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ShakeFlash", "onCreate")
        super.onCreate(savedInstanceState)

        // Use the single instances from SingletonRepository
        val shakeDetector = SingletonRepository.shakeDetector
        val flashlightUtils = SingletonRepository.flashlightUtils

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
                // Pass state & handlers to the UI
                FlashlightSettingsScreen(
                    shakeDetectionSliders = shakeDetector.sliderSettings,
                    flashlightUtilsSliders = flashlightUtils.sliderSettings,
                    flashlightState = flashlightState,
                    onFlashlightToggle = {
                        flashlightUtils.toggleFlashlight()
                    },
                    onResetDefaults = {
                        sharedPreferences.edit().clear().apply()
                        shakeDetector.sliderSettings.forEach { it.reset() }
                        flashlightUtils.sliderSettings.forEach { it.reset() }
                        Log.d("MainActivity", "Settings reset to defaults")
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
    onFlashlightToggle: () -> Unit,
    shakeDetectionSliders: List<SliderSetting<out Number>>,
    flashlightUtilsSliders: List<SliderSetting<out Number>>,
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
                        "two times to enable the flashlight.",
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

            // Sliders for flashlight settings
            flashlightUtilsSliders.forEach { slider ->
                SettingSlider(slider)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Shake Detection Settings",
                style = MaterialTheme.typography.titleLarge
            )

            // Sliders for shake detection settings
            shakeDetectionSliders.forEach { slider ->
                SettingSlider(slider)
            }

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
                Text(text = "Buy me a coffee  ☕")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun <T : Number> SettingSlider(setting: SliderSetting<T>) {
    val value by setting.preferredState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${setting.label}: ${"%.2f".format(value)}")
        Slider(
            value = setting.reverser(value),
            onValueChange = { setting.setValue(setting.converter(it)) },
            valueRange = setting.reverser(setting.min)..setting.reverser(setting.max),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


/*
// This WAS an empty sample of the UI so that you can view it in the previewer
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