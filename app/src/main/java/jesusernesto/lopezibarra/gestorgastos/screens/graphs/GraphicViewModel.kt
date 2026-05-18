package jesusernesto.lopezibarra.gestorgastos.screens.graphs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jesusernesto.lopezibarra.gestorgastos.data.AppDatabase
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.entity.CategoriaEntity
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoEntity
import jesusernesto.lopezibarra.gestorgastos.data.enums.PeriodoGrafica

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class PresupuestoGraficaUi(
    val categoria: String,
    val emoji: String,
    val asignado: Double,
    val gastado: Double
){
    val restante get() = asignado - gastado
    val porcentaje get() = if (asignado > 0) (gastado / asignado * 100).toInt().coerceIn(0, 100) else 0
    val excedido get() = gastado > asignado
}

data class CategoriaGraficaUi(
    val emoji: String,
    val nombre: String,
    val totalGastado: Double
)

data class ResumenPeriodoUi(
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
){
    val balance get() = totalIngresos - totalGastos
}

data class GraphicUiState(
    val presupuestos: List<PresupuestoGraficaUi> = emptyList(),
    val categorias: List<CategoriaGraficaUi> = emptyList(),
    val resumen: ResumenPeriodoUi = ResumenPeriodoUi(),
    val cargando: Boolean = true
)

class GraphicViewModel (application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    private val _periodo = MutableStateFlow(PeriodoGrafica.DIA)
    val periodo: StateFlow<PeriodoGrafica> = _periodo

    private val _fechaSeleccionada = MutableStateFlow(hoyUtcMillis())
    val fechaSeleccionada: StateFlow<Long> = _fechaSeleccionada

    private val _uiState = MutableStateFlow(GraphicUiState())
    val uiState: StateFlow<GraphicUiState> = _uiState

    init {
        observarDatos()
    }

    fun setPeriodo(p: PeriodoGrafica) {
        _periodo.value = p
    }

    fun setFecha(millis: Long) {
        _fechaSeleccionada.value = millis
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(_periodo, _fechaSeleccionada) { p, f -> Pair(p, f) }
                .collect { (periodo, fechaMs) ->
                    val idUsuario = SessionManager.usuarioActual?.idUsuario ?: return@collect
                    val (desde, hasta) = rangoParaPeriodo(periodo, fechaMs)

                    val gastos = db.gastoDao()
                        .obtenerPorUsuario(idUsuario)
                        .map { lista -> lista.filter { it.fecha in desde..hasta } }
                        .firstOrNull() ?: emptyList()

                    val categorias = db.categoriaDao().obtenerTodas().firstOrNull() ?: emptyList()

                    val cal = Calendar.getInstance().apply { timeInMillis = fechaMs }
                    val mes = cal.get(Calendar.MONTH) + 1
                    val anio = cal.get(Calendar.YEAR)

                    val presupuesto = db.presupuestoDao().obtenerPresupuesto(idUsuario, mes, anio)
                    val detalles = if (presupuesto != null)
                        db.detallePresupuestoDao().obtenerPorPresupuesto(presupuesto.idPresupuesto)
                    else emptyList()

                    val gastoPorCat = gastos
                        .groupBy { it.idCategoria }
                        .mapValues { (_, lista) -> lista.sumOf { it.monto } }

                    val presupuestosUi = detalles.mapNotNull { detalle ->
                        val cat = categorias.find { it.idCategoria == detalle.idCategoria }
                            ?: return@mapNotNull null
                        PresupuestoGraficaUi(
                            categoria = cat.nombre,
                            emoji = emojiParaCategoria(cat.nombre),
                            asignado = detalle.montoLimite,
                            gastado = gastoPorCat[detalle.idCategoria] ?: 0.0,
                        )
                    }

                    val categoriasUi = categorias
                        .map { cat ->
                            CategoriaGraficaUi(
                                emoji = emojiParaCategoria(cat.nombre),
                                nombre = cat.nombre,
                                totalGastado = gastoPorCat[cat.idCategoria] ?: 0.0,
                            )
                        }
                        .filter { it.totalGastado > 0 }
                        .sortedByDescending { it.totalGastado }

                    val totalGastos = gastos.sumOf { it.monto }
                    val totalIngresos = presupuesto?.ingresoMensual ?: 0.0

                    _uiState.value = GraphicUiState(
                        presupuestos = presupuestosUi,
                        categorias = categoriasUi,
                        resumen = ResumenPeriodoUi(totalIngresos, totalGastos),
                        cargando = false,
                    )
                }
        }
    }

    private fun rangoParaPeriodo(periodo: PeriodoGrafica, fechaMs: Long): Pair<Long, Long>{
        val cal = Calendar.getInstance().apply { timeInMillis = fechaMs }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (periodo){
            PeriodoGrafica.DIA -> {
                val inicio = cal.timeInMillis
                val fin = inicio + 86_400_000L - 1
                Pair(inicio, fin)
            }
            PeriodoGrafica.SEMANAL -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val inicio = cal.timeInMillis
                val fin = inicio + 7 * 86_400_000L - 1
                Pair(inicio, fin)
            }
            PeriodoGrafica.QUINCENAL -> {
                val dia = cal.get(Calendar.DAY_OF_MONTH)
                if (dia <= 15){
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val inicio = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, 15)
                    cal.set(android.icu.util.Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    Pair(inicio, cal.timeInMillis)
                }else{
                    cal.set(Calendar.DAY_OF_MONTH, 16)
                    val inicio = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    Pair(inicio, cal.timeInMillis)
                }
            }
        }
    }


    private fun emojiParaCategoria(nombre: String): String = when{
        nombre.contains("vivien", ignoreCase = true) ||
                nombre.contains("hogar", ignoreCase = true) ||
                nombre.contains("casa", ignoreCase = true)        -> "🏠"
        nombre.contains("entret", ignoreCase = true) ||
                nombre.contains("ocio", ignoreCase = true)        -> "🎬"
        nombre.contains("transp", ignoreCase = true)      -> "🚗"
        nombre.contains("alim", ignoreCase = true) ||
                nombre.contains("comida", ignoreCase = true) ||
                nombre.contains("food", ignoreCase = true)        -> "🍔"
        nombre.contains("salud", ignoreCase = true) ||
                nombre.contains("médic", ignoreCase = true)       -> "❤️"
        nombre.contains("ropa", ignoreCase = true) ||
                nombre.contains("compra", ignoreCase = true)      -> "🛍️"
        nombre.contains("educat", ignoreCase = true) ||
                nombre.contains("escuel", ignoreCase = true)      -> "📚"
        else                                               -> "📦"
    }


    private fun hoyUtcMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}