package com.perrylackowski.shakeflash

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SliderSetting<T : Number>(
    /* EXAMPLES
        val maxTimeBetweenConsecutiveShakes = SliderSetting(
        label = "Max time between consecutive shakes (seconds)",
        min = 0.1f,
        max = 1.0f,
        default = 0.5f,
        key = "MaxTimeBetweenConsecutiveShakes",
        prefs = prefs,
        converter = { it }, // For floats, no conversion is needed
        reverser = { it }
    )

    //Example for integer settings which need to convert to floats to work with the slider components
    val shakeCountThreshold = SliderSetting(
        label = "Shake count threshold",
        min = 1,
        max = 10,
        default = 3,
        key = "ShakeCountThreshold",
        prefs = prefs,
        converter = { it.toInt() }, // Convert `Float` to `Int`
        reverser = { it.toFloat() } // Convert `Int` to `Float`
    )

    //Example of unit conversion
    val maxTimeInactivity = SliderSetting(
    label = "Max inactivity time (minutes)",
    min = 0.1f, // 6 seconds in minutes
    max = 10f, // 10 minutes
    default = 1f, // 1 minute
    key = "MaxTimeInactivity",
    prefs = prefs,
    toDisplay = { it / 60f }, // Convert milliseconds to minutes
    fromDisplay = { it * 60f } // Convert minutes to milliseconds
    )

    */

    private val key: String,
    val label: String,
    val min: T,
    val max: T,
    val default: T,
    val unitConversionFactor: Float = 1f,
    val converter: (Float) -> T = { it as T },       // Default: No conversion
    val reverser: (T) -> Float = { it.toFloat() },   // Default: No conversion
//    val toDisplay: (T) -> T = { it },               // Default: No transformation
//    val fromDisplay: (T) -> T = { it }  ,            // Default: No transformation
) {
    private val prefs = ShakeFlashApp.sharedPreferences
    private val _state = MutableStateFlow(prefs.getFloat(key, reverser(default)))

    val state: StateFlow<T> = _state.map { converter(it) }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Lazily,
        initialValue = default
    )
    fun setValue(value: T) {
        _state.value = reverser(value)
        prefs.edit().putFloat(key, reverser(value)).apply()
    }
    fun reset() {
        setValue(default)
    }
    fun toCodeUnits(): Float {
        return state.value.toFloat() * unitConversionFactor
    }

}





