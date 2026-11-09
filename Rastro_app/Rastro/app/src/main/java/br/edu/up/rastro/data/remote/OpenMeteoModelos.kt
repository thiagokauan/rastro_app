package br.edu.up.rastro.data.remote

import com.google.gson.annotations.SerializedName

// ---- Resposta de https://api.open-meteo.com/v1/forecast ----

data class PrevisaoResposta(
    val current: ClimaAtual?,
    val daily: ClimaDiario?
)

data class ClimaAtual(
    val time: String,
    @SerializedName("temperature_2m") val temperatura: Double?,
    @SerializedName("weather_code") val codigoClima: Int?,
    @SerializedName("wind_speed_10m") val ventoKmh: Double?
)

data class ClimaDiario(
    val time: List<String>,
    @SerializedName("weather_code") val codigosClima: List<Int?>,
    @SerializedName("temperature_2m_max") val temperaturaMax: List<Double?>,
    @SerializedName("temperature_2m_min") val temperaturaMin: List<Double?>,
    @SerializedName("precipitation_probability_max") val chanceChuva: List<Int?>?
)

// ---- Resposta de https://geocoding-api.open-meteo.com/v1/search ----

data class GeocodingResposta(
    val results: List<Cidade>?   // vem ausente quando nada é encontrado
)

data class Cidade(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?          // estado
) {
    fun descricao(): String = listOfNotNull(name, admin1, country).distinct().joinToString(", ")
}
