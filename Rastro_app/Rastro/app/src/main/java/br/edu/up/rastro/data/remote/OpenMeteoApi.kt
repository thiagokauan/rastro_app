package br.edu.up.rastro.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoints da Open-Meteo usados pelo app. A API é gratuita e não exige chave.
 * O Retrofit gera a implementação desta interface em tempo de execução.
 */
interface OpenMeteoApi {

    /** Previsão do tempo para uma coordenada. */
    @GET("v1/forecast")
    suspend fun previsao(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") atual: String,
        @Query("daily") diario: String,
        @Query("timezone") fusoHorario: String,
        @Query("forecast_days") dias: Int
    ): PrevisaoResposta

    /** Busca de cidades pelo nome (devolve latitude e longitude). URL absoluta: outro host. */
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun buscarCidade(
        @Query("name") nome: String,
        @Query("count") quantidade: Int,
        @Query("language") idioma: String,
        @Query("format") formato: String
    ): GeocodingResposta
}

object RetrofitClient {
    val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}
