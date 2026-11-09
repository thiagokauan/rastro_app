package br.edu.up.rastro.ui.detalhe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.up.rastro.data.local.Local
import br.edu.up.rastro.data.local.Saida
import br.edu.up.rastro.data.remote.PrevisaoResposta
import br.edu.up.rastro.ui.AppViewModelProvider
import br.edu.up.rastro.ui.corConteudo
import br.edu.up.rastro.ui.corFundo
import br.edu.up.rastro.ui.icone
import br.edu.up.rastro.util.abrirNoMapa
import br.edu.up.rastro.util.compartilharTexto
import br.edu.up.rastro.util.descricaoClima
import br.edu.up.rastro.util.emojiClima
import br.edu.up.rastro.util.formatarCoordenadas
import br.edu.up.rastro.util.formatarData
import br.edu.up.rastro.util.formatarDataHora
import br.edu.up.rastro.util.formatarDiaPrevisao
import br.edu.up.rastro.util.formatarDuracao
import br.edu.up.rastro.util.formatarKm
import br.edu.up.rastro.util.textoLocal
import br.edu.up.rastro.util.textoSaida
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDetalheScreen(
    onVoltar: () -> Unit,
    onEditarLocal: (Long) -> Unit,
    onNovaSaida: (Long) -> Unit,
    onEditarSaida: (localId: Long, saidaId: Long) -> Unit,
    viewModel: LocalDetalheViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val local by viewModel.local.collectAsStateWithLifecycle()
    val saidas by viewModel.saidas.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // rememberSaveable: os diálogos continuam abertos se a tela girar
    var confirmarExclusaoLocal by rememberSaveable { mutableStateOf(false) }
    var saidaParaExcluir by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(local?.nome.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditarLocal(viewModel.localId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar local")
                    }
                    IconButton(onClick = { confirmarExclusaoLocal = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Excluir local")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNovaSaida(viewModel.localId) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Registrar saída") }
            )
        }
    ) { padding ->
        val l = local
        if (l == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!viewModel.excluindo) {
                    if (viewModel.naoEncontrado) Text("Local não encontrado.") else CircularProgressIndicator()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CabecalhoLocal(
                        local = l,
                        onMapa = { abrirNoMapa(context, l) },
                        onCompartilhar = { compartilharTexto(context, l.nome, textoLocal(l)) }
                    )
                }
                item { CartaoClima(estado = viewModel.clima, onTentarNovamente = viewModel::recarregarClima) }
                item {
                    Text(
                        "Saídas (${saidas.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (saidas.isEmpty()) {
                    item {
                        Text(
                            "Nenhuma saída registrada aqui. Toque em \"Registrar saída\" para adicionar a primeira.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(saidas, key = { it.id }) { saida ->
                    SaidaCard(
                        saida = saida,
                        onCompartilhar = { compartilharTexto(context, "Saída em ${l.nome}", textoSaida(l, saida)) },
                        onEditar = { onEditarSaida(l.id, saida.id) },
                        onExcluir = { saidaParaExcluir = saida.id }
                    )
                }
            }
        }
    }

    if (confirmarExclusaoLocal) {
        AlertDialog(
            onDismissRequest = { confirmarExclusaoLocal = false },
            title = { Text("Excluir local?") },
            text = {
                Text(
                    if (saidas.isEmpty()) "Esta ação não pode ser desfeita."
                    else "As ${saidas.size} saída(s) registradas neste local também serão excluídas. Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusaoLocal = false
                    viewModel.excluirLocal(onExcluido = onVoltar)
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { confirmarExclusaoLocal = false }) { Text("Cancelar") } }
        )
    }

    saidaParaExcluir?.let { id ->
        AlertDialog(
            onDismissRequest = { saidaParaExcluir = null },
            title = { Text("Excluir saída?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.excluirSaida(id)
                    saidaParaExcluir = null
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { saidaParaExcluir = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CabecalhoLocal(local: Local, onMapa: () -> Unit, onCompartilhar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = {},
            label = { Text(local.tipo.rotulo) },
            leadingIcon = { Icon(local.tipo.icone(), contentDescription = null, Modifier.size(18.dp)) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = local.tipo.corFundo(),
                labelColor = local.tipo.corConteudo(),
                leadingIconContentColor = local.tipo.corConteudo()
            ),
            border = null
        )
        Text(local.cidade, style = MaterialTheme.typography.titleMedium)
        Text(
            formatarCoordenadas(local.latitude, local.longitude),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (local.observacoes.isNotBlank()) {
            Text(local.observacoes, style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onMapa) {
                Icon(Icons.Filled.Map, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Abrir no Maps")
            }
            OutlinedButton(onClick = onCompartilhar) {
                Icon(Icons.Filled.Share, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Compartilhar")
            }
        }
    }
}

@Composable
private fun CartaoClima(estado: ClimaEstado, onTentarNovamente: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Previsão do tempo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (estado !is ClimaEstado.Carregando) {
                    IconButton(onClick = onTentarNovamente) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar previsão")
                    }
                }
            }
            when (estado) {
                ClimaEstado.Carregando -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Consultando a Open-Meteo...", Modifier.padding(start = 12.dp))
                }
                is ClimaEstado.Erro -> Column {
                    Text(estado.mensagem, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
                }
                is ClimaEstado.Sucesso -> {
                    val r = estado.resultado
                    ConteudoClima(r.previsao)
                    Text(
                        if (r.doCache) "Sem conexão. Mostrando a previsão salva em ${formatarDataHora(r.atualizadoEm)}."
                        else "Atualizado em ${formatarDataHora(r.atualizadoEm)}. Fonte: Open-Meteo.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (r.doCache) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ConteudoClima(p: PrevisaoResposta) {
    p.current?.let { atual ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emojiClima(atual.codigoClima), fontSize = 40.sp)
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    atual.temperatura?.let { "${it.roundToInt()}°C" } ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(descricaoClima(atual.codigoClima), style = MaterialTheme.typography.bodyMedium)
                atual.ventoKmh?.let {
                    Text("Vento: ${it.roundToInt()} km/h", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    p.daily?.let { d ->
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(d.time.indices.toList()) { i ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(formatarDiaPrevisao(d.time[i]), style = MaterialTheme.typography.labelMedium)
                        Text(emojiClima(d.codigosClima.getOrNull(i)), fontSize = 22.sp)
                        Text(
                            "${d.temperaturaMax.getOrNull(i)?.roundToInt() ?: "--"}° / ${d.temperaturaMin.getOrNull(i)?.roundToInt() ?: "--"}°",
                            style = MaterialTheme.typography.labelMedium
                        )
                        d.chanceChuva?.getOrNull(i)?.let {
                            Text("Chuva $it%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaidaCard(
    saida: Saida,
    onCompartilhar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 4.dp)) {
            Text(formatarData(saida.data), style = MaterialTheme.typography.titleSmall)
            val detalhes = buildList {
                add(formatarDuracao(saida.duracaoMinutos))
                saida.distanciaKm?.let { add(formatarKm(it)) }
                saida.peixesCapturados?.let { add("Peixes: $it") }
            }
            Text(detalhes.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
            if (saida.companhia.isNotBlank()) {
                Text(
                    "Com ${saida.companhia}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (saida.observacoes.isNotBlank()) {
                Text(saida.observacoes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onCompartilhar) { Icon(Icons.Filled.Share, contentDescription = "Compartilhar saída") }
                IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, contentDescription = "Editar saída") }
                IconButton(onClick = onExcluir) { Icon(Icons.Filled.Delete, contentDescription = "Excluir saída") }
            }
        }
    }
}
