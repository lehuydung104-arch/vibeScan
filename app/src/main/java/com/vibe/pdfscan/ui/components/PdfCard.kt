package com.vibe.pdfscan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.pdfscan.data.ScannedPdf
import com.vibe.pdfscan.ui.theme.PdfRed
import com.vibe.pdfscan.ui.theme.PdfRedLight

// Caching static shapes to avoid object recreation on every frame during fast scrolling
private val CardShape = RoundedCornerShape(16.dp)
private val IconBadgeShape = RoundedCornerShape(12.dp)
private val CategoryBadgeShape = RoundedCornerShape(6.dp)
private val CloudUploadIconColor = Color(0xFF10B981)
private val CloudSyncedCheckColor = Color(0xFF10B981)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfCard(
    pdf: ScannedPdf,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (ScannedPdf) -> Unit = {},
    onLongPress: (ScannedPdf) -> Unit = {},
    onOpen: (ScannedPdf) -> Unit,
    onShare: (ScannedPdf) -> Unit,
    onRename: (ScannedPdf) -> Unit,
    onDelete: (ScannedPdf) -> Unit,
    onSyncToCloud: (ScannedPdf) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect(pdf)
                    } else {
                        onOpen(pdf)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onLongPress(pdf)
                    } else {
                        onToggleSelect(pdf)
                    }
                }
            ),
        shape = CardShape,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon tài liệu / Đổi thành dấu tích khi file được chọn
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(IconBadgeShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else PdfRedLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Đã chọn",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF File",
                        tint = PdfRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(15.dp))

            // File Info (Title + Dấu tích xanh đồng bộ + Metadata + Category Badge)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pdf.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Dấu tích xanh nhỏ gọn gàng đặt sau tên file khi đã đồng bộ
                    if (pdf.isSynced) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Đã đồng bộ lên đám mây",
                            tint = CloudSyncedCheckColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Badge (Ví dụ: Hóa đơn, Hợp đồng, Biên bản...)
                    Box(
                        modifier = Modifier
                            .clip(CategoryBadgeShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pdf.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "• ${pdf.formattedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "• ${pdf.formattedSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Menu 3 chấm (chỉ hiển thị khi KHÔNG ở chế độ chọn nhiều)
            if (!isSelectionMode) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tùy chọn",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showMenu) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Đồng bộ lên mây") },
                                leadingIcon = {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CloudUploadIconColor)
                                },
                                onClick = {
                                    showMenu = false
                                    onSyncToCloud(pdf)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Chia sẻ") },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onShare(pdf)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Đổi tên") },
                                leadingIcon = {
                                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onRename(pdf)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete(pdf)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
