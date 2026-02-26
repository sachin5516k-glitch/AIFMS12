package com.aifranchise.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    // Sales
    @POST("sales/submit")
    suspend fun submitSales(@Body request: SalesRequest): ApiResponse<SalesResponse>

    // Inventory
    @GET("inventory/items")
    suspend fun getInventoryItems(): ApiResponse<List<InventoryItem>>
    
    @POST("inventory/submit")
    suspend fun submitInventory(@Body request: InventoryRequest): ApiResponse<InventoryResponse>

    // Attendance
    @POST("attendance/checkin")
    suspend fun checkIn(@Body request: AttendanceRequest): ApiResponse<AttendanceResponse>
    
    @POST("attendance/checkout")
    suspend fun checkOut(@Body request: AttendanceRequest): ApiResponse<AttendanceResponse>

    // AI Insights
    @GET("ai/insights/{outletId}")
    suspend fun getAiInsights(@Path("outletId") outletId: String): ApiResponse<AiInsightsResponse>
}

// Auth
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val name: String,
    val role: String,
    val branchId: String?
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
