package jesusernesto.lopezibarra.gestorgastos.data.repository

import jesusernesto.lopezibarra.gestorgastos.data.FirestoreSyncService
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.dao.MovimientoDao
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.IngresoEntity
import kotlinx.coroutines.flow.Flow

/**
 * MovimientoRepository con sincronización Firestore.
 *
 * Patrón:
 *   1. Opera en Room (local, rápido, offline).
 *   2. Llama a FirestoreSyncService (fire-and-forget, no bloquea).
 *
 * El resto del código (ViewModels, pantallas) NO cambia.
 */
class MovimientoRepository(private val dao: MovimientoDao) {

    // Helper para obtener el UID de Firebase sin fallar si no hay sesión
    private val uid get() = SessionManager.firebaseUid

    // ─────────────────────────────────────────────────────────────
    // GASTOS
    // ─────────────────────────────────────────────────────────────

    suspend fun guardarGasto(gasto: GastoEntity) {
        val idInsertado = dao.insertGasto(gasto)                          // 1. Room
        val gastoConId = gasto.copy(idGasto = idInsertado.toInt())
        uid?.let { FirestoreSyncService.syncGasto(it, gastoConId) }      // 2. Firestore
    }

    suspend fun actualizarGasto(gasto: GastoEntity) {
        dao.updateGasto(gasto)                                            // 1. Room
        uid?.let { FirestoreSyncService.syncGasto(it, gasto) }           // 2. Firestore
    }

    suspend fun eliminarGasto(gasto: GastoEntity) {
        dao.deleteGasto(gasto)                                            // 1. Room
        uid?.let { FirestoreSyncService.deleteGasto(it, gasto.idGasto) } // 2. Firestore
    }

    // ─────────────────────────────────────────────────────────────
    // INGRESOS
    // ─────────────────────────────────────────────────────────────

    suspend fun guardarIngreso(ingreso: IngresoEntity) {
        val idInsertado = dao.insertIngreso(ingreso)                           // 1. Room
        val ingresoConId = ingreso.copy(idIngreso = idInsertado.toInt())
        uid?.let { FirestoreSyncService.syncIngreso(it, ingresoConId) }       // 2. Firestore
    }

    suspend fun actualizarIngreso(ingreso: IngresoEntity) {
        dao.updateIngreso(ingreso)                                             // 1. Room
        uid?.let { FirestoreSyncService.syncIngreso(it, ingreso) }            // 2. Firestore
    }

    suspend fun eliminarIngreso(ingreso: IngresoEntity) {
        dao.deleteIngreso(ingreso)                                                      // 1. Room
        uid?.let { FirestoreSyncService.deleteIngreso(it, ingreso.idIngreso) }         // 2. Firestore
    }

    // ─────────────────────────────────────────────────────────────
    // READS — siguen viniendo de Room (sin cambios)
    // ─────────────────────────────────────────────────────────────

    fun obtenerGastosPorUsuario(idUsuario: Int): Flow<List<GastoEntity>> =
        dao.getGastosPorUsuario(idUsuario)

    fun obtenerIngresosPorUsuario(idUsuario: Int): Flow<List<IngresoEntity>> =
        dao.getIngresosPorUsuario(idUsuario)

    suspend fun obtenerGastoPorId(id: Int) = dao.getGastoPorId(id)
    suspend fun obtenerIngresoPorId(id: Int) = dao.getIngresoPorId(id)
}
