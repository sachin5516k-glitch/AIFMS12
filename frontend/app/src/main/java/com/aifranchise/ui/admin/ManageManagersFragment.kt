package com.aifranchise.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aifranchise.data.remote.AddManagerRequest
import com.aifranchise.data.remote.BranchDto
import com.aifranchise.data.remote.ResultState
import com.aifranchise.databinding.DialogAddManagerBinding
import com.aifranchise.databinding.FragmentManageManagersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageManagersFragment : Fragment() {

    private var _binding: FragmentManageManagersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: ManageManagerAdapter

    private var branchList: List<BranchDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageManagersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.fetchManagers()
        viewModel.fetchBranches()
    }

    private fun setupRecyclerView() {
        adapter = ManageManagerAdapter { manager ->
            AlertDialog.Builder(requireContext())
                .setTitle("Deactivate Manager")
                .setMessage("Are you sure you want to deactivate ${manager.name}?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deactivateManager(manager.id)
                }
                .setNegativeButton("No", null)
                .show()
        }
        binding.rvManagers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManagers.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchManagers()
            viewModel.fetchBranches()
        }

        binding.fabAddManager.setOnClickListener {
            showAddManagerDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.managers.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        if (!binding.swipeRefresh.isRefreshing) {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                    is ResultState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        adapter.submitList(state.data)
                        binding.tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is ResultState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), state.exception.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.branches.collect { state ->
                if (state is ResultState.Success) {
                    branchList = state.data
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addManagerState.collect { state ->
                when (state) {
                    is ResultState.Success -> {
                        Toast.makeText(requireContext(), "Manager added successfully", Toast.LENGTH_SHORT).show()
                        viewModel.clearAddManagerState()
                    }
                    is ResultState.Error -> {
                        Toast.makeText(requireContext(), state.exception.message, Toast.LENGTH_LONG).show()
                        viewModel.clearAddManagerState()
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deactivateState.collect { state ->
                when (state) {
                    is ResultState.Success -> {
                        Toast.makeText(requireContext(), "Manager deactivated", Toast.LENGTH_SHORT).show()
                        viewModel.clearDeactivateState()
                    }
                    is ResultState.Error -> {
                        Toast.makeText(requireContext(), state.exception.message, Toast.LENGTH_LONG).show()
                        viewModel.clearDeactivateState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showAddManagerDialog() {
        if (branchList.isEmpty()) {
            Toast.makeText(requireContext(), "No branches available. Add a branch first.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddManagerBinding.inflate(layoutInflater)
        
        val branchNames = branchList.map { it.name }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, branchNames)
        dialogBinding.spinnerBranch.adapter = spinnerAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Add Manager")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val email = dialogBinding.etEmail.text.toString().trim()
                val password = dialogBinding.etPassword.text.toString().trim()
                val selectedBranchIndex = dialogBinding.spinnerBranch.selectedItemPosition

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val branchId = branchList[selectedBranchIndex].id
                viewModel.addManager(AddManagerRequest(name, email, password, branchId))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
