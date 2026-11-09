package br.edu.up.rastro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta: verde-musgo (trilha) e azul-rio (pesca)
val Musgo = Color(0xFF3F5F3A)
val MusgoClaro = Color(0xFFD7E8CD)
val Rio = Color(0xFF2D6577)
val RioClaro = Color(0xFFCDE6EF)
val Liquen = Color(0xFF7A6B2E)
val Nevoa = Color(0xFFF5F7F2)

private val Claro = lightColorScheme(
    primary = Musgo,
    onPrimary = Color.White,
    primaryContainer = MusgoClaro,
    onPrimaryContainer = Color(0xFF0F2A0D),
    secondary = Rio,
    onSecondary = Color.White,
    secondaryContainer = RioClaro,
    onSecondaryContainer = Color(0xFF062A36),
    tertiary = Liquen,
    background = Nevoa,
    surface = Nevoa,
    surfaceVariant = Color(0xFFE2E6DC),
    onSurfaceVariant = Color(0xFF44483F)
)

private val Escuro = darkColorScheme(
    primary = Color(0xFFA9CF9C),
    onPrimary = Color(0xFF173512),
    primaryContainer = Color(0xFF2B4826),
    onPrimaryContainer = MusgoClaro,
    secondary = Color(0xFF93CDE0),
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF134D5D),
    onSecondaryContainer = RioClaro,
    tertiary = Color(0xFFD9C780)
)

@Composable
fun RastroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Escuro else Claro,
        content = content
    )
}
