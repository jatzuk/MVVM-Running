package dev.jatzuk.mvvmrunning.ui.fragments

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.adapters.RunAdapter
import dev.jatzuk.mvvmrunning.databinding.FragmentRunsBinding
import dev.jatzuk.mvvmrunning.db.Run
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
            ItemTouchHelper(SwipeToDeleteCallback()).attachToRecyclerView(this)
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

    private inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_64)
        private val background = ColorDrawable(Color.RED)
        private var deletedItem: Run? = null
        private var deletedItemPosition = 0

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val list = runAdapter.currentList.toMutableList()
            deletedItem = list[position]
            deletedItemPosition = position
            list.removeAt(position)
            runAdapter.submitList(list)
            trackingViewModel.deleteRun(deletedItem!!)
            showUndo()
        }

        private fun showUndo() {
            Snackbar.make(
                requireView(),
                requireContext().getString(R.string.run_deleted),
                Snackbar.LENGTH_LONG
            ).run {
                setAction(R.string.undo) { undoDeletion() }
                setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                show()
            }
        }

        private fun undoDeletion() {
            val list = runAdapter.currentList.toMutableList()
            list.add(deletedItemPosition, deletedItem)
            runAdapter.submitList(list)
            trackingViewModel.restoreDeletedRun(deletedItem!!)
            Snackbar.make(
                requireView(),
                getString(R.string.run_restored),
                Snackbar.LENGTH_SHORT
            ).show()
            deletedItem = null
            deletedItemPosition = 0
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val itemView = viewHolder.itemView
            val backgroundCornerOffset = 20

            val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight

            when {
                dX > 0 -> {
                    val iconLeft = itemView.left
                    val iconRight = itemView.left + icon.intrinsicWidth * 3
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                }
                dX < 0 -> {
                    val iconLeft = (itemView.width - icon.intrinsicWidth)
                    val iconRight = itemView.right + icon.intrinsicWidth * 2
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(
                        (itemView.right + dX).toInt() - backgroundCornerOffset,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                }
                else -> {
                    background.setBounds(0, 0, 0, 0)
                }
            }

            background.draw(c)
            icon.draw(c)
        }
    }
}
