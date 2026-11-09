package br.edu.up.rastro.ui.locais

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.up.rastro.data.RastroRepository
import br.edu.up.rastro.data.local.LocalComTotal
import br.edu.up.rastro.data.local.TipoLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LocaisViewModel(repository: RastroRepository) : ViewModel() {

    private val _filtro = MutableStateFlow<TipoLocal?>(null) // null = todos
    val filtro: StateFlow<TipoLocal?> = _filtro.asStateFlow()

    /** null enquanto o Room ainda não respondeu (mostra carregando). */
    val locais: StateFlow<List<LocalComTotal>?> =
        combine(repository.locaisComTotal(), _filtro) { lista, filtro ->
            if (filtro == null) lista else lista.filter { it.local.tipo == filtro }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun filtrar(tipo: TipoLocal?) {
        _filtro.value = tipo
    }
}
