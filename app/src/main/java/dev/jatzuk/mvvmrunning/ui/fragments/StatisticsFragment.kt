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
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentStatisticsBinding
import dev.jatzuk.mvvmrunning.other.CustomMarkerView
import dev.jatzuk.mvvmrunning.ui.viewmodels.StatisticsViewModel

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private var color = 0

    private val statisticsViewModel: StatisticsViewModel by viewModels()

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

            description.text = "Avg Speed over Time"
            legend.isEnabled = true
        }
    }

    private fun subscribeToObservers() {
        statisticsViewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds =
                    it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over time").apply {
                    valueTextColor = color
                    this.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.barChart.apply {
                    data = BarData(barDataSet)
                    marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
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
