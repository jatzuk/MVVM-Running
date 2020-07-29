package dev.jatzuk.mvvmrunning.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.db.Run
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

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
    text = context.getString(R.string.avg_speed_binding_format, run.avgSpeedInKMH.toString())
}

@BindingAdapter("setDistanceInKm")
fun MaterialTextView.setDistanceInKm(run: Run) {
    text = context.getString(R.string.distance_binding_format, run.distanceInMeters.toString())
}

@BindingAdapter("setCaloriesBurned")
fun MaterialTextView.setCaloriesBurned(run: Run) {
    text = context.getString(R.string.calories_burned_binding_format, run.caloriesBurned.toString())
}
