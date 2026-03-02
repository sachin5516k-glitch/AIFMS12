package com.aifranchise.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aifranchise.databinding.FragmentProfileBinding
import com.aifranchise.util.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import com.aifranchise.R

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val role = tokenManager.getUserRole() ?: "Unknown Role"
            val name = tokenManager.getUserName() ?: "Unknown User"
            val branchId = tokenManager.getBranchId() ?: "N/A"

            binding.tvProfileName.text = name
            binding.tvProfileRole.text = "Role: ${role.uppercase()}"
            binding.tvProfileBranch.text = "Branch: $branchId"

            binding.btnLogout.setOnClickListener {
                tokenManager.forceLogout()
            }
        } catch (e: Exception) {
            // Safe Error Handling
            binding.tvProfileName.text = "Error loading profile"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
