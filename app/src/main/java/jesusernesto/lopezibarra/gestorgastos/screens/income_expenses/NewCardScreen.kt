package jesusernesto.lopezibarra.gestorgastos.screens.income_expenses

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.data.viewModel.MetodoPagoViewModel
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*

@Composable
fun AddCardScreen(onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: MetodoPagoViewModel = viewModel()) {
    AddCardContent(onBack = onBack,
        onSaveClick = { nombre, numero, esCredito ->
            viewModel.guardarTarjeta(
                nombre = nombre,
                numeroCompleto = numero,
                esCredito = esCredito,
                onSuccess = onSave)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardContent(
    onBack: () -> Unit,
    onSaveClick: (String, String, Boolean) -> Unit
) {
    var nombreTarjeta by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaExp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var propietario by remember { mutableStateOf("") }
    var esCredito by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Surface(shadowElevation = 4.dp, color = Color.White) {
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
                    text = "Nueva tarjeta",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = DarkNavy,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {

            Spacer(modifier = Modifier.height(20.dp))
            CardPreview(number = numeroTarjeta, holder = propietario, expiry = fechaExp, name = nombreTarjeta)
            Spacer(modifier = Modifier.height(28.dp))

            CardFieldLabel("NOMBRE DE LA TARJETA")
            CardTextField(
                value = nombreTarjeta,
                onValueChange = { nombreTarjeta = it },
                placeholder = "Ej. Amex Oro",
                keyboardType = KeyboardType.Text,
                leadingIcon = Icons.Outlined.Label
            )

            Spacer(modifier = Modifier.height(16.dp))

            CardFieldLabel("TIPO DE TARJETA")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = !esCredito,
                    onClick = { esCredito = false },
                    label = { Text("Débito") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleLight,
                        selectedLabelColor = Purple
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !esCredito,
                        borderColor = PurpleLight,
                        selectedBorderColor = Purple
                    )
                )
                FilterChip(
                    selected = esCredito,
                    onClick = { esCredito = true },
                    label = { Text("Crédito") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleLight,
                        selectedLabelColor = Purple
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = esCredito,
                        borderColor = PurpleLight,
                        selectedBorderColor = Purple
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CardFieldLabel("NÚMERO DE TARJETA")
            CardTextField(value = numeroTarjeta,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }.take(16)
                    numeroTarjeta = digits.chunked(4).joinToString(" ")
                },
                placeholder = "0000 0000 0000 0000",
                keyboardType = KeyboardType.Number,
                leadingIcon = Icons.Outlined.CreditCard
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    CardFieldLabel("FECHA DE VENCIMIENTO")
                    OutlinedTextField(
                        value = fechaExp,
                        onValueChange = {
                            val digits = it.filter { c -> c.isDigit() }.take(4)
                            fechaExp = when {
                                digits.length > 2 -> "${digits.take(2)}/${digits.drop(2)}"
                                else -> digits
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(10.dp), singleLine = true,
                        placeholder = { Text("MM/AA", color = TextGray.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null,
                                tint = Purple, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = cardTextFieldColors()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    CardFieldLabel("CÓDIGO DE SEGURIDAD")
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            cvv = it.filter { c -> c.isDigit() }.take(4)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(10.dp), singleLine = true,
                        placeholder = { Text("CVV", color = TextGray.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null,
                                tint = Purple, modifier = Modifier.size(18.dp))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = cardTextFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CardFieldLabel("NOMBRE DEL TITULAR")
            CardTextField(value = propietario, onValueChange = { propietario = it }, placeholder = "Como aparece en la tarjeta",
                keyboardType = KeyboardType.Text, leadingIcon = Icons.Outlined.Person)

            Spacer(modifier = Modifier.height(30.dp))
        }

        Surface(shadowElevation = 8.dp, color = Color.White) {
            Button(
                onClick = {
                    if (nombreTarjeta.isNotBlank() && numeroTarjeta.replace(" ", "").length >= 4) {
                        onSaveClick(
                            nombreTarjeta,
                            numeroTarjeta.replace(" ", ""),
                            esCredito
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(46.dp),
                shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {

                Text(text = "Guardar Tarjeta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun CardPreview(number: String, holder: String, expiry: String, name: String) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))
            .background(Purple).padding(24.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(width = 40.dp, height = 30.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFFFD700).copy(alpha = 0.8f)))
                Text(text = name.uppercase(), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text(text = number.ifBlank { "•••• •••• •••• ••••" }, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = Color.White, letterSpacing = 2.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("TITULAR", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(text = holder.ifBlank { "NOMBRE" }.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("VENCE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(text = expiry.ifBlank { "MM/AA" }, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CardFieldLabel(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        color = TextGray, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun CardTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType,
    leadingIcon: ImageVector) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = cardTextFieldColors()
    )
}

@Composable
private fun cardTextFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = PurpleLight, focusedBorderColor = Purple,
    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
)

@Preview(showBackground = true)
@Composable
fun AddCardScreenPreview() {
    AddCardContent(onBack = {}, onSaveClick = { _, _, _ -> })
}
