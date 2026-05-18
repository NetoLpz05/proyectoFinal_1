package jesusernesto.lopezibarra.gestorgastos.data.repository

import jesusernesto.lopezibarra.gestorgastos.data.FirestoreSyncService
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.dao.MetodoPagoDao
import jesusernesto.lopezibarra.gestorgastos.data.entity.MetodoPagoEntity
import kotlinx.coroutines.flow.Flow

class MetodoPagoRepository(private val dao: MetodoPagoDao) {

    private val uid get() = SessionManager.firebaseUid

    fun obtenerMetodosPagoUsuario(idUsuario: Int): Flow<List<MetodoPagoEntity>> {
        return dao.obtenerPorUsuario(idUsuario)
    }

    suspend fun insertar(metodo: MetodoPagoEntity): MetodoPagoEntity {
        val id = dao.insertar(metodo)
        val metodoConId = metodo.copy(idMetodoPago = id.toInt())
        uid?.let { FirestoreSyncService.syncMetodoPago(it, metodoConId) }   // Firestore
        return metodoConId
    }

    suspend fun eliminar(metodo: MetodoPagoEntity) {
        dao.eliminar(metodo)
        uid?.let { FirestoreSyncService.deleteMetodoPago(it, metodo.idMetodoPago) }  // Firestore
    }
}
