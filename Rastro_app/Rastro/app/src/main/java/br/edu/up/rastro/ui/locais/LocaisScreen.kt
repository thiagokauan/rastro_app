package br.edu.up.rastro.ui.locais

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.up.rastro.data.local.LocalComTotal
import br.edu.up.rastro.data.local.TipoLocal
import br.edu.up.rastro.ui.AppViewModelProvider
import br.edu.up.rastro.ui.corConteudo
import br.edu.up.rastro.ui.corFundo
import br.edu.up.rastro.ui.icone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocaisScreen(
    onNovoLocal: () -> Unit,
    onAbrirLocal: (Long) -> Unit,
    viewModel: LocaisViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val locais by viewModel.locais.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rastro")
                        Text("Diário de trilhas e pescarias", style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNovoLocal,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo local") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro == null,
                    onClick = { viewModel.filtrar(null) },
                    label = { Text("Todos") }
                )
                TipoLocal.entries.forEach { tipo ->
                    FilterChip(
                        selected = filtro == tipo,
                        onClick = { viewModel.filtrar(tipo) },
                        label = { Text(tipo.plural) },
                        leadingIcon = { Icon(tipo.icone(), contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
            }

            val lista = locais
            when {
                lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                lista.isEmpty() -> EstadoVazio(filtro = filtro, onNovoLocal = onNovoLocal)
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lista, key = { it.local.id }) { item ->
                        LocalCard(item = item, onClick = { onAbrirLocal(item.local.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalCard(item: LocalComTotal, onClick: () -> Unit) {
    val tipo = item.local.tipo
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(tipo.corFundo(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(tipo.icone(), contentDescription = tipo.rotulo, tint = tipo.corConteudo())
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    item.local.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.local.cidade,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when (item.totalSaidas) {
                        0 -> "Nenhuma saída ainda"
                        1 -> "1 saída"
                        else -> "${item.totalSaidas} saídas"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun EstadoVazio(filtro: TipoLocal?, onNovoLocal: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            filtro?.icone() ?: Icons.Filled.Landscape,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            when (filtro) {
                null -> "Nenhum local cadastrado"
                TipoLocal.TRILHA -> "Nenhuma trilha cadastrada"
                TipoLocal.PESCA -> "Nenhum ponto de pesca cadastrado"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cadastre um local para registrar suas saídas e ver a previsão do tempo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onNovoLocal) { Text("Cadastrar local") }
    }
}
