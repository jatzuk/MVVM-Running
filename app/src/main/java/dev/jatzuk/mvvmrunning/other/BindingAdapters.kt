package dev.jatzuk.mvvmrunning.other

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import dev.jatzuk.mvvmrunning.ui.viewmodels.TrackingViewModel

@BindingAdapter("toggleRunText")
fun MaterialButton.toggleRunText(isTracking: Boolean) {
    text = if (isTracking) "Stop" else "Start"
}

@BindingAdapter("setSelection")
fun Spinner.setSelection(trackingViewModel: TrackingViewModel) {
    setSelection(trackingViewModel.sortType.ordinal)
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            trackingViewModel.sortRuns(SortType.values()[position])
        }
    }
}
