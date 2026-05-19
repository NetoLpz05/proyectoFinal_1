package jesusernesto.lopezibarra.gestorgastos.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.entity.DeudaGrupoEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoGrupoEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.GrupoEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.UsuarioEntity
import jesusernesto.lopezibarra.gestorgastos.data.repository.GrupoRepository
import jesusernesto.lopezibarra.gestorgastos.data.repository.GrupoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GrupoUiState(
    val grupos: List<GrupoEntity> = emptyList(),
    val miembros: List<UsuarioEntity> = emptyList(),
    val gastosGrupo: List<GastoGrupoEntity> = emptyList(),
    val deudasPendientes: List<DeudaGrupoEntity> = emptyList(),
    val totalGastos: Float = 0f,
    val cargando: Boolean = false,
    val error: String? = null,
    val grupoCreado: GrupoEntity? = null,
    val codigoBuscado: GrupoEntity? = null
)

class GrupoViewModel (application: Application) : AndroidViewModel(application){

    private val repository: GrupoRepository by lazy{
        val db = AppDatabase.getInstance(application)
        GrupoRepository(db.grupoDao(), db.movimientoDao())
    }

    private val _uiState = MutableStateFlow(GrupoUiState())
    val uiState: StateFlow<GrupoUiState> = _uiState

    init{
        cargarGrupos()
        cargarDeudasPendientes()
    }

    private fun cargarGrupos(){
        viewModelScope.launch {
            repository.obtenerGrupos().collect { lista ->
            _uiState.update { it.copy(grupos = lista) }}
        }
    }

    private fun cargarDeudasPendientes(){
        val idUsuario = SessionManager.usuarioActual?.idUsuario ?: return
        viewModelScope.launch {
            repository.obtenerDeudasPendientes(idUsuario).collect { deudas ->
                _uiState.update { it.copy(deudasPendientes = deudas) }
            }
        }
    }

    fun seleccionarGrupo(idGrupo: Int){
        viewModelScope.launch {
            repository.obtenerMiembros(idGrupo).collect { miembros ->
                _uiState.update { it.copy(miembros = miembros) }
            }
        }
        viewModelScope.launch {
            repository.obtenerGastosGrupo(idGrupo).collect { gastos ->
                _uiState.update { it.copy(gastosGrupo = gastos) }
            }
        }
        viewModelScope.launch {
            val total = repository.totalGastos(idGrupo)
            _uiState.update { it.copy(totalGastos = total) }
        }
    }

    fun crearGrupo(nombre: String, tipo: String){
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val result = repository.crearGrupo(nombre, tipo)){
                is GrupoResult.Exito -> {
                    SessionManager.usuarioActual?.idUsuario?.let { idUsuario ->
                        repository.agregarMiembro(result.grupo.idGrupo, idUsuario)
                    }
                    _uiState.update { it.copy(cargando = false, grupoCreado = result.grupo) }
                }
                is GrupoResult.Error -> _uiState.update {
                    it.copy(cargando = false, error = result.mensaje)
                }
            }
        }
    }

    fun eliminarGrupo(grupo: GrupoEntity){
        viewModelScope.launch { repository.eliminarGrupo(grupo) }
    }

    fun buscarPorCodigo(codigo: String){
        viewModelScope.launch {
            val grupo = repository.buscarPorCodigo(codigo)
            _uiState.update {
                it.copy(
                    codigoBuscado = grupo,
                    error = if (grupo == null) "Código no encontrado" else null
                )
            }
            if (grupo != null) {
                SessionManager.usuarioActual?.idUsuario?.let { idUsuario ->
                    repository.agregarMiembro(grupo.idGrupo, idUsuario)
                }
            }
        }

    }

    fun agregarGastoGrupo(
        idGrupo: Int,
        idUsuarioPago: Int,
        idCategoria: Int,
        idMetodoPago: Int,
        nombre: String,
        monto: Double,
        fecha: Long,
        participantes: List<Int>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val result = repository.agregarGastoGrupo(
                idGrupo, idUsuarioPago, idCategoria, idMetodoPago, nombre, monto, fecha, participantes
            )
            when (result) {
                is GrupoResult.Exito -> {
                    _uiState.update { it.copy(cargando = false) }
                    seleccionarGrupo(idGrupo) // Recargar datos del grupo
                }
                is GrupoResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = result.mensaje) }
                }
            }
        }
    }

    fun marcarDeudaPagada(idDeuda: Int, idGrupo: Int){
        viewModelScope.launch {
            repository.marcarPagada(idDeuda)
            seleccionarGrupo(idGrupo)
        }
    }

    fun resetGrupoCreado() = _uiState.update { it.copy(grupoCreado = null) }
    fun resetError() = _uiState.update { it.copy(error = null) }
}