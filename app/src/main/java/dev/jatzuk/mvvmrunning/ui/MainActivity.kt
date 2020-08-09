package dev.jatzuk.mvvmrunning.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.databinding.ActivityMainBinding
import dev.jatzuk.mvvmrunning.db.UserInfo
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_FINISH_RUN
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dev.jatzuk.mvvmrunning.ui.fragments.TrackingFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    @Inject
    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootView)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.navHostFragment)

        navigateToTrackingFragmentIfNeeded(intent)

        with(binding) {
            bottomNavigationView.apply {
                setupWithNavController(navController)
                setOnNavigationItemReselectedListener { /* no-op */ }
            }
            tvToolbarTitle.text = getString(R.string.let_s_go_username, userInfo.name)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = when (destination.id) {
                R.id.settingsFragment, R.id.runsFragment, R.id.statisticsFragment -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        when (intent?.action) {
            ACTION_SHOW_TRACKING_FRAGMENT -> {
                navController.navigate(R.id.action_global_trackingFragment)
            }
            ACTION_FINISH_RUN -> {
                val action = TrackingFragmentDirections.actionGlobalTrackingFragment(true)
                navController.navigate(action)
            }
        }
    }
}
