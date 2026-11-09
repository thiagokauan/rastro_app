package br.edu.up.rastro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** CRUD da tabela locais. */
@Dao
interface LocalDao {

    // CREATE
    @Insert
    suspend fun inserir(local: Local): Long

    // READ: lista com a contagem de saídas (LEFT JOIN para incluir locais sem saídas)
    @Query(
        """
        SELECT locais.*, COUNT(saidas.id) AS totalSaidas
        FROM locais
        LEFT JOIN saidas ON saidas.localId = locais.id
        GROUP BY locais.id
        ORDER BY locais.nome COLLATE NOCASE
        """
    )
    fun listarComTotal(): Flow<List<LocalComTotal>>

    // READ: um local, observado (a tela atualiza sozinha quando ele muda)
    @Query("SELECT * FROM locais WHERE id = :id")
    fun observar(id: Long): Flow<Local?>

    // READ: um local, leitura única (usado para preencher o formulário de edição)
    @Query("SELECT * FROM locais WHERE id = :id")
    suspend fun obter(id: Long): Local?

    // UPDATE
    @Update
    suspend fun atualizar(local: Local)

    // DELETE
    @Delete
    suspend fun excluir(local: Local)
}
