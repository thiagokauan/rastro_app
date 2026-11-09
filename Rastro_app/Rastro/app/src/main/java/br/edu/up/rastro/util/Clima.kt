package br.edu.up.rastro.util

/**
 * Tradução dos códigos WMO usados pela Open-Meteo (campo weather_code).
 * Tabela oficial: https://open-meteo.com/en/docs (seção "WMO Weather interpretation codes").
 */
fun descricaoClima(codigo: Int?): String = when (codigo) {
    0 -> "Céu limpo"
    1 -> "Predomínio de sol"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Neblina"
    51, 53, 55 -> "Garoa"
    56, 57 -> "Garoa congelante"
    61 -> "Chuva fraca"
    63 -> "Chuva moderada"
    65 -> "Chuva forte"
    66, 67 -> "Chuva congelante"
    71, 73, 75, 77 -> "Neve"
    80, 81 -> "Pancadas de chuva"
    82 -> "Pancadas fortes"
    85, 86 -> "Pancadas de neve"
    95 -> "Trovoadas"
    96, 99 -> "Trovoadas com granizo"
    else -> "Sem informação"
}

fun emojiClima(codigo: Int?): String = when (codigo) {
    0 -> "☀️"
    1, 2 -> "🌤️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    in 51..57, 80, 81 -> "🌦️"
    in 61..67, 82 -> "🌧️"
    in 71..77, 85, 86 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "❔"
}
