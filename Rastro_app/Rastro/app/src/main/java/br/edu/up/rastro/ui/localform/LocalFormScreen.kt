package br.edu.up.rastro.ui.localform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.up.rastro.data.local.TipoLocal
import br.edu.up.rastro.ui.AppViewModelProvider
import br.edu.up.rastro.ui.icone
import br.edu.up.rastro.util.formatarCoordenadas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFormScreen(
    onVoltar: () -> Unit,
    viewModel: LocalFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.estado
    val teclado = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.editando) "Editar local" else "Novo local") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.nome,
                onValueChange = viewModel::onNome,
                label = { Text("Nome do local") },
                placeholder = {
                    Text(if (estado.tipo == TipoLocal.PESCA) "Ex.: Lago Azul" else "Ex.: Pico do Marumbi")
                },
                isError = estado.erroNome != null,
                supportingText = estado.erroNome?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tipo", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoLocal.entries.forEach { tipo ->
                        FilterChip(
                            selected = estado.tipo == tipo,
                            onClick = { viewModel.onTipo(tipo) },
                            label = { Text(tipo.rotulo) },
                            leadingIcon = { Icon(tipo.icone(), contentDescription = null, Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            // ----- Cidade via Geocoding API -----
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cidade", style = MaterialTheme.typography.titleSmall)

                if (estado.latitude != null && estado.longitude != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(estado.cidade, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    formatarCoordenadas(estado.latitude, estado.longitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = estado.busca,
                        onValueChange = viewModel::onBusca,
                        label = { Text(if (estado.latitude == null) "Buscar cidade" else "Trocar cidade") },
                        singleLine = true,
                        isError = estado.erroCidade != null,
                        supportingText = estado.erroCidade?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            teclado?.hide()
                            viewModel.buscarCidade()
                        }),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        teclado?.hide()
                        viewModel.buscarCidade()
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar cidade")
                    }
                }

                if (estado.buscando) LinearProgressIndicator(Modifier.fillMaxWidth())
                estado.mensagemBusca?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                if (estado.resultados.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        estado.resultados.forEachIndexed { i, cidade ->
                            if (i > 0) HorizontalDivider()
                            ListItem(
                                headlineContent = { Text(cidade.name) },
                                supportingContent = {
                                    Text(listOfNotNull(cidade.admin1, cidade.country).joinToString(", "))
                                },
                                leadingContent = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                                modifier = Modifier.clickable { viewModel.selecionarCidade(cidade) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = estado.observacoes,
                onValueChange = viewModel::onObservacoes,
                label = { Text("Observações (opcional)") },
                placeholder = { Text("Acesso, dificuldade, melhor isca...") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.salvar(onSalvo = onVoltar) },
                enabled = !estado.salvando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (viewModel.editando) "Salvar alterações" else "Salvar local")
            }
        }
    }
}
