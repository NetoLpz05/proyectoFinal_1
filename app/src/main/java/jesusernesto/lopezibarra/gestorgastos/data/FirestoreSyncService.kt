package jesusernesto.lopezibarra.gestorgastos.data

import com.google.firebase.firestore.FirebaseFirestore
import jesusernesto.lopezibarra.gestorgastos.data.entity.*

/**
 * FirestoreSyncService
 *
 * Servicio centralizado de sincronización con Firestore.
 * Todos los repositorios lo llaman después de escribir en Room.
 *
 * ESTRUCTURA EN FIRESTORE:
 *
 *   usuarios/{firebaseUid}/
 *       gastos/{idGasto}/
 *       ingresos/{idIngreso}/
 *       presupuestos/{mes_anio}/
 *           detalles/{idDetalle}/
 *           gastosFijos/{idGastoFijo}/
 *       metodosPago/{idMetodoPago}/
 *       alertas/{idAlerta}/
 *
 *   grupos/{idGrupo}/
 *       gastos/{idGastoGrupo}/
 *       miembros/{idUsuario}/
 *       deudas/{idDeuda}/
 *
 * PATRÓN: fire-and-forget
 *   - Escribe primero en Room (fuente de verdad local, rápido, sin internet).
 *   - Luego llama a sync aquí (no se espera, no bloquea la UI).
 *   - Si hay error de red, Firestore lo reintenta automáticamente cuando vuelve la conexión.
 */
object FirestoreSyncService {

    private val db = FirebaseFirestore.getInstance()

    // ─────────────────────────────────────────────────────────────
    // GASTOS
    // ─────────────────────────────────────────────────────────────

    fun syncGasto(firebaseUid: String, gasto: GastoEntity) {
        val doc = mapOf(
            "idGastoRoom" to gasto.idGasto,
            "idUsuario" to gasto.idUsuario,
            "idCategoria" to gasto.idCategoria,
            "idMetodoPago" to gasto.idMetodoPago,
            "monto" to gasto.monto,
            "descripcion" to gasto.descripcion,
            "fecha" to gasto.fecha,
            "latitud" to gasto.latitud,
            "longitud" to gasto.longitud,
            "nombreUbicacion" to gasto.nombreUbicacion,
            "fotoRecibo" to gasto.fotoRecibo,
            "esGrupal" to gasto.esGrupal,
            "idGastoGrupo" to gasto.idGastoGrupo,
            "createdAt" to gasto.createdAt
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("gastos")
            .document(gasto.idGasto.toString())
            .set(doc)
        // fire-and-forget: no usamos await() para no bloquear
    }

    fun deleteGasto(firebaseUid: String, idGasto: Int) {
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("gastos")
            .document(idGasto.toString())
            .delete()
    }

    // ─────────────────────────────────────────────────────────────
    // INGRESOS
    // ─────────────────────────────────────────────────────────────

    fun syncIngreso(firebaseUid: String, ingreso: IngresoEntity) {
        val doc = mapOf(
            "idIngresoRoom" to ingreso.idIngreso,
            "idUsuario" to ingreso.idUsuario,
            "idCategoria" to ingreso.idCategoria,
            "idMetodoPago" to ingreso.idMetodoPago,
            "monto" to ingreso.monto,
            "descripcion" to ingreso.descripcion,
            "fecha" to ingreso.fecha,
            "latitud" to ingreso.latitud,
            "longitud" to ingreso.longitud,
            "nombreUbicacion" to ingreso.nombreUbicacion,
            "fotoRecibo" to ingreso.fotoRecibo,
            "createdAt" to ingreso.createdAt
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("ingresos")
            .document(ingreso.idIngreso.toString())
            .set(doc)
    }

    fun deleteIngreso(firebaseUid: String, idIngreso: Int) {
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("ingresos")
            .document(idIngreso.toString())
            .delete()
    }

    // ─────────────────────────────────────────────────────────────
    // PRESUPUESTOS
    // ─────────────────────────────────────────────────────────────

    fun syncPresupuesto(firebaseUid: String, presupuesto: PresupuestoEntity) {
        val docId = "${presupuesto.mes}_${presupuesto.anio}"
        val doc = mapOf(
            "idPresupuestoRoom" to presupuesto.idPresupuesto,
            "idUsuario" to presupuesto.idUsuario,
            "mes" to presupuesto.mes,
            "anio" to presupuesto.anio,
            "ingresoMensual" to presupuesto.ingresoMensual
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("presupuestos")
            .document(docId)
            .set(doc)
    }

    fun syncDetallePresupuesto(firebaseUid: String, meAnio: String, detalle: DetallePresupuestoEntity) {
        val doc = mapOf(
            "idDetalleRoom" to detalle.idDetallePresupuesto,
            "idPresupuesto" to detalle.idPresupuesto,
            "idCategoria" to detalle.idCategoria,
            "montoLimite" to detalle.montoLimite
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("presupuestos")
            .document(meAnio)
            .collection("detalles")
            .document(detalle.idDetallePresupuesto.toString())
            .set(doc)
    }

    fun syncGastoFijo(firebaseUid: String, mesAnio: String, gastoFijo: GastoFijoEntity) {
        val doc = mapOf(
            "idGastoFijoRoom" to gastoFijo.idGastoFijo,
            "idPresupuesto" to gastoFijo.idPresupuesto,
            "nombre" to gastoFijo.nombre,
            "monto" to gastoFijo.monto
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("presupuestos")
            .document(mesAnio)
            .collection("gastosFijos")
            .document(gastoFijo.idGastoFijo.toString())
            .set(doc)
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODOS DE PAGO
    // ─────────────────────────────────────────────────────────────

    fun syncMetodoPago(firebaseUid: String, metodo: MetodoPagoEntity) {
        val doc = mapOf(
            "idMetodoPagoRoom" to metodo.idMetodoPago,
            "tipo" to metodo.tipo?.name,
            "nombre" to metodo.nombre,
            "ultimosDigitos" to metodo.ultimosDigitos
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("metodosPago")
            .document(metodo.idMetodoPago.toString())
            .set(doc)
    }

    fun deleteMetodoPago(firebaseUid: String, idMetodoPago: Int) {
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("metodosPago")
            .document(idMetodoPago.toString())
            .delete()
    }

    // ─────────────────────────────────────────────────────────────
    // ALERTAS
    // ─────────────────────────────────────────────────────────────

    fun syncAlerta(firebaseUid: String, alerta: AlertaEntity) {
        val doc = mapOf(
            "idAlertaRoom" to alerta.idAlerta,
            "idUsuario" to alerta.idUsuario,
            "idPresupuesto" to alerta.idPresupuesto,
            "limiteAlerta" to alerta.limiteAlerta,
            "activa" to alerta.activa
        )
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("alertas")
            .document(alerta.idAlerta.toString())
            .set(doc)
    }

    fun deleteAlerta(firebaseUid: String, idAlerta: Int) {
        db.collection("usuarios")
            .document(firebaseUid)
            .collection("alertas")
            .document(idAlerta.toString())
            .delete()
    }

    // ─────────────────────────────────────────────────────────────
    // GRUPOS
    // ─────────────────────────────────────────────────────────────

    fun syncGrupo(grupo: GrupoEntity) {
        val doc = mapOf(
            "idGrupoRoom" to grupo.idGrupo,
            "nombre" to grupo.nombre,
            "tipo" to grupo.tipo,
            "codigoInvitacion" to grupo.codigoInvitacion,
            "imagen" to grupo.imagen
        )
        db.collection("grupos")
            .document(grupo.idGrupo.toString())
            .set(doc)
    }

    fun syncGastoGrupo(gastoGrupo: GastoGrupoEntity) {
        val doc = mapOf(
            "idGastoGrupoRoom" to gastoGrupo.idGastoGrupo,
            "idGrupo" to gastoGrupo.idGrupo,
            "idUsuarioPago" to gastoGrupo.idUsuarioPago,
            "idCategoria" to gastoGrupo.idCategoria,
            "nombre" to gastoGrupo.nombre,
            "monto" to gastoGrupo.monto,
            "fecha" to gastoGrupo.fecha
        )
        db.collection("grupos")
            .document(gastoGrupo.idGrupo.toString())
            .collection("gastos")
            .document(gastoGrupo.idGastoGrupo.toString())
            .set(doc)
    }

    fun syncDeudaGrupo(deuda: DeudaGrupoEntity) {
        val doc = mapOf(
            "idDeudaRoom" to deuda.idDeudaGrupo,
            "idGastoGrupo" to deuda.idGastoGrupo,
            "idUsuario" to deuda.idUsuario,
            "montoDeuda" to deuda.montoDeuda,
            "pagada" to deuda.pagado
        )
        db.collection("grupos")
            .document(deuda.idGastoGrupo.toString())   // ajusta si tienes idGrupo disponible
            .collection("deudas")
            .document(deuda.idDeudaGrupo.toString())
            .set(doc)
    }
}
