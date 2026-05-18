package jesusernesto.lopezibarra.gestorgastos.screens.user

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * CAMBIO NECESARIO EN ForgetPassScreen.kt
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Antes el flujo era:
 *   1. Usuario ingresa email → app busca en Room → muestra campos de nueva contraseña
 *   2. Usuario escribe nueva contraseña → app actualiza Room
 *
 * Ahora el flujo es:
 *   1. Usuario ingresa email → Firebase manda correo de recuperación
 *   2. Usuario hace clic en el enlace del correo → Firebase cambia la contraseña en la nube
 *   3. Al volver a la app, simplemente hace login normal con su nueva contraseña
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * REEMPLAZA EL CONTENIDO DE ForgetPassScreen.kt con esto:
 * ─────────────────────────────────────────────────────────────────────────────
 */



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.ui.theme.Purple
import jesusernesto.lopezibarra.gestorgastos.ui.theme.PurpleLight
import jesusernesto.lopezibarra.gestorgastos.ui.theme.TextGray

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ForgotpasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Recuperar contraseña", fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Te enviaremos un correo con un enlace para restablecer tu contraseña.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        if (uiState.emailEnviado) {
            // ── Estado de éxito ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ Correo enviado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Revisa tu bandeja de entrada en $email y sigue las instrucciones del correo.",
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Volver al inicio de sesión", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

        } else {
            // ── Formulario de email ──────────────────────────────────────────
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }

            Text("Correo Electrónico", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = TextGray, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.resetError() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = PurpleLight, focusedBorderColor = Purple,
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.enviarEmailRecuperacion(email) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                enabled = !uiState.cargando
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar correo de recuperación", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

