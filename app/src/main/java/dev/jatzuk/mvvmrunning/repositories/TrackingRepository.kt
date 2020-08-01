package dev.jatzuk.mvvmrunning.repositories

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import dev.jatzuk.mvvmrunning.other.Constants
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingRepository @Inject constructor() {

    var isFirstRun = true
        private set
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    var isCancelled = false

    fun initStartingValues() {
        pathPoints.value = mutableListOf()
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
        lapTime = 0L
        timeRun = 0L
        timeStarted = 0L
        lastSecondTimestamp = 0L
    }

    private fun startTracking() {
        addEmptyPolyline()
        isTracking.value = true
        isCancelled = false
    }

    fun startRun(firstRun: Boolean = false) {
        startTracking()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        isFirstRun = firstRun

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L

                    var distance = 0L
                    for (polyline in pathPoints.value!!) {
                        // TODO update only last sec
                        distance += TrackingUtility.calculatePolylineLength(polyline).toLong()
                    }
                    distanceInMeters.postValue(distance)
                    caloriesBurned.postValue(((distance / 1000f) * 80f).toLong())
                }

                delay(Constants.TIMER_UPDATE_INTERVAL)
            }

            timeRun += lapTime
        }
    }

    fun pauseRun() {
        isTracking.value = false
        isTimerEnabled = false
    }

    fun cancelRun() {
        isCancelled = true
        isFirstRun = true
        timeRunInMillis.value = 0L // reset value for correct fragment observers income values
        distanceInMeters.value = 0L
        caloriesBurned.value = 0L
        pauseRun()
        initStartingValues()
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

    companion object {
        val isTracking = MutableLiveData(false)
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData(0L)
        val timeRunInSeconds = MutableLiveData(0L)
        val distanceInMeters = MutableLiveData(0L)
        val caloriesBurned = MutableLiveData(0L)
    }
}
