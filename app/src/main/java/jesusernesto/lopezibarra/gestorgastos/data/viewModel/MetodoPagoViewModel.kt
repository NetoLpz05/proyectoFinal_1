package jesusernesto.lopezibarra.gestorgastos.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.entity.MetodoPagoEntity
import jesusernesto.lopezibarra.gestorgastos.data.enums.TipoMetodoPago
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetodoPagoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.Companion.getInstance(application).metodoPagoDao()
    private val idUsuario = SessionManager.usuarioActual?.idUsuario ?: 0

    val metodosPago: StateFlow<List<MetodoPagoEntity>> = dao.obtenerPorUsuario(idUsuario)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardarTarjeta(
        nombre: String,
        numeroCompleto: String,
        esCredito: Boolean,
        onSuccess: () -> Unit) {

        viewModelScope.launch {
            val ultimos = numeroCompleto.takeLast(4).toIntOrNull()
            val nuevo = MetodoPagoEntity(
                idUsuario = idUsuario,
                nombre = nombre,
                tipo = if (esCredito) TipoMetodoPago.TARJETA_CREDITO else TipoMetodoPago.TARJETA_DEBITO,
                ultimosDigitos = ultimos
            )
            dao.insertar(nuevo)
            onSuccess()
        }
    }
}