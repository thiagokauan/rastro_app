package br.edu.up.rastro.ui.saidaform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.up.rastro.data.RastroRepository
import br.edu.up.rastro.data.local.Saida
import br.edu.up.rastro.data.local.TipoLocal
import br.edu.up.rastro.util.hojeUtcMillis
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

data class SaidaFormEstado(
    val nomeLocal: String = "",
    val tipo: TipoLocal = TipoLocal.TRILHA,
    val data: Long = hojeUtcMillis(),
    val duracao: String = "",
    val companhia: String = "",
    val distancia: String = "",
    val peixes: String = "",
    val observacoes: String = "",
    val erroDuracao: String? = null,
    val erroDistancia: String? = null,
    val salvando: Boolean = false
)

class SaidaFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RastroRepository
) : ViewModel() {

    private val localId: Long = checkNotNull(savedStateHandle["localId"])
    private val saidaId: Long = savedStateHandle["saidaId"] ?: -1L
    val editando: Boolean = saidaId > 0

    var estado by mutableStateOf(SaidaFormEstado())
        private set

    init {
        viewModelScope.launch {
            // O tipo do local define se o formulário pede distância (trilha) ou peixes (pesca)
            repository.obterLocal(localId)?.let { l ->
                estado = estado.copy(nomeLocal = l.nome, tipo = l.tipo)
            }
            if (editando) {
                repository.obterSaida(saidaId)?.let { s ->
                    estado = estado.copy(
                        data = s.data,
                        duracao = horasDeMinutos(s.duracaoMinutos),
                        companhia = s.companhia,
                        distancia = s.distanciaKm?.toString()?.replace('.', ',').orEmpty(),
                        peixes = s.peixesCapturados.orEmpty(),
                        observacoes = s.observacoes
                    )
                }
            }
        }
    }

    fun onData(millis: Long) { estado = estado.copy(data = millis) }
    fun onDuracao(v: String) { estado = estado.copy(duracao = v.filter { it.isDigit() || it == ',' || it == '.' }, erroDuracao = null) }
    fun onCompanhia(v: String) { estado = estado.copy(companhia = v) }
    fun onDistancia(v: String) { estado = estado.copy(distancia = v.filter { it.isDigit() || it == ',' || it == '.' }, erroDistancia = null) }
    fun onPeixes(v: String) { estado = estado.copy(peixes = v) }
    fun onObservacoes(v: String) { estado = estado.copy(observacoes = v) }

    fun salvar(onSalvo: () -> Unit) {
        val e = estado
        val duracao = e.duracao.replace(',', '.').toDoubleOrNull()?.let { (it * 60).roundToInt() }
        val distancia = e.distancia.replace(',', '.').toDoubleOrNull()
        val peixes = e.peixes.trim().ifBlank { null }

        val erroDuracao = if (duracao == null || duracao <= 0) "Informe a duração em horas, ex.: 2,5" else null
        val erroDistancia = if (e.tipo == TipoLocal.TRILHA && e.distancia.isNotBlank() && distancia == null) "Use um número, ex.: 8,5" else null

        if (erroDuracao != null || erroDistancia != null) {
            estado = e.copy(erroDuracao = erroDuracao, erroDistancia = erroDistancia)
            return
        }

        estado = e.copy(salvando = true)
        viewModelScope.launch {
            repository.salvarSaida(
                Saida(
                    id = if (editando) saidaId else 0,
                    localId = localId,
                    data = e.data,
                    duracaoMinutos = duracao!!,
                    companhia = e.companhia.trim(),
                    distanciaKm = if (e.tipo == TipoLocal.TRILHA) distancia else null,
                    peixesCapturados = if (e.tipo == TipoLocal.PESCA) peixes else null,
                    observacoes = e.observacoes.trim()
                )
            )
            onSalvo()
        }
    }
}

private fun horasDeMinutos(minutos: Int): String =
    String.format(Locale.US, "%.2f", minutos / 60.0).trimEnd('0').trimEnd('.').replace('.', ',')
