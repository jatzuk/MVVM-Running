package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentTrackingBinding
import dev.jatzuk.mvvmrunning.other.Constants.MAP_ZOOM
import dev.jatzuk.mvvmrunning.other.Constants.POLYLINE_COLOR
import dev.jatzuk.mvvmrunning.other.Constants.POLYLINE_WIDTH
import dev.jatzuk.mvvmrunning.other.MapLifecycleObserver
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.Polyline
import dev.jatzuk.mvvmrunning.ui.viewmodels.MainViewModel
import dev.jatzuk.mvvmrunning.ui.viewmodels.TrackingViewModel

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private val trackingViewModel: TrackingViewModel by viewModels()
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private lateinit var mapLifecycleObserver: MapLifecycleObserver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        binding.lifecycleOwner = this
        binding.viewModel = trackingViewModel

        subscribeToObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView?.let { mapView ->
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync {
                map = it
                addAllPolylines()
            }
        }
    }

    private fun subscribeToObservers() {
        trackingViewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        trackingViewModel.currentTimeInMillis.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun addAllPolylines() {
        pathPoints.forEach {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
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
