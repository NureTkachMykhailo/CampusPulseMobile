package com.mtkach.campuspulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Emerald = Color(0xFF0F6E4F)
private val EmeraldContainer = Color(0xFFD3EFE1)
private val Surface = Color(0xFFF7FAF8)
private val Background = Color(0xFFEFF4F1)

private val LightColors = lightColorScheme(
    primary = Emerald,
    primaryContainer = EmeraldContainer,
    surface = Surface,
    background = Background,
)

private val ChronicleTypography = Typography(
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
)

@Composable
fun CampusPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ChronicleTypography,
        content = content,
    )
}
