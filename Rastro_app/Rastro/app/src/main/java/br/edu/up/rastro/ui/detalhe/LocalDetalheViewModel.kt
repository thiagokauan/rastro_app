package br.edu.up.rastro.ui.detalhe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.up.rastro.data.ClimaResultado
import br.edu.up.rastro.data.RastroRepository
import br.edu.up.rastro.data.local.Local
import br.edu.up.rastro.data.local.Saida
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estados possíveis do cartão de clima. */
sealed interface ClimaEstado {
    data object Carregando : ClimaEstado
    data class Sucesso(val resultado: ClimaResultado) : ClimaEstado
    data class Erro(val mensagem: String) : ClimaEstado
}

class LocalDetalheViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RastroRepository
) : ViewModel() {

    val localId: Long = checkNotNull(savedStateHandle["localId"])

    val local: StateFlow<Local?> = repository.observarLocal(localId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val saidas: StateFlow<List<Saida>> = repository.saidasDoLocal(localId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var clima by mutableStateOf<ClimaEstado>(ClimaEstado.Carregando)
        private set

    var naoEncontrado by mutableStateOf(false)
        private set

    var excluindo by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            if (repository.obterLocal(localId) == null) naoEncontrado = true
        }
        // Busca o clima ao abrir e de novo sempre que as coordenadas mudarem (após editar o local)
        viewModelScope.launch {
            repository.observarLocal(localId)
                .filterNotNull()
                .map { it.latitude to it.longitude }
                .distinctUntilChanged()
                .collect { (lat, lon) -> carregarClima(lat, lon) }
        }
    }

    fun recarregarClima() {
        val l = local.value ?: return
        viewModelScope.launch { carregarClima(l.latitude, l.longitude) }
    }

    private suspend fun carregarClima(latitude: Double, longitude: Double) {
        clima = ClimaEstado.Carregando
        clima = try {
            ClimaEstado.Sucesso(repository.previsao(localId, latitude, longitude))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            ClimaEstado.Erro("Não foi possível obter a previsão do tempo. Verifique a internet.")
        }
    }

    fun excluirLocal(onExcluido: () -> Unit) {
        val l = local.value ?: return
        excluindo = true
        viewModelScope.launch {
            repository.excluirLocal(l) // as saídas são apagadas pelo CASCADE
            onExcluido()
        }
    }

    fun excluirSaida(saidaId: Long) {
        val saida = saidas.value.firstOrNull { it.id == saidaId } ?: return
        viewModelScope.launch { repository.excluirSaida(saida) }
    }
}
