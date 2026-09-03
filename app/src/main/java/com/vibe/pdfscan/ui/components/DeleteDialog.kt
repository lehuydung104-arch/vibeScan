package com.vibe.pdfscan.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vibe.pdfscan.R
import com.vibe.pdfscan.data.ScannedPdf

@Composable
fun DeleteDialog(
    pdf: ScannedPdf,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = stringResource(R.string.delete_confirm_title))
        },
        text = {
            Text(text = "Bạn có chắc chắn muốn xóa file \"${pdf.name}\" không? Tài liệu sẽ bị xóa hoàn toàn khỏi bộ nhớ máy.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
