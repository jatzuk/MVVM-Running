package dev.jatzuk.mvvmrunning.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_PAUSE_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_STOP_SERVICE
import dev.jatzuk.mvvmrunning.other.SortType
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.MainRepository
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.services.TrackingService
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.round

class TrackingViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeRunInMillis

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByTime = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE
        private set

    @set:Inject
    var weight = 80f

    init {
        runs.addSource(runsSortedByDate) {
            if (sortType == SortType.DATE) it?.let { runs.value = it }
        }

        runs.addSource(runsSortedByTime) {
            if (sortType == SortType.RUNNING_TIME) it?.let { runs.value = it }
        }

        runs.addSource(runsSortedByAvgSpeed) {
            if (sortType == SortType.AVG_SPEED) it?.let { runs.value = it }
        }

        runs.addSource(runsSortedByDistance) {
            if (sortType == SortType.DISTANCE) it?.let { runs.value = it }
        }

        runs.addSource(runsSortedByCaloriesBurned) {
            if (sortType == SortType.CALORIES_BURNED) it?.let { runs.value = it }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTime.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also { this.sortType = sortType }

    fun sendCommandToService(context: Context) {
        val action =
            if (isTracking.value!!) ACTION_PAUSE_SERVICE
            else ACTION_START_OR_RESUME_SERVICE
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun setCancelCommand(context: Context) {
        Intent(context, TrackingService::class.java).also {
            it.action = ACTION_STOP_SERVICE
            context.startService(it)
        }
    }

    fun processRun(context: Context, bitmap: Bitmap) {
        val dateTimestamp = Calendar.getInstance().timeInMillis
        var distanceInMeters = 0
        for (polyline in pathPoints.value!!) {
            distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        val avgSpeed = round(
            (distanceInMeters / 1000f) / (currentTimeInMillis.value!! / 1000f / 60 / 60) * 10
        ) / 10f
        val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
        val run = Run(
            bitmap,
            dateTimestamp,
            avgSpeed,
            distanceInMeters,
            currentTimeInMillis.value!!,
            caloriesBurned
        )

        viewModelScope.launch {
            setCancelCommand(context)
            mainRepository.insertRun(run)
        }
    }
}
