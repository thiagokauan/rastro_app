package br.edu.up.rastro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Configuração do banco Room. Existe uma única instância (singleton) no app inteiro.
 */
@Database(
    entities = [Local::class, Saida::class, ClimaCache::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun localDao(): LocalDao
    abstract fun saidaDao(): SaidaDao
    abstract fun climaCacheDao(): ClimaCacheDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obter(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rastro.db"
                ).fallbackToDestructiveMigration().build().also { instancia = it }
            }
    }
}
