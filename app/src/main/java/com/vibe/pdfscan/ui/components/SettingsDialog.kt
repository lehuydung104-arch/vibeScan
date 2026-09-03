package com.vibe.pdfscan.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.vibe.pdfscan.cloud.CloudAccountManager
import com.vibe.pdfscan.cloud.CloudSyncEngine
import com.vibe.pdfscan.cloud.CloudTarget
import com.vibe.pdfscan.data.ThemeManager
import com.vibe.pdfscan.ui.theme.ThemeColorPreset
import com.vibe.pdfscan.ui.theme.ThemeDisplayMode

/**
 * Các phân mục cài đặt của VibeScan
 */
enum class SettingsSection {
    MENU,               // Danh mục chọn 2 phần: Màu nền / Giao diện & Đồng bộ hóa
    THEME_APPEARANCE,   // Chi tiết cài đặt Giao diện, Màu nền & Chế độ sáng/tối
    CLOUD_SYNC          // Chi tiết cài đặt Đồng bộ hóa Đám mây & Tài khoản
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    themeManager: ThemeManager,
    cloudAccountManager: CloudAccountManager,
    onDismiss: () -> Unit,
    pdfList: List<com.vibe.pdfscan.data.ScannedPdf> = emptyList(),
    onConnectGoogleDrive: () -> Unit = {},
    onDisconnectGoogleDrive: () -> Unit = {},
    onPickGoogleAccountFromSystem: () -> Unit = {},
    onSyncAllPdfsNow: () -> Unit = {},
    initialSection: SettingsSection = SettingsSection.MENU
) {
    var currentSection by remember { mutableStateOf(initialSection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentSection != SettingsSection.MENU) {
                        IconButton(
                            onClick = { currentSection = SettingsSection.MENU },
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = when (currentSection) {
                                SettingsSection.MENU -> "Cài đặt & Tùy chỉnh"
                                SettingsSection.THEME_APPEARANCE -> "Giao diện & Màu nền"
                                SettingsSection.CLOUD_SYNC -> "Đồng bộ hóa Google Drive"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = when (currentSection) {
                                SettingsSection.MENU -> "Tùy biến màu sắc, theme và sao lưu"
                                SettingsSection.THEME_APPEARANCE -> "Chế độ hiển thị & Bảng màu chủ đạo"
                                SettingsSection.CLOUD_SYNC -> "Google Drive & Sao lưu tự động"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            AnimatedContent(
                targetState = currentSection,
                transitionSpec = {
                    if (targetState != SettingsSection.MENU && initialState == SettingsSection.MENU) {
                        (slideInHorizontally(animationSpec = tween(240)) { it } + fadeIn()).togetherWith(
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally(animationSpec = tween(240)) { -it } + fadeIn()).togetherWith(
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut()
                        )
                    }
                },
                label = "settingsSectionTransition"
            ) { section ->
                when (section) {
                    SettingsSection.MENU -> {
                        SettingsMenuContent(
                            themeManager = themeManager,
                            cloudAccountManager = cloudAccountManager,
                            onNavigateToTheme = { currentSection = SettingsSection.THEME_APPEARANCE },
                            onNavigateToCloudSync = { currentSection = SettingsSection.CLOUD_SYNC }
                        )
                    }
                    SettingsSection.THEME_APPEARANCE -> {
                        ThemeSettingsContent(themeManager = themeManager)
                    }
                    SettingsSection.CLOUD_SYNC -> {
                        CloudSyncSettingsContent(
                            cloudAccountManager = cloudAccountManager,
                            pdfList = pdfList,
                            onConnectGoogleDrive = onConnectGoogleDrive,
                            onDisconnectGoogleDrive = onDisconnectGoogleDrive,
                            onPickGoogleAccountFromSystem = onPickGoogleAccountFromSystem,
                            onSyncAllPdfsNow = onSyncAllPdfsNow
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentSection != SettingsSection.MENU) {
                    OutlinedButton(
                        onClick = { currentSection = SettingsSection.MENU },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Quay lại", fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xong", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

/**
 * Màn hình Menu chính: Chia rõ 2 mục riêng biệt:
 * 1. Cài đặt Màu nền & Giao diện
 * 2. Cài đặt Đồng bộ hóa
 */
@Composable
private fun SettingsMenuContent(
    themeManager: ThemeManager,
    cloudAccountManager: CloudAccountManager,
    onNavigateToTheme: () -> Unit,
    onNavigateToCloudSync: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // MỤC 1: CÀI ĐẶT MÀU NỀN & GIAO DIỆN
        // ==========================================
        Card(
            onClick = onNavigateToTheme,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (themeManager.selectedPreset.isGradient) {
                                    themeManager.selectedPreset.createBrush()
                                } else {
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(themeManager.selectedPreset.primaryColor, themeManager.selectedPreset.secondaryColor)
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Giao diện & Màu nền",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (themeManager.displayMode) {
                                    ThemeDisplayMode.LIGHT -> "Sáng"
                                    ThemeDisplayMode.DARK -> "Tối"
                                    ThemeDisplayMode.SYSTEM -> "Tự động"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ${if (themeManager.isDynamicColorEnabled) "Màu động Material You" else themeManager.selectedPreset.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Chế độ hiển thị, màu chủ đạo & gradient",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Chi tiết",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // ==========================================
        // MỤC 2: CÀI ĐẶT ĐỒNG BỘ HÓA ĐÁM MÂY
        // ==========================================
        Card(
            onClick = onNavigateToCloudSync,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (cloudAccountManager.isAnyCloudConnected) {
                                    Color(0xFF10B981)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Đồng bộ hóa Đám mây",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cloudAccountManager.isAnyCloudConnected) {
                                Text(
                                    text = "🟢 Đã kết nối",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            } else {
                                Text(
                                    text = "⚪ Chưa liên kết",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "Google Drive & thư mục máy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Chi tiết",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

        // Thông tin ứng dụng
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "VibeScan v1.0.0 • AI OCR Auto Scanner & Cloud Sync",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Chi tiết Cài đặt Giao diện & Màu sắc
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeSettingsContent(themeManager: ThemeManager) {
    val scrollState = rememberScrollState()
    var selectedColorCategoryTab by remember {
        mutableIntStateOf(if (themeManager.selectedPreset.isGradient) 1 else 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ==========================================
        // 1. CHẾ ĐỘ HIỂN THỊ (SÁNG / TỐI / HỆ THỐNG)
        // ==========================================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BrightnessAuto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Chế độ hiển thị",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeDisplayModeOption(
                    label = "Sáng",
                    icon = Icons.Default.LightMode,
                    isSelected = themeManager.displayMode == ThemeDisplayMode.LIGHT,
                    modifier = Modifier.weight(1f),
                    onClick = { themeManager.setDisplayMode(ThemeDisplayMode.LIGHT) }
                )
                ThemeDisplayModeOption(
                    label = "Tối",
                    icon = Icons.Default.DarkMode,
                    isSelected = themeManager.displayMode == ThemeDisplayMode.DARK,
                    modifier = Modifier.weight(1f),
                    onClick = { themeManager.setDisplayMode(ThemeDisplayMode.DARK) }
                )
                ThemeDisplayModeOption(
                    label = "Tự động",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = themeManager.displayMode == ThemeDisplayMode.SYSTEM,
                    modifier = Modifier.weight(1f),
                    onClick = { themeManager.setDisplayMode(ThemeDisplayMode.SYSTEM) }
                )
            }

            // Tùy chọn Dynamic Colors cho Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Màu động Material You",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Hòa hợp màu sắc theo hình nền máy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = themeManager.isDynamicColorEnabled,
                            onCheckedChange = { themeManager.setDynamicColor(it) },
                            thumbContent = if (themeManager.isDynamicColorEnabled) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // ==========================================
        // 2. CHỦ ĐỀ MÀU SẮC & NỀN (ĐƠN SẮC & GRADIENT)
        // ==========================================
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bảng màu giao diện",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = themeManager.selectedPreset.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Tabs: Màu Đơn Sắc vs Màu Gradient
            TabRow(
                selectedTabIndex = selectedColorCategoryTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedColorCategoryTab]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedColorCategoryTab == 0,
                    onClick = { selectedColorCategoryTab = 0 },
                    text = {
                        Text(
                            text = "🎨 Màu Đơn Sắc",
                            fontWeight = if (selectedColorCategoryTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedColorCategoryTab == 1,
                    onClick = { selectedColorCategoryTab = 1 },
                    text = {
                        Text(
                            text = "✨ Màu Gradient",
                            fontWeight = if (selectedColorCategoryTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            // Danh sách màu Đơn Sắc
            if (selectedColorCategoryTab == 0) {
                val solidPresets = ThemeColorPreset.values().filter { !it.isGradient }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4
                ) {
                    solidPresets.forEach { preset ->
                        SolidColorSwatch(
                            preset = preset,
                            isSelected = themeManager.selectedPreset == preset && !themeManager.isDynamicColorEnabled,
                            onClick = { themeManager.setPreset(preset) }
                        )
                    }
                }
            } else {
                // Danh sách màu Gradient
                val gradientPresets = ThemeColorPreset.values().filter { it.isGradient }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    gradientPresets.forEach { preset ->
                        GradientThemeCard(
                            preset = preset,
                            isSelected = themeManager.selectedPreset == preset && !themeManager.isDynamicColorEnabled,
                            onClick = { themeManager.setPreset(preset) }
                        )
                    }
                }
            }

            // Thẻ Xem trước giao diện mẫu (Live Theme Preview)
            ThemeLivePreviewCard(preset = themeManager.selectedPreset)
        }
    }
}

/**
 * Chi tiết Cài đặt Đồng bộ hóa Google Drive
 */
@Composable
private fun CloudSyncSettingsContent(
    cloudAccountManager: CloudAccountManager,
    pdfList: List<com.vibe.pdfscan.data.ScannedPdf> = emptyList(),
    onConnectGoogleDrive: () -> Unit,
    onDisconnectGoogleDrive: () -> Unit,
    onPickGoogleAccountFromSystem: () -> Unit,
    onSyncAllPdfsNow: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLoginDialog by remember { mutableStateOf(false) }
    var showFolderGuide by remember { mutableStateOf(false) }

    // Dialog đăng nhập / chọn tài khoản Google
    if (showLoginDialog) {
        LoginAccountDialog(
            currentEmail = cloudAccountManager.googleEmail,
            onDismiss = { showLoginDialog = false },
            onPickFromSystem = onPickGoogleAccountFromSystem,
            onConfirmAccount = { email ->
                cloudAccountManager.setGoogleAccount(email, email.substringBefore("@"))
            }
        )
    }

    // Dialog hướng dẫn chọn thư mục Google Drive
    if (showFolderGuide) {
        CloudFolderGuideDialog(
            onDismiss = { showFolderGuide = false },
            onOpenCloudApp = {
                CloudSyncEngine.openGoogleDriveApp(context)
            },
            onLaunchPicker = onConnectGoogleDrive
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. DỊCH VỤ GOOGLE DRIVE
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                if (cloudAccountManager.isGoogleConnected) Color(0xFF4285F4).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hàng 1: Tiêu đề dịch vụ + Huy hiệu trạng thái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google Drive",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (cloudAccountManager.isGoogleConnected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🟢 Đã liên kết",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "⚪ Chưa kết nối",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Hàng 2: Mô tả dịch vụ (chiếm trọn hàng, không bị co hẹp)
                Text(
                    text = "Dung lượng 15GB miễn phí với tài khoản Google",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Hàng 3: Khu vực Tài khoản Google
                if (cloudAccountManager.googleEmail != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tài khoản Google đã kết nối:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cloudAccountManager.googleEmail ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        OutlinedButton(
                            onClick = { showLoginDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đổi sang tài khoản Google khác", fontSize = 12.sp)
                        }
                    }

                    // Cơ chế lưu & chọn thư mục Google Drive
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4285F4).copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 Cách chọn thư mục & lưu vào Google Drive:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1967D2)
                        )
                        Text(
                            text = "Khi bạn bấm \"Đồng bộ\" trên tài liệu, ứng dụng sẽ mở hộp thoại Lưu vào Drive chính thức. Bạn có thể chọn bất kỳ thư mục nào trên Google Drive (hoặc tạo thư mục mới trên Drive).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (pdfList.isNotEmpty()) {
                                    CloudSyncEngine.uploadMultiplePdfsToGoogleDrive(context, pdfList.map { it.file })
                                } else {
                                    val samplePdf = CloudSyncEngine.getOrCreateSamplePdf(context)
                                    CloudSyncEngine.uploadSinglePdfToGoogleDrive(context, samplePdf)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pdfList.isNotEmpty()) "🚀 Tải lên Google Drive (${pdfList.size} tài liệu)" else "🚀 Thử mở hộp thoại Lưu vào Drive",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Thư mục sao lưu nội bộ trên máy
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Thư mục sao lưu cục bộ trên máy:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cloudAccountManager.googleFolderName ?: "Documents/VibeScan/Google_Drive",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        OutlinedButton(
                            onClick = { showFolderGuide = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đổi thư mục sao lưu trên máy", fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { showLoginDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng nhập / Chọn tài khoản Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Hàng 5: Nút Mở ứng dụng & Ngắt kết nối
                if (cloudAccountManager.isGoogleConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { CloudSyncEngine.openGoogleDriveApp(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mở Google Drive", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onDisconnectGoogleDrive,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Ngắt kết nối", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. Nút tải lên toàn bộ tài liệu
        // ==========================================
        if (cloudAccountManager.isAnyCloudConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tải lên toàn bộ tài liệu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tải toàn bộ tài liệu PDF đã quét lên Google Drive của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val unsyncedCount = pdfList.count { !it.isSynced }
                    Button(
                        onClick = onSyncAllPdfsNow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (unsyncedCount > 0) "Tải lên $unsyncedCount tài liệu mới lên Google Drive" else "Tất cả tài liệu đã được tải lên Google Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tùy chọn Chế độ hiển thị (Sáng / Tối / Tự động)
 */
@Composable
private fun ThemeDisplayModeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        label = "modeBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        label = "modeBg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Swatch tròn chọn Màu Đơn Sắc
 */
@Composable
private fun SolidColorSwatch(
    preset: ThemeColorPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(preset.primaryColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Đang chọn",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = preset.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Thẻ chọn Theme Gradient
 */
@Composable
private fun GradientThemeCard(
    preset: ThemeColorPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val brush = remember(preset) { preset.createBrush() }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gradient preview bar
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = preset.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Hiệu ứng chuyển màu mượt mà",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Thẻ xem trước trực quan theme (Mini Live Preview)
 */
@Composable
private fun ThemeLivePreviewCard(preset: ThemeColorPreset) {
    val isGradient = preset.isGradient
    val brush = remember(preset) { preset.createBrush() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Xem trước giao diện:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Preview Demo Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isGradient) {
                            Modifier.background(brush)
                        } else {
                            Modifier.background(preset.primaryColor)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quét tài liệu mới",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Preview Chip and Card line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(preset.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Màu Container",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = preset.primaryColor
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nền Thẻ Surface",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
