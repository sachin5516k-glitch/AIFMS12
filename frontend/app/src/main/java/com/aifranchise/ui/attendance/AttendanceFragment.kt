package com.aifranchise.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentAttendanceBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttendanceFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels()
    private var capturedImageUrl: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    private var currentLat = 0.0
    private var currentLng = 0.0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                enableMyLocation()
            } else {
                Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapPlaceholder) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.btnCaptureSelfie.setOnClickListener {
            // Mock Camera Logic
            capturedImageUrl = "mock_selfie.jpg"
            binding.ivSelfie.setImageResource(R.drawable.ic_launcher_background)
            Toast.makeText(context, "Selfie Captured", Toast.LENGTH_SHORT).show()
        }

        binding.btnCheckIn.setOnClickListener {
            submitAttendance(true)
        }

        binding.btnCheckOut.setOnClickListener {
            submitAttendance(false)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.attendanceState.collect { state ->
                when (state) {
                    is ResultState.Loading -> binding.progressBar.isVisible = true
                    is ResultState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.btnCheckIn.isEnabled = true
                        val timeStr = state.data.checkInTime ?: state.data.checkOutTime ?: "Unknown Time"
                        Toast.makeText(context, "Attendance Marked: ${state.data.status} at $timeStr", Toast.LENGTH_LONG).show()
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                val currentLatLng = LatLng(currentLat, currentLng)
                googleMap?.addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitAttendance(isCheckIn: Boolean) {
        if (currentLat == 0.0 || currentLng == 0.0) {
            Toast.makeText(context, "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.markAttendance(isCheckIn, "user_123", "outlet_001", currentLat, currentLng, capturedImageUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
