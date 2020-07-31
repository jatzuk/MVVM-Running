package dev.jatzuk.mvvmrunning.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.FragmentStatisticsBinding
import dev.jatzuk.mvvmrunning.ui.viewmodels.StatisticsViewModel

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        binding.apply {
            this.statisticsViewModel = this@StatisticsFragment.statisticsViewModel
            lifecycleOwner = this@StatisticsFragment
        }
        return binding.root
    }
}
