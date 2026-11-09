package br.edu.up.rastro.ui.localform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.up.rastro.data.RastroRepository
import br.edu.up.rastro.data.local.Local
import br.edu.up.rastro.data.local.TipoLocal
import br.edu.up.rastro.data.remote.Cidade
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Tudo o que a tela de formulário mostra. Fica no ViewModel, então sobrevive à rotação. */
data class LocalFormEstado(
    val nome: String = "",
    val tipo: TipoLocal = TipoLocal.TRILHA,
    val cidade: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val observacoes: String = "",
    val busca: String = "",
    val resultados: List<Cidade> = emptyList(),
    val buscando: Boolean = false,
    val mensagemBusca: String? = null,
    val erroNome: String? = null,
    val erroCidade: String? = null,
    val salvando: Boolean = false
)

class LocalFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RastroRepository
) : ViewModel() {

    private val localId: Long = savedStateHandle["localId"] ?: -1L
    val editando: Boolean = localId > 0

    var estado by mutableStateOf(LocalFormEstado())
        private set

    init {
        // Edição: carrega o local do banco e preenche o formulário
        if (editando) {
            viewModelScope.launch {
                repository.obterLocal(localId)?.let { l ->
                    estado = estado.copy(
                        nome = l.nome,
                        tipo = l.tipo,
                        cidade = l.cidade,
                        latitude = l.latitude,
                        longitude = l.longitude,
                        observacoes = l.observacoes
                    )
                }
            }
        }
    }

    fun onNome(valor: String) { estado = estado.copy(nome = valor, erroNome = null) }
    fun onTipo(valor: TipoLocal) { estado = estado.copy(tipo = valor) }
    fun onObservacoes(valor: String) { estado = estado.copy(observacoes = valor) }
    fun onBusca(valor: String) { estado = estado.copy(busca = valor, mensagemBusca = null) }

    /** Consulta a Geocoding API da Open-Meteo para transformar o nome da cidade em coordenadas. */
    fun buscarCidade() {
        val termo = estado.busca.trim()
        if (termo.length < 2) {
            estado = estado.copy(mensagemBusca = "Digite pelo menos 2 letras do nome da cidade.")
            return
        }
        viewModelScope.launch {
            estado = estado.copy(buscando = true, mensagemBusca = null, resultados = emptyList())
            estado = try {
                val resultados = repository.buscarCidades(termo)
                estado.copy(
                    buscando = false,
                    resultados = resultados,
                    mensagemBusca = if (resultados.isEmpty()) "Nenhuma cidade encontrada para \"$termo\"." else null
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                estado.copy(buscando = false, mensagemBusca = "Sem conexão com o serviço de busca. Verifique a internet e tente de novo.")
            }
        }
    }

    fun selecionarCidade(cidade: Cidade) {
        estado = estado.copy(
            cidade = cidade.descricao(),
            latitude = cidade.latitude,
            longitude = cidade.longitude,
            resultados = emptyList(),
            busca = "",
            erroCidade = null
        )
    }

    fun salvar(onSalvo: () -> Unit) {
        val e = estado
        val erroNome = if (e.nome.isBlank()) "Informe o nome do local." else null
        val erroCidade = if (e.latitude == null || e.longitude == null) "Busque e selecione a cidade do local." else null
        if (erroNome != null || erroCidade != null) {
            estado = e.copy(erroNome = erroNome, erroCidade = erroCidade)
            return
        }
        estado = e.copy(salvando = true)
        viewModelScope.launch {
            repository.salvarLocal(
                Local(
                    id = if (editando) localId else 0,
                    nome = e.nome.trim(),
                    tipo = e.tipo,
                    cidade = e.cidade,
                    latitude = e.latitude!!,
                    longitude = e.longitude!!,
                    observacoes = e.observacoes.trim()
                )
            )
            onSalvo()
        }
    }
}
