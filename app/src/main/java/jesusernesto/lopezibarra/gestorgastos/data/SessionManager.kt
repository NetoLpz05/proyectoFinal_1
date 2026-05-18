package jesusernesto.lopezibarra.gestorgastos.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import jesusernesto.lopezibarra.gestorgastos.data.entity.UsuarioEntity

/**
 * SessionManager híbrido:
 *   - usuarioActual: sigue siendo UsuarioEntity (Room) para que todo el resto de la app
 *     funcione exactamente igual (mismo idUsuario Int en todas las pantallas).
 *   - firebaseUid:   UID de Firebase, usado como clave en Firestore.
 *
 * Sin cambios en las pantallas ni ViewModels existentes.
 */
object SessionManager {
    //var usuarioActual: UsuarioEntity? = null
    var usuarioActual by mutableStateOf<UsuarioEntity?>(null)
    val estaLogueado get() = usuarioActual != null

    /** UID de Firebase Auth. Se llena al hacer login/registro exitoso. */
    val firebaseUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun cerrarSesion() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        usuarioActual = null
    }
}