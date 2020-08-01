package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository.Companion.pathPoints
import dev.jatzuk.mvvmrunning.ui.viewmodels.TrackingViewModel
import timber.log.Timber

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val trackingViewModel: TrackingViewModel by viewModels()

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private lateinit var mapLifecycleObserver: MapLifecycleObserver

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        setHasOptionsMenu(true)

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        binding.lifecycleOwner = this
        binding.viewModel = trackingViewModel

        subscribeToObservers()

        binding.btnFinishRun.setOnClickListener {
            finishRun()
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
            }
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
            setPositiveButtonListener { trackingViewModel.setCancelCommand(requireContext()) }
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
