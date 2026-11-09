package br.edu.up.rastro.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.up.rastro.RastroApp
import br.edu.up.rastro.ui.detalhe.LocalDetalheViewModel
import br.edu.up.rastro.ui.locais.LocaisViewModel
import br.edu.up.rastro.ui.localform.LocalFormViewModel
import br.edu.up.rastro.ui.saidaform.SaidaFormViewModel

/**
 * Fábrica que cria os ViewModels entregando o repositório a cada um.
 * O SavedStateHandle traz os argumentos da navegação (ex.: localId).
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LocaisViewModel(rastroApp().repository) }
        initializer { LocalFormViewModel(createSavedStateHandle(), rastroApp().repository) }
        initializer { LocalDetalheViewModel(createSavedStateHandle(), rastroApp().repository) }
        initializer { SaidaFormViewModel(createSavedStateHandle(), rastroApp().repository) }
    }
}

private fun CreationExtras.rastroApp(): RastroApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RastroApp
