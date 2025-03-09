package com.perrylackowski.shakeflash
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

// Only needed to pass the context of the application to the singleton repository.
// The flashlightUtils singleton needs access to the context so it can toggle the flashlight.
class ShakeFlashApp : Application() {

    companion object {
        lateinit var sharedPreferences: SharedPreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        SingletonRepository.initialize(this) // Pass application context
    }
}

object SingletonRepository {
    lateinit var flashlightUtils: FlashlightUtils
        private set // Prevent modification from outside

    val shakeDetector: ShakeDetector by lazy { ShakeDetector() }

    fun initialize(context: Context) {
        flashlightUtils = FlashlightUtils(context.applicationContext) // Use application context
    }
}