package jesusernesto.lopezibarra.gestorgastos.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.repository.AuthResult
import jesusernesto.lopezibarra.gestorgastos.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val emailEnviado: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
)


class ForgotpasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository by lazy {
        val dao = AppDatabase.getInstance(application).usuarioDao()
        UsuarioRepository(dao)
    }

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun enviarEmailRecuperacion(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Correo electrónico inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val result = repository.enviarEmailRecuperacion(email)

            _uiState.update {
                when (result) {
                    is AuthResult.Exito -> it.copy(cargando = false, emailEnviado = true)
                    is AuthResult.Error -> it.copy(cargando = false, error = result.mensaje)
                }
            }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
