package jesusernesto.lopezibarra.gestorgastos.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import jesusernesto.lopezibarra.gestorgastos.data.dao.UsuarioDao
import jesusernesto.lopezibarra.gestorgastos.data.entity.UsuarioEntity
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Exito(val usuario: UsuarioEntity) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

/**
 * UsuarioRepository HÍBRIDO:
 *
 *  LOGIN   → Firebase Auth valida credenciales → busca UsuarioEntity en Room por firebaseUid
 *  REGISTRO → Firebase Auth crea cuenta → Room guarda perfil local → Firestore guarda perfil nube
 *  ACTUALIZAR PERFIL → Room actualiza local → Firestore actualiza nube en paralelo
 *  RECUPERAR CONTRASEÑA → Firebase manda email (no Room, Firebase lo maneja)
 *
 *  Las pantallas y ViewModels existentes NO cambian porque siguen recibiendo UsuarioEntity.
 */
class UsuarioRepository(private val dao: UsuarioDao) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────
    suspend fun login(email: String, contrasena: String): AuthResult {
        if (email.isBlank() || contrasena.isBlank()) {
            return AuthResult.Error("Completa todos los campos")
        }

        // 1. Firebase Auth valida las credenciales
        val firebaseResult = try {
            auth.signInWithEmailAndPassword(email.trim(), contrasena).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return AuthResult.Error("Correo o contraseña incorrectos")
        } catch (e: FirebaseAuthInvalidUserException) {
            return AuthResult.Error("No existe una cuenta con ese correo")
        } catch (e: Exception) {
            return AuthResult.Error("Error al iniciar sesión: ${e.localizedMessage}")
        }

        val firebaseUid = firebaseResult.user?.uid
            ?: return AuthResult.Error("Error desconocido al iniciar sesión")

        // Si está en Room, úsalo directo
        val usuarioRoom = dao.buscarPorFirebaseUid(firebaseUid)
            ?: dao.buscarPorEmail(email.trim().lowercase())
            ?: run {
                // No está en Room (reinstalación / nuevo dispositivo)
                // → busca el perfil en Firestore y guárdalo localmente
                val doc = firestore.collection("usuarios").document(firebaseUid).get().await()
                if (!doc.exists()) return AuthResult.Error("Perfil no encontrado. Intenta registrarte de nuevo.")

                val data = doc.data!!
                val perfilRestaurado = UsuarioEntity(
                    firebaseUid   = firebaseUid,
                    nombre        = data["nombre"] as? String ?: "",
                    apellido      = data["apellido"] as? String ?: "",
                    email         = data["email"] as? String ?: email.trim().lowercase(),
                    fechaNacimiento = data["fechaNacimiento"] as? String ?: "",
                    genero        = data["genero"] as? String ?: "",
                    telefono      = data["telefono"] as? String ?: "",
                    fotoPerfil    = data["fotoPerfil"] as? String,
                    biometriaActiva = data["biometriaActiva"] as? Boolean ?: false,
                    tema          = data["tema"] as? String ?: "CLARO"
                )
                val id = dao.insertar(perfilRestaurado)
                perfilRestaurado.copy(idUsuario = id.toInt())
            }

        return AuthResult.Exito(usuarioRoom)
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTRO
    // ─────────────────────────────────────────────────────────────
    suspend fun registrar(
        nombre: String,
        apellido: String,
        email: String,
        contrasena: String,
        fechaNacimiento: String,
        genero: String,
        telefono: String = "",
        fotoPerfil: String? = null
    ): AuthResult {
        // Validaciones básicas (igual que antes)
        if (nombre.isBlank() || apellido.isBlank()) {
            return AuthResult.Error("El nombre y apellido son obligatorios")
        }
        if (!email.contains("@") || !email.contains(".")) {
            return AuthResult.Error("Correo electrónico inválido")
        }
        if (contrasena.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        // 1. Crear usuario en Firebase Auth
        val firebaseResult = try {
            auth.createUserWithEmailAndPassword(email.trim(), contrasena).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            return AuthResult.Error("Este correo ya está registrado")
        } catch (e: FirebaseAuthWeakPasswordException) {
            return AuthResult.Error("La contraseña es muy débil, usa al menos 6 caracteres")
        } catch (e: Exception) {
            return AuthResult.Error("Error al crear la cuenta: ${e.localizedMessage}")
        }

        val firebaseUid = firebaseResult.user?.uid
            ?: return AuthResult.Error("Error al obtener UID de Firebase")

        // 2. Guardar perfil en Room (local) — incluye el firebaseUid
        return try {
            val nuevoUsuario = UsuarioEntity(
                firebaseUid = firebaseUid,
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim().lowercase(),
                contrasena = "",              // Firebase maneja la contraseña, no Room
                fechaNacimiento = fechaNacimiento,
                genero = genero,
                telefono = telefono.trim(),
                fotoPerfil = fotoPerfil
            )
            val id = dao.insertar(nuevoUsuario)
            val usuarioConId = nuevoUsuario.copy(idUsuario = id.toInt())

            // 3. Guardar perfil en Firestore (nube) — en paralelo, sin bloquear
            sincronizarPerfilFirestore(firebaseUid, usuarioConId)

            AuthResult.Exito(usuarioConId)

        } catch (e: Exception) {
            // Si Room falla (email duplicado local), borrar la cuenta de Firebase para no quedar inconsistente
            firebaseResult.user?.delete()
            AuthResult.Error("Error al guardar el perfil local: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // OBTENER USUARIO (igual que antes, sin cambios)
    // ─────────────────────────────────────────────────────────────
    suspend fun obtenerUsuario(id: Int): UsuarioEntity? {
        return dao.buscarPorId(id)
    }

    // ─────────────────────────────────────────────────────────────
    // ACTUALIZAR PERFIL
    // ─────────────────────────────────────────────────────────────
    suspend fun actualizarPerfil(usuario: UsuarioEntity): AuthResult {
        return try {
            // 1. Actualizar Room local
            dao.actualizar(usuario)

            // 2. Actualizar Firestore en paralelo (fire-and-forget)
            if (usuario.firebaseUid.isNotBlank()) {
                sincronizarPerfilFirestore(usuario.firebaseUid, usuario)
            }

            AuthResult.Exito(usuario)
        } catch (e: Exception) {
            AuthResult.Error("No se pudo actualizar el perfil")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CAMBIAR TEMA Y BIOMETRÍA (igual que antes)
    // ─────────────────────────────────────────────────────────────
    suspend fun cambiarTema(id: Int, tema: String) {
        dao.actualizarTema(id, tema)
        // Opcional: sincronizar en Firestore también
        val usuario = dao.buscarPorId(id)
        usuario?.let {
            if (it.firebaseUid.isNotBlank()) {
                firestore.collection("usuarios")
                    .document(it.firebaseUid)
                    .update("tema", tema)
            }
        }
    }

    suspend fun cambiarBiometria(id: Int, activa: Boolean) {
        dao.actualizarBiometria(id, activa)
    }

    // ─────────────────────────────────────────────────────────────
    // RECUPERAR CONTRASEÑA — Firebase envía el email, sin tocar Room
    // ─────────────────────────────────────────────────────────────
    suspend fun enviarEmailRecuperacion(email: String): AuthResult {
        if (email.isBlank() || !email.contains("@")) {
            return AuthResult.Error("Correo electrónico inválido")
        }
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            // Usamos un usuario "vacío" como señal de éxito
            AuthResult.Exito(
                UsuarioEntity(
                    nombre = "", apellido = "", email = email,
                    fechaNacimiento = "", genero = ""
                )
            )
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No existe una cuenta con ese correo")
        } catch (e: Exception) {
            AuthResult.Error("No se pudo enviar el correo: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER PRIVADO: sube el perfil a Firestore
    // ─────────────────────────────────────────────────────────────
    private fun sincronizarPerfilFirestore(uid: String, usuario: UsuarioEntity) {
        val datos = hashMapOf(
            "idUsuarioRoom" to usuario.idUsuario,
            "nombre" to usuario.nombre,
            "apellido" to usuario.apellido,
            "email" to usuario.email,
            "fechaNacimiento" to usuario.fechaNacimiento,
            "genero" to usuario.genero,
            "telefono" to usuario.telefono,
            "fotoPerfil" to usuario.fotoPerfil,
            "biometriaActiva" to usuario.biometriaActiva,
            "tema" to usuario.tema,
            "createdAt" to usuario.createdAt
        )
        // fire-and-forget: no bloqueamos la app si Firestore falla
        firestore.collection("usuarios")
            .document(uid)
            .set(datos)
    }
}
