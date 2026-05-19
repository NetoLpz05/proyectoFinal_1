package jesusernesto.lopezibarra.gestorgastos.data.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.BiometricHelper
import jesusernesto.lopezibarra.gestorgastos.data.Notifications.NotificationScheduler
import jesusernesto.lopezibarra.gestorgastos.data.Notifications.Notificationhelper
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.entity.UsuarioEntity
import jesusernesto.lopezibarra.gestorgastos.data.repository.AuthResult
import jesusernesto.lopezibarra.gestorgastos.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Cargando : AuthUiState()
    data class Exito(val usuario: UsuarioEntity) : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository by lazy {
        val dao = AppDatabase.getInstance(application).usuarioDao()
        UsuarioRepository(dao)
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, contrasena: String, context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando

            val result = repository.login(email, contrasena)

            _uiState.value = when (result) {
                is AuthResult.Exito -> {
                    SessionManager.usuarioActual = result.usuario

                    val prefs = getApplication<Application>()
                        .getSharedPreferences("alertas_config", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putInt("id_usuario_actual", result.usuario.idUsuario)
                        .apply()
                    Notificationhelper.crearCanal(getApplication())
                    NotificationScheduler.programar(getApplication())

                    if (result.usuario.biometriaActiva) {
                        BiometricHelper.saveCredentials(context, email, contrasena)
                    }


                    AuthUiState.Exito(result.usuario)

                }

                is AuthResult.Error -> AuthUiState.Error(result.mensaje)
            }
        }
    }

    fun registrar(
        nombre: String,
        apellido: String,
        email: String,
        contrasena: String,
        confirmacion: String,
        fechaNacimiento: String,
        genero: String,
        telefono: String = "",
        fotoPerfil: String? = null
    ) {
        if (contrasena != confirmacion) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando

            val result = repository.registrar(
                nombre = nombre,
                apellido = apellido,
                email = email,
                contrasena = contrasena,
                fechaNacimiento = fechaNacimiento,
                genero = genero,
                telefono = telefono,
                fotoPerfil = fotoPerfil
            )

            _uiState.value = when (result) {
                is AuthResult.Exito -> {
                    SessionManager.usuarioActual = result.usuario
                    AuthUiState.Exito(result.usuario)
                }
                is AuthResult.Error -> AuthUiState.Error(result.mensaje)
            }
        }
    }

    fun actualizarPerfil(
        nombre: String,
        apellido: String,
        telefono: String,
        fotoPerfil: String?
    ) {
        val usuario = SessionManager.usuarioActual ?: return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando

            val usuarioActualizado = usuario.copy(
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                fotoPerfil = fotoPerfil
            )

            val result = repository.actualizarPerfil(usuarioActualizado)

            _uiState.value = when (result) {
                is AuthResult.Exito -> {
                    SessionManager.usuarioActual = result.usuario
                    AuthUiState.Exito(result.usuario)
                }
                is AuthResult.Error -> AuthUiState.Error(result.mensaje)
            }
        }
    }


    fun loginConBiometria(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val credenciales = BiometricHelper.getCredentials(context) ?: run {
            onError("No hay credenciales guardadas")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            val result = repository.login(credenciales.first, credenciales.second)
            _uiState.value = when (result) {
                is AuthResult.Exito -> {
                    SessionManager.usuarioActual = result.usuario
                    val prefs = context.getSharedPreferences("alertas_config", Context.MODE_PRIVATE)
                    prefs.edit().putInt("id_usuario_actual", result.usuario.idUsuario).apply()
                    Notificationhelper.crearCanal(context)
                    NotificationScheduler.programar(context)
                    onSuccess()
                    AuthUiState.Exito(result.usuario)
                }
                is AuthResult.Error -> AuthUiState.Error(result.mensaje)
            }
        }
    }


    fun actualizarBiometria(activa: Boolean) {
        val usuario = SessionManager.usuarioActual ?: return
        viewModelScope.launch {
            repository.cambiarBiometria(usuario.idUsuario, activa)
            SessionManager.usuarioActual = usuario.copy(biometriaActiva = activa)
        }
    }

    fun cerrarSesion() {
        SessionManager.cerrarSesion()
        _uiState.value = AuthUiState.Idle
        NotificationScheduler.cancelar(getApplication())
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}