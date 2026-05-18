package jesusernesto.lopezibarra.gestorgastos.data.dao


import androidx.room.*
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoFijoEntity

@Dao
interface GastoFijoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoFijoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(gastos: List<GastoFijoEntity>): List<Long>

    @Update
    suspend fun actualizar(gasto: GastoFijoEntity)

    @Delete
    suspend fun eliminar(gasto: GastoFijoEntity)

    @Query("""
        SELECT * FROM gasto_fijo
        WHERE idPresupuesto = :idPresupuesto
    """)
    suspend fun obtenerPorPresupuesto(
        idPresupuesto: Int
    ): List<GastoFijoEntity>

    @Query("""
        DELETE FROM gasto_fijo
        WHERE idPresupuesto = :idPresupuesto
    """)
    suspend fun eliminarPorPresupuesto(
        idPresupuesto: Int
    )
}
