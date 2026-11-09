package br.edu.up.rastro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Tabela auxiliar: guarda a última previsão recebida da API para cada local.
 * Se a internet falhar, o app mostra a previsão salva em vez de uma tela de erro.
 */
@Entity(
    tableName = "clima_cache",
    foreignKeys = [
        ForeignKey(
            entity = Local::class,
            parentColumns = ["id"],
            childColumns = ["localId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ClimaCache(
    @PrimaryKey val localId: Long,
    val json: String,
    val atualizadoEm: Long
)
