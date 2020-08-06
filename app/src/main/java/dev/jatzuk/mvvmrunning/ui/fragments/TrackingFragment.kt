package dev.jatzuk.mvvmrunning.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentTrackingBinding
import dev.jatzuk.mvvmrunning.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import dev.jatzuk.mvvmrunning.other.Constants.MAP_ZOOM
import dev.jatzuk.mvvmrunning.other.Constants.POLYLINE_COLOR
import dev.jatzuk.mvvmrunning.other.Constants.POLYLINE_WIDTH
import dev.jatzuk.mvvmrunning.other.MapLifecycleObserver
import dev.jatzuk.mvvmrunning.other.MusicApps
import dev.jatzuk.mvvmrunning.other.TargetType
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository.Companion.pathPoints
import dev.jatzuk.mvvmrunning.ui.viewmodels.TrackingViewModel
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val trackingViewModel: TrackingViewModel by viewModels()

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private lateinit var mapLifecycleObserver: MapLifecycleObserver

    private var menu: Menu? = null
    private lateinit var motionLayout: MotionLayout

    private val args: TrackingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        motionLayout = binding.root.findViewById(R.id.clInnerLayout)!!

        setHasOptionsMenu(true)

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        loadPreviouslySelectedRunTarget()
        subscribeToObservers()

        binding.apply {
            lifecycleOwner = this@TrackingFragment
            viewModel = trackingViewModel

            spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    apply {
                        tvTargetValueInfo!!.text =
                            resources.getStringArray(R.array.target_types_measurements)[position]
                        etTargetValue!!.visibility = if (position == 3) View.GONE else View.VISIBLE
                    }
                }
            }

            btnToggleRun.setOnClickListener {
                if (trackingViewModel.currentTimeInMillis.value!! == 0L) {
                    val success = setRunTarget()
                    if (success) {
                        trackingViewModel.sendCommandToService(requireContext())
                        motionLayout.transitionToEnd()
                    }
                } else {
                    trackingViewModel.sendCommandToService(requireContext())
                }
            }

            btnFinishRun.setOnClickListener { finishRun() }

            ivLaunchMusicPlayer.setOnClickListener { startMusicPlayer() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            ) as CancelTrackingDialog?
            cancelTrackingDialog?.setPositiveButtonListener {
                trackingViewModel.setCancelCommand(requireContext())
            }
        }

        mapView?.let { mapView ->
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync {
                map = it
                addAllPolylines()
                if (args.isFinishActionFired) {
                    finishRun()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (trackingViewModel.isTracking.value!! || trackingViewModel.currentTimeInMillis.value!! > 0L) {
            motionLayout.transitionToEnd()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.getItem(0).isVisible = trackingViewModel.currentTimeInMillis.value!! > 0L
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.cancel_run -> {
            showCancelTrackingDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setPositiveButtonListener {
                trackingViewModel.setCancelCommand(requireContext())
                motionLayout.transitionToStart()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun subscribeToObservers() {
        trackingViewModel.isTracking.observe(viewLifecycleOwner, Observer {
            menu?.getItem(0)?.isVisible = it || trackingViewModel.currentTimeInMillis.value!! > 0L
        })

        trackingViewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            addLatestPolyline()
            moveCameraToUser()
        })

        trackingViewModel.currentTimeInMillis.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun moveCameraToUser() {
        if (pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.value!!.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun addAllPolylines() {
        pathPoints.value?.forEach {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints.value!!) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        val latLngBounds = try {
            bounds.build()
        } catch (e: IllegalStateException) {
            Timber.e(e, "Cannot find any path points, associated with this run")
            return
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                mapView!!.width,
                mapView!!.height,
                (mapView!!.height * 0.05f).toInt()
            )
        )
    }

    private fun finishRun() {
        zoomToSeeWholeTrack()
        map!!.snapshot {
            trackingViewModel.processRun(requireContext(), it)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.run_saved_successfully),
                Snackbar.LENGTH_LONG
            ).show()
        }
        findNavController().navigate(R.id.action_trackingFragment_to_runsFragment)
    }

    private fun addLatestPolyline() {
        if (pathPoints.value!!.isNotEmpty() && pathPoints.value!!.last().size > 1) {
            val preLastLatLng = pathPoints.value!!.last()[pathPoints.value!!.last().size - 2]
            val lastLatLng = pathPoints.value!!.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun startMusicPlayer() {
        val packageManager = requireActivity().packageManager
        val intent = Intent()

        for (app in MusicApps.values()) {
            if (isPackageInstalled(app.packagePath)) {
                intent.selector = packageManager.getLaunchIntentForPackage(app.packagePath)
                break
            }
        }

        if (intent.selector != null) {
            startActivity(intent)
        } else {
            Snackbar.make(requireView(), getString(R.string.no_music_app), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun isPackageInstalled(packageName: String) =
        try {
            requireActivity().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    private fun setRunTarget(): Boolean {
        val typeString = binding.spinner?.selectedItem.toString()
        val targetType = TargetType.valueOf(typeString.toUpperCase(Locale.getDefault()))
        val targetValue = binding.etTargetValue?.text?.toString()?.toFloat()!!

        if (targetValue == 0f && targetType != TargetType.NONE) {
            binding.etTargetValue?.error = requireContext().getString(
                R.string.error_must_be_provided,
                requireContext().getString(R.string.target_value)
            )
            return false
        } else {
            if (targetType != TargetType.NONE) targetType.value = targetValue
        }

        trackingViewModel.userInfo.applyChanges(targetType = targetType)
        return true
    }

    private fun loadPreviouslySelectedRunTarget() {
        val targetType = trackingViewModel.userInfo.targetType
        binding.apply {
            spinner!!.setSelection(targetType.ordinal)
            etTargetValue!!.setText(targetType.value.toString())
            tvTargetValueInfo!!.text =
                resources.getStringArray(R.array.target_types_measurements)[targetType.ordinal]

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
