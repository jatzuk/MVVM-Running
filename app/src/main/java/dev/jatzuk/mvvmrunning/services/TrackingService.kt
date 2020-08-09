package dev.jatzuk.mvvmrunning.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_FINISH_RUN
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_PAUSE_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.ACTION_STOP_SERVICE
import dev.jatzuk.mvvmrunning.other.Constants.FASTEST_LOCATION_INTERVAL
import dev.jatzuk.mvvmrunning.other.Constants.LOCATION_UPDATE_INTERVAL
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_CHANNEL_ID
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_CHANNEL_NAME
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_CHANNEL_TARGET_ID
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_CHANNEL_TARGET_NAME
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_ID
import dev.jatzuk.mvvmrunning.other.Constants.NOTIFICATION_TARGET_ID
import dev.jatzuk.mvvmrunning.other.Constants.REQUEST_CODE_ACTION_FINISH_RUN
import dev.jatzuk.mvvmrunning.other.TargetType
import dev.jatzuk.mvvmrunning.other.TrackingUtility
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository
import dev.jatzuk.mvvmrunning.repositories.TrackingRepository.Companion.isTracking
import dev.jatzuk.mvvmrunning.ui.MainActivity
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var trackingRepository: TrackingRepository
    private var isAlreadyNotifiedAboutTargetReached = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (isTracking.value!!) {
                locationResult?.locations?.forEach(::addPathPoint)
            }
        }
    }

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    @Named("baseNotificationBuilder")
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    @Named("targetReachedNotificationBuilder")
    lateinit var targetReachedNotificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        trackingRepository.initStartingValues()

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })

        TrackingRepository.isTargetReached.observe(this, Observer {
            if (it && !isAlreadyNotifiedAboutTargetReached) notifyTargetReached()
        })
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            trackingRepository.addPoint(LatLng(it.latitude, it.longitude))
        }
    }

    // Handled by EasyPermissions
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service, first run: ${trackingRepository.isFirstRun}")
                    if (trackingRepository.isFirstRun) {
                        startForegroundService()
                        trackingRepository.startRun(true)
                    } else {
                        Timber.d("Resuming service...")
                        trackingRepository.startRun()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    trackingRepository.pauseRun()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText =
            if (isTracking) getString(R.string.pause)
            else getString(R.string.resume)

        val pendingIntent = PendingIntent.getService(
            this,
            if (isTracking) 1 else 2,
            Intent(this, TrackingService::class.java).apply {
                action = if (isTracking) ACTION_PAUSE_SERVICE else ACTION_START_OR_RESUME_SERVICE
            },
            FLAG_UPDATE_CURRENT
        )

        baseNotificationBuilder.apply {
            mActions.clear()
            addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
        }

        if (!trackingRepository.isCancelled) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.build())
        }
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        TrackingRepository.timeRunInSeconds.observe(this, Observer {
            if (!trackingRepository.isCancelled) {
                val time = TrackingUtility.getFormattedStopWatchTime(it * 1000L)
                val info = "${TrackingRepository.distanceInMeters.value} m | " +
                        "${TrackingRepository.caloriesBurned.value} kcal" +
                        if (TrackingRepository.targetType.value!! != TargetType.NONE) {
                            " | ${TrackingRepository.progress.value!!} % - ${getString(R.string.target).toLowerCase(
                                Locale.getDefault()
                            )}"
                        } else ""

                baseNotificationBuilder
                    .setContentTitle(time)
                    .setContentText(info)
                notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTargetReachedNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_TARGET_ID,
            NOTIFICATION_CHANNEL_TARGET_NAME,
            IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("RestrictedApi")
    private fun notifyTargetReached() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createTargetReachedNotificationChannel(notificationManager)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_ACTION_FINISH_RUN,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_FINISH_RUN
            },
            FLAG_UPDATE_CURRENT
        )

        targetReachedNotificationBuilder
            .addAction(
                R.drawable.ic_pause_black_24dp,
                getString(R.string.finish),
                pendingIntent
            )

        if (!trackingRepository.isCancelled) {
            notificationManager.notify(
                NOTIFICATION_TARGET_ID,
                targetReachedNotificationBuilder.build()
            )
            isAlreadyNotifiedAboutTargetReached = true
        }
    }

    private fun killService() {
        trackingRepository.cancelRun()
        stopForeground(true)
        stopSelf()
        isAlreadyNotifiedAboutTargetReached = false
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
