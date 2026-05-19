package jesusernesto.lopezibarra.gestorgastos.screens.income_expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.entity.*
import jesusernesto.lopezibarra.gestorgastos.data.enums.TipoMetodoPago
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NewMovementScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onNavigateToNewCard: () -> Unit,
    movimientoViewModel: MovimientoViewModel = viewModel(),
    metodoPagoViewModel: MetodoPagoViewModel = viewModel()
) {
    val metodosPago by metodoPagoViewModel.metodosPago.collectAsState()
    val saveSuccess by movimientoViewModel.saveSuccess.collectAsState()
    val categorias by movimientoViewModel.categorias.collectAsState(initial = emptyList())

    NewMovementContent(
        onBack = onBack,
        onSave = onSave,
        onNavigateToNewCard = onNavigateToNewCard,
        metodosPago = metodosPago,
        saveSuccess = saveSuccess,
        categorias = categorias,
        onGuardarMovimiento = { isGasto, monto, description, date, catId, mpId, location, photoUri ->
            movimientoViewModel.guardarMovimiento(
                isGasto = isGasto,
                monto = monto,
                descripcion = description,
                fecha = date,
                idCategoria = catId,
                idMetodoPago = mpId,
                ubicacion = location,
                fotoUri = photoUri
            )
        },
        onResetSaveSuccess = { movimientoViewModel.resetSaveSuccess() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMovementContent(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onNavigateToNewCard: () -> Unit,
    metodosPago: List<MetodoPagoEntity>,
    saveSuccess: Boolean,
    categorias: List<CategoriaEntity>,
    onGuardarMovimiento: (Boolean, Float, String, String, Int, Int, String?, String?) -> Unit,
    onResetSaveSuccess: () -> Unit) {

    var isGasto by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var selectedCategory by remember { mutableStateOf("Entretenimiento") }
    var selectedPaymentIndex by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditingAmount by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    val selectedMetodo = metodosPago.getOrNull(selectedPaymentIndex)

    var location by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationInput by remember { mutableStateOf("") }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onSave()
            onResetSaveSuccess()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Regresar", tint = Purple)
                }
                Text(
                    text = "Nuevo Movimiento",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, PurpleLight, RoundedCornerShape(10.dp))
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .background(if (isGasto) MaterialTheme.colorScheme.surface else PurpleLight.copy(alpha = 0.2f)).clickable { isGasto = true }, contentAlignment = Alignment.Center) {
                    Text("Gasto", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = if (isGasto) RedGasto else TextGray)
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                        .background(if (!isGasto) MaterialTheme.colorScheme.surface else PurpleLight.copy(alpha = 0.2f)).clickable { isGasto = false }, contentAlignment = Alignment.Center
                ) {
                    Text("Ingreso", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = if (!isGasto) GreenIncome else TextGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isGasto) "MONTO DEL GASTO" else "MONTO DEL INGRESO",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextGray
            )

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = TextGray)
                Spacer(modifier = Modifier.width(12.dp))

                if (isEditingAmount) {
                    OutlinedTextField(value = amount,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                amount = it
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextGray),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent))
                } else {
                    Text(
                        text = amount.ifBlank { "0.00" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = TextGray.copy(alpha = if (amount.isBlank()) 0.3f else 1f),
                        modifier = Modifier.clickable { isEditingAmount = true }
                    )
                }
            }

            SectionLabel("Descripción")
            OutlinedTextField(value = description, onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(10.dp), singleLine = true, colors = movementFieldColors())

            SectionLabel("Fecha")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = PurpleLight,
                        disabledTextColor   = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledLeadingIconColor = Color.Black
                    )
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(Date(it))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            SectionLabel("Categoría")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryIcon("Vivienda", Icons.Outlined.Home, selectedCategory == "Vivienda") { selectedCategory = "Vivienda" }
                CategoryIcon("Entretenimiento", Icons.Outlined.Tv, selectedCategory == "Entretenimiento") { selectedCategory = "Entretenimiento" }
                CategoryIcon("Transporte", Icons.Outlined.DirectionsCar, selectedCategory == "Transporte") { selectedCategory = "Transporte" }
                CategoryIcon("Alimentación", Icons.Outlined.Restaurant, selectedCategory == "Alimentación") { selectedCategory = "Alimentación" }
                CategoryIcon("Salud", Icons.Outlined.FavoriteBorder, selectedCategory == "Salud") { selectedCategory = "Salud" }
                CategoryIcon("Otros", Icons.Outlined.GridView, selectedCategory == "Otros") { selectedCategory = "Otros" }
            }

            SectionLabel("Método de pago")
            selectedMetodo?.let { metodo ->
                PaymentCard(
                    nombre = metodo.nombre ?: (if (metodo.tipo == TipoMetodoPago.TARJETA_CREDITO) "Tarjeta de Crédito" else if (metodo.tipo == TipoMetodoPago.TARJETA_DEBITO) "Tarjeta de Débito" else "Efectivo"),
                    detalle = if (metodo.tipo == TipoMetodoPago.EFECTIVO) "DINERO FÍSICO" else "**** ${metodo.ultimosDigitos ?: "0000"}",
                    onClick = { showPaymentSheet = true }
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(12.dp))
                        .background(PurpleLight.copy(alpha = 0.2f)).clickable { showPaymentSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Seleccionar método de pago", color = TextGray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, PurpleLight, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showLocationDialog = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null,
                    tint = Purple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = location ?: "Agregar Ubicación...",
                    color = if (location != null) MaterialTheme.colorScheme.onSurface else TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = if (location == null) FontStyle.Italic else FontStyle.Normal
                )
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
                            location = locationInput.takeIf { it.isNotBlank() }
                            showLocationDialog = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocationDialog = false }) { Text("Cancelar") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp))
                    .border(2.dp, PurpleLight, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface).clickable { photoLauncher.launch("image/*") }.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = if (photoUri == null) "Adjuntar foto del recibo" else "Foto adjuntada", color = TextGray, fontSize = 14.sp)

                if (photoUri != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GreenIncome)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val categoriaEncontrada = categorias.find { it.nombre.contains(selectedCategory, ignoreCase = true) }
                    val catId = categoriaEncontrada?.idCategoria ?: 0
                    val mpId = selectedMetodo?.idMetodoPago ?: 0

                    onGuardarMovimiento(isGasto, amount.toFloatOrNull() ?: 0f,
                        description, date, catId, mpId, location, photoUri?.toString())
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Text("Guardar Movimiento", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showPaymentSheet) {
            ModalBottomSheet(onDismissRequest = { showPaymentSheet = false }) {
                FormaPagoCard(
                    metodos = metodosPago,
                    selectedIndex = selectedPaymentIndex,
                    onSelect = { index ->
                        selectedPaymentIndex = index
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
    Text(
        text = text.uppercase(),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextGray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CategoryIcon(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))
                .border(2.dp, if (isSelected) Purple else Color.Transparent, RoundedCornerShape(12.dp))
                .background(if (isSelected) Color.White else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Purple else TextGray.copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
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
private fun movementFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = PurpleLight,
    focusedBorderColor = Purple,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun FormaPagoCard(
    metodos: List<MetodoPagoEntity>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddCard: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp).size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp)).background(PurpleLight))

        Text(text = "Selecciona el método", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

        metodos.forEachIndexed { index, metodo ->
            val isSelected = selectedIndex == index

            val emoji = when(metodo.tipo) {
                TipoMetodoPago.EFECTIVO -> "💵"
                else -> "💳"
            }
            val label = metodo.nombre ?: (if (metodo.tipo == TipoMetodoPago.TARJETA_CREDITO) "Tarjeta de Crédito" else if (metodo.tipo == TipoMetodoPago.TARJETA_DEBITO) "Tarjeta de Débito" else "Efectivo")
            val detail = if (metodo.tipo == TipoMetodoPago.EFECTIVO) "DINERO FÍSICO" else "**** ${metodo.ultimosDigitos ?: "0000"}"

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp)).background(if (isSelected) Purple else PurpleLight.copy(alpha = 0.2f))
                .clickable { onSelect(index) }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
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



@Preview(showBackground = true)
@Composable
fun NewMovementScreenPreview() {
    GestorGastosTheme {
        NewMovementContent(
            onBack = {},
            onSave = {},
            onNavigateToNewCard = {},
            metodosPago = emptyList(),
            saveSuccess = false,
            categorias = listOf(
                CategoriaEntity(1, "🍔 Comida"),
                CategoriaEntity(2, "🚗 Transporte"),
                CategoriaEntity(3, "🏠 Hogar"),
                CategoriaEntity(4, "🎁 Regalos"),
                CategoriaEntity(5, "🩺 Salud"),
                CategoriaEntity(6, "📚 Educación")
            ),
            onGuardarMovimiento = { _, _, _, _, _, _, _, _ -> },
            onResetSaveSuccess = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FormaPagoCardPreview() {
    GestorGastosTheme {
        var selectedIndex by remember { mutableStateOf(0) }
        FormaPagoCard(metodos = emptyList(), selectedIndex = selectedIndex, onSelect = { selectedIndex = it })
    }
}

@Composable
fun GestorGastosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme, content = content)
}
