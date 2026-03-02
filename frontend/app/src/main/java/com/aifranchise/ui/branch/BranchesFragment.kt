package com.aifranchise.ui.branch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aifranchise.data.remote.ApiService
import com.aifranchise.data.remote.AddBranchRequest
import com.aifranchise.data.remote.LocationDto
import com.aifranchise.databinding.FragmentBranchesBinding
import com.aifranchise.databinding.DialogAddBranchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.appcompat.app.AlertDialog

@AndroidEntryPoint
class BranchesFragment : Fragment() {

    private var _binding: FragmentBranchesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var apiService: ApiService
    
    private lateinit var adapter: BranchesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BranchesAdapter(emptyList()) { branch ->
            val intent = android.content.Intent(requireContext(), com.aifranchise.ui.branch.BranchDetailActivity::class.java).apply {
                putExtra("BRANCH_ID", branch.id)
                putExtra("BRANCH_NAME", branch.name)
            }
            startActivity(intent)
        }
        binding.rvBranches.adapter = adapter

        binding.fabAddBranch.setOnClickListener {
            showAddBranchDialog()
        }

        fetchBranches()
    }

    private fun showAddBranchDialog() {
        org.osmdroid.config.Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        val dialogBinding = DialogAddBranchBinding.inflate(layoutInflater)
        
        dialogBinding.mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        dialogBinding.mapView.setMultiTouchControls(true)
        val mapController = dialogBinding.mapView.controller
        mapController.setZoom(15.0)
        val startPoint = org.osmdroid.util.GeoPoint(28.6139, 77.2090)
        mapController.setCenter(startPoint)

        dialogBinding.mapView.addMapListener(object : org.osmdroid.events.MapListener {
            override fun onScroll(event: org.osmdroid.events.ScrollEvent): Boolean {
                val center = dialogBinding.mapView.mapCenter
                dialogBinding.tvCoordinates.text = java.lang.String.format(java.util.Locale.US, "Lat: %.4f, Lng: %.4f", center.latitude, center.longitude)
                return true
            }
            override fun onZoom(event: org.osmdroid.events.ZoomEvent): Boolean {
                return true
            }
        })
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Branch")
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null) // handle positive manually to prevent auto-dismiss on error
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.etName.text.toString().trim()
                        val address = dialogBinding.etAddress.text.toString().trim()
                        val city = dialogBinding.etCity.text.toString().trim()
                        val state = dialogBinding.etState.text.toString().trim()

                        if (name.isEmpty() || address.isEmpty() || city.isEmpty() || state.isEmpty()) {
                            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val center = dialogBinding.mapView.mapCenter
                        val request = AddBranchRequest(
                            name = name,
                            location = LocationDto(
                                address = address, 
                                city = city, 
                                state = state,
                                lat = center.latitude,
                                lng = center.longitude
                            )
                        )
                        submitBranch(request, this)
                    }
                }
                show()
            }
    }

    private fun submitBranch(request: AddBranchRequest, dialog: AlertDialog) {
        binding.progressBar.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.addBranch(request)
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    if (response.success) {
                        Toast.makeText(requireContext(), "Branch Added successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchBranches()
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    val errorMsg = com.aifranchise.util.ApiUtils.parseError(e)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchBranches() {
        binding.progressBar.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getBranches()
                binding.progressBar.isVisible = false
                if (response.success && response.data != null) {
                    adapter.submitList(response.data)
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                Toast.makeText(context, e.message ?: "Failed to load branches.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
