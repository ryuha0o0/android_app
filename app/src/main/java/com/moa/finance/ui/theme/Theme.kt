package com.moa.finance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MoaColorScheme = lightColorScheme(
    primary = MoaBlue,
    onPrimary = MoaSurface,
    primaryContainer = MoaBlueLight,
    onPrimaryContainer = MoaBlue,
    background = MoaBackground,
    onBackground = MoaTextPrimary,
    surface = MoaSurface,
    onSurface = MoaTextPrimary,
    onSurfaceVariant = MoaTextSecondary,
    outline = MoaDivider,
    error = MoaDanger,
)

@Composable
fun MoaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MoaColorScheme,
        typography = Typography(),
        content = content,
    )
}
