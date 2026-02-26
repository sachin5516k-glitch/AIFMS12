package com.aifranchise.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

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
data class LoginResponse(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val role: String,
    val outletId: String?,
    val token: String
)

// Sales
data class SalesRequest(
    val outletId: String,
    val amount: Double,
    val paymentMode: String,
    val imageUrl: String // URL from separate upload or Base64 for now
)
data class SalesResponse(
    @SerializedName("_id") val id: String, 
    val status: String, 
    val fraudScore: Int
)

// Inventory
data class InventoryItem(
    @SerializedName("_id") val id: String, 
    val name: String, 
    val lastStock: Int
)
data class InventoryRequest(
    val outletId: String,
    val items: List<InventoryUpdateItem>
)
data class InventoryUpdateItem(val itemId: String, val opening: Int, val closing: Int)

data class InventoryResponse(
    @SerializedName("_id") val id: String,
    val items: List<InventoryResultItem>
)
data class InventoryResultItem(
    val itemId: String,
    val opening: Int,
    val closing: Int,
    val variance: Int,
    @SerializedName("_id") val id: String
)

// Attendance
data class AttendanceRequest(
    val userId: String,
    val outletId: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String
)
data class AttendanceResponse(
    @SerializedName("_id") val id: String,
    val status: String, 
    val checkInTime: String?,
    val checkOutTime: String?
)

// AI Insights
data class AiInsightsResponse(
    val healthScore: Int,
    val fraudProbability: Int,
    val failureRisk: String, // LOW, MEDIUM, HIGH
    val topFactors: List<String>
)
