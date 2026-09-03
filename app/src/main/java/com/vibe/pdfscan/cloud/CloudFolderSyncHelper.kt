package com.vibe.pdfscan.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream

object CloudFolderSyncHelper {

    /**
     * Giữ quyền truy cập lâu dài (persistable permission) vào thư mục đã chọn
     */
    fun takePersistablePermission(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Tự động sao chép file PDF sang thư mục đám mây (Google Drive / OneDrive)
     * Tự động tạo và lưu vào thư mục con riêng biệt (Hóa đơn, Hợp đồng, Biên bản...)
     */
    fun syncPdfToFolder(
        context: Context,
        folderUri: Uri,
        pdfFile: File,
        categoryName: String? = null
    ): Boolean {
        return try {
            val rootFolder = DocumentFile.fromTreeUri(context, folderUri) ?: return false
            if (!rootFolder.canWrite()) return false

            // Tìm hoặc tự động tạo thư mục con tương ứng trên Google Drive / OneDrive
            val targetFolder = if (!categoryName.isNullOrBlank() && categoryName != "Tất cả") {
                rootFolder.findFile(categoryName) ?: rootFolder.createDirectory(categoryName) ?: rootFolder
            } else {
                rootFolder
            }

            // Kiểm tra xem file đã tồn tại trong thư mục chưa, nếu có thì xóa để ghi đè bản mới nhất
            val existingFile = targetFolder.findFile(pdfFile.name)
            existingFile?.delete()

            // Tạo file mới trong thư mục đám mây
            val newFile = targetFolder.createFile("application/pdf", pdfFile.name) ?: return false

            context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                FileInputStream(pdfFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
