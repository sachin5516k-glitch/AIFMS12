package com.aifranchise.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

interface ApiService {
    
    @GET("ping")
    suspend fun pingServer(): okhttp3.ResponseBody
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    // Sales
    @POST("sales/submit")
    suspend fun submitSales(@Body request: SalesRequest): ApiResponse<SalesResponse>

    // Items
    @GET("items")
    suspend fun getItems(): ApiResponse<List<ItemDto>>

    // Inventory
    @GET("inventory/items")
    suspend fun getInventoryItems(): ApiResponse<List<InventoryItem>>
    
    @GET("inventory/items")
    suspend fun getBranchInventoryItems(@retrofit2.http.Query("branchId") branchId: String): ApiResponse<List<InventoryItem>>
    
    @POST("inventory/submit")
    suspend fun submitInventory(@Body request: InventoryRequest): ApiResponse<InventoryResponse>

    // Attendance
    @POST("attendance/checkin")
    suspend fun checkIn(@Body request: AttendanceRequest): ApiResponse<AttendanceResponse>
    
    @POST("attendance/checkout")
    suspend fun checkOut(@Body request: AttendanceRequest): ApiResponse<AttendanceResponse>

    @GET("ai/insights/{outletId}")
    suspend fun getAiInsights(@Path("outletId") outletId: String): ApiResponse<AiInsightsResponse>

    @GET("analytics/item-sales")
    suspend fun getItemSales(): ApiResponse<List<AnalyticsItemSalesDto>>

    // Dashboard Integration
    @GET("dashboard/manager")
    suspend fun getManagerDashboard(): ApiResponse<DashboardSummaryResponse>

    @GET("admin/dashboard-summary")
    suspend fun getAdminDashboard(): ApiResponse<AdminDashboardSummaryResponse>

    @GET("admin/inventory-summary")
    suspend fun getAdminInventorySummary(): ApiResponse<AdminInventorySummaryResponse>

    // Branches
    @GET("branches")
    suspend fun getBranches(): ApiResponse<List<BranchDto>>

    @POST("branches")
    suspend fun addBranch(@Body request: AddBranchRequest): ApiResponse<BranchDto>

    @GET("branches/{id}")
    suspend fun getBranchById(@Path("id") id: String): ApiResponse<BranchDetailDto>

    // Transfers
    @GET("transfers/recommendations")
    suspend fun getTransferRecommendations(): ApiResponse<List<TransferRecommendationDto>>

    @POST("admin/stock-transfer/request")
    suspend fun createManualTransfer(@Body request: ManualTransferRequest): ApiResponse<TransferRecommendationDto>

    // Admin
    @GET("admin/managers")
    suspend fun getManagers(): ApiResponse<List<ManagerDto>>

    @POST("admin/managers")
    suspend fun addManager(@Body request: AddManagerRequest): ApiResponse<ManagerDto>

    @PUT("admin/managers/{id}/deactivate")
    suspend fun deactivateManager(@Path("id") id: String): ApiResponse<ManagerDto>
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

data class BranchDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val location: LocationDto?,
    val status: String,
    val healthStatus: String? = null
)

data class BranchDetailDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val location: LocationDto?,
    val status: String,
    val managerName: String,
    val staffCount: Int,
    val totalSales: Double,
    val stockHealth: String
)

data class LocationDto(
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class AddBranchRequest(
    val name: String,
    val location: LocationDto
)

// Sales
data class SalesRequest(
    val itemId: String,
    val quantitySold: Int,
    val paymentMode: String
)
data class SalesResponse(
    @SerializedName("_id") val id: String,
    val totalAmount: Double? = null,
    val quantitySold: Int? = null
)

// Items
data class ItemDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val category: String? = null,
    val unitPrice: Double
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
data class InventoryUpdateItem(val itemId: String, val quantityAdded: Int)

data class InventoryResponse(
    @SerializedName("_id") val id: String,
    val items: List<InventoryResultItem>
)
data class InventoryResultItem(
    val closingStock: Int,
    val sales: Int,
    val transferOut: Int,
    val transferIn: Int,
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

data class AnalyticsItemSalesDto(
    val itemName: String,
    val totalSales: Int
)

// Dashboard
data class DashboardSummaryResponse(
    val profitability: Profitability?,
    val incomingTransfers: Int?,
    val outgoingTransfers: Int?
)

data class AdminDashboardSummaryResponse(
    val profitability: Profitability?,
    val branchCount: Int?,
    val attendancePercentage: Int?,
    val inventoryStatus: AdminInventoryStatus?
)

data class AdminInventoryStatus(
    val healthyPercentage: Int,
    val lowStockItems: Int,
    val totalItems: Int
)

data class Profitability(
    val revenue: Double,
    val profitPercentage: String
)

data class AdminInventorySummaryResponse(
    val totalItems: Int,
    val lowStockCount: Int,
    val healthyPercentage: Int,
    val totalValuation: Double?
)

data class TransferRecommendationDto(
    @SerializedName("_id") val id: String,
    val fromBranchId: BranchRefDto?,
    val toBranchId: BranchRefDto?,
    val itemId: ItemRefDto?,
    val suggestedQuantity: Int?,
    val quantity: Int?, // For manual ones
    val distanceKm: Double?,
    val status: String,
    val reason: String?
)

data class ManualTransferRequest(
    val fromBranchId: String,
    val toBranchId: String,
    val itemId: String,
    val quantity: Int
)
data class BranchRefDto(@SerializedName("_id") val id: String, val name: String)
data class ItemRefDto(@SerializedName("_id") val id: String, val name: String)

data class ManagerDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String?,
    val role: String,
    val branchId: BranchRefDto?,
    val status: String,
    val createdAt: String?
)

data class AddManagerRequest(
    val name: String,
    val email: String,
    val password: String,
    val branchId: String
)
