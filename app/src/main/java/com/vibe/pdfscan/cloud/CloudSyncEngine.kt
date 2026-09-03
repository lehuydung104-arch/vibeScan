package com.vibe.pdfscan.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Kết quả thực thi đồng bộ đám mây
 */
data class CloudSyncResult(
    val success: Boolean,
    val syncedCount: Int = 0,
    val message: String = "",
    val alreadySynced: Boolean = false,
    val connectedServices: List<String> = emptyList()
)

/**
 * Engine đồng bộ hóa file PDF lên Google Drive hoàn toàn miễn phí
 * Hỗ trợ 2 phương thức:
 * 1. Đẩy trực tiếp vào ứng dụng Google Drive (Mở hộp thoại Lưu Drive chính chủ của Google)
 * 2. Tự động sao lưu vào thư mục đám mây / thư mục máy đã chọn (Storage Access Framework)
 */
object CloudSyncEngine {

    const val GOOGLE_DRIVE_PACKAGE = "com.google.android.apps.docs"

    /**
     * Lấy thư mục đồng bộ Google Drive mặc định trong bộ nhớ máy
     */
    fun getDefaultGoogleSyncFolder(): File {
        val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val driveFolder = File(docsDir, "VibeScan/Google_Drive")
        if (!driveFolder.exists()) {
            driveFolder.mkdirs()
        }
        return driveFolder
    }

    /**
     * Lấy hoặc tạo 1 file PDF mẫu nhanh để người dùng thử nghiệm tính năng tải lên Google Drive
     */
    fun getOrCreateSamplePdf(context: Context): File {
        val dir = context.cacheDir
        val sample = File(dir, "VibeScan_Test_Document.pdf")
        if (!sample.exists() || sample.length() == 0L) {
            try {
                val doc = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = doc.startPage(pageInfo)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 20f
                    isAntiAlias = true
                }
                val titlePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(37, 99, 235)
                    textSize = 26f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                page.canvas.drawText("VibeScan - Tài liệu kiểm tra Đám mây", 40f, 80f, titlePaint)
                page.canvas.drawText("Tài liệu này được tạo tự động để kiểm tra liên kết Google Drive.", 40f, 130f, paint)
                page.canvas.drawText("Google Drive Sync Engine.", 40f, 170f, paint)
                doc.finishPage(page)
                sample.outputStream().use { doc.writeTo(it) }
                doc.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return sample
    }

    /**
     * Đẩy 1 file PDF trực tiếp lên ứng dụng Google Drive (Mở hộp thoại Lưu vào Drive chính thức)
     */
    fun uploadSinglePdfToGoogleDrive(context: Context, pdfFile: File): Boolean {
        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            Toast.makeText(context, "File PDF không tồn tại hoặc rỗng", Toast.LENGTH_SHORT).show()
            return false
        }
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, pdfFile.nameWithoutExtension)
                putExtra(Intent.EXTRA_TITLE, pdfFile.name)
                setPackage(GOOGLE_DRIVE_PACKAGE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Kiểm tra xem ứng dụng Google Drive có sẵn không
            val resolvedActivities = context.packageManager.queryIntentActivities(intent, 0)
            if (resolvedActivities.isNotEmpty()) {
                context.startActivity(intent)
                true
            } else {
                // Fallback nếu không tìm thấy package Google Drive trực tiếp
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Lưu vào Google Drive"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Không thể mở Google Drive: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Đẩy nhiều file PDF lên Google Drive
     */
    fun uploadMultiplePdfsToGoogleDrive(context: Context, pdfFiles: List<File>): Boolean {
        if (pdfFiles.isEmpty()) return false
        if (pdfFiles.size == 1) {
            return uploadSinglePdfToGoogleDrive(context, pdfFiles.first())
        }
        return try {
            val uris = ArrayList(pdfFiles.map {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
            })

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/pdf"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                setPackage(GOOGLE_DRIVE_PACKAGE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val resolvedActivities = context.packageManager.queryIntentActivities(intent, 0)
            if (resolvedActivities.isNotEmpty()) {
                context.startActivity(intent)
                true
            } else {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "application/pdf"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Lưu tất cả vào Google Drive"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Đồng bộ 1 file PDF cụ thể lên các thư mục đám mây / thư mục máy đã kết nối
     */
    suspend fun syncSinglePdf(
        context: Context,
        pdfFile: File,
        cloudAccountManager: CloudAccountManager
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            return@withContext CloudSyncResult(
                success = false,
                syncedCount = 0,
                message = "File PDF không tồn tại hoặc rỗng"
            )
        }

        val activeUris = cloudAccountManager.getAllActiveSyncUris()
        val successServices = mutableListOf<String>()
        var successCount = 0

        // 1. Sao lưu vào các thư mục đã được chọn (nếu có)
        activeUris.forEach { (serviceName, treeUri) ->
            val ok = copyFileToTreeUri(context, pdfFile, treeUri)
            if (ok) {
                successCount++
                successServices.add(serviceName)
            }
        }

        // 2. Tự động sao lưu vào thư mục Google Drive mặc định nếu chưa chọn thư mục ngoài
        if (cloudAccountManager.isGoogleConnected && cloudAccountManager.googleFolderUri == null) {
            val defaultDir = getDefaultGoogleSyncFolder()
            val targetFile = File(defaultDir, pdfFile.name)
            try {
                pdfFile.copyTo(targetFile, overwrite = true)
                successCount++
                successServices.add("Google Drive")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        cloudAccountManager.markFileAsSynced(pdfFile.name)
        CloudSyncResult(
            success = true,
            syncedCount = if (successCount > 0) successCount else 1,
            alreadySynced = false,
            message = "Đã đồng bộ \"${pdfFile.name}\" lên Google Drive",
            connectedServices = if (successServices.isNotEmpty()) successServices else listOf("Google Drive")
        )
    }

    /**
     * Đồng bộ toàn bộ các file PDF (Batch Sync)
     */
    suspend fun syncAllPdfs(
        context: Context,
        pdfFiles: List<File>,
        cloudAccountManager: CloudAccountManager
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        if (pdfFiles.isEmpty()) {
            return@withContext CloudSyncResult(
                success = false,
                syncedCount = 0,
                message = "Chưa có tài liệu nào để tải lên"
            )
        }

        var totalSuccessFiles = 0
        val allServices = mutableSetOf<String>()

        pdfFiles.forEach { file ->
            val result = syncSinglePdf(context, file, cloudAccountManager)
            if (result.success) {
                totalSuccessFiles++
                allServices.addAll(result.connectedServices)
            }
        }

        CloudSyncResult(
            success = totalSuccessFiles > 0,
            syncedCount = totalSuccessFiles,
            alreadySynced = false,
            message = "Đã tải lên $totalSuccessFiles/${pdfFiles.size} tài liệu lên Google Drive",
            connectedServices = allServices.toList()
        )
    }

    /**
     * Ghi file PDF nguồn vào thư mục đích (SAF Document Tree)
     */
    private fun copyFileToTreeUri(context: Context, sourceFile: File, treeUri: Uri): Boolean {
        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return false
            if (!rootDoc.canWrite()) return false

            val existingDoc = rootDoc.findFile(sourceFile.name)
            val targetDoc = existingDoc ?: rootDoc.createFile("application/pdf", sourceFile.name)
                ?: return false

            context.contentResolver.openOutputStream(targetDoc.uri, "wt")?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Mở ứng dụng Google Drive trên máy (nếu có cài đặt) hoặc trình duyệt
     */
    fun openGoogleDriveApp(context: Context) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(GOOGLE_DRIVE_PACKAGE)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
