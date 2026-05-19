package jesusernesto.lopezibarra.gestorgastos.screens.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.entity.GrupoEntity
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.GrupoUiState
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.GrupoViewModel
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*

@Composable
fun MisGruposContent(
    uiState: GrupoUiState,
    onAtras: () -> Unit = {},
    onAbrirGrupo: (GrupoEntity) -> Unit = {},
    onCrearGrupo: () -> Unit = {},
    onBuscarCodigo: (String) -> Unit = {},
    onEliminarGrupo: (GrupoEntity) -> Unit = {},
    onResetError: () -> Unit = {}
) {
    var mostrarDialogoCodigo by remember { mutableStateOf(false) }
    var codigoInput by remember { mutableStateOf("") }
    var grupoAEliminar by remember { mutableStateOf<GrupoEntity?>(null) }

    if (mostrarDialogoCodigo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCodigo = false
                codigoInput = ""
                onResetError()
            },
            title = { Text("Unirse a un grupo") },
            text = {
                Column {
                    Text("Ingresa el código del grupo:", color = TextGray, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codigoInput,
                        onValueChange = { codigoInput = it.uppercase().take(6) },
                        placeholder = { Text("Ej. AX92L3") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PurpleLight,
                            focusedBorderColor = Purple
                        )
                    )
                    if (uiState.error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    if (uiState.codigoBuscado != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "✓ Te uniste a: ${uiState.codigoBuscado.nombre}",
                            color = GreenIncome,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onBuscarCodigo(codigoInput) }) {
                    Text("Buscar", color = Purple)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCodigo = false
                    codigoInput = ""
                    onResetError()
                }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    grupoAEliminar?.let { grupo ->
        AlertDialog(
            onDismissRequest = { grupoAEliminar = null },
            title = { Text("Eliminar grupo") },
            text = { Text("¿Seguro que quieres eliminar \"${grupo.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarGrupo(grupo)
                    grupoAEliminar = null
                }) { Text("Eliminar", color = RedExpense) }
            },
            dismissButton = {
                TextButton(onClick = { grupoAEliminar = null }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAtras) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Mis Grupos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 12.dp).weight(1f)
                    )
                    IconButton(onClick = { mostrarDialogoCodigo = true }) {
                        Icon(
                            Icons.Outlined.QrCodeScanner,
                            contentDescription = "Unirse con código",
                            tint = Purple
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            if (uiState.grupos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Sin grupos todavía",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Crea uno o únete con un código",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            } else {
                items(uiState.grupos, key = { it.idGrupo }) { grupo ->
                    GrupoCard(
                        grupo = grupo,
                        onClick = { onAbrirGrupo(grupo) },
                        onEliminar = { grupoAEliminar = grupo }
                    )
                }
            }

            item {
                CrearGrupoCard(onClick = onCrearGrupo)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}



@Composable
fun MisGruposScreen(
    onAtras: () -> Unit = {},
    onAbrirGrupo: (GrupoEntity) -> Unit = {},
    onCrearGrupo: () -> Unit = {},
    viewModel: GrupoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoCodigo by remember { mutableStateOf(false) }
    var codigoInput by remember { mutableStateOf("") }
    var grupoAEliminar by remember { mutableStateOf<GrupoEntity?>(null) }

    // Diálogo unirse por código
    if (mostrarDialogoCodigo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCodigo = false
                codigoInput = ""
                viewModel.resetError()
            },
            title = { Text("Unirse a un grupo") },
            text = {
                Column {
                    Text("Ingresa el código del grupo:", color = TextGray, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codigoInput,
                        onValueChange = { codigoInput = it.uppercase().take(6) },
                        placeholder = { Text("Ej. AX92L3") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PurpleLight,
                            focusedBorderColor = Purple
                        )
                    )
                    if (uiState.error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    if (uiState.codigoBuscado != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "✓ Te uniste a: ${uiState.codigoBuscado!!.nombre}",
                            color = GreenIncome, fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.buscarPorCodigo(codigoInput) }) {
                    Text("Buscar", color = Purple)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCodigo = false
                    codigoInput = ""
                    viewModel.resetError()
                }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    grupoAEliminar?.let { grupo ->
        AlertDialog(
            onDismissRequest = { grupoAEliminar = null },
            title = { Text("Eliminar grupo") },
            text = { Text("¿Seguro que quieres eliminar \"${grupo.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarGrupo(grupo)
                    grupoAEliminar = null
                }) { Text("Eliminar", color = RedExpense) }
            },
            dismissButton = {
                TextButton(onClick = { grupoAEliminar = null }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "Mis Grupos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 12.dp).weight(1f)
                    )
                    // Botón unirse por código
                    IconButton(onClick = { mostrarDialogoCodigo = true }) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Unirse con código",
                            tint = Purple)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            if (uiState.grupos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Sin grupos todavía", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("Crea uno o únete con un código",
                                fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            } else {
                items(uiState.grupos, key = { it.idGrupo }) { grupo ->
                    GrupoCard(
                        grupo = grupo,
                        onClick = { onAbrirGrupo(grupo) },
                        onEliminar = { grupoAEliminar = grupo }
                    )
                }
            }

            item {
                CrearGrupoCard(onClick = onCrearGrupo)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GrupoCard(
    grupo: GrupoEntity,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiPorTipo(grupo.tipo), fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(grupo.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(grupo.tipo, fontSize = 13.sp, color = TextGray)
                Text("Código: ${grupo.codigoInvitacion}", fontSize = 11.sp,
                    color = Purple, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar",
                    tint = RedExpense, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CrearGrupoCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(width = 1.dp, color = PurpleLight),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.GroupAdd, contentDescription = null,
                tint = PurpleGrey40, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Crear un nuevo grupo", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Divide gastos de cenas o viajes", fontSize = 12.sp,
                color = PurpleGrey40, textAlign = TextAlign.Center)
        }
    }
}

val gruposFake = listOf(
    GrupoEntity(
        idGrupo = 1,
        nombre = "Viaje Cancún",
        tipo = "Viaje",
        codigoInvitacion = "AX92L3",
        imagen = "✈️"
    ),
    GrupoEntity(
        idGrupo = 2,
        nombre = "Departamento",
        tipo = "Familiar",
        codigoInvitacion = "HOME22",
        imagen = "🏠"
    ),
    GrupoEntity(
        idGrupo = 3,
        nombre = "Proyecto Escolar",
        tipo = "Escuela",
        codigoInvitacion = "UNI789",
        imagen = "🎓"
    ),
    GrupoEntity(
        idGrupo = 4,
        nombre = "Oficina",
        tipo = "Trabajo",
        codigoInvitacion = "WORK55",
        imagen = "💼"
    ),
    GrupoEntity(
        idGrupo = 5,
        nombre = "Cumpleaños Ana",
        tipo = "Evento",
        codigoInvitacion = "PARTY1",
        imagen = "🎉"
    )
)

private fun emojiPorTipo(tipo: String) = when (tipo) {
    "Familiar" -> "👨‍👩‍👧‍👦"
    "Trabajo"  -> "💼"
    "Pareja"   -> "💑"
    "Escuela"  -> "🎓"
    "Evento"   -> "🎉"
    "Viaje"    -> "✈️"
    else       -> "👥"
}

@Preview(showBackground = true)
@Composable
fun MisGruposScreenPreview() {

    val fakeState = GrupoUiState(
        grupos = gruposFake
    )

    MaterialTheme {
        MisGruposContent(
            uiState = fakeState,
            onAtras = {},
            onAbrirGrupo = {},
            onCrearGrupo = {},
            onBuscarCodigo = {},
            onEliminarGrupo = {},
            onResetError = {}
        )
    }
}