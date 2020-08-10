package dev.jatzuk.mvvmrunning.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.db.UserInfo
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_PAUSE_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_STOP_SERVICE
import dev.jatzuk.mvvmrunning.other.SortType
import dev.jatzuk.mvvmrunning.other.Sortable
import dev.jatzuk.mvvmrunning.repositories.MainRepository
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.services.TrackingService
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round

class TrackingViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository,
    val userInfo: UserInfo
) : ViewModel(), Sortable {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeRunInMillis
    val distanceInMeters = TrackingRepository.distanceInMeters
    val caloriesBurned = TrackingRepository.caloriesBurned

    val targetType = TrackingRepository.targetType
    val progress = TrackingRepository.progress

    override val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    override val runsSortedByTime = mainRepository.getAllRunsSortedByTimeInMillis()
    override val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    override val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    override val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    override val runs = MediatorLiveData<List<Run>>()
    override var sortType = SortType.DATE

    init {
        fillSources()
    }

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
        val distance = distanceInMeters.value!!
        val avgSpeed = round(
            (distance / 1000f) / (currentTimeInMillis.value!! / 1000f / 60 / 60) * 10
        ) / 10f
        val run = Run(
            bitmap,
            dateTimestamp,
            avgSpeed,
            distance,
            currentTimeInMillis.value!!,
            caloriesBurned.value!!
        )

        viewModelScope.launch {
            setCancelCommand(context)
            mainRepository.insertRun(run)
        }
    }

    fun deleteRun(run: Run) {
        viewModelScope.launch {
            mainRepository.deleteRun(run)
        }
    }

    fun restoreDeletedRun(run: Run) {
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }
}
