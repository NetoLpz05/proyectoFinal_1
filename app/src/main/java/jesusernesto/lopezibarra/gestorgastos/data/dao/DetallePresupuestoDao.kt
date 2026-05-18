package jesusernesto.lopezibarra.gestorgastos.data.dao

import androidx.room.*
import jesusernesto.lopezibarra.gestorgastos.data.entity.DetallePresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetallePresupuestoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(detalle: DetallePresupuestoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(detalles: List<DetallePresupuestoEntity>): List<Long>

    @Update
    suspend fun actualizar(detalle: DetallePresupuestoEntity)

    @Delete
    suspend fun eliminar(detalle: DetallePresupuestoEntity)

    @Query("SELECT * FROM detalle_presupuesto WHERE idPresupuesto = :idPresupuesto")
    suspend fun obtenerPorPresupuesto(idPresupuesto: Int): List<DetallePresupuestoEntity>

    @Query("SELECT * FROM detalle_presupuesto WHERE idPresupuesto = :idPresupuesto")
    fun obtenerPorPresupuestoFlow(idPresupuesto: Int): Flow<List<DetallePresupuestoEntity>>

    @Query("DELETE FROM detalle_presupuesto WHERE idPresupuesto = :idPresupuesto")
    suspend fun eliminarPorPresupuesto(idPresupuesto: Int)
}
