package br.edu.up.rastro.data

import br.edu.up.rastro.data.local.ClimaCache
import br.edu.up.rastro.data.local.ClimaCacheDao
import br.edu.up.rastro.data.local.Local
import br.edu.up.rastro.data.local.LocalComTotal
import br.edu.up.rastro.data.local.LocalDao
import br.edu.up.rastro.data.local.Saida
import br.edu.up.rastro.data.local.SaidaDao
import br.edu.up.rastro.data.remote.Cidade
import br.edu.up.rastro.data.remote.OpenMeteoApi
import br.edu.up.rastro.data.remote.PrevisaoResposta
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/** Previsão entregue à tela, indicando se veio da internet ou do cache. */
data class ClimaResultado(
    val previsao: PrevisaoResposta,
    val doCache: Boolean,
    val atualizadoEm: Long
)

/**
 * Ponto único de acesso aos dados. Os ViewModels não sabem se um dado vem do
 * Room ou da API: eles só conversam com o repositório.
 */
class RastroRepository(
    private val localDao: LocalDao,
    private val saidaDao: SaidaDao,
    private val climaCacheDao: ClimaCacheDao,
    private val api: OpenMeteoApi,
    private val gson: Gson = Gson()
) {

    // ---------- Locais ----------

    fun locaisComTotal(): Flow<List<LocalComTotal>> = localDao.listarComTotal()

    fun observarLocal(id: Long): Flow<Local?> = localDao.observar(id)

    suspend fun obterLocal(id: Long): Local? = localDao.obter(id)

    /** Insere se o id for 0; caso contrário atualiza. */
    suspend fun salvarLocal(local: Local) {
        if (local.id == 0L) {
            localDao.inserir(local)
        } else {
            val anterior = localDao.obter(local.id)
            localDao.atualizar(local)
            if (anterior == null || anterior.latitude != local.latitude || anterior.longitude != local.longitude) {
                climaCacheDao.excluir(local.id)
            }
        }
    }

    suspend fun excluirLocal(local: Local) = localDao.excluir(local)

    // ---------- Saídas ----------

    fun saidasDoLocal(localId: Long): Flow<List<Saida>> = saidaDao.listarPorLocal(localId)

    suspend fun obterSaida(id: Long): Saida? = saidaDao.obter(id)

    suspend fun salvarSaida(saida: Saida) {
        if (saida.id == 0L) saidaDao.inserir(saida) else saidaDao.atualizar(saida)
    }

    suspend fun excluirSaida(saida: Saida) = saidaDao.excluir(saida)

    // ---------- API Open-Meteo ----------

    suspend fun buscarCidades(nome: String): List<Cidade> =
        api.buscarCidade(nome = nome, quantidade = 6, idioma = "pt", formato = "json").results.orEmpty()

    /**
     * Busca a previsão na API e salva no cache. Se a rede falhar, devolve a
     * última previsão salva; se não houver nenhuma, repassa o erro.
     */
    suspend fun previsao(localId: Long, latitude: Double, longitude: Double): ClimaResultado {
        return try {
            val resposta = api.previsao(
                latitude = latitude,
                longitude = longitude,
                atual = "temperature_2m,weather_code,wind_speed_10m",
                diario = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
                fusoHorario = "auto",
                dias = 7
            )
            val agora = System.currentTimeMillis()
            climaCacheDao.salvar(ClimaCache(localId, gson.toJson(resposta), agora))
            ClimaResultado(resposta, doCache = false, atualizadoEm = agora)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val cache = climaCacheDao.obter(localId) ?: throw e
            ClimaResultado(
                previsao = gson.fromJson(cache.json, PrevisaoResposta::class.java),
                doCache = true,
                atualizadoEm = cache.atualizadoEm
            )
        }
    }
}
