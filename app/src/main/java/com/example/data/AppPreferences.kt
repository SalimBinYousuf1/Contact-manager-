package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.DisplayNameOrder
import com.example.domain.ListDensity
import com.example.domain.SortOrder
import com.example.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("salim_prefs", Context.MODE_PRIVATE)

    private val _sortOrder = MutableStateFlow(
        SortOrder.valueOf(prefs.getString(KEY_SORT_ORDER, SortOrder.FIRST_NAME.name) ?: SortOrder.FIRST_NAME.name)
    )
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _displayNameOrder = MutableStateFlow(
        DisplayNameOrder.valueOf(prefs.getString(KEY_NAME_ORDER, DisplayNameOrder.FIRST_LAST.name) ?: DisplayNameOrder.FIRST_LAST.name)
    )
    val displayNameOrder: StateFlow<DisplayNameOrder> = _displayNameOrder.asStateFlow()

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _listDensity = MutableStateFlow(
        ListDensity.valueOf(prefs.getString(KEY_DENSITY, ListDensity.COMFORTABLE.name) ?: ListDensity.COMFORTABLE.name)
    )
    val listDensity: StateFlow<ListDensity> = _listDensity.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK, false))
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _appLockPin = MutableStateFlow(prefs.getString(KEY_PIN, "1234") ?: "1234")
    val appLockPin: StateFlow<String> = _appLockPin.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setSortOrder(order: SortOrder) {
        prefs.edit().putString(KEY_SORT_ORDER, order.name).apply()
        _sortOrder.value = order
    }

    fun setDisplayNameOrder(order: DisplayNameOrder) {
        prefs.edit().putString(KEY_NAME_ORDER, order.name).apply()
        _displayNameOrder.value = order
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setListDensity(density: ListDensity) {
        prefs.edit().putString(KEY_DENSITY, density.name).apply()
        _listDensity.value = density
    }

    fun setAppLock(enabled: Boolean, pin: String = "1234") {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).putString(KEY_PIN, pin).apply()
        _isAppLockEnabled.value = enabled
        _appLockPin.value = pin
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        _hasCompletedOnboarding.value = true
    }

    companion object {
        private const val KEY_SORT_ORDER = "pref_sort_order"
        private const val KEY_NAME_ORDER = "pref_name_order"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DENSITY = "pref_density"
        private const val KEY_APP_LOCK = "pref_app_lock"
        private const val KEY_PIN = "pref_pin"
        private const val KEY_ONBOARDING = "pref_onboarding"
    }
}
