package jesusernesto.lopezibarra.gestorgastos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import jesusernesto.lopezibarra.gestorgastos.data.dao.*
import jesusernesto.lopezibarra.gestorgastos.data.entity.*
import jesusernesto.lopezibarra.gestorgastos.data.enums.TipoMetodoPago
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        CategoriaEntity::class,
        MetodoPagoEntity::class,
        GrupoEntity::class,
        GastoGrupoEntity::class,
        GastoEntity::class,
        IngresoEntity::class,
        PresupuestoEntity::class,
        DetallePresupuestoEntity::class,
        AlertaEntity::class,
        UsuarioGrupoEntity::class,
        DeudaGrupoEntity::class,
        GastoFijoEntity::class
    ], version = 6, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun grupoDao(): GrupoDao
    abstract fun alertaDao(): AlertaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun detallePresupuestoDao(): DetallePresupuestoDao
    abstract fun gastoFijoDao(): GastoFijoDao
    abstract fun gastoDao(): GastoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun metodoPagoDao(): MetodoPagoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestorgastos_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDatabase(database)
                    }
                }
            }

            private suspend fun prepopulateDatabase(db: AppDatabase) {
                val categoriaDao = db.categoriaDao()
                val metodoPagoDao = db.metodoPagoDao()

                if (categoriaDao.contarCategorias() == 0) {
                    categoriaDao.insertarTodas(
                        listOf(
                            CategoriaEntity(nombre = "🏠 Vivienda", predefinida = true),
                            CategoriaEntity(nombre = "🎬 Entretenimiento", predefinida = true),
                            CategoriaEntity(nombre = "🚗 Transporte", predefinida = true),
                            CategoriaEntity(nombre = "🍔 Alimentación", predefinida = true),
                            CategoriaEntity(nombre = "❤️ Salud", predefinida = true),
                            CategoriaEntity(nombre = "📦 Otros", predefinida = true)
                        )
                    )
                }

                if (metodoPagoDao.contarMP() == 0) {
                    metodoPagoDao.insertar(MetodoPagoEntity(tipo = TipoMetodoPago.EFECTIVO, nombre = "Efectivo"))
                    metodoPagoDao.insertar(MetodoPagoEntity(tipo = TipoMetodoPago.TARJETA_DEBITO, nombre = "Tarjeta de Prueba"))
                }
            }
        }
    }
}
