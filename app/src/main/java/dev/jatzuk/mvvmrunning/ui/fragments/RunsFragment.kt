package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.adapters.RunAdapter
import dev.jatzuk.mvvmrunning.databinding.FragmentRunsBinding
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.ui.viewmodels.TrackingViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunsFragment : Fragment(R.layout.fragment_runs), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentRunsBinding? = null
    private val binding get() = _binding!!

    private val trackingViewModel: TrackingViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = trackingViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TrackingUtility.requestPermissions(this)
        setupRecyclerView()

        trackingViewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runsFragment_to_trackingFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.rvRuns.apply {
            runAdapter = RunAdapter()
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            TrackingUtility.requestPermissions(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
