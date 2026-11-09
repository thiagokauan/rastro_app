package br.edu.up.rastro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClimaCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(cache: ClimaCache)

    @Query("SELECT * FROM clima_cache WHERE localId = :localId")
    suspend fun obter(localId: Long): ClimaCache?

    @Query("DELETE FROM clima_cache WHERE localId = :localId")
    suspend fun excluir(localId: Long)
}
