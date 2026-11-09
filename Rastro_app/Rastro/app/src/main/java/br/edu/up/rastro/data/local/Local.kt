package br.edu.up.rastro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tipo do local. O Room grava enums como texto ("TRILHA" ou "PESCA"). */
enum class TipoLocal(val rotulo: String, val plural: String) {
    TRILHA("Trilha", "Trilhas"),
    PESCA("Pesca", "Pescas")
}

/**
 * TABELA 1: locais.
 * Um local (trilha ou ponto de pesca) tem várias saídas (relação 1:N com a tabela saidas).
 */
@Entity(tableName = "locais")
data class Local(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val tipo: TipoLocal,
    val cidade: String,
    val latitude: Double,
    val longitude: Double,
    val observacoes: String
)
