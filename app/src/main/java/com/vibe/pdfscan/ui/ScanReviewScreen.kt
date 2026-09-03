package com.vibe.pdfscan.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vibe.pdfscan.scanner.DocFilterMode
import com.vibe.pdfscan.scanner.ImageFilterHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReviewScreen(
    pageUris: List<Uri>,
    initialTitle: String,
    isTitleExtractedByAi: Boolean,
    isCloudConnected: Boolean,
    onGetSuggestedName: (String) -> String,
    onSave: (String, DocFilterMode, Float) -> Unit,
    onSaveAndShare: (String, DocFilterMode, Float) -> Unit,
    onCancel: () -> Unit,
) {
    // Định dạng ngày_tháng_năm lấy từ đồng hồ trên máy (ví dụ: Scan_03_09_2026)
    val systemClockDate = remember {
        SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
    }
    val defaultFormattedName = remember(systemClockDate) {
        onGetSuggestedName("Scan_$systemClockDate")
    }

    var documentName by remember {
        mutableStateOf(if (isTitleExtractedByAi && initialTitle.isNotBlank()) onGetSuggestedName(initialTitle) else defaultFormattedName)
    }
    var selectedFilter by remember { mutableStateOf(DocFilterMode.ORIGINAL) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(initialTitle) {
        if (isTitleExtractedByAi && initialTitle.isNotBlank()) {
            documentName = onGetSuggestedName(initialTitle)
        }
    }

    val categoryTags = listOf(
        "🧾 Hóa đơn" to "HoaDon",
        "📑 Hợp đồng" to "HopDong",
        "🪪 Giấy tờ" to "GiayTo",
        "📄 Tài liệu" to "TaiLieu",
        "📋 Biên bản" to "BienBan",
        "📨 Công văn" to "CongVan",
        "📚 Sách vở" to "Sach",
        "🏷️ Ghi chú" to "GhiChu",
    )

    // Tạo ColorFilter thời gian thực cho bản xem trước
    val composeColorFilter = remember(selectedFilter) {
        ImageFilterHelper.getColorMatrix(selectedFilter)?.let { androidMatrix ->
            ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(androidMatrix.array))
        }
    }

    Scaffold(
        topBar = {
            // Thanh tiêu đề phía trên: Nút quay lại & Tiêu đề màn hình
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Xem trước bản scan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "${pageUris.size} trang",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            // PHẦN NHẬP TÊN Ở DƯỚI (theo đúng yêu cầu đặt xuống dưới thay vì trên cùng)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 1. Thanh công cụ bộ lọc (Gốc, Nâng cao, Trắng đen...) & Xoay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DocFilterMode.entries.forEach { mode ->
                            FilterChip(
                                selected = selectedFilter == mode,
                                onClick = { selectedFilter = mode },
                                label = { Text("${mode.iconText} ${mode.label}") },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }

                        // Nút Xoay 90 độ
                        OutlinedButton(
                            onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                contentDescription = "Xoay",
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xoay", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // 2. Ô nhập tên bản scan (Đặt ở dưới, chỉ hiện bàn phím khi bấm vào)
                    OutlinedTextField(
                        value = documentName,
                        onValueChange = { documentName = it },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tên bản scan")
                                if (isTitleExtractedByAi) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "• AI nhận diện",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        },
                        placeholder = { Text(defaultFormattedName) },
                        singleLine = true,
                        suffix = {
                            Text(
                                text = ".pdf",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isTitleExtractedByAi) Icons.Default.AutoAwesome else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isTitleExtractedByAi) Color(0xFFEAB308) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            if (documentName.isNotEmpty()) {
                                IconButton(onClick = { documentName = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa chữ",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // 3. Dải nhãn tùy chọn nhanh 1 chạm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        categoryTags.forEach { (label, prefix) ->
                            FilterChip(
                                selected = documentName.startsWith(prefix, ignoreCase = true) || documentName.contains(label.substring(2).trim(), ignoreCase = true),
                                onClick = {
                                    documentName = onGetSuggestedName("${prefix}_$systemClockDate")
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }

                    // 4. Thông báo đồng bộ mây nếu có
                    if (isCloudConnected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tự động đồng bộ lên Google Drive / OneDrive khi Lưu",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D),
                                ),
                            )
                        }
                    }

                    // 5. Cụm nút Lưu tài liệu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                val finalName = documentName.ifBlank { defaultFormattedName }
                                onSave(finalName, selectedFilter, rotationDegrees)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LƯU",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val finalName = documentName.ifBlank { defaultFormattedName }
                                onSaveAndShare(finalName, selectedFilter, rotationDegrees)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chia sẻ", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        // KHU VỰC XEM TRƯỚC TÀI LIỆU Ở GIỮA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            if (pageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(pageUris) { index, uri ->
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(0.70f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Trang ${index + 1}",
                                    colorFilter = composeColorFilter,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .rotate(rotationDegrees)
                                        .background(Color.Black.copy(alpha = 0.05f)),
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "Trang ${index + 1} / ${pageUris.size} • ${selectedFilter.label}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bản scan sẵn sàng",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }
        }
    }
}
