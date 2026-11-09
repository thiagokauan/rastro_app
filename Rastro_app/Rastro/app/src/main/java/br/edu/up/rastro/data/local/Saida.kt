package br.edu.up.rastro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * TABELA 2: saidas.
 * Cada saída pertence a um local (chave estrangeira localId).
 * onDelete = CASCADE: ao excluir um local, as saídas dele são excluídas junto.
 */
@Entity(
    tableName = "saidas",
    foreignKeys = [
        ForeignKey(
            entity = Local::class,
            parentColumns = ["id"],
            childColumns = ["localId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("localId")]
)
data class Saida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localId: Long,
    val data: Long,                 // data da saída em milissegundos (meia-noite UTC)
    val duracaoMinutos: Int,
    val companhia: String,
    val distanciaKm: Double?,       // usado quando o local é trilha
    val peixesCapturados: String?,     // usado quando o local é pesca
    val observacoes: String
)
