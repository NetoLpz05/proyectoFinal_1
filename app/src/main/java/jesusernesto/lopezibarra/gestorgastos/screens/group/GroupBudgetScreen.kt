package jesusernesto.lopezibarra.gestorgastos.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import jesusernesto.lopezibarra.gestorgastos.data.enums.CategoriaGasto
import jesusernesto.lopezibarra.gestorgastos.screens.components.GastoTotalCard
import jesusernesto.lopezibarra.gestorgastos.screens.components.GastoTotalCardData
import jesusernesto.lopezibarra.gestorgastos.ui.theme.Background
import jesusernesto.lopezibarra.gestorgastos.ui.theme.DarkNavy
import jesusernesto.lopezibarra.gestorgastos.ui.theme.Purple
import jesusernesto.lopezibarra.gestorgastos.ui.theme.PurpleGrey40
import jesusernesto.lopezibarra.gestorgastos.ui.theme.White

//DATOS MOCK

val cardEjemplo = GastoTotalCardData(
    montoTotal         = "$1,200.00",
    totalParticipantes = 5
)



@Composable
fun ViajesScreen(
    cardData: GastoTotalCardData = cardEjemplo,
    gastos: List<GastoReciente>  = gastosEjemplo,
    codigoGrupo: String          = "AX92L",
    onAtras: () -> Unit          = {},
    onFiltrar: () -> Unit        = {},
    onNavegacion: (Int) -> Unit  = {}
) {
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
                    IconButton(onClick = onAtras) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Purple
                        )
                    }
                    Text(
                        text = "Viajes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkNavy,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
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
                    data = cardData,
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
                        text = "Gastos Recientes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    IconButton(
                        onClick = onFiltrar,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Filtrar",
                            tint = PurpleGrey40,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            items(gastos, key = { it.id }) { gasto ->
                GastoItem(gasto = gasto)
                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CÓDIGO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xCCFFFFFF),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = codigoGrupo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@Composable
private fun GastoItem(gasto: GastoReciente) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoCategoria(categoria = gasto.categoria)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkNavy
                )
                Spacer(Modifier.height(3.dp))
                Row {
                    Text(text = "Pagado por  ", fontSize = 13.sp, color = PurpleGrey40)
                    Text(
                        text = gasto.pagadoPor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = gasto.monto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Spacer(Modifier.height(3.dp))
                Text(text = gasto.fecha, fontSize = 12.sp, color = PurpleGrey40)
            }
        }
    }
}


@Composable
private fun IconoCategoria(categoria: CategoriaGasto) {
    val (fondo, emoji) = when (categoria) {
        CategoriaGasto.COMIDA     -> Background     to "🍕"
        CategoriaGasto.TRANSPORTE -> Background to "🚗"
        CategoriaGasto.BEBIDAS    -> Background    to "🍹"
        CategoriaGasto.OTRO       -> Background       to "📦"
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fondo)
    ) {
        Text(text = emoji, fontSize = 22.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ViajesScreenPreview() {
    MaterialTheme {
        ViajesScreen()
    }
}

data class GastoReciente(val id: Int,
                         val nombre: String,
                         val categoria: CategoriaGasto,
                         val monto: String,
                         val pagadoPor: String,
                         val fecha: String)

val gastosEjemplo = listOf(
    GastoReciente(1, "Cena en La Parrilla",  CategoriaGasto.COMIDA,     "$280.00", "Mateo",  "14 MAY"),
    GastoReciente(2, "Renta de Auto",         CategoriaGasto.TRANSPORTE, "$650.00", "Carlos", "12 MAY"),
    GastoReciente(3, "Bebidas en Coco Bongo", CategoriaGasto.BEBIDAS,    "$270.00", "Ana",    "11 MAY")
)