package dev.jatzuk.mvvmrunning.repositories

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import dev.jatzuk.mvvmrunning.other.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

object TrackingRepository {

    var isTracking = MutableLiveData(false)
    val pathPoints = MutableLiveData<Polylines>()

    val timeRunInMillis = MutableLiveData(0L)

    private val timeRunInSeconds = MutableLiveData(0L)
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    fun initStartingValues() {
        pathPoints.value = mutableListOf()
    }

    private fun startTracking() {
        addEmptyPolyline()
        isTracking.value = true
    }

    fun startTimer() {
        startTracking()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                delay(Constants.TIMER_UPDATE_INTERVAL)
            }

            timeRun += lapTime
        }
    }

    fun pauseTimer() {
        isTracking.value = false
        isTimerEnabled = false
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
