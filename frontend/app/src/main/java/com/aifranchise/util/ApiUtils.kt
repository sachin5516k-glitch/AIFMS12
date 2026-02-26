package com.aifranchise.util

import com.aifranchise.data.remote.ApiResponse
import com.google.gson.Gson
import retrofit2.HttpException

object ApiUtils {
    fun parseError(exception: Exception): String {
        if (exception is HttpException) {
            try {
                val errorBody = exception.response()?.errorBody()?.string()
                if (errorBody != null) {
                    // Extract success/message details
                    val response = Gson().fromJson(errorBody, ApiResponse::class.java)
                    if (response.message != null) {
                        return response.message
                    }
                }
            } catch (ignore: Exception) {
                // Fallback to default message
            }
            return "HTTP Error ${exception.code()}: ${exception.message()}"
        }
        return exception.message ?: "Unknown error occurred"
    }
}
