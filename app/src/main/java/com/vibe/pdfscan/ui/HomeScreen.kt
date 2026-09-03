package com.vibe.pdfscan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Palette
import com.vibe.pdfscan.data.ThemeManager
import com.vibe.pdfscan.ui.components.SettingsDialog
import com.vibe.pdfscan.ui.theme.LocalAppGradient

// Caching static shapes and values to avoid runtime allocations during scrolling
private val ScanBtnShape = RoundedCornerShape(16.dp)
private val CategoryBtnShape = RoundedCornerShape(12.dp)
private val EmptyCardShape = RoundedCornerShape(16.dp)
private val CloudActiveDotColor = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pdfList: List<ScannedPdf>,
    accountManager: CloudAccountManager,
    themeManager: ThemeManager,
    onStartScan: () -> Unit,
    onOpenPdf: (ScannedPdf) -> Unit,
    onSharePdf: (ScannedPdf) -> Unit,
    onShareMultiplePdfs: (List<ScannedPdf>) -> Unit,
    onRenamePdf: (ScannedPdf, String) -> Unit,
    onDeletePdf: (ScannedPdf) -> Unit,
    onDeleteMultiplePdfs: (List<ScannedPdf>) -> Unit,
    onSyncPdfToCloud: (ScannedPdf) -> Unit,
    onConnectGoogleDrive: () -> Unit,
    onDisconnectGoogleDrive: () -> Unit,
    onPickGoogleAccountFromSystem: () -> Unit = {},
    onSyncAllPdfsNow: () -> Unit = {},
    onManualUploadToDrive: () -> Unit = {}
) {
    var pdfToRename by remember { mutableStateOf<ScannedPdf?>(null) }
    var pdfToDelete by remember { mutableStateOf<ScannedPdf?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val appGradient = LocalAppGradient.current

    // Chế độ chọn nhiều file
    var selectedPdfs by remember { mutableStateOf<Set<ScannedPdf>>(emptySet()) }
    val isSelectionMode = selectedPdfs.isNotEmpty()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Lọc theo thư mục danh mục (Tất cả, Hóa đơn, Hợp đồng, Biên bản...)
    var selectedCategory by remember { mutableStateOf(DocumentCategory.ALL) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Tìm kiếm file
    var searchQuery by remember { mutableStateOf("") }

    // Tính toán số lượng theo từng danh mục
    val categoryCounts by remember(pdfList) {
        derivedStateOf {
            DocumentCategory.values().associateWith { cat ->
                if (cat == DocumentCategory.ALL) {
                    pdfList.size
                } else {
                    pdfList.count {
                        it.category.equals(cat.displayName, ignoreCase = true) ||
                        it.category.equals(cat.folderName, ignoreCase = true)
                    }
                }
            }
        }
    }

    // Danh sách PDF sau khi lọc theo danh mục và từ khóa tìm kiếm
    val displayedList by remember(pdfList, selectedCategory, searchQuery) {
        derivedStateOf {
            val categoryFiltered = if (selectedCategory == DocumentCategory.ALL) {
                pdfList
            } else {
                pdfList.filter {
                    it.category.equals(selectedCategory.displayName, ignoreCase = true) ||
                    it.category.equals(selectedCategory.folderName, ignoreCase = true)
                }
            }
            if (searchQuery.isBlank()) {
                categoryFiltered
            } else {
                categoryFiltered.filter {
                    it.name.contains(searchQuery.trim(), ignoreCase = true)
                }
            }
        }
    }

    // Nút cuộn lên đầu trang (hiển thị khi vuốt xuống)
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    // Xử lý nút Back của Android khi đang ở chế độ chọn nhiều
    BackHandler(enabled = isSelectionMode) {
        selectedPdfs = emptySet()
    }

    Scaffold(
        topBar = {
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
                    // Nút Tải lên Google Drive thủ công (nằm bên trái icon Cài đặt)
                    IconButton(onClick = onManualUploadToDrive) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Tải lên Google Drive thủ công",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt & Giao diện",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (accountManager.isAnyCloudConnected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CloudActiveDotColor)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (isSelectionMode) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedPdfs = emptySet() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hủy", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedPdfs = if (selectedPdfs.size == displayedList.size) {
                                    emptySet()
                                } else {
                                    displayedList.toSet()
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedPdfs.size == displayedList.size) "Bỏ chọn" else "Chọn Tất cả",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onShareMultiplePdfs(selectedPdfs.toList()) },
                                enabled = selectedPdfs.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(
                                onClick = { showBatchDeleteDialog = true },
                                enabled = selectedPdfs.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val bottomMargin = (configuration.screenHeightDp * 0.05f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. CỤM PHẦN TỬ CỐ ĐỊNH PHÍA TRÊN (Nút quét tài liệu, Thanh tìm kiếm, Tên thư mục, Dropdown chọn thư mục)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                // NÚT "QUÉT TÀI LIỆU MỚI" CỐ ĐỊNH
                if (appGradient.isGradientActive) {
                    Surface(
                        onClick = onStartScan,
                        shape = ScanBtnShape,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appGradient.brush)
                                .padding(horizontal = 24.dp, vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(15.dp))
                                Text(
                                    text = stringResource(R.string.scan_button),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.5.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = ScanBtnShape,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = stringResource(R.string.scan_button),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.5.sp
                            )
                        )
                    }
                }

                // Khoảng cách phía TRÊN thanh tìm kiếm
                Spacer(modifier = Modifier.height(11.dp))

                // THANH TÌM KIẾM FILE (Kích thước nhỏ gọn dưới nút Quét)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm kiếm tài liệu...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Khoảng cách phía DƯỚI thanh tìm kiếm (bằng chính xác khoảng cách phía trên)
                Spacer(modifier = Modifier.height(11.dp))

                // TIÊU ĐỀ & MENU DROPDOWN CHỌN THƯ MỤC CỐ ĐỊNH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thư mục tài liệu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Nút Dropdown chọn thư mục
                    Box {
                        Surface(
                            onClick = { isCategoryDropdownExpanded = true },
                            shape = CategoryBtnShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedCategory.icon} ${selectedCategory.displayName} (${displayedList.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Chọn thư mục",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (isCategoryDropdownExpanded) {
                            DropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DocumentCategory.values().forEach { category ->
                                    val count = categoryCounts[category] ?: 0
                                    val isSelected = selectedCategory == category

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${category.icon}  ${category.displayName}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = "($count)",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            isCategoryDropdownExpanded = false
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Ô HIỂN THỊ CÁC FILE ĐÃ LƯU (Cố định kích thước theo màn hình, cuộn độc lập bên trong ô)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                if (displayedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pdfList.isEmpty()) {
                            EmptyScanState(onStartScan = onStartScan)
                        } else if (searchQuery.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = EmptyCardShape,
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
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Không tìm thấy tài liệu phù hợp với \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = EmptyCardShape,
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        items(
                            items = displayedList,
                            key = { it.file.absolutePath },
                            contentType = { "PDF_CARD" }
                        ) { pdf ->
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

                        item(contentType = "SPACER") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Nút cuộn lên đầu trang (Scroll to Top FAB)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showScrollToTop && !isSelectionMode,
                    enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) + scaleIn(tween(180, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(150, easing = FastOutSlowInEasing)) + scaleOut(tween(150, easing = FastOutSlowInEasing)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Cuộn lên đầu",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Khoảng cách tầm 10% chiều cao màn hình so với cạnh dưới của điện thoại
            Spacer(modifier = Modifier.height(bottomMargin))
        }
    }

    // Dialogs
    if (showSettingsDialog) {
        SettingsDialog(
            themeManager = themeManager,
            cloudAccountManager = accountManager,
            pdfList = pdfList,
            onDismiss = { showSettingsDialog = false },
            onConnectGoogleDrive = onConnectGoogleDrive,
            onDisconnectGoogleDrive = onDisconnectGoogleDrive,
            onPickGoogleAccountFromSystem = onPickGoogleAccountFromSystem,
            onSyncAllPdfsNow = onSyncAllPdfsNow
        )
    }

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
            pdfList = pdfList,
            onDismiss = { showSyncDialog = false },
            onConnectGoogleDrive = onConnectGoogleDrive,
            onDisconnectGoogleDrive = onDisconnectGoogleDrive,
            onPickGoogleAccountFromSystem = onPickGoogleAccountFromSystem,
            onSyncAllPdfsNow = onSyncAllPdfsNow
        )
    }

    pdfToRename?.let { pdf ->
        RenameDialog(
            pdf = pdf,
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
                    imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.no_documents_title),
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
