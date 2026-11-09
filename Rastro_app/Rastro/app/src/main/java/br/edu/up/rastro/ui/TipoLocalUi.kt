package br.edu.up.rastro.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import br.edu.up.rastro.data.local.TipoLocal

/** Ícone e cores de cada tipo, usados em todas as telas para manter a identidade visual. */
fun TipoLocal.icone(): ImageVector = when (this) {
    TipoLocal.TRILHA -> Icons.Filled.Hiking
    TipoLocal.PESCA -> Icons.Filled.Phishing
}

@Composable
fun TipoLocal.corFundo(): Color = when (this) {
    TipoLocal.TRILHA -> MaterialTheme.colorScheme.primaryContainer
    TipoLocal.PESCA -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
fun TipoLocal.corConteudo(): Color = when (this) {
    TipoLocal.TRILHA -> MaterialTheme.colorScheme.onPrimaryContainer
    TipoLocal.PESCA -> MaterialTheme.colorScheme.onSecondaryContainer
}
