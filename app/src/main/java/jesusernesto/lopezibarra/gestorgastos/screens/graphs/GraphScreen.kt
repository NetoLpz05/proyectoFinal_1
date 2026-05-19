package jesusernesto.lopezibarra.gestorgastos.screens.graphs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.enums.PeriodoGrafica
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.CategoriaGraficaUi
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.GraphicViewModel
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.ResumenPeriodoUi
import jesusernesto.lopezibarra.gestorgastos.screens.components.AppTopBar
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val sdfDisplay = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphicScreen(
    onBack: () -> Unit = {},
    viewModel: GraphicViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val periodoActual by viewModel.periodo.collectAsState()
    val fechaMs by viewModel.fechaSeleccionada.collectAsState()

    val periodos = listOf("Día", "Semanal", "Quincenal")
    val periodoLabel = when (periodoActual) {
        PeriodoGrafica.DIA      -> "Día"
        PeriodoGrafica.SEMANAL  -> "Semanal"
        PeriodoGrafica.QUINCENAL -> "Quincenal"
    }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaMs
    )

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setFecha(it) }
                    mostrarDatePicker = false
                }) { Text("Aceptar", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Purple,
                    todayDateBorderColor = Purple,
                    selectedYearContainerColor = Purple
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(title = "Gráficas", onBack = onBack)

        if (uiState.cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Purple)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ResumenCard(resumen = uiState.resumen)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Periodo",
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PeriodoSelector(
                periodos = periodos,
                seleccionado = periodoLabel,
                onSeleccionar = { label ->
                    viewModel.setPeriodo(
                        when (label) {
                            "Semanal"   -> PeriodoGrafica.SEMANAL
                            "Quincenal" -> PeriodoGrafica.QUINCENAL
                            else        -> PeriodoGrafica.DIA
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Seleccionar fecha",
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FechaSelector(
                fecha = sdfDisplay.format(Date(fechaMs)),
                onClick = { mostrarDatePicker = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.presupuestos.isEmpty()) {
                EmptyCard(mensaje = "No hay presupuesto configurado para este período")
            } else {
                uiState.presupuestos.forEach { p ->
                    PresupuestoCard(
                        categoria = p.categoria,
                        emoji = p.emoji,
                        asignado = p.asignado,
                        gastado = p.gastado,
                        porcentaje = p.porcentaje,
                        restante = p.restante,
                        excedido = p.excedido,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.categorias.isNotEmpty()) {
                Text(
                    text = "Gasto por categoría",
                    fontSize = 14.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                CategoriasGrid(categorias = uiState.categorias)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@Composable
private fun ResumenCard(resumen: ResumenPeriodoUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, PurpleLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ResumenItem(
            label = "Ingresos",
            monto = resumen.totalIngresos,
            color = GreenIncome
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp)
                .background(PurpleLight)
        )
        ResumenItem(
            label = "Gastos",
            monto = resumen.totalGastos,
            color = RedExpense
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp)
                .background(PurpleLight)
        )
        ResumenItem(
            label = "Balance",
            monto = resumen.balance,
            color = if (resumen.balance >= 0) Purple else RedExpense
        )
    }
}

@Composable
private fun ResumenItem(label: String, monto: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$${"%,.0f".format(monto)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Composable
fun PeriodoSelector(
    periodos: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, PurpleLight, RoundedCornerShape(12.dp))
    ) {
        periodos.forEach { periodo ->
            val isSelected = periodo == seleccionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PurpleLight else Color.Transparent)
                    .clickable { onSeleccionar(periodo) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = periodo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else DarkNavy
                )
            }
        }
    }
}


@Composable
fun FechaSelector(fecha: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, PurpleLight, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = DarkNavy,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = fecha,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DarkNavy
        )
    }
}


@Composable
fun PresupuestoCard(
    categoria: String,
    emoji: String,
    asignado: Double,
    gastado: Double,
    porcentaje: Int,
    restante: Double,
    excedido: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, if (excedido) RedExpense.copy(alpha = 0.4f) else PurpleLight, RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Presupuesto",
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${"%,.0f".format(asignado)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Purple
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = categoria,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BarraProgreso(
            porcentajeReal = porcentaje,
            porcentajeRestante = (100 - porcentaje).coerceAtLeast(0),
            excedido = excedido
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Gasto real", fontSize = 11.sp, color = TextGray)
                Text(
                    text = "$${"%,.0f".format(gastado)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (excedido) RedExpense else DarkNavy
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (excedido) "Excedido" else "Disponible",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    text = "$${"%,.0f".format(kotlin.math.abs(restante))}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (excedido) RedExpense else DarkNavy
                )
            }
        }
    }
}


@Composable
fun BarraProgreso(
    porcentajeReal: Int,
    porcentajeRestante: Int,
    excedido: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (porcentajeReal > 0) {
            Box(
                modifier = Modifier
                    .weight(porcentajeReal.toFloat())
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            bottomStart = 8.dp,
                            topEnd = if (porcentajeRestante == 0) 8.dp else 0.dp,
                            bottomEnd = if (porcentajeRestante == 0) 8.dp else 0.dp
                        )
                    )
                    .background(if (excedido) RedExpense else Purple)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "$porcentajeReal%",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (porcentajeRestante > 0) {
            Box(
                modifier = Modifier
                    .weight(porcentajeRestante.toFloat())
                    .clip(
                        RoundedCornerShape(
                            topStart = if (porcentajeReal == 0) 8.dp else 0.dp,
                            bottomStart = if (porcentajeReal == 0) 8.dp else 0.dp,
                            topEnd = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .background(PurpleLight)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "$porcentajeRestante%",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun CategoriasGrid(categorias: List<CategoriaGraficaUi>) {
    val filas = categorias.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fila.forEachIndexed { index, cat ->
                    CategoriaChip(
                        emoji = cat.emoji,
                        nombre = cat.nombre,
                        monto = cat.totalGastado,
                        isDestacada = index == 0 && fila === filas.first(),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoriaChip(
    emoji: String,
    nombre: String,
    monto: Double,
    isDestacada: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDestacada) Purple else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isDestacada) 0.dp else 1.5.dp,
                color = if (isDestacada) Color.Transparent else PurpleLight,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = nombre,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestacada) Color.White else DarkNavy
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$${"%,.0f".format(monto)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDestacada) Color.White.copy(alpha = 0.9f) else Purple
        )
    }
}


@Composable
private fun EmptyCard(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, PurpleLight, RoundedCornerShape(14.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GraphicScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            PeriodoSelector(
                periodos = listOf("Día", "Semanal", "Quincenal"),
                seleccionado = "Semanal",
                onSeleccionar = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            FechaSelector(fecha = "12 May 2026")

            Spacer(modifier = Modifier.height(16.dp))

            PresupuestoCard(
                categoria = "Alimentación",
                emoji = "🍔",
                asignado = 3200.0,
                gastado = 1420.0,
                porcentaje = 44,
                restante = 1780.0,
                excedido = false,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PresupuestoCard(
                categoria = "Entretenimiento",
                emoji = "🎬",
                asignado = 1850.0,
                gastado = 2100.0,
                porcentaje = 100,
                restante = -250.0,
                excedido = true,
            )
        }
    }
}