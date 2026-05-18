package jesusernesto.lopezibarra.gestorgastos.data.repository

import jesusernesto.lopezibarra.gestorgastos.data.FirestoreSyncService
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.dao.AlertaDao
import jesusernesto.lopezibarra.gestorgastos.data.entity.AlertaEntity
import kotlinx.coroutines.flow.Flow

class AlertaRepository(private val dao: AlertaDao) {

    private val uid get() = SessionManager.firebaseUid

    fun obtenerAlertas(idUsuario: Int): Flow<List<AlertaEntity>> =
        dao.obtenerPorUsuario(idUsuario)

    fun obtenerAlertasActivas(idUsuario: Int): Flow<List<AlertaEntity>> =
        dao.obtenerActivas(idUsuario)

    suspend fun crearAlerta(idUsuario: Int, idPresupuesto: Int, limiteAlerta: Double): AlertaEntity {
        val alerta = AlertaEntity(
            idUsuario = idUsuario,
            idPresupuesto = idPresupuesto,
            limiteAlerta = limiteAlerta,
            activa = true
        )
        val id = dao.insertar(alerta)
        val alertaConId = alerta.copy(idAlerta = id.toInt())
        uid?.let { FirestoreSyncService.syncAlerta(it, alertaConId) }    // Firestore
        return alertaConId
    }

    suspend fun toggleAlerta(id: Int, activa: Boolean) {
        dao.toggleActiva(id, activa)
        // Opcional: obtener y sincronizar el objeto completo o solo el campo en Firestore
    }

    suspend fun actualizarLimite(id: Int, limite: Double) {
        dao.actualizarLimite(id, limite)
    }

    suspend fun eliminarAlerta(alerta: AlertaEntity) {
        dao.eliminar(alerta)
        uid?.let { FirestoreSyncService.deleteAlerta(it, alerta.idAlerta) }  // Firestore
    }
}
