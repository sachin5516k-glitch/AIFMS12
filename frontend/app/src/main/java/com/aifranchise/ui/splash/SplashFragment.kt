package com.aifranchise.ui.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aifranchise.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.aifranchise.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var apiService: com.aifranchise.data.remote.ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val llLogoContainer = view.findViewById<LinearLayout>(R.id.llLogoContainer)
        val ivFoodElement1 = view.findViewById<View>(R.id.ivFoodElement1)
        val ivFoodElement2 = view.findViewById<View>(R.id.ivFoodElement2)
        val llConnectionStatus = view.findViewById<LinearLayout>(R.id.llConnectionStatus)
        val tvConnectionStatus = view.findViewById<TextView>(R.id.tvConnectionStatus)

        // Fade in Logo
        ObjectAnimator.ofFloat(llLogoContainer, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }

        ObjectAnimator.ofFloat(ivFoodElement1, "alpha", 0f, 0.2f).apply {
            duration = 1500
            start()
        }

        ObjectAnimator.ofFloat(ivFoodElement1, "translationY", 0f, -20f).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }

        ObjectAnimator.ofFloat(ivFoodElement2, "alpha", 0f, 0.2f).apply {
            duration = 1500
            start()
        }

        ObjectAnimator.ofFloat(ivFoodElement2, "translationY", 0f, 20f).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }

        // Show "Connecting to server..." after 2 seconds if still on splash
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            if (isAdded) {
                ObjectAnimator.ofFloat(llConnectionStatus, "alpha", 0f, 1f).apply {
                    duration = 500
                    start()
                }
            }
        }

        // Fire ping + wait for completion (up to 45s) so backend is warm before login
        viewLifecycleOwner.lifecycleScope.launch {
            val startTime = System.currentTimeMillis()

            // Fire the server warmup ping in IO thread
            val pingJob = launch(Dispatchers.IO) {
                try {
                    apiService.pingServer()
                } catch (_: Exception) {
                    // Ignore — just warming up
                }
            }

            // Wait for ping to finish, but cap at 45 seconds
            val minSplashMs = 2200L
            val maxWaitMs = 45_000L
            try {
                withTimeout(maxWaitMs) {
                    pingJob.join()
                }
            } catch (_: Exception) {
                // Timeout — proceed anyway
            }

            // Update status text
            if (isAdded) {
                withContext(Dispatchers.Main) {
                    tvConnectionStatus?.text = "Connected ✓"
                }
                delay(500) // Brief pause to show "Connected"
            }

            // Ensure at least minSplashMs has elapsed for animation
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < minSplashMs) {
                delay(minSplashMs - elapsed)
            }

            // Navigate
            if (isAdded) {
                if (tokenManager.isTokenValid()) {
                    val role = tokenManager.getUserRole() ?: ""
                    val action = when (role.lowercase()) {
                        "admin" -> R.id.action_splash_to_owner
                        "manager" -> R.id.action_splash_to_manager
                        "employee" -> R.id.action_splash_to_outlet
                        else -> R.id.action_splash_to_login
                    }
                    try {
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        tokenManager.clearToken()
                        findNavController().navigate(R.id.action_splash_to_login)
                    }
                } else {
                    tokenManager.clearToken()
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }
        }
    }
}
