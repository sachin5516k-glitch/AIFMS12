package com.aifranchise.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun saveUser(name: String, role: String) {
        prefs.edit().putString("user_name", name).putString("user_role", role).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun getUserName(): String? = prefs.getString("user_name", "User Name")
    
    fun getUserRole(): String? = prefs.getString("user_role", "Role")

    fun clearToken() {
        prefs.edit().clear().apply()
    }

    fun forceLogout() {
        clearToken()
        val intent = android.content.Intent(context, com.aifranchise.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}
