package com.vibe.pdfscan.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.vibe.pdfscan.scanner.DocFilterMode
import com.vibe.pdfscan.scanner.ImageFilterHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

enum class DocumentCategory(val folderName: String, val icon: String, val displayName: String) {
    ALL("Tất cả", "📁", "Tất cả"),
    INVOICE("Hóa đơn", "🧾", "Hóa đơn"),
    CONTRACT("Hợp đồng", "📑", "Hợp đồng"),
    RECORD("Biên bản", "📋", "Biên bản"),
    OFFICIAL_LETTER("Công văn", "📨", "Công văn"),
    IDENTIFICATION("Giấy tờ", "🪪", "Giấy tờ"),
    DOCUMENT("Tài liệu", "📄", "Tài liệu"),
    BOOK("Sách vở", "📚", "Sách vở"),
    GENERAL("Bản scan", "📁", "Bản scan");

    companion object {
        fun fromFileName(fileName: String): DocumentCategory {
            val lower = fileName.lowercase()
            return when {
                lower.contains("hoadon") || lower.contains("hóa đơn") || lower.contains("hoa_don") ||
                lower.contains("bienlai") || lower.contains("biên lai") || lower.contains("invoice") || lower.contains("receipt") -> INVOICE
                lower.contains("hopdong") || lower.contains("hợp đồng") || lower.contains("hop_dong") || lower.contains("contract") -> CONTRACT
                lower.contains("bienban") || lower.contains("biên bản") || lower.contains("bien_ban") -> RECORD
                lower.contains("congvan") || lower.contains("công văn") || lower.contains("cong_van") -> OFFICIAL_LETTER
                lower.contains("giayto") || lower.contains("giấy tờ") || lower.contains("giay_to") || lower.contains("cccd") || lower.contains("cmnd") -> IDENTIFICATION
                lower.contains("tailieu") || lower.contains("tài liệu") || lower.contains("tai_lieu") || lower.contains("document") -> DOCUMENT
                lower.contains("sach") || lower.contains("sách") || lower.contains("book") -> BOOK
                else -> GENERAL
            }
        }
    }
}

class PdfStorageManager(private val context: Context) {

    // Thư mục gốc lưu trữ VibeScan trong bộ nhớ tài liệu của ứng dụng
    val storageDir: File by lazy {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val scanFolder = File(baseDir, "VibeScan")
        if (!scanFolder.exists()) {
            scanFolder.mkdirs()
        }
        scanFolder
    }

    /**
     * Lấy hoặc tạo thư mục con riêng biệt theo danh mục (Hóa đơn, Hợp đồng, v.v.)
     */
    fun getCategoryDir(category: DocumentCategory): File {
        val folder = if (category == DocumentCategory.ALL) {
            storageDir
        } else {
            File(storageDir, category.folderName)
        }
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    /**
     * Tự động tạo tên file duy nhất chống trùng lặp:
     * Nếu "Scan_03_09_2026.pdf" đã tồn tại, tự động tăng thành "Scan_03_09_2026 (1).pdf", "(2).pdf"...
     */
    fun getUniqueFile(baseName: String, category: DocumentCategory = DocumentCategory.fromFileName(baseName)): File {
        var clean = baseName.trim().replace('/', '-')
        if (clean.endsWith(".pdf", ignoreCase = true)) {
            clean = clean.substring(0, clean.length - 4)
        }

        val targetDir = getCategoryDir(category)
        var target = File(targetDir, "$clean.pdf")
        var counter = 1

        // Kiểm tra xem file đã tồn tại trong thư mục con này hoặc thư mục gốc chưa
        while (target.exists() || File(storageDir, "$clean${if (counter > 1) " ($counter)" else ""}.pdf").exists()) {
            target = File(targetDir, "$clean ($counter).pdf")
            counter++
        }
        return target
    }

    /**
     * Lấy tên file gợi ý chống trùng cho giao diện nhập tên
     */
    fun getSuggestedName(baseName: String): String {
        val category = DocumentCategory.fromFileName(baseName)
        return getUniqueFile(baseName, category).name.removeSuffix(".pdf")
    }

    /**
     * Sao chép file PDF vào đúng thư mục con theo loại tài liệu
     */
    fun saveScannedPdf(sourceUri: Uri, customName: String? = null): ScannedPdf? {
        return try {
            val baseName = if (!customName.isNullOrBlank()) {
                customName
            } else {
                val timestamp = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
                "Scan_$timestamp"
            }

            val category = DocumentCategory.fromFileName(baseName)
            val targetFile = getUniqueFile(baseName, category)

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                ScannedPdf(targetFile, isSynced = false, category = category.displayName)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Xuất và lưu file PDF theo bộ lọc (Nâng cao, Gốc, Trắng đen...) vào đúng thư mục riêng
     */
    fun saveFilteredPdf(
        context: Context,
        fallbackPdfUri: Uri,
        pageUris: List<Uri>,
        customName: String?,
        filterMode: DocFilterMode,
        rotationDegrees: Float
    ): ScannedPdf? {
        val baseName = if (!customName.isNullOrBlank()) {
            customName
        } else {
            val timestamp = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
            "Scan_$timestamp"
        }

        val category = DocumentCategory.fromFileName(baseName)
        val targetFile = getUniqueFile(baseName, category)

        // Nếu người dùng giữ nguyên ảnh Gốc và không xoay, sao chép file gốc
        if (filterMode == DocFilterMode.ORIGINAL && rotationDegrees % 360f == 0f) {
            return saveScannedPdf(fallbackPdfUri, targetFile.name)
        }

        // Nếu người dùng chọn Nâng cao, Trắng đen hoặc Xoay, tạo file PDF mới
        val success = ImageFilterHelper.exportToPdf(
            context = context,
            pageUris = pageUris,
            filterMode = filterMode,
            rotationDegrees = rotationDegrees,
            targetFile = targetFile
        )

        return if (success && targetFile.exists() && targetFile.length() > 0) {
            ScannedPdf(targetFile, isSynced = false, category = category.displayName)
        } else {
            saveScannedPdf(fallbackPdfUri, targetFile.name)
        }
    }

    /**
     * Lấy danh sách tất cả các file PDF đã quét từ thư mục gốc và mọi thư mục con
     */
    fun getAllPdfs(isSyncedChecker: (String) -> Boolean = { false }): List<ScannedPdf> {
        val pdfList = mutableListOf<ScannedPdf>()

        // Quét đệ quy thư mục gốc và tất cả thư mục con (Hóa đơn, Hợp đồng, Biên bản...)
        storageDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension.equals("pdf", ignoreCase = true) && file.length() > 0) {
                // Xác định category dựa trên tên thư mục cha hoặc tên file
                val parentName = file.parentFile?.name
                val categoryName = if (parentName != null && parentName != "VibeScan") {
                    parentName
                } else {
                    DocumentCategory.fromFileName(file.name).displayName
                }

                pdfList.add(
                    ScannedPdf(
                        file = file,
                        isSynced = isSyncedChecker(file.name),
                        category = categoryName
                    )
                )
            }
        }

        return pdfList.sortedByDescending { it.lastModified }
    }

    /**
     * Xóa 1 file PDF
     */
    fun deletePdf(pdf: ScannedPdf): Boolean {
        return try {
            pdf.file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Xóa nhiều file PDF cùng một lúc (Batch Delete)
     */
    fun deleteMultiplePdfs(pdfs: List<ScannedPdf>): Int {
        var deletedCount = 0
        for (pdf in pdfs) {
            if (deletePdf(pdf)) {
                deletedCount++
            }
        }
        return deletedCount
    }

    /**
     * Đổi tên file PDF (giữ nguyên thư mục cha)
     */
    fun renamePdf(pdf: ScannedPdf, newName: String): Boolean {
        return try {
            val cleanName = newName.trim().replace('/', '-')
            val finalName = if (cleanName.endsWith(".pdf", ignoreCase = true)) cleanName else "$cleanName.pdf"
            val targetFile = File(pdf.file.parentFile ?: storageDir, finalName)

            if (targetFile.exists()) {
                false
            } else {
                pdf.file.renameTo(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Mở file PDF bằng ứng dụng đọc PDF mặc định trên máy
     */
    fun openPdf(pdf: ScannedPdf) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdf.file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Chia sẻ 1 file PDF qua Zalo, Email, Drive...
     */
    fun sharePdf(pdf: ScannedPdf) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdf.file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ tài liệu PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Chia sẻ nhiều file PDF cùng một lúc qua Zalo, Gmail, v.v.
     */
    fun shareMultiplePdfs(pdfs: List<ScannedPdf>) {
        if (pdfs.isEmpty()) return
        if (pdfs.size == 1) {
            sharePdf(pdfs.first())
            return
        }

        try {
            val uriList = ArrayList<Uri>()
            for (pdf in pdfs) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdf.file
                )
                uriList.add(uri)
            }

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/pdf"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ${pdfs.size} tài liệu PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
