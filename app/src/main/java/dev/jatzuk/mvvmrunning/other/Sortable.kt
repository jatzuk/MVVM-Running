package dev.jatzuk.mvvmrunning.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import dev.jatzuk.mvvmrunning.db.Run

interface Sortable {

    var sortType: SortType

    val runs: MediatorLiveData<List<Run>>

    val runsSortedByDate: LiveData<List<Run>>
    val runsSortedByTime: LiveData<List<Run>>
    val runsSortedByAvgSpeed: LiveData<List<Run>>
    val runsSortedByDistance: LiveData<List<Run>>
    val runsSortedByCaloriesBurned: LiveData<List<Run>>

    fun fillSources() {
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
}
