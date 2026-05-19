package jesusernesto.lopezibarra.gestorgastos.data.repository

import jesusernesto.lopezibarra.gestorgastos.data.FirestoreSyncService
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.dao.MovimientoDao
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.IngresoEntity
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(private val dao: MovimientoDao) {

    // Helper para obtener el UID de Firebase sin fallar si no hay sesión
    private val uid get() = SessionManager.firebaseUid

    suspend fun guardarGasto(gasto: GastoEntity) {
        val idInsertado = dao.insertGasto(gasto)
        val gastoConId = gasto.copy(idGasto = idInsertado.toInt())

        val uidActual = SessionManager.firebaseUid
        android.util.Log.d("Firestore", "UID al guardar gasto: $uidActual")

        uidActual?.let { FirestoreSyncService.syncGasto(it, gastoConId) }
    }

    suspend fun actualizarGasto(gasto: GastoEntity) {
        dao.updateGasto(gasto)
        uid?.let { FirestoreSyncService.syncGasto(it, gasto) }
    }

    suspend fun eliminarGasto(gasto: GastoEntity) {
        dao.deleteGasto(gasto)
        uid?.let { FirestoreSyncService.deleteGasto(it, gasto.idGasto) }
    }

    suspend fun guardarIngreso(ingreso: IngresoEntity) {
        val idInsertado = dao.insertIngreso(ingreso)
        val ingresoConId = ingreso.copy(idIngreso = idInsertado.toInt())
        uid?.let { FirestoreSyncService.syncIngreso(it, ingresoConId) }
    }

    suspend fun actualizarIngreso(ingreso: IngresoEntity) {
        dao.updateIngreso(ingreso)
        uid?.let { FirestoreSyncService.syncIngreso(it, ingreso) }
    }

    suspend fun eliminarIngreso(ingreso: IngresoEntity) {
        dao.deleteIngreso(ingreso)
        uid?.let { FirestoreSyncService.deleteIngreso(it, ingreso.idIngreso) }
    }

    fun obtenerGastosPorUsuario(idUsuario: Int): Flow<List<GastoEntity>> =
        dao.getGastosPorUsuario(idUsuario)

    fun obtenerIngresosPorUsuario(idUsuario: Int): Flow<List<IngresoEntity>> =
        dao.getIngresosPorUsuario(idUsuario)

    suspend fun obtenerGastoPorId(id: Int) = dao.getGastoPorId(id)
    suspend fun obtenerIngresoPorId(id: Int) = dao.getIngresoPorId(id)
}
