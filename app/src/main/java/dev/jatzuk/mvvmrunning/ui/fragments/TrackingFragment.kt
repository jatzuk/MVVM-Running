package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentTrackingBinding
import dev.jatzuk.mvvmrunning.other.MapLifecycleObserver
import dev.jatzuk.mvvmrunning.ui.viewmodels.MainViewModel

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    private var mapView: MapView? = null

    //    @Inject
    lateinit var mapLifecycleObserver: MapLifecycleObserver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView?.let { mapView ->
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync { map = it }
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
