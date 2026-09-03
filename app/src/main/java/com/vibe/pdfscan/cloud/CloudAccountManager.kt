package com.vibe.pdfscan.cloud

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri

enum class CloudTarget {
    GOOGLE_DRIVE,
    GENERAL
}

/**
 * Quản lý cấu hình liên kết tài khoản và thư mục đám mây Google Drive
 */
class CloudAccountManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vibe_cloud_sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GOOGLE_EMAIL = "google_email"
        private const val KEY_GOOGLE_NAME = "google_name"
        private const val KEY_GOOGLE_CONNECTED = "google_connected"
        private const val KEY_GOOGLE_FOLDER_URI = "google_folder_uri"
        private const val KEY_GOOGLE_FOLDER_NAME = "google_folder_name"

        private const val KEY_GENERAL_FOLDER_URI = "general_sync_folder_uri"
        private const val KEY_GENERAL_FOLDER_NAME = "general_sync_folder_name"

        private const val KEY_SYNCED_FILES = "synced_files_set"
    }

    // ==================== GOOGLE DRIVE ====================
    var isGoogleConnected by mutableStateOf(prefs.getBoolean(KEY_GOOGLE_CONNECTED, false))
        private set

    var googleEmail by mutableStateOf(prefs.getString(KEY_GOOGLE_EMAIL, null))
        private set

    var googleName by mutableStateOf(prefs.getString(KEY_GOOGLE_NAME, null))
        private set

    var googleFolderUri: Uri? by mutableStateOf(
        prefs.getString(KEY_GOOGLE_FOLDER_URI, null)?.let { runCatching { it.toUri() }.getOrNull() }
    )
        private set

    var googleFolderName by mutableStateOf(prefs.getString(KEY_GOOGLE_FOLDER_NAME, null))
        private set

    fun setGoogleAccount(email: String, name: String? = null) {
        isGoogleConnected = true
        googleEmail = email
        if (name != null) googleName = name
        prefs.edit {
            putBoolean(KEY_GOOGLE_CONNECTED, true)
            putString(KEY_GOOGLE_EMAIL, email)
            if (name != null) putString(KEY_GOOGLE_NAME, name)
        }
    }

    fun setGoogleSyncFolder(uri: Uri, folderName: String?, email: String? = null) {
        isGoogleConnected = true
        googleFolderUri = uri
        googleFolderName = folderName ?: "Google Drive"
        if (email != null) googleEmail = email

        prefs.edit {
            putBoolean(KEY_GOOGLE_CONNECTED, true)
            putString(KEY_GOOGLE_FOLDER_URI, uri.toString())
            putString(KEY_GOOGLE_FOLDER_NAME, googleFolderName)
            if (email != null) putString(KEY_GOOGLE_EMAIL, email)
        }
    }

    fun clearGoogleAccount() {
        isGoogleConnected = false
        googleEmail = null
        googleName = null
        googleFolderUri = null
        googleFolderName = null

        prefs.edit {
            putBoolean(KEY_GOOGLE_CONNECTED, false)
            remove(KEY_GOOGLE_EMAIL)
            remove(KEY_GOOGLE_NAME)
            remove(KEY_GOOGLE_FOLDER_URI)
            remove(KEY_GOOGLE_FOLDER_NAME)
        }
    }

    // ==================== THƯ MỤC CHUNG ====================
    var generalFolderUri: Uri? by mutableStateOf(
        prefs.getString(KEY_GENERAL_FOLDER_URI, null)?.let { runCatching { it.toUri() }.getOrNull() }
    )
        private set

    var generalFolderName by mutableStateOf(prefs.getString(KEY_GENERAL_FOLDER_NAME, null))
        private set

    fun setGeneralSyncFolder(uri: Uri, folderName: String?) {
        generalFolderUri = uri
        generalFolderName = folderName ?: "Thư mục tùy chọn"
        prefs.edit {
            putString(KEY_GENERAL_FOLDER_URI, uri.toString())
            putString(KEY_GENERAL_FOLDER_NAME, generalFolderName)
        }
    }

    fun clearGeneralSyncFolder() {
        generalFolderUri = null
        generalFolderName = null
        prefs.edit {
            remove(KEY_GENERAL_FOLDER_URI)
            remove(KEY_GENERAL_FOLDER_NAME)
        }
    }

    // Thư mục đồng bộ chính hiển thị
    val syncFolderUri: Uri?
        get() = googleFolderUri ?: generalFolderUri

    val syncFolderName: String?
        get() = googleFolderName ?: generalFolderName

    // Quản lý danh sách file đã đồng bộ (Chống trùng lặp khi đồng bộ)
    fun isFileSynced(fileName: String): Boolean {
        val set = prefs.getStringSet(KEY_SYNCED_FILES, emptySet()) ?: emptySet()
        return set.contains(fileName)
    }

    fun markFileAsSynced(fileName: String) {
        val currentSet = prefs.getStringSet(KEY_SYNCED_FILES, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(fileName)
        prefs.edit { putStringSet(KEY_SYNCED_FILES, currentSet) }
    }

    fun markFileAsUnsynced(fileName: String) {
        val currentSet = prefs.getStringSet(KEY_SYNCED_FILES, emptySet())?.toMutableSet() ?: return
        if (currentSet.remove(fileName)) {
            prefs.edit { putStringSet(KEY_SYNCED_FILES, currentSet) }
        }
    }

    fun renameSyncedFile(oldName: String, newName: String) {
        val currentSet = prefs.getStringSet(KEY_SYNCED_FILES, emptySet())?.toMutableSet() ?: return
        if (currentSet.remove(oldName)) {
            currentSet.add(newName)
            prefs.edit { putStringSet(KEY_SYNCED_FILES, currentSet) }
        }
    }

    fun getSyncedFileNames(): Set<String> {
        return prefs.getStringSet(KEY_SYNCED_FILES, emptySet()) ?: emptySet()
    }

    fun clearAllSyncedFiles() {
        prefs.edit { remove(KEY_SYNCED_FILES) }
    }

    val isAnyCloudConnected: Boolean
        get() = isGoogleConnected || generalFolderUri != null

    /**
     * Lấy danh sách tất cả các URI thư mục đám mây đang kích hoạt để đồng bộ file
     */
    fun getAllActiveSyncUris(): Map<String, Uri> {
        val map = mutableMapOf<String, Uri>()
        googleFolderUri?.let { map["Google Drive (${googleFolderName ?: "Thư mục"})"] = it }
        if (map.isEmpty()) {
            generalFolderUri?.let { map[generalFolderName ?: "Thư mục đám mây"] = it }
        }
        return map
    }
}
