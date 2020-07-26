package dev.jatzuk.mvvmrunning.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository.pauseTracking
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository.startTracking

class TrackingViewModel : ViewModel() {

    val isTracking = MutableLiveData(TrackingRepository.isTracking)
    val pathPoints = TrackingRepository.pathPoints

    fun toggleRun() {
        if (TrackingRepository.isTracking) pauseTracking()
        else startTracking()

        isTracking.value = TrackingRepository.isTracking
    }

    fun finishRun() {
        TrackingRepository.finishRun()
    }
}
