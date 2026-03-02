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

    fun saveUser(name: String, role: String, branchId: String?) {
        val editor = prefs.edit().putString("user_name", name).putString("user_role", role)
        if (branchId != null) {
            editor.putString("branch_id", branchId)
        } else {
            editor.remove("branch_id")
        }
        editor.apply()
    }

    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false
        try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            val payloadString = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val jsonObject = org.json.JSONObject(payloadString)
            val exp = jsonObject.getLong("exp")
            val currentSeconds = System.currentTimeMillis() / 1000
            return exp > currentSeconds
        } catch (e: Exception) {
            return false
        }
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun getUserName(): String? = prefs.getString("user_name", "User Name")
    
    fun getUserRole(): String? = prefs.getString("user_role", "Role")
    
    fun getBranchId(): String? = prefs.getString("branch_id", null)

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
