package dev.jatzuk.mvvmrunning.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_PAUSE_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.services.TrackingService

class TrackingViewModel : ViewModel() {

    val isTracking = TrackingRepository.isTracking
    val pathPoints = TrackingRepository.pathPoints
    val currentTimeInMillis = TrackingRepository.timeRunInMillis

    fun sendCommandToService(context: Context) {
        val action =
            if (isTracking.value!!) ACTION_PAUSE_SERVICE
            else ACTION_START_OR_RESUME_SERVICE
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }
}
