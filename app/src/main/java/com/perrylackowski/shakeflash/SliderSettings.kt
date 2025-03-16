package com.perrylackowski.shakeflash

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// This is the bare-bones class that is used to define each setting that will appear in the UI.
// Settings are then instantiated in the ShakeDetector and FlashlightUtils models.
data class SliderSetting<T : Number>(
    private val key: String,    //string for getting sharedPreferences key:value pair
    val label: String,  //Label that appears in the UI
    val min: T,
    val max: T,
    val default: T,
    val unitConversionFactor: Float = 1f,   //Multiply desired UI units (min or sec) by this factor to get code-required units (millisec)
    val converter: (Float) -> T = { it as T },       // Default: No conversion
    val reverser: (T) -> Float = { it.toFloat() },   // Default: No conversion
) {
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
    */

    private val prefs = ShakeFlashApp.sharedPreferences

    private val _preferredState = MutableStateFlow(prefs.getFloat(key, reverser(default)))
    private val _convertedState = MutableStateFlow(
        reverser(converter(_preferredState.value)) * unitConversionFactor
    )

    // The preferred state might be in minutes, while the converted state might be in milliseconds
    val preferredState: StateFlow<T> = _preferredState.map { converter(it) }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Lazily,
        initialValue = default
    )

    val convertedState: StateFlow<Float> = _convertedState

    fun setValue(value: T) {
        val preferredValue = reverser(value)
        _preferredState.value = preferredValue
        _convertedState.value = preferredValue * unitConversionFactor
        prefs.edit().putFloat(key, preferredValue).apply()
    }

    fun reset() {
        setValue(default)
    }

    // The model functions will use this to retrieve the current settings.
    fun toCodeUnits(): Float { //getConvertedValue
        return _convertedState.value
    }
}




