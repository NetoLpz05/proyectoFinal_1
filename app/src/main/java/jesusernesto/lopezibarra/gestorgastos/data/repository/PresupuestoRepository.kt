package jesusernesto.lopezibarra.gestorgastos.data.repository

import jesusernesto.lopezibarra.gestorgastos.data.FirestoreSyncService
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.dao.*
import jesusernesto.lopezibarra.gestorgastos.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class PresupuestoRepository(
    private val presupuestoDao: PresupuestoDao,
    private val detallePresupuestoDao: DetallePresupuestoDao,
    private val gastoFijoDao: GastoFijoDao,
    private val categoriaDao: CategoriaDao
) {
    private val uid get() = SessionManager.firebaseUid

    fun obtenerPresupuestoCompleto(idUsuario: Int, mes: Int, anio: Int): Flow<PresupuestoConDetalles?> {
        return presupuestoDao.obtenerPresupuestoFlow(idUsuario, mes, anio).flatMapLatest { presupuesto ->
            if (presupuesto == null) return@flatMapLatest flowOf(null)
            detallePresupuestoDao.obtenerPorPresupuestoFlow(presupuesto.idPresupuesto).map { detalles ->
                PresupuestoConDetalles(presupuesto, detalles)
            }
        }
    }

    suspend fun guardarPresupuestoCompleto(
        idUsuario: Int,
        mes: Int,
        anio: Int,
        ingresoMensual: Double,
        gastosFijos: List<GastoFijoEntity>,
        detalles: List<DetallePresupuestoEntity>
    ) {
        val mesAnio = "${mes}_${anio}"

        val presupuestoExistente = presupuestoDao.obtenerPresupuesto(idUsuario, mes, anio)
        val idPresupuesto = if (presupuestoExistente != null) {
            val actualizado = presupuestoExistente.copy(ingresoMensual = ingresoMensual)
            presupuestoDao.actualizar(actualizado)
            uid?.let { FirestoreSyncService.syncPresupuesto(it, actualizado) }
            presupuestoExistente.idPresupuesto
        } else {
            val nuevo = PresupuestoEntity(
                idUsuario = idUsuario,
                mes = mes,
                anio = anio,
                ingresoMensual = ingresoMensual
            )
            val id = presupuestoDao.insertar(nuevo).toInt()
            val nuevoConId = nuevo.copy(idPresupuesto = id)
            uid?.let { FirestoreSyncService.syncPresupuesto(it, nuevoConId) }
            id
        }

        gastoFijoDao.eliminarPorPresupuesto(idPresupuesto)
        val gastosFijosConId = gastosFijos.map { it.copy(idPresupuesto = idPresupuesto) }
        gastoFijoDao.insertarTodos(gastosFijosConId)
        uid?.let { uidVal ->
            gastosFijosConId.forEach { gf ->
                FirestoreSyncService.syncGastoFijo(uidVal, mesAnio, gf)
            }
        }

        detallePresupuestoDao.eliminarPorPresupuesto(idPresupuesto)
        val detallesConId = detalles.map { it.copy(idPresupuesto = idPresupuesto) }
        detallePresupuestoDao.insertarTodos(detallesConId)
        uid?.let { uidVal ->
            detallesConId.forEach { det ->
                FirestoreSyncService.syncDetallePresupuesto(uidVal, mesAnio, det)
            }
        }
    }

    suspend fun obtenerGastosFijos(idPresupuesto: Int) =
        gastoFijoDao.obtenerPorPresupuesto(idPresupuesto)

    fun obtenerCategorias(): Flow<List<CategoriaEntity>> = categoriaDao.obtenerTodas()
}

data class PresupuestoConDetalles(
    val presupuesto: PresupuestoEntity,
    val detalles: List<DetallePresupuestoEntity>
)
