package br.edu.up.rastro.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import br.edu.up.rastro.data.local.Local
import br.edu.up.rastro.data.local.Saida
import br.edu.up.rastro.data.local.TipoLocal
import java.util.Locale

/**
 * COMPARTILHAMENTO COM OUTROS APPS (Intents implícitas).
 * O app não escolhe o destino: o Android mostra os apps capazes de receber o conteúdo.
 */

/** Envia texto para qualquer app que aceite texto (WhatsApp, Gmail, Mensagens, Keep...). */
fun compartilharTexto(context: Context, assunto: String, texto: String) {
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, assunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(envio, "Compartilhar com"))
}

/** Abre o local no Google Maps (ou outro app de mapas); se não houver, tenta o navegador. */
fun abrirNoMapa(context: Context, local: Local) {
    val lat = local.latitude
    val lon = local.longitude
    val geo = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(local.nome)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, geo))
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkMapa(local))))
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(context, "Nenhum app de mapas ou navegador encontrado.", Toast.LENGTH_LONG).show()
        }
    }
}

fun linkMapa(local: Local): String =
    String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", local.latitude, local.longitude)

fun textoLocal(local: Local): String = buildString {
    appendLine("${local.tipo.rotulo}: ${local.nome}")
    appendLine(local.cidade)
    if (local.observacoes.isNotBlank()) appendLine(local.observacoes)
    appendLine("Mapa: ${linkMapa(local)}")
    appendLine()
    append("Registrado no Rastro")
}

fun textoSaida(local: Local, saida: Saida): String = buildString {
    val titulo = if (local.tipo == TipoLocal.TRILHA) "Trilha" else "Pescaria"
    appendLine("$titulo em ${local.nome} (${local.cidade})")
    appendLine("Data: ${formatarData(saida.data)}")
    appendLine("Duração: ${formatarDuracao(saida.duracaoMinutos)}")
    saida.distanciaKm?.let { appendLine("Distância: ${formatarKm(it)}") }
    saida.peixesCapturados?.let { appendLine("Peixes capturados: $it") }
    if (saida.companhia.isNotBlank()) appendLine("Companhia: ${saida.companhia}")
    if (saida.observacoes.isNotBlank()) appendLine("Observações: ${saida.observacoes}")
    appendLine("Mapa: ${linkMapa(local)}")
    appendLine()
    append("Registrado no Rastro")
}
