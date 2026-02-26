package com.aifranchise.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentAttendanceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels()
    private var capturedImageUrl: String? = null

    // Mock Location
    private val mockLat = 28.7041
    private val mockLng = 77.1025

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCaptureSelfie.setOnClickListener {
            // Mock Camera Logic
            capturedImageUrl = "mock_selfie.jpg"
            binding.ivSelfie.setImageResource(R.drawable.ic_launcher_background)
            Toast.makeText(context, "Selfie Captured", Toast.LENGTH_SHORT).show()
        }

        binding.btnCheckIn.setOnClickListener {
            viewModel.markAttendance(true, "user_123", "outlet_001", mockLat, mockLng, capturedImageUrl)
        }

        binding.btnCheckOut.setOnClickListener {
            viewModel.markAttendance(false, "user_123", "outlet_001", mockLat, mockLng, capturedImageUrl)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attendanceState.collect { state ->
                when (state) {
                    is ResultState.Loading -> binding.progressBar.isVisible = true
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnCheckIn.isEnabled = true
                        Toast.makeText(context, "Attendance Marked: ${state.data.status} at ${state.data.checkInTime}", Toast.LENGTH_LONG).show()
                    }
                    is ResultState.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
