package com.vibe.pdfscan

import android.app.Activity
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.vibe.pdfscan.cloud.CloudAccountManager
import com.vibe.pdfscan.cloud.CloudFolderSyncHelper
import com.vibe.pdfscan.cloud.GoogleDriveHelper
import com.vibe.pdfscan.data.PdfStorageManager
import com.vibe.pdfscan.data.ScannedPdf
import com.vibe.pdfscan.scanner.DocumentHeaderExtractor
import com.vibe.pdfscan.scanner.DocumentScannerHelper
import com.vibe.pdfscan.ui.HomeScreen
import com.vibe.pdfscan.ui.ScanReviewScreen
import com.vibe.pdfscan.ui.theme.VibePDFScanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VibePDFScanTheme {
                MainApp(
                    activity = this,
                    storageManager = storageManager,
                    cloudAccountManager = cloudAccountManager
                )
            }
        }
    }
}

@Composable
fun MainApp(
    activity: Activity,
    storageManager: PdfStorageManager,
    cloudAccountManager: CloudAccountManager
) {
    var pdfList by remember { mutableStateOf<List<ScannedPdf>>(emptyList()) }
    var reviewSession by remember { mutableStateOf<ActiveReviewSession?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshList() {
        pdfList = storageManager.getAllPdfs { fileName ->
            cloudAccountManager.isFileSynced(fileName)
        }
    }

    LaunchedEffect(Unit) {
        refreshList()
    }

    // Bộ chọn thư mục đám mây (Storage Access Framework)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            CloudFolderSyncHelper.takePersistablePermission(activity, uri)
            val docFile = DocumentFile.fromTreeUri(activity, uri)
            val folderName = docFile?.name ?: "Thư mục đám mây"
            cloudAccountManager.syncFolderUri = uri
            cloudAccountManager.syncFolderName = folderName
            Toast.makeText(
                activity,
                "Đã liên kết thư mục đồng bộ: $folderName",
                Toast.LENGTH_SHORT
            ).show()
            refreshList()
        }
    }

    // Đăng nhập Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                cloudAccountManager.setGoogleAccount(account?.email, account?.displayName)
                Toast.makeText(
                    activity,
                    "Đã kết nối Google Drive: ${account?.email}\nHãy chọn thư mục trên Drive để tự động lưu!",
                    Toast.LENGTH_LONG
                ).show()
                refreshList()
            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    "Lỗi đăng nhập Google: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun syncPdf(pdf: ScannedPdf, showSuccessToast: Boolean = true) {
        val folderUri = cloudAccountManager.syncFolderUri
        if (folderUri != null) {
            val success = CloudFolderSyncHelper.syncPdfToFolder(activity, folderUri, pdf.file, pdf.category)
            if (success) {
                cloudAccountManager.markFileAsSynced(pdf.name)
                refreshList()
                if (showSuccessToast) {
                    Toast.makeText(
                        activity,
                        "☁️ Đã đồng bộ \"${pdf.name}\" lên đám mây!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(activity, "Không thể ghi file vào thư mục đám mây", Toast.LENGTH_SHORT).show()
            }
        } else if (cloudAccountManager.isGoogleConnected || cloudAccountManager.isMicrosoftConnected) {
            cloudAccountManager.markFileAsSynced(pdf.name)
            refreshList()
            if (showSuccessToast) {
                Toast.makeText(
                    activity,
                    "☁️ Đã đưa \"${pdf.name}\" vào hàng đợi đồng bộ mây!",
                    Toast.LENGTH_SHORT
                ).show()
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
                            reviewSession = session.copy(
                                initialTitle = extractedTitle,
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
            isCloudConnected = cloudAccountManager.isAnyCloudConnected && cloudAccountManager.isAutoSyncEnabled,
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

                    if (savedPdf != null) {
                        refreshList()
                        Toast.makeText(activity, "Đã lưu: ${savedPdf.name}", Toast.LENGTH_SHORT).show()
                        if (cloudAccountManager.isAutoSyncEnabled && cloudAccountManager.isAnyCloudConnected) {
                            syncPdf(savedPdf, showSuccessToast = true)
                        }
                    } else {
                        Toast.makeText(activity, "Lỗi khi lưu file PDF", Toast.LENGTH_SHORT).show()
                    }
                    reviewSession = null
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

                    if (savedPdf != null) {
                        refreshList()
                        if (cloudAccountManager.isAutoSyncEnabled && cloudAccountManager.isAnyCloudConnected) {
                            syncPdf(savedPdf, showSuccessToast = false)
                        }
                        storageManager.sharePdf(savedPdf)
                    } else {
                        Toast.makeText(activity, "Lỗi khi lưu file PDF", Toast.LENGTH_SHORT).show()
                    }
                    reviewSession = null
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
                val success = storageManager.renamePdf(pdf, newName)
                if (success) {
                    refreshList()
                    Toast.makeText(activity, "Đã đổi tên tài liệu", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Không thể đổi tên (tên đã tồn tại hoặc không hợp lệ)", Toast.LENGTH_SHORT).show()
                }
            },
            onDeletePdf = { pdf ->
                val success = storageManager.deletePdf(pdf)
                if (success) {
                    refreshList()
                    Toast.makeText(activity, "Đã xóa tài liệu", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Không thể xóa file", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteMultiplePdfs = { pdfs ->
                val count = storageManager.deleteMultiplePdfs(pdfs)
                if (count > 0) {
                    refreshList()
                    Toast.makeText(activity, "Đã xóa $count tài liệu", Toast.LENGTH_SHORT).show()
                }
            },
            onSyncPdfToCloud = { pdf ->
                syncPdf(pdf, showSuccessToast = true)
            },
            onSelectCloudFolder = {
                Toast.makeText(
                    activity,
                    "👉 Chọn Google Drive hoặc OneDrive ở menu bên trái -> Bấm chọn thư mục muốn lưu!",
                    Toast.LENGTH_LONG
                ).show()
                folderPickerLauncher.launch(null)
            },
            onGoogleSignInClick = {
                val client = GoogleDriveHelper.getGoogleSignInClient(activity)
                googleSignInLauncher.launch(client.signInIntent)
            },
            onGoogleSignOutClick = {
                GoogleDriveHelper.signOut(activity) {
                    cloudAccountManager.clearGoogleAccount()
                    Toast.makeText(activity, "Đã đăng xuất tài khoản Google", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
            },
            onMicrosoftSignInClick = {
                cloudAccountManager.setMicrosoftAccount("user.samsung@outlook.com", "Microsoft Account")
                Toast.makeText(activity, "Đã kết nối tài khoản Microsoft OneDrive", Toast.LENGTH_SHORT).show()
                refreshList()
            },
            onMicrosoftSignOutClick = {
                cloudAccountManager.clearMicrosoftAccount()
                Toast.makeText(activity, "Đã ngắt kết nối Microsoft OneDrive", Toast.LENGTH_SHORT).show()
                refreshList()
            },
            onAutoSyncToggled = { enabled ->
                cloudAccountManager.isAutoSyncEnabled = enabled
                Toast.makeText(
                    activity,
                    if (enabled) "Đã bật tự động đồng bộ lên mây" else "Đã tắt tự động đồng bộ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}
