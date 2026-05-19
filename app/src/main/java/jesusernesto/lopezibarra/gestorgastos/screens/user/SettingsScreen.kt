package jesusernesto.lopezibarra.gestorgastos.screens.user

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.Notifications.NotificationScheduler
import jesusernesto.lopezibarra.gestorgastos.screens.components.AppTopBar
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*


@Composable
fun AlertasScreen(
    onBack: () -> Unit = {},
    viewModel: AlertaViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var umbralLocal by remember { mutableStateOf(uiState.umbralGlobal) }
    var alertasHabilitadasLocal by remember { mutableStateOf(uiState.alertasHabilitadas) }
    val categoriasState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            categorias.forEach { (_, nombre) -> put(nombre, true) }
        }
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            snackbarHostState.showSnackbar("Configuración guardada")
            viewModel.resetGuardado()
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppTopBar(title = "Alertas", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Habilitar alertas", fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Recibe notificaciones en tiempo real",
                                fontSize = 13.sp, color = TextGray
                            )
                        }
                        Switch(
                            checked = alertasHabilitadasLocal,
                            onCheckedChange = { alertasHabilitadasLocal = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Purple
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Umbral de Notificación", fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${(umbralLocal * 100).toInt()}%", fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = Purple
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Slider(
                            value = umbralLocal,
                            onValueChange = { umbralLocal = it },
                            valueRange = 0f..1f,
                            enabled = alertasHabilitadasLocal,
                            colors = SliderDefaults.colors(
                                thumbColor = Purple,
                                activeTrackColor = Purple,
                                inactiveTrackColor = PurpleLight
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("0%", "25%", "50%", "75%", "100%").forEach { label ->
                                Text(label, fontSize = 11.sp, color = TextGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\"Notifícame cuando el presupuesto llegue al ${(umbralLocal * 100).toInt()}%\"",
                            fontSize = 13.sp, color = TextGray,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Alertas por Categoría", fontWeight = FontWeight.Bold,
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                categorias.forEach { (emoji, nombre) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                nombre, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = categoriasState[nombre] ?: true,
                                onCheckedChange = { categoriasState[nombre] = it },
                                enabled = alertasHabilitadasLocal,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Purple
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column {
                    OutlinedButton(
                            onClick = {
                                val prefs = context.getSharedPreferences("alertas_config", Context.MODE_PRIVATE)
                                val editor = prefs.edit()
                                prefs.all.keys
                                    .filter { it.startsWith("alerta_") || it.startsWith("excedido_") }
                                    .forEach { editor.remove(it) }
                                editor.apply()
                                NotificationScheduler.ejecutarAhora(context)
                            },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Purple)
                    ) {
                        Text(
                            "🔔 Probar notificación",
                            color = Purple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.guardarConfiguracion(umbralLocal, alertasHabilitadasLocal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) {
                        Text(
                            "Guardar Configuración",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}



data class AlertasUiState(
    val alertasHabilitadas: Boolean = true,
    val umbralGlobal: Float = 0.5f,
    val guardado: Boolean = false
)

@Composable
fun AlertasContent(
    uiState: AlertasUiState,
    categorias: List<Pair<String, String>>,
    categoriasSeleccionadas: Map<String, Boolean>,
    onCategoriaChange: (String, Boolean) -> Unit,
    onAlertasHabilitadasChange: (Boolean) -> Unit,
    onUmbralChange: (Float) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var umbralLocal by remember { mutableStateOf(uiState.umbralGlobal) }
    var alertasHabilitadasLocal by remember { mutableStateOf(uiState.alertasHabilitadas) }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            snackbarHostState.showSnackbar("Configuración guardada")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppTopBar(title = "Alertas", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Habilitar alertas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Recibe notificaciones en tiempo real",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = alertasHabilitadasLocal,
                            onCheckedChange = {
                                alertasHabilitadasLocal = it
                                onAlertasHabilitadasChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Purple
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Umbral de Notificación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${(umbralLocal * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Purple
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Slider(
                            value = umbralLocal,
                            onValueChange = {
                                umbralLocal = it
                                onUmbralChange(it)
                            },
                            valueRange = 0f..1f,
                            enabled = alertasHabilitadasLocal,
                            colors = SliderDefaults.colors(
                                thumbColor = Purple,
                                activeTrackColor = Purple,
                                inactiveTrackColor = PurpleLight
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("0%", "25%", "50%", "75%", "100%").forEach { label ->
                                Text(label, fontSize = 11.sp, color = TextGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\"Notifícame cuando el presupuesto llegue al ${(umbralLocal * 100).toInt()}%\"",
                            fontSize = 13.sp,
                            color = TextGray,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Alertas por Categoría",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                categorias.forEach { (emoji, nombre) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                nombre,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = categoriasSeleccionadas[nombre] ?: true,
                                onCheckedChange = { onCategoriaChange(nombre, it) },
                                enabled = alertasHabilitadasLocal,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Purple
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = onGuardar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text(
                        "Guardar Configuración",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AlertasScreenPreview() {
    val fakeCategorias = listOf(
        "💼" to "Trabajo",
        "🎉" to "Evento",
        "✈️" to "Viaje",
        "🎓" to "Escuela"
    )

    val fakeSeleccionadas = mapOf(
        "Trabajo" to true,
        "Evento" to false,
        "Viaje" to true,
        "Escuela" to true
    )

    MaterialTheme {
        AlertasContent(
            uiState = AlertasUiState(
                alertasHabilitadas = true,
                umbralGlobal = 0.5f,
                guardado = false
            ),
            categorias = fakeCategorias,
            categoriasSeleccionadas = fakeSeleccionadas,
            onCategoriaChange = { _, _ -> },
            onAlertasHabilitadasChange = { },
            onUmbralChange = { },
            onGuardar = { },
            onBack = {}
        )
    }
}
val categorias = listOf(
    "🏠" to "Vivienda",
    "🎬" to "Entretenimiento",
    "🚗" to "Transporte",
    "🍔" to "Alimentación",
    "❤️" to "Salud",
    "📦" to "Otros",
)
