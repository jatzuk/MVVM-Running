package dev.jatzuk.mvvmrunning.repositories

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

object TrackingRepository {

    var isTracking = false
        private set
    val pathPoints = MutableLiveData<Polylines>()

    fun initStartingValues() {
        isTracking = false
        pathPoints.value = mutableListOf()
    }

    fun startTracking() {
        addEmptyPolyline()
        isTracking = true
    }

    fun pauseTracking() {
        isTracking = false
    }

    fun finishRun() {
        /* no-op */
    }

    fun addPoint(latLng: LatLng) {
        pathPoints.value?.last()?.add(latLng)
        pathPoints.postValue(pathPoints.value)
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.add(mutableListOf())
    }
}
