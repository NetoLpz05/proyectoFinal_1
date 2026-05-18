package jesusernesto.lopezibarra.gestorgastos.screens.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import jesusernesto.lopezibarra.gestorgastos.R
import jesusernesto.lopezibarra.gestorgastos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    uiState: AuthUiState,
    onSubmit: (
        nombre: String,
        apellido: String,
        email: String,
        contrasena: String,
        confirmacion: String,
        fechaNacimiento: String,
        genero: String,
        telefono: String,
        fotoPerfil: String?
    ) -> Unit,
    onLoginClick: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Hombre", "Mujer", "Otro")
    var telefono by remember { mutableStateOf("") }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoPerfilUri = uri
    }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        birthDate = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")).format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = Purple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
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
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding(5.dp))

        Image(
            painter = painterResource(id = R.drawable.logoapp),
            contentDescription = "Logo",
            modifier = Modifier
                .size(162.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crear Cuenta",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = DarkNavy,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(PurpleLight.copy(alpha = 0.2f))
                .border(2.dp, PurpleLight, CircleShape)
                .clickable {
                    photoLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (fotoPerfilUri != null) {
                AsyncImage(
                    model = fotoPerfilUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Text(
            text = "Agregar foto de perfil",
            fontSize = 12.sp,
            color = Purple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp).clickable { photoLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.mensaje,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        RegisterLabel("Nombre")
        RegisterTextField(value = nombre, onValueChange = { nombre = it }, placeholder = "Tu nombre")

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Apellido")
        RegisterTextField(value = apellido, onValueChange = { apellido = it }, placeholder = "Tu apellido")

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Correo Electrónico")
        RegisterTextField(value = email, onValueChange = { email = it }, placeholder = "correo@ejemplo.com")

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Teléfono")
        RegisterTextField(
            value = telefono,
            onValueChange = { telefono = it },
            placeholder = "6441234567"
        )

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Fecha de nacimiento")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                placeholder = {
                    Text("Seleccionar fecha", color = TextGray.copy(alpha = 0.5f))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = PurpleLight,
                    disabledContainerColor = Color.White,
                    disabledTextColor = DarkNavy,
                    disabledPlaceholderColor = TextGray.copy(alpha = 0.5f),
                    disabledLeadingIconColor = Purple
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Género")
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .menuAnchor(),
                placeholder = { Text("Seleccionar género", color = TextGray.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(10.dp),
                colors = registerTextFieldColors()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { gender = option; expanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Contraseña")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                }
            },
            colors = registerTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        RegisterLabel("Confirmar Contraseña")
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                }
            },
            colors = registerTextFieldColors()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSubmit(nombre, apellido, email, password, confirmPassword, birthDate, gender, telefono, fotoPerfilUri?.toString())
            },
            modifier = Modifier.fillMaxWidth().height(45.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            enabled = uiState !is AuthUiState.Cargando
        ) {
            if (uiState is AuthUiState.Cargando) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear Cuenta", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                append("¿Ya tienes cuenta? ")
                withStyle(SpanStyle(color = Purple)) { append("Iniciar Sesión") }
            },
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.clickable { onLoginClick() }.padding(bottom = 28.dp)
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: UsuarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Exito) {
            viewModel.resetState()
            onRegisterClick()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onSubmit = { n, a, e, p, cp, b, g, t, f ->
            viewModel.registrar(
                nombre = n,
                apellido = a,
                email = e,
                contrasena = p,
                confirmacion = cp,
                fechaNacimiento = b,
                genero = g,
                telefono = t,
                fotoPerfil = f
            )
        },
        onLoginClick = onLoginClick
    )
}

@Composable
private fun RegisterLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = TextGray,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

@Composable
private fun RegisterTextField(value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        colors = registerTextFieldColors()
    )
}

@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = PurpleLight, focusedBorderColor = Purple,
    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
)

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreenContent(
            uiState = AuthUiState.Idle,
            onSubmit = { _, _, _, _, _, _, _, _, _ -> },
            onLoginClick = {}
        )
    }
}
