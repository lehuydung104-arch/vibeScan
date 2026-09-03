package com.vibe.pdfscan.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.vibe.pdfscan.ui.theme.ThemeColorPreset
import com.vibe.pdfscan.ui.theme.ThemeDisplayMode

/**
 * Quản lý Cài đặt Giao diện, Màu sắc và Theme của VibeScan
 * Tự động đồng bộ với SharedPreferences và cập nhật State của Compose trong thời gian thực.
 */
class ThemeManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vibe_theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "key_theme_display_mode"
        private const val KEY_PRESET_ID = "key_theme_preset_id"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color_enabled"
    }

    // Chế độ hiển thị: SYSTEM / LIGHT / DARK
    private val _displayMode = mutableStateOf(
        try {
            val savedMode = prefs.getString(KEY_THEME_MODE, ThemeDisplayMode.SYSTEM.name)
            ThemeDisplayMode.valueOf(savedMode ?: ThemeDisplayMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeDisplayMode.SYSTEM
        }
    )
    val displayMode: ThemeDisplayMode get() = _displayMode.value

    // Theme Preset đang chọn (Đơn sắc hoặc Gradient)
    private val _selectedPreset = mutableStateOf(
        ThemeColorPreset.fromId(prefs.getString(KEY_PRESET_ID, ThemeColorPreset.BLUE_INDIGO.id))
    )
    val selectedPreset: ThemeColorPreset get() = _selectedPreset.value

    // Tùy chọn Dynamic Color của Material You (Android 12+)
    private val _isDynamicColorEnabled = mutableStateOf(
        prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
    )
    val isDynamicColorEnabled: Boolean get() = _isDynamicColorEnabled.value

    /**
     * Cập nhật chế độ hiển thị (Sáng / Tối / Theo hệ thống)
     */
    fun setDisplayMode(mode: ThemeDisplayMode) {
        _displayMode.value = mode
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    /**
     * Cập nhật màu chủ đạo / Theme Preset
     */
    fun setPreset(preset: ThemeColorPreset) {
        _selectedPreset.value = preset
        // Khi người dùng chọn 1 preset cụ thể, tắt dynamic color để áp dụng màu đó ngay
        if (_isDynamicColorEnabled.value) {
            _isDynamicColorEnabled.value = false
            prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, false) }
        }
        prefs.edit { putString(KEY_PRESET_ID, preset.id) }
    }

    /**
     * Bật / tắt màu động Material You (Android 12+)
     */
    fun setDynamicColor(enabled: Boolean) {
        _isDynamicColorEnabled.value = enabled
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
    }
}
