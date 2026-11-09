package br.edu.up.rastro

import android.app.Application
import br.edu.up.rastro.data.RastroRepository
import br.edu.up.rastro.data.local.AppDatabase
import br.edu.up.rastro.data.remote.RetrofitClient

/**
 * Classe Application: criada uma vez quando o app abre.
 * Guarda o banco e o repositório para todas as telas usarem a mesma instância.
 */
class RastroApp : Application() {

    private val database by lazy { AppDatabase.obter(this) }

    val repository by lazy {
        RastroRepository(
            localDao = database.localDao(),
            saidaDao = database.saidaDao(),
            climaCacheDao = database.climaCacheDao(),
            api = RetrofitClient.api
        )
    }
}
