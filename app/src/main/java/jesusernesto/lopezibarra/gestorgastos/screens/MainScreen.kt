package jesusernesto.lopezibarra.gestorgastos.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import jesusernesto.lopezibarra.gestorgastos.data.SessionManager
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MovimientoUI
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MovimientoViewModel
import jesusernesto.lopezibarra.gestorgastos.screens.budget.BudgetScreen
import jesusernesto.lopezibarra.gestorgastos.screens.components.BottomNavBar
import jesusernesto.lopezibarra.gestorgastos.screens.graphs.GraphicScreen
import jesusernesto.lopezibarra.gestorgastos.screens.group.CrearGrupoScreen
import jesusernesto.lopezibarra.gestorgastos.screens.group.MisGruposScreen
import jesusernesto.lopezibarra.gestorgastos.screens.group.GroupDetailScreen
import jesusernesto.lopezibarra.gestorgastos.screens.group.AddGroupExpenseScreen
import jesusernesto.lopezibarra.gestorgastos.screens.income_expenses.AddCardScreen
import jesusernesto.lopezibarra.gestorgastos.screens.income_expenses.DetalleMovimientoScreen
import jesusernesto.lopezibarra.gestorgastos.screens.income_expenses.EditExpenseScreen
import jesusernesto.lopezibarra.gestorgastos.screens.income_expenses.NewMovementScreen
import jesusernesto.lopezibarra.gestorgastos.screens.user.AlertasScreen
import jesusernesto.lopezibarra.gestorgastos.screens.user.ProfileScreen
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-MX"))
private fun Long.toDisplayDate(): String = displayFormat.format(Date(this))

private fun startOfMonthMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun endOfDayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59);      cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}


@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "Inicio"

    val ocultarBottomBar = currentRoute == "NuevoMovimiento"
            || currentRoute.startsWith("DetalleMovimiento")
            || currentRoute == "Graficas"
            || currentRoute == "AddCard"
            || currentRoute.startsWith("EditarGasto")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!ocultarBottomBar) {
                BottomNavBar(
                    tabActivo = currentRoute,
                    onTabSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo("Inicio") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNewMovement = { navController.navigate("NuevoMovimiento") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "Inicio",
            modifier = Modifier.padding(padding)
        ) {
            composable("Inicio") {
                HomeScreen(
                    onTransaccionClick = { tipo, id ->
                        navController.navigate("DetalleMovimiento/$tipo/$id")
                    },
                    onEditTransaccion = { tipo, id ->
                        navController.navigate("EditarGasto/$tipo/$id")
                    },
                    onBalanceClick = {
                        navController.navigate("Graficas")
                    }
                )
            }
            composable("Presupuesto") {
                BudgetScreen(onBack = { navController.popBackStack() })
            }
            composable("Grupos") {
                MisGruposScreen(
                    onAtras = { navController.popBackStack() },
                    onAbrirGrupo = { navController.navigate("GrupoDetalle/${it.idGrupo}") },
                    onCrearGrupo = { navController.navigate("CrearGrupo") }
                )
            }
            composable("GrupoDetalle/{idGrupo}") { backStackEntry ->
                val idGrupo = backStackEntry.arguments?.getString("idGrupo")?.toIntOrNull() ?: 0
                GroupDetailScreen(
                    idGrupo = idGrupo,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate("NuevoGastoGrupo/$idGrupo") }
                )
            }
            composable("NuevoGastoGrupo/{idGrupo}") { backStackEntry ->
                val idGrupo = backStackEntry.arguments?.getString("idGrupo")?.toIntOrNull() ?: 0
                AddGroupExpenseScreen(
                    idGrupo = idGrupo,
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
            composable("CrearGrupo") {
                CrearGrupoScreen(
                    onBack = { navController.popBackStack() },
                    onCrear = { _, _ -> navController.popBackStack() },
                    onCancelar = { navController.popBackStack() }
                )
            }
            composable("Perfil") {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = onLogout,
                    onSettings = { navController.navigate("Alertas") },
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
            composable("NuevoMovimiento") {
                NewMovementScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() },
                    onNavigateToNewCard = { navController.navigate("AddCard") }
                )
            }
            composable("AddCard") {
                AddCardScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }

            composable("Alertas") {
                AlertasScreen(onBack = { navController.popBackStack() })
            }

            composable("DetalleMovimiento/{tipo}/{id}") { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                DetalleMovimientoScreen(
                    tipo = tipo,
                    id = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { t, i -> navController.navigate("EditarGasto/$t/$i") }
                )
            }
            composable("EditarGasto/{tipo}/{id}") { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                EditExpenseScreen(
                    tipo = tipo,
                    id = id,
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() },
                    onDelete = { navController.popBackStack() }
                )
            }
            composable("Graficas") {
                GraphicScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTransaccionClick: (String, Int) -> Unit = { _, _ -> },
    onEditTransaccion: (String, Int) -> Unit = { _, _ -> },
    onBalanceClick: () -> Unit = {},
    movimientoViewModel: MovimientoViewModel = viewModel()
) {
    val categoriasFiltro = listOf("Todos", "Vivienda", "Comida", "Transporte", "Compras", "Otros")
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
    var fechaDesde by remember { mutableLongStateOf(startOfMonthMillis()) }
    var fechaHasta by remember { mutableLongStateOf(endOfDayMillis()) }
    var pickerAbierto by remember { mutableStateOf<String?>(null) }

    val usuario = SessionManager.usuarioActual
    val movimientos by movimientoViewModel.todosLosMovimientos.collectAsState(initial = emptyList())

    val stateDesde = rememberDatePickerState(initialSelectedDateMillis = fechaDesde)
    val stateHasta = rememberDatePickerState(initialSelectedDateMillis = fechaHasta)

    val movimientosFiltrados = movimientos.filter { m ->
        val matchCategoria = categoriaSeleccionada == "Todos" ||
                m.categoriaNombre.contains(categoriaSeleccionada, ignoreCase = true)
        val matchBusqueda = busqueda.isBlank() ||
                m.descripcion.contains(busqueda, ignoreCase = true)
        val matchFecha = m.fecha in fechaDesde..fechaHasta
        matchCategoria && matchBusqueda && matchFecha
    }

    val totalIngresos = movimientosFiltrados.filterIsInstance<MovimientoUI.Ingreso>().sumOf { it.monto }
    val totalGastos = movimientosFiltrados.filterIsInstance<MovimientoUI.Gasto>().sumOf { it.monto }
    val balanceActual = totalIngresos - totalGastos

    if (pickerAbierto == "desde") {
        DatePickerDialog(
            onDismissRequest = { pickerAbierto = null },
            confirmButton = {
                TextButton(onClick = {
                    stateDesde.selectedDateMillis?.let { fechaDesde = it }
                    pickerAbierto = null
                }) { Text("Aceptar", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { pickerAbierto = null }) { Text("Cancelar", color = TextGray) }
            }
        ) {
            DatePicker(
                state = stateDesde,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Purple,
                    todayDateBorderColor = Purple,
                    selectedYearContainerColor = Purple
                )
            )
        }
    }
    if (pickerAbierto == "hasta") {
        DatePickerDialog(
            onDismissRequest = { pickerAbierto = null },
            confirmButton = {
                TextButton(onClick = {
                    stateHasta.selectedDateMillis?.let { 
                        // Set to end of the day chosen
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = it
                        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                        fechaHasta = cal.timeInMillis 
                    }
                    pickerAbierto = null
                }) { Text("Aceptar", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { pickerAbierto = null }) { Text("Cancelar", color = TextGray) }
            }
        ) {
            DatePicker(
                state = stateHasta,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                if (usuario?.fotoPerfil != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(usuario.fotoPerfil)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                val mesAnio = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX")).format(Date())
                Text(text = mesAnio.replaceFirstChar { it.uppercase() }, fontSize = 20.sp, color = TextGray)
                Text(
                    text = if (usuario != null) "Hola, ${usuario.nombre}" else "Mis finanzas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            item {
                BalanceGraphCard(
                    balance = balanceActual,
                    ingresos = totalIngresos,
                    gastos = totalGastos,
                    onClick = onBalanceClick
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.5.dp, PurpleLight, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickerAbierto = "desde" }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                            tint = Purple, modifier = Modifier.size(14.dp))
                        Column {
                            Text("Desde", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Medium)
                            Text(fechaDesde.toDisplayDate(), fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(PurpleLight))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickerAbierto = "hasta" }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                            tint = Purple, modifier = Modifier.size(14.dp))
                        Column {
                            Text("Hasta", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Medium)
                            Text(fechaHasta.toDisplayDate(), fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Buscar movimientos...", color = TextGray.copy(alpha = 0.5f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PurpleLight, focusedBorderColor = Purple,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriasFiltro.forEach { cat ->
                        val isSelected = cat == categoriaSeleccionada
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else PurpleLight, RoundedCornerShape(20.dp))
                                .clickable { categoriaSeleccionada = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat, fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.surface else TextGray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "Ultimos movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 19.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (movimientosFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin movimientos", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                items(movimientosFiltrados) { m ->
                    MovimientoRealRow(
                        movimiento = m,
                        onClick = {
                            val tipo = if (m is MovimientoUI.Gasto) "gasto" else "ingreso"
                            onTransaccionClick(tipo, m.id)
                        },
                        onEditClick = {
                            val tipo = if (m is MovimientoUI.Gasto) "gasto" else "ingreso"
                            onEditTransaccion(tipo, m.id)
                        }
                    )
                    HorizontalDivider(color = PurpleLight.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun MovimientoRealRow(
    movimiento: MovimientoUI,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val isIngreso = movimiento is MovimientoUI.Ingreso
    
    val partesCat = movimiento.categoriaNombre.split(" ", limit = 2)
    val emoji = partesCat.firstOrNull() ?: if (isIngreso) "💰" else "📦"
    val nombreCat = partesCat.getOrNull(1) ?: movimiento.categoriaNombre

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(PurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movimiento.descripcion.ifBlank { nombreCat }, 
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "$nombreCat - ${movimiento.fecha.toDisplayDate()}", fontSize = 12.sp, color = TextGray)
        }
        Text(
            text = "${if (isIngreso) "+ " else "- "}$${"%,.2f".format(movimiento.monto)}",
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isIngreso) GreenIncome else RedExpense
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box {
            IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Opciones", tint = TextGray, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar Movimiento") },
                    onClick = {
                        menuAbierto = false
                        onEditClick()
                    },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }
        }
    }
}

@Composable
fun BalanceGraphCard(
    balance: Double,
    ingresos: Double,
    gastos: Double,
    onClick: () -> Unit = {}
) {
    val weeklyData = listOf(
        Pair(70f, 40f), // S1
        Pair(100f, 65f), // S2
        Pair(60f, 85f), // S3
        Pair(80f, 35f)  // S4
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Purple)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text("Balance del mes", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                    Text(
                        text = "$${"%,.0f".format(balance)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    WeeklyBarGraph(data = weeklyData)
                }

                Column(
                    modifier = Modifier.weight(0.7f),
                    horizontalAlignment = Alignment.End
                ) {
                    Spacer(modifier = Modifier.height(35.dp))
                    
                    Text("Ingresos", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    Text(
                        text = "$${"%,.0f".format(ingresos)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Gastos", fontSize = 13.sp, color = Color(0xFFFF9494))
                    Text(
                        text = "$${"%,.0f".format(gastos)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9494)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyBarGraph(data: List<Pair<Float, Float>>) {
    Column {
        Row(
            modifier = Modifier.height(90.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, (income, expense) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(income / 100f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(expense / 100f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFFFF9494).copy(alpha = 0.7f))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "S${index + 1}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ingresos", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF9494)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gastos", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}
