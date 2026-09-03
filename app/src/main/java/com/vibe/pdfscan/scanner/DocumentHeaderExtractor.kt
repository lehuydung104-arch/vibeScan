package com.vibe.pdfscan.scanner

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

object DocumentHeaderExtractor {

    // Danh sách từ khóa nhận diện các loại tiêu đề văn bản phổ biến
    private val TITLE_PATTERNS = listOf(
        Pattern.compile(".*(hợp\\s*đồng|hop\\s*dong).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(biên\\s*bản|bien\\s*ban).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(công\\s*văn|cong\\s*van).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(biên\\s*lai|bien\\s*lai).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(hóa\\s*đơn|hoa\\s*don).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(phân\\s*công|phan\\s*cong).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(quyết\\s*định|quyet\\s*dinh).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(thông\\s*báo|thong\\s*bao).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(giấy\\s*chứng\\s*nhận|giay\\s*chung\\s*nhan|giấy\\s*xác\\s*nhận|giấy\\s*đề\\s*nghị).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(tờ\\s*trình|to\\s*trinh).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(phiếu\\s*thu|phiếu\\s*chi|phiếu\\s*nhập|phiếu\\s*xuất).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE),
        Pattern.compile(".*(báo\\s*cáo|bao\\s*cao).*", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
    )

    /**
     * Tự động quét trang đầu tiên của tài liệu để trích xuất tiêu đề bằng Google ML Kit
     */
    suspend fun extractTitleFromImage(context: Context, imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val detectedTitle = findDocumentTitle(visionText.text)
                        continuation.resume(detectedTitle)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }

    private fun findDocumentTitle(fullText: String): String? {
        if (fullText.isBlank()) return null

        val lines = fullText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 1. Quét tìm các dòng chứa từ khóa tiêu đề (Hợp đồng, Biên bản, Công văn, Hóa đơn...)
        for (i in lines.indices) {
            val line = lines[i]
            for (pattern in TITLE_PATTERNS) {
                if (pattern.matcher(line).matches()) {
                    var titleCandidate = line

                    // Nếu dòng tiêu đề ngắn (ví dụ chỉ có chữ "HỢP ĐỒNG"), lấy thêm dòng phụ bên dưới
                    if (titleCandidate.length < 15 && i + 1 < lines.size) {
                        val nextLine = lines[i + 1]
                        if (nextLine.length in 3..40 && !isGenericHeader(nextLine)) {
                            titleCandidate = "$titleCandidate $nextLine"
                        }
                    }

                    val sanitized = sanitizeFileName(titleCandidate)
                    if (sanitized.isNotBlank()) {
                        return sanitized
                    }
                }
            }
        }

        // 2. Nếu không có từ khóa chính thức, tìm dòng in hoa nổi bật ở phần đầu văn bản
        for (line in lines.take(6)) {
            if (line.length in 6..45 && isAllUpperCase(line) && !isGenericHeader(line)) {
                val sanitized = sanitizeFileName(line)
                if (sanitized.isNotBlank()) {
                    return sanitized
                }
            }
        }

        return null
    }

    private fun isGenericHeader(line: String): Boolean {
        val lower = line.lowercase(Locale.getDefault())
        return lower.contains("cộng hòa xã hội") ||
                lower.contains("độc lập - tự do") ||
                lower.contains("việt nam") ||
                lower.contains("số:") ||
                lower.contains("ngày ")
    }

    private fun isAllUpperCase(str: String): Boolean {
        val letters = str.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.all { it.isUpperCase() }
    }

    private fun sanitizeFileName(raw: String): String {
        // Loại bỏ các ký tự cấm trong tên file hệ điều hành
        var clean = raw.replace(Regex("[\\\\/:*?\"<>|\\n\\r\\t]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (clean.length > 50) {
            clean = clean.substring(0, 50).trim()
        }

        return clean
    }
}
