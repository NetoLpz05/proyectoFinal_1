package jesusernesto.lopezibarra.gestorgastos.screens.group

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.GrupoViewModel
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MovimientoViewModel
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MetodoPagoViewModel
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupExpenseScreen(
    idGrupo: Int,
    onBack: () -> Unit,
    onSave: () -> Unit,
    grupoViewModel: GrupoViewModel = viewModel(),
    movimientoViewModel: MovimientoViewModel = viewModel(),
    metodoPagoViewModel: MetodoPagoViewModel = viewModel()
) {
    val currentUser = SessionManager.usuarioActual

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var selectedPaymentId by remember { mutableIntStateOf(0) }
    var selectedPayerId by remember { mutableIntStateOf(currentUser?.idUsuario ?: 0) }
    var selectedParticipants by remember { mutableStateOf(setOf<Int>()) }

    val uiState by grupoViewModel.uiState.collectAsState()
    val categorias by movimientoViewModel.categorias.collectAsState(initial = emptyList())
    val metodosPago by metodoPagoViewModel.metodosPago.collectAsState()

    LaunchedEffect(idGrupo) {
        grupoViewModel.seleccionarGrupo(idGrupo)
    }

    LaunchedEffect(uiState.miembros) {
        if (uiState.miembros.isNotEmpty()) {
            selectedParticipants = uiState.miembros.map { it.idUsuario }.toSet()
            if (selectedPayerId == 0) {
                selectedPayerId = currentUser?.idUsuario ?: uiState.miembros.first().idUsuario
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White)) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, tint = Purple)
                    }
                    Text("Nuevo Gasto Grupal", modifier = Modifier.align(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("MONTO DEL GASTO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                    textStyle = LocalTextStyle.current.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    placeholder = { Text("0.00", color = TextGray.copy(0.3f)) }
                )
            }

            SectionLabel("DESCRIPCIÓN")
            EditFieldSimple(value = description, onValueChange = { description = it })

            SectionLabel("CATEGORÍA")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categorias.forEach { cat ->
                    val partes = cat.nombre.split(" ", limit = 2)
                    CategoryIconItemSimple(partes.getOrNull(1) ?: cat.nombre, partes.firstOrNull() ?: "📦", selectedCategoryId == cat.idCategoria) {
                        selectedCategoryId = cat.idCategoria
                    }
                }
            }

            SectionLabel("¿QUIÉN PAGÓ?")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.miembros.forEach { miembro ->
                    val isPayer = selectedPayerId == miembro.idUsuario
                    FilterChip(
                        selected = isPayer,
                        onClick = { selectedPayerId = miembro.idUsuario },
                        label = { Text(miembro.nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            SectionLabel("MÉTODO DE PAGO")
            val selectedMetodo = metodosPago.find { it.idMetodoPago == selectedPaymentId }
            PaymentCardSimple(
                nombre = selectedMetodo?.nombre ?: "Seleccionar",
                detalle = "**** ${selectedMetodo?.ultimosDigitos ?: "0000"}",
                onClick = { /* Implement selection if multiple */ }
            )
            LaunchedEffect(metodosPago) {
                if (selectedPaymentId == 0 && metodosPago.isNotEmpty()) {
                    selectedPaymentId = metodosPago.first().idMetodoPago
                }
            }

            SectionLabel("PARTICIPANTES (DIVIDIR ENTRE)")
            uiState.miembros.forEach { miembro ->
                val isSelected = selectedParticipants.contains(miembro.idUsuario)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedParticipants = if (isSelected) {
                                if (selectedParticipants.size > 1) selectedParticipants - miembro.idUsuario else selectedParticipants
                            } else {
                                selectedParticipants + miembro.idUsuario
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isSelected) Purple else PurpleLight), contentAlignment = Alignment.Center) {
                        if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(miembro.nombre, modifier = Modifier.weight(1f))
                    if (isSelected && amount.isNotBlank()) {
                        val div = (amount.toDoubleOrNull() ?: 0.0) / selectedParticipants.size
                        Text("$${"%,.2f".format(div)}", color = TextGray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    grupoViewModel.agregarGastoGrupo(
                        idGrupo = idGrupo,
                        idUsuarioPago = selectedPayerId,
                        idCategoria = selectedCategoryId,
                        idMetodoPago = selectedPaymentId,
                        nombre = description,
                        monto = amount.toDoubleOrNull() ?: 0.0,
                        fecha = System.currentTimeMillis(),
                        participantes = selectedParticipants.toList()
                    )
                    onSave()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = amount.isNotBlank() && description.isNotBlank() && selectedCategoryId != 0 && selectedParticipants.isNotEmpty()
            ) {
                Text("Guardar Gasto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
}

@Composable
private fun EditFieldSimple(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = PurpleLight,
            focusedBorderColor = Purple
        )
    )
}

@Composable
private fun CategoryIconItemSimple(label: String, emoji: String, isSelected: Boolean, onClick: () -> Unit) {
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
private fun PaymentCardSimple(nombre: String, detalle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = Purple) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(detalle, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}
