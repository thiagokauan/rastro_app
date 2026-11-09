package br.edu.up.rastro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** CRUD da tabela saidas. */
@Dao
interface SaidaDao {

    // CREATE
    @Insert
    suspend fun inserir(saida: Saida): Long

    // READ: saídas de um local, da mais recente para a mais antiga
    @Query("SELECT * FROM saidas WHERE localId = :localId ORDER BY data DESC, id DESC")
    fun listarPorLocal(localId: Long): Flow<List<Saida>>

    // READ: uma saída
    @Query("SELECT * FROM saidas WHERE id = :id")
    suspend fun obter(id: Long): Saida?

    // UPDATE
    @Update
    suspend fun atualizar(saida: Saida)

    // DELETE
    @Delete
    suspend fun excluir(saida: Saida)
}
