package com.aifranchise.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentAttendanceBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// OSMDroid imports
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView

    private var currentLat = 10.060 // Default Karaikudi
    private var currentLng = 78.790

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
                showKaraikudi()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Init OSMDroid configuration
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
        
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        checkLocationPermission()

        // Start Map Shimmer
        ObjectAnimator.ofFloat(binding.vMapShimmer, "alpha", 1f, 0.5f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
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
                        playSuccessRipple(state.data.status ?: "Logged", timeStr)
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
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
                val point = GeoPoint(currentLat, currentLng)
                val marker = Marker(mapView)
                marker.position = point
                marker.title = "Your Location"
                mapView.overlays.add(marker)
                mapView.controller.setCenter(point)
                
                binding.vMapShimmer.animate().alpha(0f).setDuration(500).withEndAction {
                    binding.vMapShimmer.isVisible = false
                }.start()
            } else {
                Toast.makeText(context, "Unable to get location, showing Karaikudi", Toast.LENGTH_SHORT).show()
                showKaraikudi()
            }
        }
    }
    
    private fun showKaraikudi() {
        val point = GeoPoint(10.060, 78.790) // Karaikudi
        val marker = Marker(mapView)
        marker.position = point
        marker.title = "Karaikudi, Tamil Nadu"
        mapView.overlays.add(marker)
        mapView.controller.setCenter(point)
        
        binding.vMapShimmer.animate().alpha(0f).setDuration(500).withEndAction {
            binding.vMapShimmer.isVisible = false
        }.start()
    }

    private fun playSuccessRipple(status: String, timeStr: String) {
        binding.tvStatusText.text = "$status @ $timeStr"
        
        binding.vSuccessRipple.isVisible = true
        binding.vSuccessRipple.alpha = 1f
        binding.vSuccessRipple.scaleX = 0f
        binding.vSuccessRipple.scaleY = 0f

        binding.vSuccessRipple.animate()
            .scaleX(30f)
            .scaleY(30f)
            .alpha(0f)
            .setDuration(800)
            .withEndAction {
                binding.vSuccessRipple.isVisible = false
                binding.vSuccessRipple.scaleX = 0f
                binding.vSuccessRipple.scaleY = 0f
            }.start()
    }

    private fun submitAttendance(isCheckIn: Boolean) {
        if (currentLat == 0.0 || currentLng == 0.0) {
            Toast.makeText(context, "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }
        val imageUrl = if (isCheckIn) "mock_selfie_url.jpg" else ""
        viewModel.markAttendance(isCheckIn, "user_123", "outlet_001", currentLat, currentLng, imageUrl)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
        _binding = null
    }
}
