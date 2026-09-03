package com.vibe.pdfscan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.pdfscan.R
import com.vibe.pdfscan.cloud.CloudAccountManager
import com.vibe.pdfscan.data.DocumentCategory
import com.vibe.pdfscan.data.ScannedPdf
import com.vibe.pdfscan.ui.components.BatchDeleteDialog
import com.vibe.pdfscan.ui.components.DeleteDialog
import com.vibe.pdfscan.ui.components.PdfCard
import com.vibe.pdfscan.ui.components.RenameDialog
import com.vibe.pdfscan.ui.components.SyncSettingsDialog
import com.vibe.pdfscan.ui.theme.BluePrimary
import com.vibe.pdfscan.ui.theme.BlueSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pdfList: List<ScannedPdf>,
    accountManager: CloudAccountManager,
    onStartScan: () -> Unit,
    onOpenPdf: (ScannedPdf) -> Unit,
    onSharePdf: (ScannedPdf) -> Unit,
    onShareMultiplePdfs: (List<ScannedPdf>) -> Unit,
    onRenamePdf: (ScannedPdf, String) -> Unit,
    onDeletePdf: (ScannedPdf) -> Unit,
    onDeleteMultiplePdfs: (List<ScannedPdf>) -> Unit,
    onSyncPdfToCloud: (ScannedPdf) -> Unit,
    onSelectCloudFolder: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGoogleSignOutClick: () -> Unit,
    onMicrosoftSignInClick: () -> Unit,
    onMicrosoftSignOutClick: () -> Unit,
    onAutoSyncToggled: (Boolean) -> Unit
) {
    var pdfToRename by remember { mutableStateOf<ScannedPdf?>(null) }
    var pdfToDelete by remember { mutableStateOf<ScannedPdf?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    // Chế độ chọn nhiều file
    var selectedPdfs by remember { mutableStateOf<Set<ScannedPdf>>(emptySet()) }
    val isSelectionMode = selectedPdfs.isNotEmpty()

    // Lọc theo thư mục danh mục (Tất cả, Hóa đơn, Hợp đồng, Biên bản...)
    var selectedCategory by remember { mutableStateOf(DocumentCategory.ALL) }

    val displayedList = remember(pdfList, selectedCategory) {
        if (selectedCategory == DocumentCategory.ALL) {
            pdfList
        } else {
            pdfList.filter {
                it.category.equals(selectedCategory.displayName, ignoreCase = true) ||
                it.category.equals(selectedCategory.folderName, ignoreCase = true)
            }
        }
    }

    // Xử lý nút Back của Android khi đang ở chế độ chọn nhiều
    BackHandler(enabled = isSelectionMode) {
        selectedPdfs = emptySet()
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // Thanh thao tác khi đang chọn nhiều file (Selection Action Bar)
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { selectedPdfs = emptySet() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Bỏ chọn tất cả"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Đã chọn: ${selectedPdfs.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        // Nút chọn tất cả / bỏ chọn
                        TextButton(
                            onClick = {
                                selectedPdfs = if (selectedPdfs.size == displayedList.size) {
                                    emptySet()
                                } else {
                                    displayedList.toSet()
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedPdfs.size == displayedList.size) "Bỏ chọn" else "Tất cả",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Nút chia sẻ nhiều file
                        IconButton(onClick = { onShareMultiplePdfs(selectedPdfs.toList()) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Chia sẻ các file đã chọn"
                            )
                        }

                        // Nút xóa hàng loạt
                        IconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa các file đã chọn",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            } else {
                // Thanh tiêu đề chuẩn
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PDF",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSyncDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Cài đặt đồng bộ đám mây",
                                    tint = if (accountManager.isAnyCloudConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                )
                            }
                            if (accountManager.isAnyCloudConnected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (pdfList.isNotEmpty() && !isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = onStartScan,
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                    text = { Text("Quét mới") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Scan Action Card
            item {
                HeroScanCard(onStartScan = onStartScan)
            }

            // THẺ TRẠNG THÁI ĐỒNG BỘ ĐÁM MÂY (Hiển thị tài khoản Google & Thư mục tự động lưu)
            item {
                CloudSyncStatusCard(
                    accountManager = accountManager,
                    onSelectCloudFolder = onSelectCloudFolder,
                    onGoogleSignInClick = onGoogleSignInClick,
                    onGoogleSignOutClick = onGoogleSignOutClick,
                    onOpenSyncDialog = { showSyncDialog = true }
                )
            }

            // DẢI TAB THƯ MỤC PHÂN LOẠI (Tất cả, Hóa đơn, Hợp đồng, Biên bản...)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thư mục tài liệu",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = if (isSelectionMode) "Đang chọn file" else "Bấm giữ để chọn nhiều",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Thanh cuộn ngang các thư mục
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DocumentCategory.values().forEach { category ->
                            val count = if (category == DocumentCategory.ALL) {
                                pdfList.size
                            } else {
                                pdfList.count {
                                    it.category.equals(category.displayName, ignoreCase = true) ||
                                    it.category.equals(category.folderName, ignoreCase = true)
                                }
                            }

                            // Chỉ hiển thị tab Tất cả hoặc các thư mục đã có file (hoặc nhóm chính)
                            if (category == DocumentCategory.ALL || count > 0 ||
                                category == DocumentCategory.INVOICE || category == DocumentCategory.CONTRACT ||
                                category == DocumentCategory.RECORD || category == DocumentCategory.DOCUMENT) {
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text("${category.icon} ${category.displayName} ($count)")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Danh sách PDF hoặc Trạng thái trống
            if (displayedList.isEmpty()) {
                item {
                    if (pdfList.isEmpty()) {
                        EmptyScanState(onStartScan = onStartScan)
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Thư mục ${selectedCategory.displayName} chưa có tài liệu nào.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(displayedList, key = { it.file.absolutePath }) { pdf ->
                    PdfCard(
                        pdf = pdf,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedPdfs.contains(pdf),
                        onToggleSelect = { toggled ->
                            selectedPdfs = if (selectedPdfs.contains(toggled)) {
                                selectedPdfs - toggled
                            } else {
                                selectedPdfs + toggled
                            }
                        },
                        onLongPress = { target ->
                            selectedPdfs = selectedPdfs + target
                        },
                        onOpen = onOpenPdf,
                        onShare = onSharePdf,
                        onRename = { pdfToRename = it },
                        onDelete = { pdfToDelete = it },
                        onSyncToCloud = onSyncPdfToCloud
                    )
                }
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialogs
    if (showBatchDeleteDialog) {
        BatchDeleteDialog(
            count = selectedPdfs.size,
            onDismiss = { showBatchDeleteDialog = false },
            onConfirm = {
                onDeleteMultiplePdfs(selectedPdfs.toList())
                selectedPdfs = emptySet()
                showBatchDeleteDialog = false
            }
        )
    }

    if (showSyncDialog) {
        SyncSettingsDialog(
            accountManager = accountManager,
            onDismiss = { showSyncDialog = false },
            onSelectCloudFolder = onSelectCloudFolder,
            onGoogleSignInClick = onGoogleSignInClick,
            onGoogleSignOutClick = onGoogleSignOutClick,
            onMicrosoftSignInClick = onMicrosoftSignInClick,
            onMicrosoftSignOutClick = onMicrosoftSignOutClick,
            onAutoSyncToggled = onAutoSyncToggled
        )
    }

    pdfToRename?.let { pdf ->
        RenameDialog(
            currentName = pdf.name,
            onDismiss = { pdfToRename = null },
            onConfirm = { newName ->
                onRenamePdf(pdf, newName)
                pdfToRename = null
            }
        )
    }

    pdfToDelete?.let { pdf ->
        DeleteDialog(
            pdf = pdf,
            onDismiss = { pdfToDelete = null },
            onConfirm = {
                onDeletePdf(pdf)
                pdfToDelete = null
            }
        )
    }
}

/**
 * Thẻ hiển thị trạng thái đồng bộ đám mây và cho phép chọn thư mục lưu trực tiếp trên màn hình chính
 */
@Composable
private fun CloudSyncStatusCard(
    accountManager: CloudAccountManager,
    onSelectCloudFolder: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGoogleSignOutClick: () -> Unit,
    onOpenSyncDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accountManager.isAnyCloudConnected) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (accountManager.isAnyCloudConnected) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header hàng trên
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (accountManager.isAnyCloudConnected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (accountManager.isAnyCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (accountManager.isAnyCloudConnected) Color(0xFF15803D) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Đồng bộ Đám mây",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (accountManager.isAnyCloudConnected) "Đang kích hoạt tự động lưu" else "Miễn phí 100% • Tự động lưu",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (accountManager.isAnyCloudConnected) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onOpenSyncDialog, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Trạng thái tài khoản và thư mục lưu
            if (accountManager.isGoogleConnected || accountManager.isMicrosoftConnected) {
                // Đã đăng nhập tài khoản
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (accountManager.isGoogleConnected) "🟢 Google Drive:" else "🔵 OneDrive:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = accountManager.googleEmail ?: accountManager.microsoftEmail ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            if (accountManager.isGoogleConnected) onGoogleSignOutClick() else onGoogleSignOutClick()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Đăng xuất", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                    }
                }

                // THƯ MỤC LƯU TRÊN GOOGLE DRIVE / ONEDRIVE
                if (accountManager.syncFolderName != null) {
                    // Đã chọn thư mục
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDF4))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Thư mục lưu tự động:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = accountManager.syncFolderName ?: "",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onSelectCloudFolder,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Đổi", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    // Chưa chọn thư mục -> Nút bấm to nổi bật nhắc chọn thư mục
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bạn chưa chọn thư mục tự động lưu!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            )
                        }

                        Text(
                            text = "Hãy chọn một thư mục trên Google Drive để tài liệu scan tự động bay vào các thư mục riêng (Hóa đơn, Hợp đồng...) ngay khi bạn bấm Lưu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )

                        Button(
                            onClick = onSelectCloudFolder,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chọn thư mục lưu trên Google Drive",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Chưa đăng nhập tài khoản
                Text(
                    text = "Kết nối Google Drive hoặc OneDrive để tự động sao lưu tài liệu an toàn và mở xem trên máy tính bất kỳ lúc nào.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onGoogleSignInClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Kết nối Google Drive",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = onSelectCloudFolder,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chọn thư mục", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroScanCard(onStartScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BluePrimary, BlueSecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GOOGLE AI SCANNER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quét tài liệu sang PDF",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Tự động phân loại thư mục • Bộ lọc nét chữ • Đồng bộ Google Drive & OneDrive",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                Button(
                    onClick = onStartScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.scan_button),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyScanState(onStartScan: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.no_documents),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.no_documents_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Button(
                onClick = onStartScan,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.scan_button))
            }
        }
    }
}
