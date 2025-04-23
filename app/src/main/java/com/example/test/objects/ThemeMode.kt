package com.example.test.objects

import android.content.Context
import android.content.SharedPreferences

class ThemeMode private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LIGHT_MODE = "is_light_mode"
        @Volatile private var instance: ThemeMode? = null

        fun getInstance(context: Context): ThemeMode {
            return instance ?: synchronized(this) {
                instance ?: ThemeMode(context.applicationContext).also { instance = it }
            }
        }
    }

    // Setter
    fun setIsLightMode(value: Boolean) {
        prefs.edit().putBoolean(IS_LIGHT_MODE, value).apply()
    }

    // Getter
    fun isLightMode(): Boolean {
        return prefs.getBoolean(IS_LIGHT_MODE, true) // Default to light mode
    }

    // Theme Colors
    val dark_primary = 0xFF05103A.toInt()
    val dark_secondary = 0xFF101C43.toInt()
    val dark_tertiary = 0xFF2A6FCF.toInt()
    val dark_text = 0xFF000000.toInt()

    val light_primary = 0xFFF9FAFB.toInt()
    val light_secondary = 0xFFFFFFFF.toInt()
    val light_text = 0xFFFFFFFF.toInt()



    val primary: Int
        get() = if (isLightMode()) light_primary else dark_primary

    val secondary: Int
        get() = if (isLightMode()) light_secondary else dark_secondary

    val tertiary: Int
        get() = dark_tertiary

    val fontColor : Int
        get() = if (isLightMode()) dark_text else light_text

    val buttonColor : Int
        get() = if (isLightMode()) dark_secondary else light_secondary

    val textButtonColor : Int
        get() = if (isLightMode()) light_text else dark_text


}
