package com.vibe.pdfscan.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Loại Theme: Đơn sắc (Solid) hoặc Gradient
 */
enum class ThemeCategory {
    SOLID,
    GRADIENT
}

/**
 * Danh mục các Preset màu giao diện cho VibeScan
 */
enum class ThemeColorPreset(
    val id: String,
    val displayName: String,
    val category: ThemeCategory,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val containerColor: Color,
    val gradientColors: List<Color> = emptyList()
) {
    // ==================== MÀU ĐƠN SẮC (SOLID PRESETS) ====================
    BLUE_INDIGO(
        id = "blue_indigo",
        displayName = "Xanh Dương",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFF1D4ED8),
        secondaryColor = Color(0xFF2563EB),
        tertiaryColor = Color(0xFF3B82F6),
        containerColor = Color(0xFFDBEAFE)
    ),
    EMERALD_GREEN(
        id = "emerald_green",
        displayName = "Xanh Ngọc Lục",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF10B981),
        tertiaryColor = Color(0xFF34D399),
        containerColor = Color(0xFFD1FAE5)
    ),
    SUNSET_ORANGE(
        id = "sunset_orange",
        displayName = "Cam Hoàng Hôn",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFFF97316),
        tertiaryColor = Color(0xFFFB923C),
        containerColor = Color(0xFFFFEDD5)
    ),
    ROYAL_PURPLE(
        id = "royal_purple",
        displayName = "Tím Hoàng Gia",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFF8B5CF6),
        tertiaryColor = Color(0xFFA78BFA),
        containerColor = Color(0xFFEDE9FE)
    ),
    ROSE_PINK(
        id = "rose_pink",
        displayName = "Hồng Ruby",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFFE11D48),
        secondaryColor = Color(0xFFF43F5E),
        tertiaryColor = Color(0xFFFB7185),
        containerColor = Color(0xFFFFE4E6)
    ),
    OCEAN_TEAL(
        id = "ocean_teal",
        displayName = "Xanh Biển Sâu",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFF0D9488),
        secondaryColor = Color(0xFF14B8A6),
        tertiaryColor = Color(0xFF2DD4BF),
        containerColor = Color(0xFFCCFBF1)
    ),
    MIDNIGHT_SLATE(
        id = "midnight_slate",
        displayName = "Đen Xám Tinh Tế",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFF334155),
        secondaryColor = Color(0xFF475569),
        tertiaryColor = Color(0xFF64748B),
        containerColor = Color(0xFFE2E8F0)
    ),
    CYBER_AMBER(
        id = "cyber_amber",
        displayName = "Vàng Hổ Phách",
        category = ThemeCategory.SOLID,
        primaryColor = Color(0xFFD97706),
        secondaryColor = Color(0xFFF59E0B),
        tertiaryColor = Color(0xFFFBBF24),
        containerColor = Color(0xFFFEF3C7)
    ),

    // ==================== MÀU GRADIENT (GRADIENT PRESETS) ====================
    GRADIENT_SUNRISE(
        id = "grad_sunrise",
        displayName = "Bình Minh Rực Rỡ",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFFE11D48),
        secondaryColor = Color(0xFFFF512F),
        tertiaryColor = Color(0xFFF97316),
        containerColor = Color(0xFFFFE4E6),
        gradientColors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))
    ),
    GRADIENT_AURORA(
        id = "grad_aurora",
        displayName = "Cực Quang Bắc Cực",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF00C9FF),
        tertiaryColor = Color(0xFF92FE9D),
        containerColor = Color(0xFFD1FAE5),
        gradientColors = listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
    ),
    GRADIENT_PURPLE_SUNSET(
        id = "grad_purple_sunset",
        displayName = "Hoàng Hôn Tím",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFF8A2387),
        secondaryColor = Color(0xFFE94057),
        tertiaryColor = Color(0xFFF27121),
        containerColor = Color(0xFFEDE9FE),
        gradientColors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
    ),
    GRADIENT_OCEAN_BREEZE(
        id = "grad_ocean_breeze",
        displayName = "Đại Dương Sâu",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFF1D4ED8),
        secondaryColor = Color(0xFF2E3192),
        tertiaryColor = Color(0xFF1BFFFF),
        containerColor = Color(0xFFDBEAFE),
        gradientColors = listOf(Color(0xFF2E3192), Color(0xFF1BFFFF))
    ),
    GRADIENT_CYBER_NEON(
        id = "grad_cyber_neon",
        displayName = "Cyber Neon",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFF8E2DE2),
        tertiaryColor = Color(0xFF4A00E0),
        containerColor = Color(0xFFEDE9FE),
        gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    ),
    GRADIENT_LUSH_FOREST(
        id = "grad_lush_forest",
        displayName = "Rừng Nhiệt Đới",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFF15803D),
        secondaryColor = Color(0xFF134E5E),
        tertiaryColor = Color(0xFF71B280),
        containerColor = Color(0xFFDCFCE7),
        gradientColors = listOf(Color(0xFF134E5E), Color(0xFF71B280))
    ),
    GRADIENT_SAKURA(
        id = "grad_sakura",
        displayName = "Hoa Anh Đào",
        category = ThemeCategory.GRADIENT,
        primaryColor = Color(0xFFDB2777),
        secondaryColor = Color(0xFFFF758C),
        tertiaryColor = Color(0xFFFF7EB3),
        containerColor = Color(0xFFFCE7F3),
        gradientColors = listOf(Color(0xFFFF758C), Color(0xFFFF7EB3))
    );

    val isGradient: Boolean
        get() = category == ThemeCategory.GRADIENT

    /**
     * Tạo Gradient Brush để vẽ nền hoặc nút bấm
     */
    fun createBrush(): Brush {
        return if (isGradient && gradientColors.size >= 2) {
            Brush.horizontalGradient(gradientColors)
        } else {
            Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
        }
    }

    companion object {
        fun fromId(id: String?): ThemeColorPreset {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BLUE_INDIGO
        }
    }
}

/**
 * Chế độ màu hiển thị (Theme Mode)
 */
enum class ThemeDisplayMode(val displayName: String) {
    SYSTEM("Theo hệ thống"),
    LIGHT("Chế độ sáng"),
    DARK("Chế độ tối")
}

/**
 * Đối tượng nắm giữ Gradient hiện hành của App
 */
data class AppGradient(
    val preset: ThemeColorPreset,
    val brush: Brush,
    val isGradientActive: Boolean
)

val LocalAppGradient = compositionLocalOf {
    AppGradient(
        preset = ThemeColorPreset.BLUE_INDIGO,
        brush = ThemeColorPreset.BLUE_INDIGO.createBrush(),
        isGradientActive = false
    )
}

/**
 * Pha trộn màu sắc để tạo màu nền có ánh sắc tinh tế của Theme (Tonal tinting)
 */
fun blendColors(base: Color, tint: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    val r = base.red * (1f - f) + tint.red * f
    val g = base.green * (1f - f) + tint.green * f
    val b = base.blue * (1f - f) + tint.blue * f
    return Color(red = r, green = g, blue = b, alpha = 1f)
}

/**
 * Tạo ColorScheme Light theo Theme Preset với nền có ánh sắc của màu chủ đạo
 */
fun createLightColorScheme(preset: ThemeColorPreset): ColorScheme {
    val tintedBackground = blendColors(Color(0xFFF7F9FC), preset.primaryColor, 0.07f)
    val tintedSurface = blendColors(Color(0xFFFFFFFF), preset.primaryColor, 0.03f)
    val tintedSurfaceVariant = blendColors(Color(0xFFEEF2F7), preset.primaryColor, 0.11f)

    return lightColorScheme(
        primary = preset.primaryColor,
        secondary = preset.secondaryColor,
        tertiary = preset.tertiaryColor,
        primaryContainer = preset.containerColor,
        onPrimaryContainer = preset.primaryColor,
        secondaryContainer = blendColors(preset.containerColor, Color.White, 0.35f),
        onSecondaryContainer = preset.secondaryColor,
        background = tintedBackground,
        surface = tintedSurface,
        surfaceVariant = tintedSurfaceVariant,
        onPrimary = Color.White,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        onSurfaceVariant = TextSecondaryLight
    )
}

/**
 * Tạo ColorScheme Dark theo Theme Preset với nền tối có chiều sâu ánh sắc của màu chủ đạo
 */
fun createDarkColorScheme(preset: ThemeColorPreset): ColorScheme {
    val tintedDarkBackground = blendColors(Color(0xFF0C101A), preset.primaryColor, 0.14f)
    val tintedDarkSurface = blendColors(Color(0xFF161E2D), preset.primaryColor, 0.18f)
    val tintedDarkSurfaceVariant = blendColors(Color(0xFF232D40), preset.primaryColor, 0.23f)

    return darkColorScheme(
        primary = preset.tertiaryColor,
        secondary = preset.secondaryColor,
        tertiary = preset.primaryColor,
        primaryContainer = preset.primaryColor.copy(alpha = 0.35f),
        onPrimaryContainer = Color.White,
        secondaryContainer = preset.secondaryColor.copy(alpha = 0.25f),
        onSecondaryContainer = Color.White,
        background = tintedDarkBackground,
        surface = tintedDarkSurface,
        surfaceVariant = tintedDarkSurfaceVariant,
        onPrimary = Color.White,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        onSurfaceVariant = TextSecondaryDark
    )
}
