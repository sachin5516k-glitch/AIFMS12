package com.aifranchise.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Sales
    @POST("sales/submit")
    suspend fun submitSales(@Body request: SalesRequest): SalesResponse

    // Inventory
    @GET("inventory/items")
    suspend fun getInventoryItems(): List<InventoryItem>
    
    @POST("inventory/submit")
    suspend fun submitInventory(@Body request: InventoryRequest): InventoryResponse

    // Attendance
    @POST("attendance/checkin")
    suspend fun checkIn(@Body request: AttendanceRequest): AttendanceResponse
    
    @POST("attendance/checkout")
    suspend fun checkOut(@Body request: AttendanceRequest): AttendanceResponse

    // AI Insights
    @GET("ai/insights/{outletId}")
    suspend fun getAiInsights(@Path("outletId") outletId: String): AiInsightsResponse
}

// Auth
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val user: UserDto)
data class UserDto(val id: String, val name: String, val role: String, val outletId: String?)

// Sales
data class SalesRequest(
    val outletId: String,
    val amount: Double,
    val paymentMode: String,
    val imageUrl: String // URL from separate upload or Base64 for now
)
data class SalesResponse(val id: String, val status: String, val fraudScore: Int)

// Inventory
data class InventoryItem(val id: String, val name: String, val lastStock: Int)
data class InventoryRequest(
    val outletId: String,
    val items: List<InventoryUpdateItem>
)
data class InventoryUpdateItem(val itemId: String, val opening: Int, val closing: Int)
data class InventoryResponse(val status: String, val varianceIds: List<String>)

// Attendance
data class AttendanceRequest(
    val userId: String,
    val outletId: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String
)
data class AttendanceResponse(val status: String, val time: String)

// AI Insights
data class AiInsightsResponse(
    val healthScore: Int,
    val fraudProbability: Int,
    val failureRisk: String, // LOW, MEDIUM, HIGH
    val topFactors: List<String>
)
