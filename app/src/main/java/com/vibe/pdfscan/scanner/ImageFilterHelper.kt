package com.vibe.pdfscan.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

enum class DocFilterMode(val label: String, val iconText: String) {
    ORIGINAL("Gốc", "📷"),
    AUTO_ENHANCE("Nâng cao", "✨"),
    BLACK_WHITE("Trắng đen", "📄"),
    GRAYSCALE("Thang xám", "🌫️")
}

object ImageFilterHelper {

    /**
     * Tạo ColorMatrix tương ứng với bộ lọc đã chọn
     */
    fun getColorMatrix(filterMode: DocFilterMode): ColorMatrix? {
        return when (filterMode) {
            DocFilterMode.ORIGINAL -> null
            DocFilterMode.GRAYSCALE -> ColorMatrix().apply {
                setSaturation(0f)
            }
            DocFilterMode.AUTO_ENHANCE -> ColorMatrix().apply {
                // Tăng độ tương phản + độ sáng để làm trắng nền giấy và đậm chữ
                val contrast = 1.35f
                val brightness = 25f
                set(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                )
            }
            DocFilterMode.BLACK_WHITE -> ColorMatrix().apply {
                // Chuyển trắng đen tương phản cao (photocopy)
                val m = floatArrayOf(
                    1.8f, 1.8f, 1.8f, 0f, -220f,
                    1.8f, 1.8f, 1.8f, 0f, -220f,
                    1.8f, 1.8f, 1.8f, 0f, -220f,
                    0f, 0f, 0f, 1f, 0f,
                )
                set(m)
            }
        }
    }

    /**
     * Áp dụng bộ lọc và góc xoay vào một Bitmap
     */
    fun processBitmap(
        source: Bitmap,
        filterMode: DocFilterMode,
        rotationDegrees: Float,
    ): Bitmap {
        var result = source

        // Xoay ảnh nếu có
        if ((rotationDegrees % 360f) != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(result, 0, 0, result.width, result.height, matrix, true)
            result = rotated
        }

        // Áp dụng bộ lọc
        val colorMatrix = getColorMatrix(filterMode) ?: return result

        val filtered = createBitmap(result.width, result.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(filtered)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(result, 0f, 0f, paint)

        return filtered
    }

    /**
     * Xuất danh sách trang ảnh thành file PDF chất lượng cao theo đúng bộ lọc và góc xoay
     */
    fun exportToPdf(
        context: Context,
        pageUris: List<Uri>,
        filterMode: DocFilterMode,
        rotationDegrees: Float,
        targetFile: File,
    ): Boolean {
        if (pageUris.isEmpty()) return false

        val pdfDocument = PdfDocument()

        try {
            for ((index, uri) in pageUris.withIndex()) {
                val inputStream = context.contentResolver.openInputStream(uri) ?: continue
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) continue

                // Xử lý bộ lọc và xoay
                val processedBitmap = processBitmap(originalBitmap, filterMode, rotationDegrees)

                // Tạo trang PDF kích thước chuẩn bằng kích thước ảnh
                val pageInfo = PdfDocument.PageInfo.Builder(
                    processedBitmap.width,
                    processedBitmap.height,
                    index + 1,
                ).create()

                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                canvas.drawBitmap(processedBitmap, 0f, 0f, paint)
                pdfDocument.finishPage(page)

                if (processedBitmap != originalBitmap) {
                    processedBitmap.recycle()
                }
                originalBitmap.recycle()
            }

            FileOutputStream(targetFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            return targetFile.exists() && (targetFile.length() > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            pdfDocument.close()
        }
    }
}
