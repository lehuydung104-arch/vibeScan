package com.vibe.pdfscan.data

import androidx.compose.runtime.Immutable
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Đại diện cho một tài liệu PDF đã quét và lưu trữ trên máy.
 * Đánh dấu @Immutable để Jetpack Compose tối ưu hóa recomposition và cuộn LazyColumn đạt 120 FPS mượt mà tuyệt đối.
 */
@Immutable
data class ScannedPdf(
    val file: File,
    val isSynced: Boolean = false,
    val category: String = "Bản scan",
    val name: String = file.name,
    val sizeBytes: Long = file.length(),
    val lastModified: Long = file.lastModified(),
    val formattedSize: String = formatSize(file.length()),
    val formattedDate: String = formatDate(file.lastModified())
) {
    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())

        fun formatSize(size: Long): String {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
                else -> "$size B"
            }
        }

        fun formatDate(time: Long): String {
            return dateFormat.format(Date(time))
        }
    }
}
