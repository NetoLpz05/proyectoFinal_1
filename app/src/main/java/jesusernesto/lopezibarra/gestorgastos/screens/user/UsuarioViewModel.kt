package jesusernesto.lopezibarra.gestorgastos.screens.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
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

    // UsuarioRepository ahora recibe el DAO de Room (igual que antes)
    // internamente también usa Firebase Auth y Firestore
    private val repository: UsuarioRepository by lazy {
        val dao = AppDatabase.getInstance(application).usuarioDao()
        UsuarioRepository(dao)
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // ─────────────────────────────────────────────────────────────
    // LOGIN  — Firebase autentica, Room devuelve el perfil local
    // ─────────────────────────────────────────────────────────────
    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando

            val result = repository.login(email, contrasena)

            _uiState.value = when (result) {
                is AuthResult.Exito -> {
                    SessionManager.usuarioActual = result.usuario

                    // Configuración de notificaciones (igual que antes)
                    val prefs = getApplication<Application>()
                        .getSharedPreferences("alertas_config", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putInt("id_usuario_actual", result.usuario.idUsuario)
                        .apply()
                    Notificationhelper.crearCanal(getApplication())
                    NotificationScheduler.programar(getApplication())

                    AuthUiState.Exito(result.usuario)
                }
                is AuthResult.Error -> AuthUiState.Error(result.mensaje)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTRO — Firebase crea cuenta, Room guarda perfil, Firestore sincroniza
    // ─────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────
    // ACTUALIZAR PERFIL — Room + Firestore en paralelo
    // ─────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────
    // BIOMETRÍA Y CIERRE DE SESIÓN (sin cambios)
    // ─────────────────────────────────────────────────────────────
    fun actualizarBiometria(activa: Boolean) {
        val usuario = SessionManager.usuarioActual ?: return
        viewModelScope.launch {
            repository.cambiarBiometria(usuario.idUsuario, activa)
            SessionManager.usuarioActual = usuario.copy(biometriaActiva = activa)
        }
    }

    fun cerrarSesion() {
        SessionManager.cerrarSesion()   // hace FirebaseAuth.signOut() + limpia sesión
        _uiState.value = AuthUiState.Idle
        NotificationScheduler.cancelar(getApplication())
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}