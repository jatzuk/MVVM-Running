package dev.jatzuk.mvvmrunning.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.other.SortType
import dev.jatzuk.mvvmrunning.other.Sortable
import dev.jatzuk.mvvmrunning.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    mainRepository: MainRepository
) : ViewModel(), Sortable {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

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
}
