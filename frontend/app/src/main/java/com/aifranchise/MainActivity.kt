package com.aifranchise

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
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

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Notifications disabled. You may miss critical alerts.", Toast.LENGTH_LONG).show()
            }
        }

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

                // Check and request notification permissions for Android 13+
                checkNotificationPermissions()
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

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Let the system handle showing rationale where it makes sense in the standard prompt
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
