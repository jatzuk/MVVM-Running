package dev.jatzuk.mvvmrunning.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dev.jatzuk.mvvmrunning.databinding.MarkerViewBinding
import dev.jatzuk.mvvmrunning.db.Run

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    private val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    private var _binding: MarkerViewBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = MarkerViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) return
        val currentRunId = e.x.toInt()
        binding.run = runs[currentRunId]
        binding.executePendingBindings()
    }

    override fun getOffset() = MPPointF(-width / 2f, -height.toFloat())
}
