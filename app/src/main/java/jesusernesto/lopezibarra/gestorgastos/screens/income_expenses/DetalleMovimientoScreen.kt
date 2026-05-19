package jesusernesto.lopezibarra.gestorgastos.screens.income_expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jesusernesto.lopezibarra.gestorgastos.data.entity.MetodoPagoEntity
import jesusernesto.lopezibarra.gestorgastos.data.enums.TipoMetodoPago
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MetodoPagoViewModel
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MovimientoUI
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MovimientoViewModel
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMovimientoScreen(
    tipo: String,
    id: Int,
    onBack: () -> Unit,
    onEdit: (String, Int) -> Unit,
    movimientoViewModel: MovimientoViewModel = viewModel(),
    metodoPagoViewModel: MetodoPagoViewModel = viewModel()
) {
    var movimiento by remember { mutableStateOf<MovimientoUI?>(null) }
    val metodosPago by metodoPagoViewModel.metodosPago.collectAsState()
    
    LaunchedEffect(id, tipo) {
        movimiento = movimientoViewModel.obtenerMovimiento(tipo, id)
    }

    if (movimiento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Purple)
        }
        return
    }

    val mov = movimiento!!
    val isGasto = tipo == "gasto"
    val metodo = metodosPago.find { it.idMetodoPago == mov.idMetodoPago }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Atrás", tint = Purple)
                }
                Text(
                    text = "Detalle del Movimiento",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { onEdit(tipo, id) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = Purple)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Monto y Tipo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isGasto) "Gasto" else "Ingreso",
                    color = if (isGasto) RedGasto else GreenIncome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "$${"%,.2f".format(mov.monto)}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mov.descripcion,
                    fontSize = 16.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de detalles
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, PurpleLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailItem(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Fecha",
                        value = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("es", "MX")).format(Date(mov.fecha))
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = PurpleLight.copy(alpha = 0.2f))
                    
                    DetailItem(
                        icon = Icons.Outlined.Category,
                        label = "Categoría",
                        value = mov.categoriaNombre
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = PurpleLight.copy(alpha = 0.2f))
                    
                    val metodoNombre = metodo?.nombre ?: when(metodo?.tipo) {
                        TipoMetodoPago.EFECTIVO -> "Efectivo"
                        TipoMetodoPago.TARJETA_DEBITO -> "Tarjeta de Débito"
                        TipoMetodoPago.TARJETA_CREDITO -> "Tarjeta de Crédito"
                        else -> "No especificado"
                    }
                    val metodoDetalle = if (metodo?.tipo == TipoMetodoPago.EFECTIVO) "Dinero físico" else if (metodo?.ultimosDigitos != null) "**** ${metodo.ultimosDigitos}" else ""
                    
                    DetailItem(
                        icon = Icons.Outlined.CreditCard,
                        label = "Método de Pago",
                        value = "$metodoNombre $metodoDetalle".trim()
                    )
                    
                    if (!mov.ubicacion.isNullOrBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = PurpleLight.copy(alpha = 0.2f))
                        DetailItem(
                            icon = Icons.Outlined.LocationOn,
                            label = "Ubicación",
                            value = mov.ubicacion!!
                        )
                    }
                }
            }

            if (!mov.fotoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Comprobante",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AsyncImage(
                    model = mov.fotoUri,
                    contentDescription = "Foto del recibo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, PurpleLight, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PurpleLight.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}
