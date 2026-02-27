package com.aifranchise

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import com.aifranchise.databinding.ActivityMainBinding
import com.aifranchise.util.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.ownerDashboardFragment,
                R.id.managerDashboardFragment,
                R.id.outletDashboardFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        val headerView = binding.navView.getHeaderView(0)
        val tvName = headerView.findViewById<android.widget.TextView>(R.id.tvHeaderUserName)
        val tvRole = headerView.findViewById<android.widget.TextView>(R.id.tvHeaderRole)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.loginFragment) {
                val role = tokenManager.getUserRole()?.lowercase() ?: ""
                val menu = binding.navView.menu
                
                // Admin Constraints
                if (role == "admin" || role == "owner") {
                    menu.findItem(R.id.salesFragment)?.isVisible = false
                    menu.findItem(R.id.inventoryFragment)?.isVisible = false
                    menu.findItem(R.id.attendanceFragment)?.isVisible = false
                } 
                // Employee Constraints
                else if (role == "employee") {
                    menu.findItem(R.id.inventoryFragment)?.isVisible = false
                    menu.findItem(R.id.aiInsightsFragment)?.isVisible = false
                }
                
                tvName?.text = tokenManager.getUserName() ?: "User"
                tvRole?.text = role.uppercase()
            }
        }

        // Setup custom click listeners for non-nav-graph menu items
        binding.navView.menu.findItem(R.id.nav_dashboard).setOnMenuItemClickListener {
            binding.drawerLayout.close()
            // Navigate back to the start
            navController.popBackStack(navController.graph.startDestinationId, false)
            true
        }
        
        binding.navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            tokenManager.forceLogout()
            binding.drawerLayout.close()
            true
        }

        // Hide Toolbar and Drawer on Login Screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                binding.toolbar.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                binding.toolbar.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
