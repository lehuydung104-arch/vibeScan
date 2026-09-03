package com.vibe.pdfscan.scanner

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

object DocumentScannerHelper {

    /**
     * Khởi tạo Scanner Client:
     * - SCANNER_MODE_BASE: Tắt tính năng tự động nâng cao/lọc màu sau khi chụp,
     *   giữ nguyên ảnh gốc tự nhiên, vẫn hỗ trợ cắt góc tự động/thủ công, xoay và sắp xếp trang.
     * - Hỗ trợ nhập ảnh tài liệu từ Thư viện Gallery
     * - Xuất trực tiếp định dạng PDF và JPEG
     * - Tối đa 100 trang trong 1 lần quét
     */
    fun createScannerClient(): GmsDocumentScanner {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
            )
            .setGalleryImportAllowed(true)
            .setPageLimit(100)
            .build()

        return GmsDocumentScanning.getClient(options)
    }

    /**
     * Kích hoạt giao diện quét tài liệu của Google ML Kit
     */
    fun startScan(
        activity: Activity,
        scannerLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onError: (Exception) -> Unit
    ) {
        val scannerClient = createScannerClient()
        scannerClient.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                val request = IntentSenderRequest.Builder(intentSender).build()
                scannerLauncher.launch(request)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
