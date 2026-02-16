package com.aifranchise.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aifranchise.data.remote.AttendanceRequest
import com.aifranchise.data.remote.AttendanceResponse
import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _attendanceState = MutableStateFlow<ResultState<AttendanceResponse>?>(null)
    val attendanceState = _attendanceState.asStateFlow()

    fun markAttendance(isCheckIn: Boolean, userId: String, outletId: String, lat: Double, lng: Double, imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            _attendanceState.value = ResultState.Error(Exception("Selfie is required"))
            return
        }
        
        // Mock Location Validation (Radius check would happen here or in Repo)
        if (lat == 0.0 || lng == 0.0) {
             _attendanceState.value = ResultState.Error(Exception("Invalid Location"))
             return
        }

        val request = AttendanceRequest(userId, outletId, lat, lng, imageUrl)

        viewModelScope.launch {
            if (isCheckIn) {
                repository.checkIn(request).collect { _attendanceState.value = it }
            } else {
                repository.checkOut(request).collect { _attendanceState.value = it }
            }
        }
    }
}
