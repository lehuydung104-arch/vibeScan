package com.vibe.pdfscan.cloud

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri

class CloudAccountManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vibe_cloud_sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GOOGLE_EMAIL = "google_email"
        private const val KEY_GOOGLE_NAME = "google_name"
        private const val KEY_GOOGLE_CONNECTED = "google_connected"

        private const val KEY_MS_EMAIL = "ms_email"
        private const val KEY_MS_NAME = "ms_name"
        private const val KEY_MS_CONNECTED = "ms_connected"

        private const val KEY_AUTO_SYNC = "auto_sync_enabled"
        private const val KEY_SYNC_FOLDER_URI = "sync_folder_uri"
        private const val KEY_SYNC_FOLDER_NAME = "sync_folder_name"
        private const val KEY_SYNCED_FILES = "synced_files_set"
    }

    // Google Account với Compose State để tự động cập nhật giao diện thời gian thực
    var isGoogleConnected by mutableStateOf(prefs.getBoolean(KEY_GOOGLE_CONNECTED, false))
        private set

    var googleEmail by mutableStateOf(prefs.getString(KEY_GOOGLE_EMAIL, null))
        private set

    var googleName by mutableStateOf(prefs.getString(KEY_GOOGLE_NAME, null))
        private set

    fun setGoogleAccount(email: String?, name: String?) {
        val connected = email != null
        isGoogleConnected = connected
        googleEmail = email
        googleName = name

        prefs.edit {
            putBoolean(KEY_GOOGLE_CONNECTED, connected)
            putString(KEY_GOOGLE_EMAIL, email)
            putString(KEY_GOOGLE_NAME, name)
        }
    }

    fun clearGoogleAccount() {
        isGoogleConnected = false
        googleEmail = null
        googleName = null

        prefs.edit {
            putBoolean(KEY_GOOGLE_CONNECTED, false)
            remove(KEY_GOOGLE_EMAIL)
            remove(KEY_GOOGLE_NAME)
        }
    }

    // Microsoft Account với Compose State
    var isMicrosoftConnected by mutableStateOf(prefs.getBoolean(KEY_MS_CONNECTED, false))
        private set

    var microsoftEmail by mutableStateOf(prefs.getString(KEY_MS_EMAIL, null))
        private set

    var microsoftName by mutableStateOf(prefs.getString(KEY_MS_NAME, null))
        private set

    fun setMicrosoftAccount(email: String?, name: String?) {
        val connected = email != null
        isMicrosoftConnected = connected
        microsoftEmail = email
        microsoftName = name

        prefs.edit {
            putBoolean(KEY_MS_CONNECTED, connected)
            putString(KEY_MS_EMAIL, email)
            putString(KEY_MS_NAME, name)
        }
    }

    fun clearMicrosoftAccount() {
        isMicrosoftConnected = false
        microsoftEmail = null
        microsoftName = null

        prefs.edit {
            putBoolean(KEY_MS_CONNECTED, false)
            remove(KEY_MS_CONNECTED)
            remove(KEY_MS_EMAIL)
            remove(KEY_MS_NAME)
        }
    }

    // Cài đặt tự động đồng bộ (Auto Sync)
    private val _isAutoSyncEnabled = mutableStateOf(prefs.getBoolean(KEY_AUTO_SYNC, true))
    var isAutoSyncEnabled: Boolean
        get() = _isAutoSyncEnabled.value
        set(value) {
            _isAutoSyncEnabled.value = value
            prefs.edit { putBoolean(KEY_AUTO_SYNC, value) }
        }

    // Thư mục đồng bộ trực tiếp trên đám mây (Storage Access Framework)
    private val _syncFolderUri = mutableStateOf(
        prefs.getString(KEY_SYNC_FOLDER_URI, null)?.let {
            try {
                it.toUri()
            } catch (_: Exception) {
                null
            }
        }
    )
    var syncFolderUri: Uri?
        get() = _syncFolderUri.value
        set(value) {
            _syncFolderUri.value = value
            prefs.edit { putString(KEY_SYNC_FOLDER_URI, value?.toString()) }
        }

    private val _syncFolderName = mutableStateOf(prefs.getString(KEY_SYNC_FOLDER_NAME, null))
    var syncFolderName: String?
        get() = _syncFolderName.value
        set(value) {
            _syncFolderName.value = value
            prefs.edit { putString(KEY_SYNC_FOLDER_NAME, value) }
        }

    // Quản lý danh sách file đã đồng bộ
    fun isFileSynced(fileName: String): Boolean {
        val set = prefs.getStringSet(KEY_SYNCED_FILES, emptySet()) ?: emptySet()
        return set.contains(fileName)
    }

    fun markFileAsSynced(fileName: String) {
        val currentSet = prefs.getStringSet(KEY_SYNCED_FILES, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(fileName)
        prefs.edit { putStringSet(KEY_SYNCED_FILES, currentSet) }
    }

    val isAnyCloudConnected: Boolean
        get() = isGoogleConnected || isMicrosoftConnected || syncFolderUri != null
}
