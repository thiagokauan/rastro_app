package br.edu.up.rastro.ui.saidaform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.up.rastro.data.local.TipoLocal
import br.edu.up.rastro.ui.AppViewModelProvider
import br.edu.up.rastro.util.formatarData
import br.edu.up.rastro.util.hojeUtcMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaidaFormScreen(
    onVoltar: () -> Unit,
    viewModel: SaidaFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.estado
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (viewModel.editando) "Editar saída" else "Nova saída")
                        if (estado.nomeLocal.isNotBlank()) {
                            Text(estado.nomeLocal, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
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
            OutlinedCard(onClick = { mostrarCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Column(Modifier.padding(start = 16.dp).weight(1f)) {
                        Text("Data", style = MaterialTheme.typography.labelMedium)
                        Text(formatarData(estado.data), style = MaterialTheme.typography.bodyLarge)
                    }
                    Text("Alterar", color = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = estado.duracao,
                onValueChange = viewModel::onDuracao,
                label = { Text("Duração (horas)") },
                placeholder = { Text("Ex.: 2,5") },
                isError = estado.erroDuracao != null,
                supportingText = estado.erroDuracao?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.tipo == TipoLocal.TRILHA) {
                OutlinedTextField(
                    value = estado.distancia,
                    onValueChange = viewModel::onDistancia,
                    label = { Text("Distância percorrida em km (opcional)") },
                    isError = estado.erroDistancia != null,
                    supportingText = estado.erroDistancia?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = estado.peixes,
                    onValueChange = viewModel::onPeixes,
                    label = { Text("Peixes capturados (opcional)") },
                    placeholder = { Text("Ex.: 3 tilápias") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = estado.companhia,
                onValueChange = viewModel::onCompanhia,
                label = { Text("Companhia (opcional)") },
                placeholder = { Text("Quem foi junto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.observacoes,
                onValueChange = viewModel::onObservacoes,
                label = { Text("Observações (opcional)") },
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
                Text(if (viewModel.editando) "Salvar alterações" else "Salvar saída")
            }
        }
    }

    if (mostrarCalendario) {
        val limite = hojeUtcMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = estado.data,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= limite
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(viewModel::onData)
                    mostrarCalendario = false
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
