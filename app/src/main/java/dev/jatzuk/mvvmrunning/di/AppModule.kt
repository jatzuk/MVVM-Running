package dev.jatzuk.mvvmrunning.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.jatzuk.mvvmrunning.db.RunningDatabase
import dev.jatzuk.mvvmrunning.db.UserInfo
import dev.jatzuk.mvvmrunning.other.Constants.KEY_FIRST_TIME_TOGGLE
import dev.jatzuk.mvvmrunning.other.Constants.KEY_NAME
import dev.jatzuk.mvvmrunning.other.Constants.KEY_WEIGHT
import dev.jatzuk.mvvmrunning.other.Constants.RUNNING_DATABASE_NAME
import dev.jatzuk.mvvmrunning.other.Constants.SHARED_PREFERENCES_NAME
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    fun provideRunningDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences): String =
        sharedPreferences.getString(KEY_NAME, "User") ?: "User"

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences): Float =
        sharedPreferences.getFloat(KEY_WEIGHT, 0f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences): Boolean =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

    @Singleton
    @Provides
    fun provideUserInfo(@ApplicationContext context: Context) = UserInfo(
        provideSharedPreferences(context)
    )
}
