package com.vibe.pdfscan

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.vibe.pdfscan.cloud.CloudAccountManager
import com.vibe.pdfscan.cloud.CloudSyncEngine
import com.vibe.pdfscan.cloud.CloudTarget
import com.vibe.pdfscan.data.PdfStorageManager
import com.vibe.pdfscan.data.ScannedPdf
import com.vibe.pdfscan.data.ThemeManager
import com.vibe.pdfscan.scanner.DocumentHeaderExtractor
import com.vibe.pdfscan.scanner.DocumentScannerHelper
import com.vibe.pdfscan.ui.HomeScreen
import com.vibe.pdfscan.ui.ScanReviewScreen
import com.vibe.pdfscan.ui.theme.VibePDFScanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveReviewSession(
    val pdfUri: Uri,
    val pageUris: List<Uri>,
    var initialTitle: String,
    var isTitleExtractedByAi: Boolean = false
)

class MainActivity : ComponentActivity() {

    private val storageManager by lazy { PdfStorageManager(this) }
    private val cloudAccountManager by lazy { CloudAccountManager(this) }
    private val themeManager by lazy { ThemeManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VibePDFScanTheme(themeManager = themeManager) {
                MainApp(
                    activity = this,
                    storageManager = storageManager,
                    cloudAccountManager = cloudAccountManager,
                    themeManager = themeManager
                )
            }
        }
    }
}

@Composable
fun MainApp(
    activity: Activity,
    storageManager: PdfStorageManager,
    cloudAccountManager: CloudAccountManager,
    themeManager: ThemeManager
) {
    var pdfList by remember { mutableStateOf<List<ScannedPdf>>(emptyList()) }
    var reviewSession by remember { mutableStateOf<ActiveReviewSession?>(null) }
    var pendingCloudTarget by remember { mutableStateOf<CloudTarget?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshList() {
        coroutineScope.launch {
            val list = withContext(Dispatchers.IO) {
                storageManager.getAllPdfs { fileName ->
                    cloudAccountManager.isFileSynced(fileName) ||
                    File(CloudSyncEngine.getDefaultGoogleSyncFolder(), fileName).exists()
                }
            }
            pdfList = list
        }
    }

    LaunchedEffect(Unit) {
        refreshList()
    }

    // Bộ chọn tài khoản hệ thống Google
    val accountChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                cloudAccountManager.setGoogleAccount(accountName, accountName.substringBefore("@"))
                Toast.makeText(activity, "Đã chọn tài khoản Google: $accountName", Toast.LENGTH_SHORT).show()
                refreshList()
            }
        }
    }

    // Bộ chọn thư mục đám mây (Google Drive / Thư mục máy) thông qua Storage Access Framework
    val cloudFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            try {
                // Xin quyền đọc/ghi vĩnh viễn (Persistable Permission)
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                activity.contentResolver.takePersistableUriPermission(treeUri, takeFlags)

                val folderDoc = DocumentFile.fromTreeUri(activity, treeUri)
                val folderName = folderDoc?.name ?: "Thư mục đám mây"

                val authority = treeUri.authority ?: ""
                val isDriveAuthority = authority.contains("docs.storage") || authority.contains("google")

                when (pendingCloudTarget) {
                    CloudTarget.GOOGLE_DRIVE -> {
                        cloudAccountManager.setGoogleSyncFolder(treeUri, folderName)
                        if (isDriveAuthority) {
                            Toast.makeText(
                                activity,
                                "☁️ Đã liên kết thư mục Google Drive: $folderName",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                activity,
                                "📁 Đã liên kết: $folderName. (Mẹo: Để lưu trực tiếp lên Google Drive, hãy mở menu ☰ góc trái khi chọn thư mục)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    CloudTarget.GENERAL, null -> {
                        cloudAccountManager.setGeneralSyncFolder(treeUri, folderName)
                        Toast.makeText(
                            activity,
                            "☁️ Đã chọn thư mục sao lưu: $folderName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                refreshList()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    activity,
                    "Không thể lưu quyền thư mục: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Đồng bộ 1 file PDF thực tế lên Google Drive
    fun syncPdf(
        pdf: ScannedPdf,
        showSuccessToast: Boolean = true,
        openInteractiveChooser: Boolean = true
    ) {
        if (!cloudAccountManager.isGoogleConnected) {
            if (showSuccessToast) {
                Toast.makeText(
                    activity,
                    "Vui lòng kết nối tài khoản Google Drive trong Cài đặt",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        coroutineScope.launch {
            val result = CloudSyncEngine.syncSinglePdf(activity, pdf.file, cloudAccountManager)
            if (result.success) {
                refreshList()
                if (showSuccessToast) {
                    Toast.makeText(
                        activity,
                        "☁️ ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (openInteractiveChooser) {
                CloudSyncEngine.uploadSinglePdfToGoogleDrive(activity, pdf.file)
            }
        }
    }

    // Tải tất cả PDF lên Google Drive thủ công (Chỉ tải các file chưa từng được tải lên)
    fun syncAllPdfsNow() {
        if (!cloudAccountManager.isGoogleConnected) {
            Toast.makeText(
                activity,
                "Vui lòng kết nối Google Drive trong Cài đặt trước khi tải lên",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (pdfList.isEmpty()) {
            Toast.makeText(activity, "Chưa có tài liệu nào để tải lên", Toast.LENGTH_SHORT).show()
            return
        }

        // Lọc hoàn toàn cục bộ trên máy: Chỉ lấy các file chưa được đánh dấu là đã tải lên (isSynced == false)
        val unsyncedPdfs = pdfList.filter { !it.isSynced }
        if (unsyncedPdfs.isEmpty()) {
            Toast.makeText(
                activity,
                "Tất cả tài liệu đã được tải lên Google Drive trước đó",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        coroutineScope.launch {
            val filesToSync = unsyncedPdfs.map { it.file }
            Toast.makeText(
                activity,
                "🚀 Đang tải ${filesToSync.size} tài liệu mới lên Google Drive...",
                Toast.LENGTH_SHORT
            ).show()
            val result = CloudSyncEngine.syncAllPdfs(activity, filesToSync, cloudAccountManager)
            refreshList()
            Toast.makeText(
                activity,
                if (result.success) "☁️ ${result.message}" else "⚠️ ${result.message}",
                Toast.LENGTH_SHORT
            ).show()

            if (filesToSync.isNotEmpty()) {
                CloudSyncEngine.uploadMultiplePdfsToGoogleDrive(activity, filesToSync)
            }
        }
    }

    // Đăng ký nhận kết quả từ Google ML Kit Document Scanner
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdfUri = scanResult?.pdf?.uri
            val pageUris = scanResult?.pages?.mapNotNull { it.imageUri } ?: emptyList()

            if (pdfUri != null) {
                val clockDate = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
                val fallbackTitle = "Scan_$clockDate"

                // Mở ngay màn hình tùy chọn sau khi quét
                val session = ActiveReviewSession(
                    pdfUri = pdfUri,
                    pageUris = pageUris,
                    initialTitle = fallbackTitle,
                    isTitleExtractedByAi = false
                )
                reviewSession = session

                // Chạy AI OCR nhận diện tiêu đề từ trang đầu tiên trong nền
                val firstPageUri = pageUris.firstOrNull()
                if (firstPageUri != null) {
                    coroutineScope.launch {
                        val extractedTitle = DocumentHeaderExtractor.extractTitleFromImage(activity, firstPageUri)
                        if (!extractedTitle.isNullOrBlank()) {
                            val boundedTitle = if (extractedTitle.length > 26) extractedTitle.substring(0, 26).trim() else extractedTitle
                            reviewSession = session.copy(
                                initialTitle = boundedTitle,
                                isTitleExtractedByAi = true
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(activity, "Không tìm thấy dữ liệu PDF từ kết quả quét", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Luồng tinh giản 2 lần bấm:
    // Lần 1: Bấm Quét trên HomeScreen -> Camera chụp
    // Lần 2: Màn hình tùy chọn (nhập tên + chọn bộ lọc) -> Bấm LƯU là xong!
    val currentSession = reviewSession
    if (currentSession != null) {
        ScanReviewScreen(
            pageUris = currentSession.pageUris,
            initialTitle = currentSession.initialTitle,
            isTitleExtractedByAi = currentSession.isTitleExtractedByAi,
            onGetSuggestedName = { baseName -> storageManager.getSuggestedName(baseName) },
            onSave = { customName, filterMode, rotationDegrees ->
                coroutineScope.launch {
                    val savedPdf = withContext(Dispatchers.IO) {
                        storageManager.saveFilteredPdf(
                            context = activity,
                            fallbackPdfUri = currentSession.pdfUri,
                            pageUris = currentSession.pageUris,
                            customName = customName,
                            filterMode = filterMode,
                            rotationDegrees = rotationDegrees
                        )
                    }

                    reviewSession = null

                    if (savedPdf != null) {
                        refreshList()
                        Toast.makeText(activity, "Đã lưu: ${savedPdf.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Lỗi khi lưu file PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSaveAndShare = { customName, filterMode, rotationDegrees ->
                coroutineScope.launch {
                    val savedPdf = withContext(Dispatchers.IO) {
                        storageManager.saveFilteredPdf(
                            context = activity,
                            fallbackPdfUri = currentSession.pdfUri,
                            pageUris = currentSession.pageUris,
                            customName = customName,
                            filterMode = filterMode,
                            rotationDegrees = rotationDegrees
                        )
                    }

                    reviewSession = null

                    if (savedPdf != null) {
                        refreshList()
                        storageManager.sharePdf(savedPdf)
                    } else {
                        Toast.makeText(activity, "Lỗi khi lưu file PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onCancel = {
                reviewSession = null
            }
        )
    } else {
        HomeScreen(
            pdfList = pdfList,
            accountManager = cloudAccountManager,
            themeManager = themeManager,
            onStartScan = {
                DocumentScannerHelper.startScan(
                    activity = activity,
                    scannerLauncher = scannerLauncher,
                    onError = { exception ->
                        Toast.makeText(
                            activity,
                            "Không thể khởi động camera scan: ${exception.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },
            onOpenPdf = { pdf ->
                storageManager.openPdf(pdf)
            },
            onSharePdf = { pdf ->
                storageManager.sharePdf(pdf)
            },
            onShareMultiplePdfs = { pdfs ->
                storageManager.shareMultiplePdfs(pdfs)
            },
            onRenamePdf = { pdf, newName ->
                val oldName = pdf.name
                val success = storageManager.renamePdf(pdf, newName)
                if (success) {
                    cloudAccountManager.renameSyncedFile(oldName, newName)
                    refreshList()
                    Toast.makeText(activity, "Đã đổi tên tài liệu", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Không thể đổi tên (tên đã tồn tại hoặc không hợp lệ)", Toast.LENGTH_SHORT).show()
                }
            },
            onDeletePdf = { pdf ->
                val name = pdf.name
                val success = storageManager.deletePdf(pdf)
                if (success) {
                    cloudAccountManager.markFileAsUnsynced(name)
                    refreshList()
                    Toast.makeText(activity, "Đã xóa tài liệu", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Không thể xóa file", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteMultiplePdfs = { pdfs ->
                pdfs.forEach { cloudAccountManager.markFileAsUnsynced(it.name) }
                val count = storageManager.deleteMultiplePdfs(pdfs)
                if (count > 0) {
                    refreshList()
                    Toast.makeText(activity, "Đã xóa $count tài liệu", Toast.LENGTH_SHORT).show()
                }
            },
            onSyncPdfToCloud = { pdf ->
                syncPdf(pdf, showSuccessToast = false, openInteractiveChooser = true)
            },
            onConnectGoogleDrive = {
                pendingCloudTarget = CloudTarget.GOOGLE_DRIVE
                Toast.makeText(activity, "Hãy chọn hoặc tạo thư mục trong Google Drive của bạn", Toast.LENGTH_LONG).show()
                cloudFolderPickerLauncher.launch(null)
            },
            onDisconnectGoogleDrive = {
                cloudAccountManager.clearGoogleAccount()
                Toast.makeText(activity, "Đã ngắt kết nối Google Drive", Toast.LENGTH_SHORT).show()
                refreshList()
            },
            onPickGoogleAccountFromSystem = {
                pendingCloudTarget = CloudTarget.GOOGLE_DRIVE
                try {
                    @Suppress("DEPRECATION")
                    val intent = AccountManager.newChooseAccountIntent(
                        null,
                        null,
                        arrayOf("com.google"),
                        false,
                        null,
                        null,
                        null,
                        null
                    )
                    accountChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(activity, "Vui lòng nhập email trực tiếp trong hộp thoại", Toast.LENGTH_SHORT).show()
                }
            },
            onSyncAllPdfsNow = {
                syncAllPdfsNow()
            },
            onManualUploadToDrive = {
                syncAllPdfsNow()
            }
        )
    }
}
