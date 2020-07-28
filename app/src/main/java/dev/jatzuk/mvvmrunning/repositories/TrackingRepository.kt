package dev.jatzuk.mvvmrunning.repositories

import androidx.lifecycle.LiveData
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

    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    private val _pathPoints = MutableLiveData<Polylines>()
    val pathPoints: LiveData<Polylines> = _pathPoints

    private val _timeRunInMillis = MutableLiveData(0L)
    val timeRunInMillis: LiveData<Long> = _timeRunInMillis

    private val _timeRunInSeconds = MutableLiveData(0L)
    val timeRunInSeconds: LiveData<Long> = _timeRunInSeconds

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    fun initStartingValues() {
        _pathPoints.value = mutableListOf()
    }

    private fun startTracking() {
        addEmptyPolyline()
        _isTracking.value = true
    }

    fun startTimer() {
        startTracking()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (_isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                _timeRunInMillis.postValue(timeRun + lapTime)

                if (_timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    _timeRunInSeconds.postValue(_timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                delay(Constants.TIMER_UPDATE_INTERVAL)
            }

            timeRun += lapTime
        }
    }

    fun pauseTimer() {
        _isTracking.value = false
        isTimerEnabled = false
    }

    fun finishRun() {
        /* no-op */
    }

    fun addPoint(latLng: LatLng) {
        _pathPoints.value?.last()?.add(latLng)
        _pathPoints.postValue(_pathPoints.value)
    }

    private fun addEmptyPolyline() {
        _pathPoints.value?.add(mutableListOf())
    }
}
