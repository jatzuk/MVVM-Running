package dev.jatzuk.mvvmrunning.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_PAUSE_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_STOP_SERVICE
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.MainRepository
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.services.TrackingService
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round

class TrackingViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeRunInMillis

    val weight = 80f

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
