package com.aifranchise.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aifranchise.R

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.facebook.shimmer.ShimmerFrameLayout
import com.aifranchise.data.remote.ResultState
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.widget.ImageView

@AndroidEntryPoint
class OwnerDashboardFragment : Fragment(R.layout.fragment_dashboard_admin) {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAdminDashboardData()
        
        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            viewModel.loadAdminDashboardData(forceRefresh = true)
        }

        val tvTotalSales = view.findViewById<TextView>(R.id.tvTotalSales)
        val tvProfit = view.findViewById<TextView>(R.id.tvProfit)
        val tvTotalBranches = view.findViewById<TextView>(R.id.tvTotalBranches)
        val tvAttendance = view.findViewById<TextView>(R.id.tvAttendance)
        val shimmerInventory = view.findViewById<ShimmerFrameLayout>(R.id.shimmerInventory)
        val rvDashboardBranches = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvDashboardBranches)
        
        // Inline inventory views
        val tvSelectedBranchName = view.findViewById<TextView>(R.id.tvSelectedBranchName)
        val pbBranchInventory = view.findViewById<android.widget.ProgressBar>(R.id.pbBranchInventory)
        val tvBranchInventoryEmpty = view.findViewById<TextView>(R.id.tvBranchInventoryEmpty)
        val rvBranchInventory = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvBranchInventory)
        rvBranchInventory?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        
        val inventoryAdapter = com.aifranchise.ui.inventory.InventoryAdapter(emptyList()) { /* no update action from dashboard */ }
        rvBranchInventory?.adapter = inventoryAdapter

        val branchAdapter = BranchHorizontalAdapter(emptyList()) { branch ->
            // Show branch name header and load inventory inline
            tvSelectedBranchName?.text = "📦 ${branch.name} — Stock"
            tvSelectedBranchName?.visibility = View.VISIBLE
            pbBranchInventory?.visibility = View.VISIBLE
            tvBranchInventoryEmpty?.visibility = View.GONE
            rvBranchInventory?.visibility = View.GONE
            viewModel.loadBranchInventory(branch.id)
        }
        rvDashboardBranches?.adapter = branchAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.adminDashboardState.collectLatest { state ->
                if (state !is ResultState.Loading) swipeRefresh?.isRefreshing = false
                
                when (state) {
                    is ResultState.Loading -> {
                        shimmerInventory.startShimmer()
                        shimmerInventory.visibility = View.VISIBLE
                    }
                    is ResultState.Success -> {
                        shimmerInventory.stopShimmer()
                        shimmerInventory.visibility = View.GONE
                        
                        val data = state.data
                        if (data != null) {
                            val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
                            tvTotalSales.text = format.format(data.profitability?.revenue ?: 0.0)
                            tvProfit.text = data.profitability?.profitPercentage ?: "0%"
                            tvTotalBranches.text = data.branchCount?.toString() ?: "0"
                            tvAttendance.text = "${data.attendancePercentage ?: 0}%"
                        }
                    }
                    is ResultState.Error -> {
                        shimmerInventory.stopShimmer()
                        shimmerInventory.visibility = View.GONE
                    }
                    null -> Unit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.branchesState.collectLatest { state ->
                if (state is ResultState.Success) {
                    val branchList = state.data ?: emptyList()
                    branchAdapter.submitList(branchList)
                    rvDashboardBranches?.visibility = View.VISIBLE
                } else if (state is ResultState.Error) {
                    rvDashboardBranches?.visibility = View.GONE
                }
            }
        }

        // Observe the selected branch inventory state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedBranchInventoryState.collectLatest { state ->
                when (state) {
                    is ResultState.Loading -> {
                        pbBranchInventory?.visibility = View.VISIBLE
                        rvBranchInventory?.visibility = View.GONE
                        tvBranchInventoryEmpty?.visibility = View.GONE
                    }
                    is ResultState.Success -> {
                        pbBranchInventory?.visibility = View.GONE
                        val items = state.data ?: emptyList()
                        if (items.isEmpty()) {
                            tvBranchInventoryEmpty?.visibility = View.VISIBLE
                            rvBranchInventory?.visibility = View.GONE
                        } else {
                            tvBranchInventoryEmpty?.visibility = View.GONE
                            inventoryAdapter.submitList(items)
                            rvBranchInventory?.visibility = View.VISIBLE
                        }
                    }
                    is ResultState.Error -> {
                        pbBranchInventory?.visibility = View.GONE
                        tvBranchInventoryEmpty?.text = "Failed to load inventory"
                        tvBranchInventoryEmpty?.visibility = View.VISIBLE
                        rvBranchInventory?.visibility = View.GONE
                    }
                    null -> Unit
                }
            }
        }

        view.findViewById<Button>(R.id.btnAi).setOnClickListener {
             try {
                 findNavController().navigate(R.id.aiInsightsFragment)
             } catch (e: Exception) {
                 android.widget.Toast.makeText(context, "Navigation error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
             }
        }
    }
}


@AndroidEntryPoint
class ManagerDashboardFragment : Fragment(R.layout.fragment_dashboard_manager) {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDashboardData()

        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            viewModel.loadDashboardData(forceRefresh = true)
        }

        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmerViewContainer)
        val content = view.findViewById<View>(R.id.contentScrollView)
        
        val tvDailySales = view.findViewById<TextView>(R.id.tvDailySales)
        val tvBranchProfit = view.findViewById<TextView>(R.id.tvBranchProfit)
        val tvStock = view.findViewById<TextView>(R.id.tvStock)
        val tvAttendance = view.findViewById<TextView>(R.id.tvAttendance)
        val tvTopItems = view.findViewById<TextView>(R.id.tvTopItems)
        val ivBackgroundBlur = view.findViewById<ImageView>(R.id.ivBackgroundBlur)
        val btnSuggestions = view.findViewById<Button>(R.id.btnSuggestions)

        btnSuggestions.setOnClickListener {
            findNavController().navigate(R.id.to_transfer_suggestions)
        }

        // Load Background using Glide memory caching to avoid large bitmaps holding main thread
        Glide.with(this)
            .load(R.drawable.img_food_pizza)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(ivBackgroundBlur)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                if (state !is ResultState.Loading) swipeRefresh?.isRefreshing = false
                when (state) {
                    is ResultState.Loading -> {
                        if (swipeRefresh?.isRefreshing != true) {
                            shimmer.startShimmer()
                            shimmer.visibility = View.VISIBLE
                            content.visibility = View.GONE
                        }
                    }
                    is ResultState.Success -> {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.GONE
                        content.visibility = View.VISIBLE
                        // Bind data
                        val data = state.data
                        if (data != null) {
                            val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
                            tvDailySales.text = format.format(data.profitability?.revenue ?: 0.0)
                            tvBranchProfit.text = "${data.profitability?.profitPercentage ?: "0"}%"
                            // Display Top items logic
                            tvTopItems.text = "Incoming Transfers: ${data.incomingTransfers ?: 0}\nOutgoing Transfers: ${data.outgoingTransfers ?: 0}"
                            
                            // Trigger Transfer Request notification
                            if((data.incomingTransfers ?: 0) > 0) {
                                com.aifranchise.util.NotificationHelper.showNotification(
                                    requireContext(),
                                    "Transfer Requests Pending",
                                    "You have ${data.incomingTransfers} incoming transfer requests waiting for action.",
                                    1002
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.GONE
                        content.visibility = View.VISIBLE // show empty or error state
                    }
                    else -> {}
                }
            }
        }
    }
}

@AndroidEntryPoint
class OutletDashboardFragment : Fragment(R.layout.fragment_dashboard_employee) {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDashboardData()

        val swipeRefresh = view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setOnRefreshListener {
            viewModel.loadDashboardData(forceRefresh = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                if (state !is ResultState.Loading) swipeRefresh?.isRefreshing = false
            }
        }
    }
}
