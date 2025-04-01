package com.example.test.objects

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject

object TokenManager {

    private const val PREFS_NAME = "TOKEN_PREFS"
    private const val TOKEN_KEY = "token"

    private fun getPrefs(context: Context) : SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token : String) {
        val editor = getPrefs(context).edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(context: Context) : String ? {
        return getPrefs(context).getString(TOKEN_KEY, null)
    }

    fun removeToken(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }


    fun isAuthenticated(context: Context) : Boolean {
        return getToken(context) != null
    }

    fun getUsername(context: Context): String? {
        val token = getToken(context) ?: return null

        // Split the token into parts: header, payload, and signature
        val parts = token.split(".")

        if (parts.size != 3) {
            return null // Invalid token format
        }

        // Decode the payload (second part of the token)
        val payload = parts[1]
        val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)

        // Parse the JSON payload to extract the username
        val jsonObject = Gson().fromJson(decodedPayload, JsonObject::class.java)
        return jsonObject.get("username")?.asString
    }
}