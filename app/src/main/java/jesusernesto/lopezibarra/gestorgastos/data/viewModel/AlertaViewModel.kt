package jesusernesto.lopezibarra.gestorgastos.data.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.entity.AlertaEntity
import jesusernesto.lopezibarra.gestorgastos.data.repository.AlertaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlertaUiState(
    val alertas: List<AlertaEntity> = emptyList(),
    val umbralGlobal: Float = 0.8f,
    val alertasHabilitadas: Boolean = true,
    val guardado: Boolean = false,
    val error: String? = null
)

class AlertaViewModel (application: Application) : AndroidViewModel(application){

    private val repository: AlertaRepository by lazy {
        AlertaRepository(AppDatabase.getInstance(application).alertaDao())
    }

    private val prefs = application.getSharedPreferences("alertas_config", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        AlertaUiState(
            umbralGlobal = prefs.getFloat("umbral", 0.8f),
            alertasHabilitadas = prefs.getBoolean("habilitadas", true)
        )
    )
    val uiState: StateFlow<AlertaUiState> = _uiState

    init {
        cargarAlertas()
    }

    private fun cargarAlertas(){
        val idUsuario = SessionManager.usuarioActual?.idUsuario ?: return
        viewModelScope.launch {
            repository.obtenerAlertas(idUsuario).collect { lista ->
                _uiState.update { it.copy(alertas = lista) }
            }
        }
    }

    fun setUmbralGlobal(valor: Float) =
        _uiState.update { it.copy(umbralGlobal = valor) }

    fun setAlertasHabilitadas(valor: Boolean){
        _uiState.update { it.copy(alertasHabilitadas = valor) }
        viewModelScope.launch {
            _uiState.value.alertas.forEach { alerta ->
                repository.toggleAlerta(alerta.idAlerta, valor)
            }
        }
    }

    fun guardarConfiguracion(umbral: Float, alertasHabilitadas: Boolean) {
        prefs.edit()
            .putFloat("umbral", umbral)
            .putBoolean("habilitadas", alertasHabilitadas)
            .apply()

        val idUsuario = SessionManager.usuarioActual?.idUsuario
        if (idUsuario != null) {
            viewModelScope.launch {
                _uiState.value.alertas.forEach { alerta ->
                    repository.toggleAlerta(alerta.idAlerta, alertasHabilitadas)
                    repository.actualizarLimite(alerta.idAlerta, umbral.toDouble())
                }
            }
        }

        _uiState.update {
            it.copy(
                umbralGlobal = umbral,
                alertasHabilitadas = alertasHabilitadas,
                guardado = true
            )
        }
    }


    fun toggleAlerta(alerta: AlertaEntity){
        viewModelScope.launch {
            repository.toggleAlerta(alerta.idAlerta, !alerta.activa)
        }
    }

    fun actualizarLimite(idAlerta: Int, nuevoLimite: Float){
        viewModelScope.launch {
            repository.actualizarLimite(idAlerta, nuevoLimite.toDouble())
        }
    }

    fun crearAlertaParaPresupuesto(idPresupuesto: Int, limite: Float){
        val idUsuario = SessionManager.usuarioActual?.idUsuario ?: return
        viewModelScope.launch {
            repository.crearAlerta(idUsuario, idPresupuesto, limite.toDouble())
            _uiState.update { it.copy(guardado = true) }
        }
    }

    fun eliminarAlerta(alerta: AlertaEntity){
        viewModelScope.launch { repository.eliminarAlerta(alerta) }
    }

    fun resetGuardado() = _uiState.update { it.copy(guardado = false) }

}
