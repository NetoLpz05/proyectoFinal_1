package jesusernesto.lopezibarra.gestorgastos.screens.group


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import jesusernesto.lopezibarra.gestorgastos.ui.theme.Background
import jesusernesto.lopezibarra.gestorgastos.ui.theme.DarkNavy
import jesusernesto.lopezibarra.gestorgastos.ui.theme.White
import jesusernesto.lopezibarra.gestorgastos.ui.theme.Purple
import jesusernesto.lopezibarra.gestorgastos.ui.theme.PurpleLight
import jesusernesto.lopezibarra.gestorgastos.ui.theme.TextGray


@Composable
fun CrearGrupoContent(
    titulo: String,
    onTituloChange: (String) -> Unit,
    selectedType: String,
    onTypeChange: (String) -> Unit,
    error: String? = null,
    cargando: Boolean = false,
    onBack: () -> Unit = {},
    onCrear: () -> Unit = {},
    onCancelar: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Purple)
                }
                Text(
                    text = "Crear Grupo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiPorTipo(selectedType), fontSize = 40.sp)
            }

            Spacer(Modifier.height(28.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "TÍTULO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = titulo,
                    onValueChange = onTituloChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TextGray,
                        focusedBorderColor = Purple,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "TIPO DE GRUPO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                GroupTypeGrid(
                    types = groupTypes,
                    selected = selectedType,
                    onSelect = onTypeChange
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onCrear,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Crear Grupo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text(
                    "Cancelar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}


@Composable
fun CrearGrupoScreen(
    onBack: () -> Unit = {},
    onCrear: (titulo: String, tipo: String) -> Unit = { _, _ -> },
    onCancelar: () -> Unit = {},
    viewModel: GrupoViewModel = viewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Familiar") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.grupoCreado) {
        if (uiState.grupoCreado != null) {
            onCrear(uiState.grupoCreado!!.nombre, uiState.grupoCreado!!.tipo)
            viewModel.resetGrupoCreado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Purple)
                }
                Text(
                    text = "Crear Grupo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text(emojiPorTipo(selectedType), fontSize = 40.sp)
            }

            Spacer(Modifier.height(28.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("TÍTULO", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TextGray,
                        focusedBorderColor = Purple,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("TIPO DE GRUPO", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                GroupTypeGrid(
                    types = groupTypes,
                    selected = selectedType,
                    onSelect = { selectedType = it }
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.crearGrupo(titulo, selectedType) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = !uiState.cargando
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(color = White,
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear Grupo", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, color = White)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GroupTypeGrid(
    types: List<GroupType>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val rows = types.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { type ->
                    Surface(
                        modifier = Modifier.weight(1f).height(52.dp)
                            .clickable { onSelect(type.label) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (type.label == selected) Purple
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp,
                            if (type.label == selected) Purple else TextGray)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(type.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                type.label, fontSize = 13.sp,
                                fontWeight = if (type.label == selected)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = if (type.label == selected) White else DarkNavy,
                                maxLines = 1
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun emojiPorTipo(tipo: String) = when (tipo) {
    "Familiar" -> "👨‍👩‍👧‍👦"
    "Trabajo"  -> "💼"
    "Pareja"   -> "💑"
    "Escuela"  -> "🎓"
    "Evento"   -> "🎉"
    "Viaje"    -> "✈️"
    else       -> "👥"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CrearGrupoScreenPreview() {
    var titulo by remember { mutableStateOf("Viaje Cancún") }
    var tipo by remember { mutableStateOf("Viaje") }

    MaterialTheme {
        CrearGrupoContent(
            titulo = titulo,
            onTituloChange = { titulo = it },
            selectedType = tipo,
            onTypeChange = { tipo = it },
            error = null,
            cargando = false,
            onBack = {},
            onCrear = {},
            onCancelar = {}
        )
    }
}

data class GroupType(val label: String, val emoji: String)

val groupTypes = listOf(
    GroupType("Familiar", "👨‍👩‍👧‍👦"),
    GroupType("Trabajo",  "💼"),
    GroupType("Pareja",   "💑"),
    GroupType("Escuela",  "🎓"),
    GroupType("Evento",   "🎉"),
    GroupType("Viaje",    "✈️"),
)
