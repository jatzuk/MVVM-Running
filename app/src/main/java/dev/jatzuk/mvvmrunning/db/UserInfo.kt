package dev.jatzuk.mvvmrunning.db

import android.content.SharedPreferences
import dev.jatzuk.mvvmrunning.other.Constants.KEY_FIRST_TIME_TOGGLE
import dev.jatzuk.mvvmrunning.other.Constants.KEY_NAME
import dev.jatzuk.mvvmrunning.other.Constants.KEY_WEIGHT
import javax.inject.Inject

data class UserInfo @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    private var _name: String = loadNameFromSharedPreferences()
    val name get() = _name

    private var _weight: Float = loadWeightFromSharedPreferences()
    val weight get() = _weight

    private var _isFirstToggle: Boolean = loadToggleInfoFromSharedPreferences()
    val isFirstToggle get() = _isFirstToggle

    fun applyChanges(
        name: String = this.name,
        weight: Float = this.weight,
        isFirstToggle: Boolean = this.isFirstToggle
    ) {
        _name = name
        _weight = weight
        _isFirstToggle = isFirstToggle
        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight)
            .putBoolean(KEY_FIRST_TIME_TOGGLE, isFirstToggle)
            .apply()
    }

    private fun loadNameFromSharedPreferences() =
        sharedPreferences.getString(KEY_NAME, "User") ?: "User".also { _name = it }

    private fun loadWeightFromSharedPreferences() =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f).also { _weight = it }

    private fun loadToggleInfoFromSharedPreferences() =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true).also { _isFirstToggle = it }
}
