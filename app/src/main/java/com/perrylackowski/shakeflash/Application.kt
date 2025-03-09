package com.perrylackowski.shakeflash
import android.app.Application
import android.content.Context

// Only needed to pass the context of the application to the singleton repository.
// The flashlightUtils singleton needs access to the context so it can toggle the flashlight.
class ShakeFlashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SingletonRepository.initialize(this) // Pass application context
    }
}

object SingletonRepository {
    lateinit var flashlightUtils: FlashlightUtils
        private set // Prevent modification from outside

    val shakeDetector: ShakeDetector by lazy { ShakeDetector() }
    val StateModel: StateModel by lazy { StateModel() }

    fun initialize(context: Context) {
        flashlightUtils = FlashlightUtils(context.applicationContext) // Use application context
    }
}