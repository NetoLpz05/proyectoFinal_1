package jesusernesto.lopezibarra.gestorgastos.screens.group

import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*

@Composable
fun NewExpenseScreen(onBack: () -> Unit, onSave: () -> Unit) {
    var amount by remember { mutableStateOf("0.00") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("22/03/2026") }
    var selectedCategory by remember { mutableStateOf("Entretenimiento") }

    val dashColor = PurpleLight // Color para los bordes punteados

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header ---
        HeaderViajes(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón Gasto Grupal ---
            OutlinedButton(
                onClick = { /* Lógica grupal */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RedGasto)
            ) {
                Text("Gasto Grupal", color = RedGasto, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Monto ---
            Text("MONTO DEL GASTO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = TextGray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(amount, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = TextGray)
            }

            // --- Campos de texto ---
            SectionLabel("DESCRIPCIÓN")
            EditFieldSimple(value = description, onValueChange = { description = it })

            Spacer(modifier = Modifier.height(12.dp))

            SectionLabel("FECHA")
            EditFieldSimple(value = date, onValueChange = {}, icon = Icons.Outlined.CalendarMonth)

            Spacer(modifier = Modifier.height(12.dp))

            // --- Categorías ---
            SectionLabel("CATEGORÍA")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryIconItem("Vivienda", Icons.Outlined.Home, selectedCategory == "Vivienda") { selectedCategory = "Vivienda" }
                CategoryIconItem("Entretenimiento", Icons.Outlined.Tv, selectedCategory == "Entretenimiento") { selectedCategory = "Entretenimiento" }
                CategoryIconItem("Transporte", Icons.Outlined.DirectionsCar, selectedCategory == "Transporte") { selectedCategory = "Transporte" }
                CategoryIconItem("Alimentación", Icons.Outlined.Restaurant, selectedCategory == "Alimentación") { selectedCategory = "Alimentación" }
                CategoryIconItem("Salud", Icons.Outlined.FavoriteBorder, selectedCategory == "Salud") { selectedCategory = "Salud" }
                CategoryIconItem("Otros", Icons.Outlined.GridView, selectedCategory == "Otros") { selectedCategory = "Otros" }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Método de Pago ---
            SectionLabel("MÉTODO DE PAGO")
            PaymentMethodCard()

            Spacer(modifier = Modifier.height(16.dp))

            // --- Ubicación (Dashed) ---
            DashedBox(color = dashColor) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Purple, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Agregar ubicación...", color = TextGray, fontStyle = FontStyle.Italic, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Adjuntar Recibo (Dashed) ---
            DashedBox(color = dashColor) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjuntar foto del recibo", color = TextGray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Botón Guardar ---
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Guardar Gasto", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HeaderViajes(onBack: () -> Unit) {
    Surface(shadowElevation = 2.dp) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Purple)
            }
            Text("Viajes", modifier = Modifier.align(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryIconItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, if (isSelected) Purple else Color.Transparent, RoundedCornerShape(12.dp))
                .background(if (isSelected) Color.White else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Purple else TextGray.copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
        }
        Text(label, fontSize = 11.sp, color = if (isSelected) Purple else TextGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PaymentMethodCard() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = Purple
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Tarjeta de Débito", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("MASTERCARD - 4291", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Text("Cambiar >", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DashedBox(color: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
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
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun EditFieldSimple(value: String, onValueChange: (String) -> Unit, icon: ImageVector? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = Color.Black) } },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = PurpleLight,
            focusedBorderColor = Purple
        )
    )
}

@Preview(showBackground = true)
@Composable
fun NewExpensePreview() {
    NewExpenseScreen(onBack = {}, onSave = {})
}