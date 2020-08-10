package dev.jatzuk.mvvmrunning.adapters

import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.other.SortType
import dev.jatzuk.mvvmrunning.other.Sortable
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@BindingAdapter("loadImage")
fun ImageView.loadImage(run: Run) {
    Glide.with(this).load(run.image).into(this)
}

@BindingAdapter("setDate")
fun MaterialTextView.setDate(run: Run) {
    val calendar = Calendar.getInstance().apply { timeInMillis = run.timestamp }
    val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    text = dateFormat.format(calendar.time)
}

@BindingAdapter("setFormattedTime")
fun MaterialTextView.setFormattedTime(run: Run) {
    text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
}

@BindingAdapter("setAvgSpeed")
fun MaterialTextView.setAvgSpeed(run: Run) {
    val avgSpeed = (run.avgSpeedInKMH * 10f).roundToInt() / 10f
    text = context.getString(R.string.avg_speed_binding_format, avgSpeed)
}

@BindingAdapter("setDistanceInKm")
fun MaterialTextView.setDistanceInKm(run: Run) {
    val km = run.distanceInMeters / 1000f
    val totalDistance = (km * 10).roundToInt() / 10f
    text = context.getString(R.string.distance_binding_format, totalDistance)
}

@BindingAdapter("setCaloriesBurned")
fun MaterialTextView.setCaloriesBurned(run: Run) {
    text = context.getString(R.string.calories_burned_binding_format, run.caloriesBurned)
}

@BindingAdapter("toggleRunText")
fun setStartButtonText(button: MaterialButton, isTracking: Boolean) {
    // workaround to avoid updates from viewmodel (millis value) in binding
    val millis = TrackingRepository.timeRunInMillis.value!!
    button.text =
        if (millis == 0L && !isTracking) button.context.getString(R.string.start)
        else if (millis > 0L && !isTracking) button.context.getString(R.string.resume)
        else button.context.getString(R.string.pause)
}

@BindingAdapter("setSelection")
fun Spinner.setSelection(viewModel: Sortable) {
    setSelection(viewModel.sortType.ordinal)
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            viewModel.sortRuns(SortType.values()[position])
        }
    }
}

@BindingAdapter("totalDistance")
fun MaterialTextView.totalDistance(distance: Long) {
    val km = distance / 1000f
    val totalDistance = (km * 10).roundToInt() / 10f
    text = context.getString(R.string.distance_binding_format, totalDistance)
}

@BindingAdapter("totalTime")
fun MaterialTextView.totalTime(time: Long) {
    text = TrackingUtility.getFormattedStopWatchTime(time)
}

@BindingAdapter("totalCaloriesBurned")
fun MaterialTextView.totalCaloriesBurned(calories: Long) {
    text = context.getString(R.string.calories_burned_binding_format, calories)
}

@BindingAdapter("totalAvgSpeed")
fun MaterialTextView.totalAvgSpeed(avgSpeed: Float) {
    text =
        context.getString(R.string.avg_speed_binding_format, ((avgSpeed * 10f).roundToInt() / 10f))
}

@BindingAdapter("currentDistance")
fun MaterialTextView.currentDistance(distance: Long) {
    text = context.getString(R.string.distance_binding_format_meters, distance)
}

@BindingAdapter("currentCaloriesBurned")
fun MaterialTextView.currentCaloriesBurned(calories: Long) {
    text = context.getString(R.string.calories_burned_binding_format, calories)
}

@BindingAdapter("updateTrackingProgress")
fun MaterialTextView.updateTrackingProgress(progress: Int) {
    text = context.getString(R.string.progress_binding_format, progress)
}

@BindingAdapter("updateProgress")
fun ProgressBar.updateProgress(progress: Int) {
    this.progress = progress
}
