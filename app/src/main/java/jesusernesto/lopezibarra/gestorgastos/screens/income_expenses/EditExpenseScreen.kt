package jesusernesto.lopezibarra.gestorgastos.screens.income_expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.entity.MetodoPagoEntity
import jesusernesto.lopezibarra.gestorgastos.data.enums.TipoMetodoPago
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.*
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    tipo: String,
    id: Int,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToNewCard: () -> Unit = {},
    movimientoViewModel: MovimientoViewModel = viewModel(),
    metodoPagoViewModel: MetodoPagoViewModel = viewModel()
) {
    var movimiento by remember { mutableStateOf<MovimientoUI?>(null) }
    
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateLong by remember { mutableLongStateOf(0L) }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var selectedPaymentId by remember { mutableIntStateOf(0) }
    var location by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationInput by remember { mutableStateOf("") }

    val categorias by movimientoViewModel.categorias.collectAsState(initial = emptyList())
    val metodosPago by metodoPagoViewModel.metodosPago.collectAsState()
    val saveSuccess by movimientoViewModel.saveSuccess.collectAsState()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri?.toString()
    }

    LaunchedEffect(id, tipo) {
        movimiento = movimientoViewModel.obtenerMovimiento(tipo, id)
    }

    LaunchedEffect(movimiento) {
        movimiento?.let {
            amount = it.monto.toString()
            description = it.descripcion
            dateLong = it.fecha
            selectedCategoryId = it.idCategoria
            selectedPaymentId = it.idMetodoPago
            location = it.ubicacion ?: ""
            locationInput = it.ubicacion ?: ""
            photoUri = it.fotoUri
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onSave()
            movimientoViewModel.resetSaveSuccess()
        }
    }

    if (movimiento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Purple)
        }
        return
    }

    val isGasto = tipo == "gasto"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = Purple)
                }
                Text(
                    text = if (isGasto) "Editar Gasto" else "Editar Ingreso",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (isGasto) "MONTO DEL GASTO" else "MONTO DEL INGRESO",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextGray
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                    textStyle = LocalTextStyle.current.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            SectionLabel("DESCRIPCIÓN")
            EditFieldSimple(value = description, onValueChange = { description = it })

            SectionLabel("FECHA")
            val dateText = remember(dateLong) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateLong))
            }
            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                EditFieldSimple(value = dateText, onValueChange = {}, icon = Icons.Outlined.CalendarMonth, enabled = false)
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateLong)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { dateLong = it }
                            showDatePicker = false
                        }) { Text("Aceptar") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            SectionLabel("CATEGORÍA")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categorias.forEach { cat ->
                    val partes = cat.nombre.split(" ", limit = 2)
                    val emoji = partes.firstOrNull() ?: "📦"
                    val nombre = partes.getOrNull(1) ?: cat.nombre
                    CategoryIconItem(nombre, emoji, selectedCategoryId == cat.idCategoria) {
                        selectedCategoryId = cat.idCategoria
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("MÉTODO DE PAGO")
            val selectedMetodo = metodosPago.find { it.idMetodoPago == selectedPaymentId }
            PaymentCard(
                nombre = selectedMetodo?.nombre ?: (if (selectedMetodo?.tipo == TipoMetodoPago.TARJETA_CREDITO) "Tarjeta de Crédito" else if (selectedMetodo?.tipo == TipoMetodoPago.TARJETA_DEBITO) "Tarjeta de Débito" else if (selectedMetodo?.tipo == TipoMetodoPago.EFECTIVO) "Efectivo" else "Seleccionar"),
                detalle = if (selectedMetodo?.tipo == TipoMetodoPago.EFECTIVO) "DINERO FÍSICO" else "**** ${selectedMetodo?.ultimosDigitos ?: "0000"}",
                onClick = { showPaymentSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DashedBoxEdit(
                color = PurpleLight,
                onClick = { showLocationDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Purple, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = location.ifBlank { "Sin ubicación" },
                        color = if (location.isNotBlank()) MaterialTheme.colorScheme.onSurface else TextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = if (location.isBlank()) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }

            if (showLocationDialog) {
                AlertDialog(
                    onDismissRequest = { showLocationDialog = false },
                    title = { Text("¿Dónde realizaste este movimiento?") },
                    text = {
                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Ej: Walmart, Calle 5 de Febrero...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            location = locationInput
                            showLocationDialog = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocationDialog = false }) { Text("Cancelar") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashedBoxEdit(
                color = PurpleLight,
                onClick = { photoLauncher.launch("image/*") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (photoUri != null) "Cambiar foto de recibo" else "Adjuntar foto", color = TextGray, fontSize = 14.sp)
                    }
                    if (photoUri != null) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenIncome)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    movimientoViewModel.actualizarMovimiento(
                        id = id,
                        isGasto = isGasto,
                        monto = amount.toDoubleOrNull() ?: 0.0,
                        descripcion = description,
                        fecha = dateLong,
                        idCategoria = selectedCategoryId,
                        idMetodoPago = selectedPaymentId,
                        ubicacion = location,
                        fotoUri = photoUri
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    movimientoViewModel.eliminarMovimiento(id, isGasto)
                    onDelete()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RedGasto)
            ) {
                Text(if (isGasto) "Eliminar Gasto" else "Eliminar Ingreso", color = RedGasto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showPaymentSheet) {
            ModalBottomSheet(onDismissRequest = { showPaymentSheet = false }) {
                FormaPagoCard1(
                    metodos = metodosPago,
                    selectedId = selectedPaymentId,
                    onSelect = { idMetodo ->
                        selectedPaymentId = idMetodo
                        showPaymentSheet = false
                    },
                    onAddCard = {
                        showPaymentSheet = false
                        onNavigateToNewCard()
                    }
                )
            }
        }
    }
}


@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun EditFieldSimple(value: String, onValueChange: (String) -> Unit, icon: ImageVector? = null, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) } },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = PurpleLight,
            focusedBorderColor = Purple,
            disabledBorderColor = PurpleLight,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun CategoryIconItem(label: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 8.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))
                .border(2.dp, if (isSelected) Purple else Color.Transparent, RoundedCornerShape(12.dp))
                .background(if (isSelected) Color.White else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Text(label, fontSize = 11.sp, color = if (isSelected) Purple else TextGray)
    }
}

@Composable
private fun PaymentCard(nombre: String, detalle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Purple
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(detalle, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Text("Cambiar >", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashedBoxEdit(color: Color, onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .clickable { onClick() }
            .drawBehind {
                val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                drawRoundRect(color = color, style = stroke)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
fun FormaPagoCard1(
    metodos: List<MetodoPagoEntity>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onAddCard: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp).size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp)).background(PurpleLight))

        Text(text = "Selecciona el método", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

        metodos.forEach { metodo ->
            val isSelected = selectedId == metodo.idMetodoPago

            val emoji = when(metodo.tipo) {
                TipoMetodoPago.EFECTIVO -> "💵"
                else -> "💳"
            }
            val label = metodo.nombre ?: (if (metodo.tipo == TipoMetodoPago.TARJETA_CREDITO) "Tarjeta de Crédito" else if (metodo.tipo == TipoMetodoPago.TARJETA_DEBITO) "Tarjeta de Débito" else "Efectivo")
            val detail = if (metodo.tipo == TipoMetodoPago.EFECTIVO) "DINERO FÍSICO" else "**** ${metodo.ultimosDigitos ?: "0000"}"

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp)).background(if (isSelected) Purple else PurpleLight.copy(alpha = 0.2f))
                .clickable { onSelect(metodo.idMetodoPago) }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.White
                    ), contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    Text(text = detail, fontSize = 12.sp, color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextGray)
                }

                if (isSelected) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Seleccionado", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp))
            .border(2.dp, Purple, RoundedCornerShape(10.dp)).clickable { onAddCard() }.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Outlined.AddCard, contentDescription = null,
                tint = Purple, modifier = Modifier.size(20.dp))

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = "Añadir tarjeta crédito / débito", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Purple)
        }
    }
}