package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentStatisticsBinding
import dev.jatzuk.mvvmrunning.other.CustomMarkerView
import dev.jatzuk.mvvmrunning.other.SortType
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.ui.viewmodels.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private var color = 0

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

    private val dateLabel = object : ValueFormatter() {

        private val calendar = Calendar.getInstance()

        override fun getBarLabel(barEntry: BarEntry?): String {
            calendar.timeInMillis = barEntry!!.y.toLong()
            return dateFormat.format(calendar.time)
        }
    }

    private val runLengthLabel = object : ValueFormatter() {

        override fun getBarLabel(barEntry: BarEntry?): String {
            return TrackingUtility.getFormattedStopWatchTime(barEntry!!.y.toLong())
        }
    }

    private val distanceLabel = object : ValueFormatter() {

        override fun getBarLabel(barEntry: BarEntry?): String {
            val km = barEntry!!.y / 1000f
            return ((km * 10).roundToInt() / 10f).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        binding.apply {
            statisticsViewModel = this@StatisticsFragment.statisticsViewModel
            lifecycleOwner = this@StatisticsFragment
        }

        setupBarChart()
        subscribeToObservers()

        color = ContextCompat.getColor(requireContext(), android.R.color.white)

        return binding.root
    }

    private fun setupBarChart() {
        with(binding.barChart) {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawLabels(false)
                axisLineColor = color
                textColor = color
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisLineColor = color
                textColor = color
                setDrawGridLines(false)
            }

            axisRight.apply {
                axisLineColor = color
                textColor = color
                setDrawGridLines(false)
            }

            description.text = requireContext().getString(R.string.your_records)
            legend.isEnabled = true
        }
    }

    private fun subscribeToObservers() {
        statisticsViewModel.runs.observe(viewLifecycleOwner, Observer {
            it?.reversed()?.let { runs ->
                val sortType = SortType.values()[binding.spFilter.selectedItemPosition]
                val barDataType = runs.indices.map { i ->
                    BarEntry(
                        i.toFloat(),
                        when (sortType) {
                            SortType.DATE -> runs[i].timestamp.toFloat()
                            SortType.RUNNING_TIME -> runs[i].timeInMillis.toFloat()
                            SortType.DISTANCE -> runs[i].distanceInMeters.toFloat()
                            SortType.AVG_SPEED -> runs[i].avgSpeedInKMH
                            SortType.CALORIES_BURNED -> runs[i].caloriesBurned.toFloat()
                        }
                    )
                }
                val labelName = sortType.name.replace('_', ' ')
                val barDataSet = BarDataSet(barDataType, labelName).apply {
                    valueTextSize = 15f
                    valueFormatter = when (sortType) {
                        SortType.DATE -> dateLabel
                        SortType.RUNNING_TIME -> runLengthLabel
                        SortType.DISTANCE -> distanceLabel
                        else -> null
                    }
                    valueTextColor = color
                    this.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.barChart.apply {
                    data = BarData(barDataSet)
                    setVisibleXRange(0f, xAxis.longestLabel.length.toFloat())
                    marker = CustomMarkerView(runs, requireContext(), R.layout.marker_view)
                    invalidate()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
