package com.aifranchise.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aifranchise.R

class OwnerDashboardFragment : Fragment(R.layout.fragment_dashboard_admin) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btnAi).setOnClickListener {
             findNavController().navigate(R.id.aiInsightsFragment)
        }
    }
}

class ManagerDashboardFragment : Fragment(R.layout.fragment_dashboard_manager) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

class OutletDashboardFragment : Fragment(R.layout.fragment_dashboard_employee) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
