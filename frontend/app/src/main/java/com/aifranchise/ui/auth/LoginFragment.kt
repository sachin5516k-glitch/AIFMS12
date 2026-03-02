package com.aifranchise.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aifranchise.R
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.login(email, pass)
            } else {
                Toast.makeText(context, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        showLoadingAnimation()
                        binding.btnLogin.isEnabled = false
                    }
                    is ResultState.Success -> {
                        hideLoadingAnimation()
                        binding.btnLogin.isEnabled = true
                        Log.d("PSK_DEBUG", "Login Success for role: ${state.data.user.role}")
                        handleRoleNavigation(state.data.user.role)
                    }
                    is ResultState.Error -> {
                        hideLoadingAnimation()
                        binding.btnLogin.isEnabled = true
                        Log.e("PSK_DEBUG", "Login Failed: ${state.exception.message}")
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_LONG).show()
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun showLoadingAnimation() {
        binding.loadingOverlay.isVisible = true
        binding.loadingOverlay.alpha = 0f
        binding.loadingOverlay.animate().alpha(1f).setDuration(300).start()
        
        val imgDosa = binding.ivLoadingDosa
        val imgBurger = binding.ivLoadingBurger
        val imgPizza = binding.ivLoadingPizza
        
        ObjectAnimator.ofFloat(imgDosa, "translationY", 0f, -30f).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        ObjectAnimator.ofFloat(imgBurger, "translationY", 0f, -30f).apply {
            duration = 500
            startDelay = 150
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        ObjectAnimator.ofFloat(imgPizza, "translationY", 0f, -30f).apply {
            duration = 500
            startDelay = 300
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun hideLoadingAnimation() {
        _binding?.loadingOverlay?.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
            _binding?.loadingOverlay?.isVisible = false
        }?.start()
    }

    private fun handleRoleNavigation(role: String) {
        val action = when (role.lowercase()) {
            "admin" -> R.id.action_login_to_owner
            "manager" -> R.id.action_login_to_manager
            "employee" -> R.id.action_login_to_outlet
            else -> {
                Toast.makeText(context, "Unknown Role: $role", Toast.LENGTH_LONG).show()
                return
            }
        }
        try {
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("PSK_DEBUG", "Navigation failed: ${e.message}")
            Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
