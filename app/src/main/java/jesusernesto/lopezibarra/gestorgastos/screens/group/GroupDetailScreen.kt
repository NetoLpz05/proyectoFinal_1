package jesusernesto.lopezibarra.gestorgastos.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import jesusernesto.lopezibarra.gestorgastos.data.entity.GastoGrupoEntity
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.GrupoViewModel
import jesusernesto.lopezibarra.gestorgastos.screens.components.GastoTotalCard
import jesusernesto.lopezibarra.gestorgastos.screens.components.GastoTotalCardData
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GroupDetailScreen(
    idGrupo: Int,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    viewModel: GrupoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.grupos.find { it.idGrupo == idGrupo }

    LaunchedEffect(idGrupo) {
        viewModel.seleccionarGrupo(idGrupo)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Background
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Purple)
                    }
                    Text(
                        text = group?.nombre ?: "Detalle del Grupo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkNavy,
                        modifier = Modifier.padding(start = 12.dp).weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Purple,
                contentColor = White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Gasto")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                GastoTotalCard(
                    data = GastoTotalCardData(
                        montoTotal = "$${"%,.2f".format(uiState.totalGastos)}",
                        totalParticipantes = uiState.miembros.size,
                        gastosTotal = "$${"%,.0f".format(uiState.totalGastos)}"
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gastos del Grupo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            if (uiState.gastosGrupo.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay gastos registrados", color = TextGray)
                    }
                }
            } else {
                items(uiState.gastosGrupo) { gasto ->
                    val pagador = uiState.miembros.find { it.idUsuario == gasto.idUsuarioPago }?.nombre ?: "Usuario"
                    GastoGrupoItem(gasto = gasto, pagador = pagador)
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CÓDIGO", fontSize = 11.sp, color = Color(0xCCFFFFFF), letterSpacing = 1.sp)
                            Text(group?.codigoInvitacion ?: "-----", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = 2.sp)
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun GastoGrupoItem(gasto: GastoGrupoEntity, pagador: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Background)
            ) {
                Text(text = "📦", fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                Row {
                    Text("Pagado por ", fontSize = 13.sp, color = TextGray)
                    Text(pagador, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Purple)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("$${"%,.2f".format(gasto.monto)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(gasto.fecha)), fontSize = 12.sp, color = TextGray)
            }
        }
    }
}
