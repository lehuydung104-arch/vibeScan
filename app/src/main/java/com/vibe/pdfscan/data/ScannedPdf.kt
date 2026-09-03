package com.vibe.pdfscan.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Đại diện cho một tài liệu PDF đã quét và lưu trữ trên máy
 */
data class ScannedPdf(
    val file: File,
    val isSynced: Boolean = false,
    val category: String = "Bản scan"
) {
    val name: String get() = file.name
    val sizeBytes: Long get() = file.length()
    val lastModified: Long get() = file.lastModified()

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
                else -> "$sizeBytes B"
            }
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}
