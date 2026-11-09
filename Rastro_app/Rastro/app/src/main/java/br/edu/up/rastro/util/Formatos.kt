package br.edu.up.rastro.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val PT_BR: Locale = Locale("pt", "BR")

/** Hoje, à meia-noite UTC: mesmo formato que o DatePicker do Material 3 devolve. */
fun hojeUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** Datas das saídas são guardadas em UTC; formatar em UTC evita mostrar o dia anterior. */
fun formatarData(millisUtc: Long): String =
    SimpleDateFormat("dd/MM/yyyy", PT_BR).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millisUtc))

fun formatarDataHora(millis: Long): String =
    SimpleDateFormat("dd/MM 'às' HH:mm", PT_BR).format(Date(millis))

fun formatarDuracao(minutos: Int): String {
    val h = minutos / 60
    val m = minutos % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "${h}h"
        else -> "${h}h${m.toString().padStart(2, '0')}"
    }
}

fun formatarKm(km: Double): String = String.format(PT_BR, "%.1f km", km)

/** "2026-10-22" -> "qui 22/10" */
fun formatarDiaPrevisao(dataIso: String): String = try {
    LocalDate.parse(dataIso)
        .format(DateTimeFormatter.ofPattern("EEE dd/MM", PT_BR))
        .replace(".", "")
} catch (e: Exception) {
    dataIso
}

fun formatarCoordenadas(lat: Double, lon: Double): String =
    String.format(Locale.US, "%.4f, %.4f", lat, lon)
