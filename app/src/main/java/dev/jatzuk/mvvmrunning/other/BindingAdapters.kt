package dev.jatzuk.mvvmrunning.other

import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

@BindingAdapter("app:toggleRunText")
fun toggleRunText(button: MaterialButton, isTracking: Boolean) {
    button.text = if (isTracking) "Stop" else "Start"
}
