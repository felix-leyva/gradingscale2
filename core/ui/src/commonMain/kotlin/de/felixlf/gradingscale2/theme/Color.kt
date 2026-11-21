package de.felixlf.gradingscale2.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// ==========================================
// Light Color Scheme Tokens (Modern Emerald Teal & Clean Slate)
// ==========================================
val primaryLight = Color(0xFF006A60)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFF8EF4E7)
val onPrimaryContainerLight = Color(0xFF00201D)

val secondaryLight = Color(0xFF4A635E)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFCCE8E2)
val onSecondaryContainerLight = Color(0xFF05201C)

val tertiaryLight = Color(0xFF456179)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFCCE5FF)
val onTertiaryContainerLight = Color(0xFF001E31)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)

// Clean, high-contrast surfaces without yellow undertones
val backgroundLight = Color(0xFFF6F9F8)
val onBackgroundLight = Color(0xFF171D1C)
val surfaceLight = Color(0xFFF6F9F8)
val onSurfaceLight = Color(0xFF171D1C)
val surfaceVariantLight = Color(0xFFDAE5E2)
val onSurfaceVariantLight = Color(0xFF3F4947)
val outlineLight = Color(0xFF6F7977)
val outlineVariantLight = Color(0xFFBEC9C6)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2C3231)
val inverseOnSurfaceLight = Color(0xFFEDF2F1)
val inversePrimaryLight = Color(0xFF71D7CB)
val surfaceDimLight = Color(0xFFD6DAD9)
val surfaceBrightLight = Color(0xFFF6F9F8)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF0F4F3)
val surfaceContainerLight = Color(0xFFEAEFEF)
val surfaceContainerHighLight = Color(0xFFE4EAE9)
val surfaceContainerHighestLight = Color(0xFFDEE4E3)

// ==========================================
// Medium Contrast Light
// ==========================================
val primaryLightMediumContrast = Color(0xFF004C44)
val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val primaryContainerLightMediumContrast = Color(0xFF1B8176)
val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val secondaryLightMediumContrast = Color(0xFF2E4743)
val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
val secondaryContainerLightMediumContrast = Color(0xFF607A74)
val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryLightMediumContrast = Color(0xFF28455C)
val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightMediumContrast = Color(0xFF5C7891)
val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val errorLightMediumContrast = Color(0xFF8C0009)
val onErrorLightMediumContrast = Color(0xFFFFFFFF)
val errorContainerLightMediumContrast = Color(0xFFDA342E)
val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
val backgroundLightMediumContrast = Color(0xFFF6F9F8)
val onBackgroundLightMediumContrast = Color(0xFF171D1C)
val surfaceLightMediumContrast = Color(0xFFF6F9F8)
val onSurfaceLightMediumContrast = Color(0xFF171D1C)
val surfaceVariantLightMediumContrast = Color(0xFFDAE5E2)
val onSurfaceVariantLightMediumContrast = Color(0xFF3B4543)
val outlineLightMediumContrast = Color(0xFF58615F)
val outlineVariantLightMediumContrast = Color(0xFF737D7B)
val scrimLightMediumContrast = Color(0xFF000000)
val inverseSurfaceLightMediumContrast = Color(0xFF2C3231)
val inverseOnSurfaceLightMediumContrast = Color(0xFFEDF2F1)
val inversePrimaryLightMediumContrast = Color(0xFF71D7CB)
val surfaceDimLightMediumContrast = Color(0xFFD6DAD9)
val surfaceBrightLightMediumContrast = Color(0xFFF6F9F8)
val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightMediumContrast = Color(0xFFF0F4F3)
val surfaceContainerLightMediumContrast = Color(0xFFEAEFEF)
val surfaceContainerHighLightMediumContrast = Color(0xFFE4EAE9)
val surfaceContainerHighestLightMediumContrast = Color(0xFFDEE4E3)

// ==========================================
// High Contrast Light
// ==========================================
val primaryLightHighContrast = Color(0xFF002824)
val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
val primaryContainerLightHighContrast = Color(0xFF004C44)
val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
val secondaryLightHighContrast = Color(0xFF0E2623)
val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
val secondaryContainerLightHighContrast = Color(0xFF2E4743)
val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
val tertiaryLightHighContrast = Color(0xFF04243A)
val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightHighContrast = Color(0xFF28455C)
val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
val errorLightHighContrast = Color(0xFF4E0002)
val onErrorLightHighContrast = Color(0xFFFFFFFF)
val errorContainerLightHighContrast = Color(0xFF8C0009)
val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
val backgroundLightHighContrast = Color(0xFFF6F9F8)
val onBackgroundLightHighContrast = Color(0xFF171D1C)
val surfaceLightHighContrast = Color(0xFFF6F9F8)
val onSurfaceLightHighContrast = Color(0xFF000000)
val surfaceVariantLightHighContrast = Color(0xFFDAE5E2)
val onSurfaceVariantLightHighContrast = Color(0xFF1D2625)
val outlineLightHighContrast = Color(0xFF3B4543)
val outlineVariantLightHighContrast = Color(0xFF3B4543)
val scrimLightHighContrast = Color(0xFF000000)
val inverseSurfaceLightHighContrast = Color(0xFF2C3231)
val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
val inversePrimaryLightHighContrast = Color(0xFFA5FFF3)
val surfaceDimLightHighContrast = Color(0xFFD6DAD9)
val surfaceBrightLightHighContrast = Color(0xFFF6F9F8)
val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightHighContrast = Color(0xFFF0F4F3)
val surfaceContainerLightHighContrast = Color(0xFFEAEFEF)
val surfaceContainerHighLightHighContrast = Color(0xFFE4EAE9)
val surfaceContainerHighestLightHighContrast = Color(0xFFDEE4E3)

// ==========================================
// Dark Color Scheme Tokens
// ==========================================
val primaryDark = Color(0xFF71D7CB)
val onPrimaryDark = Color(0xFF003732)
val primaryContainerDark = Color(0xFF005048)
val onPrimaryContainerDark = Color(0xFF8EF4E7)

val secondaryDark = Color(0xFFB1CCC6)
val onSecondaryDark = Color(0xFF1C3531)
val secondaryContainerDark = Color(0xFF334B47)
val onSecondaryContainerDark = Color(0xFFCCE8E2)

val tertiaryDark = Color(0xFFACCAE5)
val onTertiaryDark = Color(0xFF133349)
val tertiaryContainerDark = Color(0xFF2D4A60)
val onTertiaryContainerDark = Color(0xFFCCE5FF)

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val backgroundDark = Color(0xFF0E1514)
val onBackgroundDark = Color(0xFFDEE4E3)
val surfaceDark = Color(0xFF0E1514)
val onSurfaceDark = Color(0xFFDEE4E3)
val surfaceVariantDark = Color(0xFF3F4947)
val onSurfaceVariantDark = Color(0xFFBEC9C6)
val outlineDark = Color(0xFF899391)
val outlineVariantDark = Color(0xFF3F4947)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFDEE4E3)
val inverseOnSurfaceDark = Color(0xFF2C3231)
val inversePrimaryDark = Color(0xFF006A60)
val surfaceDimDark = Color(0xFF0E1514)
val surfaceBrightDark = Color(0xFF343A39)
val surfaceContainerLowestDark = Color(0xFF090F0E)
val surfaceContainerLowDark = Color(0xFF171D1C)
val surfaceContainerDark = Color(0xFF1B2120)
val surfaceContainerHighDark = Color(0xFF252B2A)
val surfaceContainerHighestDark = Color(0xFF303635)

// ==========================================
// Medium Contrast Dark
// ==========================================
val primaryDarkMediumContrast = Color(0xFF76DCD0)
val onPrimaryDarkMediumContrast = Color(0xFF001A18)
val primaryContainerDarkMediumContrast = Color(0xFF3B9F94)
val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
val secondaryDarkMediumContrast = Color(0xFFB5D0CB)
val onSecondaryDarkMediumContrast = Color(0xFF011A17)
val secondaryContainerDarkMediumContrast = Color(0xFF7C9691)
val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
val tertiaryDarkMediumContrast = Color(0xFFB1CEEA)
val onTertiaryDarkMediumContrast = Color(0xFF001828)
val tertiaryContainerDarkMediumContrast = Color(0xFF7793AE)
val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
val errorDarkMediumContrast = Color(0xFFFFBAB1)
val onErrorDarkMediumContrast = Color(0xFF370001)
val errorContainerDarkMediumContrast = Color(0xFFFF5449)
val onErrorContainerDarkMediumContrast = Color(0xFF000000)
val backgroundDarkMediumContrast = Color(0xFF0E1514)
val onBackgroundDarkMediumContrast = Color(0xFFDEE4E3)
val surfaceDarkMediumContrast = Color(0xFF0E1514)
val onSurfaceDarkMediumContrast = Color(0xFFF7FDFB)
val surfaceVariantDarkMediumContrast = Color(0xFF3F4947)
val onSurfaceVariantDarkMediumContrast = Color(0xFFC2CDC9)
val outlineDarkMediumContrast = Color(0xFF9BA5A3)
val outlineVariantDarkMediumContrast = Color(0xFF7B8583)
val scrimDarkMediumContrast = Color(0xFF000000)
val inverseSurfaceDarkMediumContrast = Color(0xFFDEE4E3)
val inverseOnSurfaceDarkMediumContrast = Color(0xFF252B2A)
val inversePrimaryDarkMediumContrast = Color(0xFF005149)
val surfaceDimDarkMediumContrast = Color(0xFF0E1514)
val surfaceBrightDarkMediumContrast = Color(0xFF343A39)
val surfaceContainerLowestDarkMediumContrast = Color(0xFF090F0E)
val surfaceContainerLowDarkMediumContrast = Color(0xFF171D1C)
val surfaceContainerDarkMediumContrast = Color(0xFF1B2120)
val surfaceContainerHighDarkMediumContrast = Color(0xFF252B2A)
val surfaceContainerHighestDarkMediumContrast = Color(0xFF303635)

// ==========================================
// High Contrast Dark
// ==========================================
val primaryDarkHighContrast = Color(0xFFEBFFF9)
val onPrimaryDarkHighContrast = Color(0xFF000000)
val primaryContainerDarkHighContrast = Color(0xFF76DCD0)
val onPrimaryContainerDarkHighContrast = Color(0xFF000000)
val secondaryDarkHighContrast = Color(0xFFEBFFF9)
val onSecondaryDarkHighContrast = Color(0xFF000000)
val secondaryContainerDarkHighContrast = Color(0xFFB5D0CB)
val onSecondaryContainerDarkHighContrast = Color(0xFF000000)
val tertiaryDarkHighContrast = Color(0xFFF7FBFF)
val onTertiaryDarkHighContrast = Color(0xFF000000)
val tertiaryContainerDarkHighContrast = Color(0xFFB1CEEA)
val onTertiaryContainerDarkHighContrast = Color(0xFF000000)
val errorDarkHighContrast = Color(0xFFFFF9F9)
val onErrorDarkHighContrast = Color(0xFF000000)
val errorContainerDarkHighContrast = Color(0xFFFFBAB1)
val onErrorContainerDarkHighContrast = Color(0xFF000000)
val backgroundDarkHighContrast = Color(0xFF0E1514)
val onBackgroundDarkHighContrast = Color(0xFFDEE4E3)
val surfaceDarkHighContrast = Color(0xFF0E1514)
val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkHighContrast = Color(0xFF3F4947)
val onSurfaceVariantDarkHighContrast = Color(0xFFF1FCF8)
val outlineDarkHighContrast = Color(0xFFC2CDC9)
val outlineVariantDarkHighContrast = Color(0xFFC2CDC9)
val scrimDarkHighContrast = Color(0xFF000000)
val inverseSurfaceDarkHighContrast = Color(0xFFDEE4E3)
val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
val inversePrimaryDarkHighContrast = Color(0xFF00302B)
val surfaceDimDarkHighContrast = Color(0xFF0E1514)
val surfaceBrightDarkHighContrast = Color(0xFF343A39)
val surfaceContainerLowestDarkHighContrast = Color(0xFF090F0E)
val surfaceContainerLowDarkHighContrast = Color(0xFF171D1C)
val surfaceContainerDarkHighContrast = Color(0xFF1B2120)
val surfaceContainerHighDarkHighContrast = Color(0xFF252B2A)
val surfaceContainerHighestDarkHighContrast = Color(0xFF303635)

// ==========================================
// Semantic Grade Color Tokens & Smooth Traffic-Light Interpolation
// ==========================================
@Immutable
data class SemanticGradeColor(
    val badgeColor: Color,
    val onBadgeColor: Color,
    val containerColor: Color,
    val onContainerColor: Color,
)

private data class GradeColorStop(val fraction: Float, val color: Color)

// Traditional temperature / traffic-light scale:
// Red (mal / poor) -> Orange -> Amber/Yellow (regular) -> Lime (satisfactorio) -> Green (bien / excelente)
private val lightGradeStops = listOf(
    GradeColorStop(0.00f, Color(0xFFDC2626)), // Red 600 (0% - mal)
    GradeColorStop(0.35f, Color(0xFFEA580C)), // Orange 600 (35%)
    GradeColorStop(0.55f, Color(0xFFD97706)), // Amber 600 (55% - regular)
    GradeColorStop(0.75f, Color(0xFF65A30D)), // Lime 600 (75% - satisfactorio)
    GradeColorStop(1.00f, Color(0xFF16A34A)), // Green 600 (100% - excelente)
)

private val darkGradeStops = listOf(
    GradeColorStop(0.00f, Color(0xFFEF4444)), // Red 500 (0% - mal)
    GradeColorStop(0.35f, Color(0xFFF97316)), // Orange 500 (35%)
    GradeColorStop(0.55f, Color(0xFFFBBF24)), // Amber 400 (55% - regular)
    GradeColorStop(0.75f, Color(0xFFA3E635)), // Lime 400 (75% - satisfactorio)
    GradeColorStop(1.00f, Color(0xFF4ADE80)), // Green 400 (100% - excelente)
)

private fun interpolateColorFromStops(stops: List<GradeColorStop>, fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    if (clamped <= stops.first().fraction) return stops.first().color
    if (clamped >= stops.last().fraction) return stops.last().color

    for (i in 0 until stops.size - 1) {
        val start = stops[i]
        val end = stops[i + 1]
        if (clamped in start.fraction..end.fraction) {
            val range = end.fraction - start.fraction
            val localFraction = if (range > 0f) (clamped - start.fraction) / range else 0f
            return lerp(start.color, end.color, localFraction)
        }
    }
    return stops.last().color
}

@Immutable
data class GradeScaleColors(
    val isDark: Boolean = false,
) {
    fun forPercentage(percentage: Double): SemanticGradeColor {
        val fraction = percentage.toFloat().coerceIn(0f, 1f)
        val stops = if (isDark) darkGradeStops else lightGradeStops
        val badge = interpolateColorFromStops(stops, fraction)
        val onBadge = if (badge.luminance() > 0.42f) Color(0xFF1F2937) else Color.White
        val container = if (isDark) badge.copy(alpha = 0.22f) else badge.copy(alpha = 0.14f)
        val onContainer = if (isDark) {
            lerp(badge, Color.White, 0.40f)
        } else {
            lerp(badge, Color.Black, 0.35f)
        }
        return SemanticGradeColor(
            badgeColor = badge,
            onBadgeColor = onBadge,
            containerColor = container,
            onContainerColor = onContainer,
        )
    }

    val gradeA: SemanticGradeColor get() = forPercentage(0.95)
    val gradeB: SemanticGradeColor get() = forPercentage(0.80)
    val gradeC: SemanticGradeColor get() = forPercentage(0.65)
    val gradeD: SemanticGradeColor get() = forPercentage(0.50)
    val gradeF: SemanticGradeColor get() = forPercentage(0.20)
}

val lightGradeColors = GradeScaleColors(isDark = false)
val darkGradeColors = GradeScaleColors(isDark = true)
