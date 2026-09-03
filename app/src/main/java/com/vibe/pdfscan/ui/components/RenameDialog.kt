package com.vibe.pdfscan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vibe.pdfscan.R
import com.vibe.pdfscan.data.ScannedPdf

@Composable
fun RenameDialog(
    pdf: ScannedPdf,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialName = pdf.name.removeSuffix(".pdf")
    var text by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = stringResource(R.string.rename_title))
        },
        text = {
            Column {
                Text(
                    text = "Nhập tên mới cho tài liệu này:",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        isError = it.isBlank()
                    },
                    isError = isError,
                    singleLine = true,
                    label = { Text("Tên tài liệu") },
                    suffix = { Text(".pdf") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isError) {
                    Text(
                        text = "Tên tài liệu không được để trống",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
