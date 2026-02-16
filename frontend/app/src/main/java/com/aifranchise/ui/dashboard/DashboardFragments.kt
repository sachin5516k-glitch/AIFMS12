package com.aifranchise.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aifranchise.R

// Shared logic for simplicity in this phase
open class BaseDashboardFragment : Fragment(R.layout.fragment_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btnSales).setOnClickListener {
            findNavController().navigate(R.id.salesFragment)
        }
        view.findViewById<Button>(R.id.btnInventory).setOnClickListener {
             findNavController().navigate(R.id.inventoryFragment)
        }
        view.findViewById<Button>(R.id.btnAttendance).setOnClickListener {
             findNavController().navigate(R.id.attendanceFragment)
        }
    }
}

class OwnerDashboardFragment : BaseDashboardFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvTitle).text = "Owner Dashboard"
        
        view.findViewById<Button>(R.id.btnAi).setOnClickListener {
             findNavController().navigate(R.id.aiInsightsFragment)
        }
    }
}

class ManagerDashboardFragment : BaseDashboardFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvTitle).text = "Manager Dashboard"
        view.findViewById<Button>(R.id.btnAi).visibility = View.GONE
    }
}

class OutletDashboardFragment : BaseDashboardFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvTitle).text = "Outlet Dashboard"
        view.findViewById<Button>(R.id.btnAi).visibility = View.GONE
    }
}
