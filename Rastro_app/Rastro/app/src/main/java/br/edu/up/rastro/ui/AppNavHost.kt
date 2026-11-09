package br.edu.up.rastro.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.edu.up.rastro.ui.detalhe.LocalDetalheScreen
import br.edu.up.rastro.ui.locais.LocaisScreen
import br.edu.up.rastro.ui.localform.LocalFormScreen
import br.edu.up.rastro.ui.saidaform.SaidaFormScreen

/** Rotas do app. -1 significa "novo cadastro". */
object Rotas {
    const val LOCAIS = "locais"
    const val FORM_LOCAL = "form-local?localId={localId}"
    const val DETALHE = "detalhe/{localId}"
    const val FORM_SAIDA = "form-saida/{localId}?saidaId={saidaId}"

    fun formLocal(localId: Long = -1L) = "form-local?localId=$localId"
    fun detalhe(localId: Long) = "detalhe/$localId"
    fun formSaida(localId: Long, saidaId: Long = -1L) = "form-saida/$localId?saidaId=$saidaId"
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Rotas.LOCAIS) {

        composable(Rotas.LOCAIS) {
            LocaisScreen(
                onNovoLocal = { nav.navigate(Rotas.formLocal()) },
                onAbrirLocal = { id -> nav.navigate(Rotas.detalhe(id)) }
            )
        }

        composable(
            Rotas.FORM_LOCAL,
            arguments = listOf(navArgument("localId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            LocalFormScreen(onVoltar = { nav.popBackStack() })
        }

        composable(
            Rotas.DETALHE,
            arguments = listOf(navArgument("localId") { type = NavType.LongType })
        ) {
            LocalDetalheScreen(
                onVoltar = { nav.popBackStack() },
                onEditarLocal = { id -> nav.navigate(Rotas.formLocal(id)) },
                onNovaSaida = { id -> nav.navigate(Rotas.formSaida(id)) },
                onEditarSaida = { localId, saidaId -> nav.navigate(Rotas.formSaida(localId, saidaId)) }
            )
        }

        composable(
            Rotas.FORM_SAIDA,
            arguments = listOf(
                navArgument("localId") { type = NavType.LongType },
                navArgument("saidaId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) {
            SaidaFormScreen(onVoltar = { nav.popBackStack() })
        }
    }
}
