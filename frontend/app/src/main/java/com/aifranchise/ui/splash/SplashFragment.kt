package com.aifranchise.ui.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aifranchise.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.aifranchise.util.TokenManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var apiService: com.aifranchise.data.remote.ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        
        val llLogoContainer = view.findViewById<LinearLayout>(R.id.llLogoContainer)
        val ivFoodElement1 = view.findViewById<View>(R.id.ivFoodElement1)
        val ivFoodElement2 = view.findViewById<View>(R.id.ivFoodElement2)

        // Silent Ping to Wake Up Render Free Tier Server!
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                apiService.pingServer()
            } catch (e: Exception) {
                // Ignore, this is just to warm up the backend
            }
        }

        // Fade in Logo
        ObjectAnimator.ofFloat(llLogoContainer, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }

        // Floating food elements fade to low opacity
        ObjectAnimator.ofFloat(ivFoodElement1, "alpha", 0f, 0.2f).apply {
            duration = 1500
            start()
        }
        
        // Floating animation
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

        // Navigate to login or dashboard after delay
        Handler(Looper.getMainLooper()).postDelayed({
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
        }, 2200)

        return view
    }
}
