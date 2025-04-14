package com.example.test.objects

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PinManager {

    private const val PREFS_NAME = "SECURE_PREFS"
    private const val PIN_KEY = "user_pin"
    private const val PIN_SET_KEY = "is_pin_set"

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun savePin(context: Context, pin: String) {
        val editor = getEncryptedPrefs(context).edit()
        editor.putString(PIN_KEY, pin)
        editor.putBoolean(PIN_SET_KEY, true)
        editor.apply()
    }

    fun isPinSet(context: Context): Boolean {
        return getEncryptedPrefs(context).getBoolean(PIN_SET_KEY, false)
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val storedPin = getEncryptedPrefs(context).getString(PIN_KEY, null)
        return storedPin == inputPin
    }

    fun removePin(context: Context) {
        val editor = getEncryptedPrefs(context).edit()
        editor.remove(PIN_KEY)
        editor.putBoolean(PIN_SET_KEY, false)
        editor.apply()
    }
}
