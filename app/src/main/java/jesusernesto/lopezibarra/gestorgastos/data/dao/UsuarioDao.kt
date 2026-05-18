package jesusernesto.lopezibarra.gestorgastos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import jesusernesto.lopezibarra.gestorgastos.data.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE firebaseUid = :uid LIMIT 1")
    suspend fun buscarPorFirebaseUid(uid: String): UsuarioEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE idUsuario = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("UPDATE usuario SET contrasena = :contrasena WHERE email = :email")
    suspend fun actualizarContrasena(email: String, contrasena: String)

    @Query("UPDATE usuario SET tema = :tema WHERE idUsuario = :id")
    suspend fun actualizarTema(id: Int, tema: String)

    @Query("UPDATE usuario SET biometriaActiva = :activa WHERE idUsuario = :id")
    suspend fun actualizarBiometria(id: Int, activa: Boolean)

    @Query("DELETE FROM usuario WHERE idUsuario = :id")
    suspend fun eliminar(id: Int)
}