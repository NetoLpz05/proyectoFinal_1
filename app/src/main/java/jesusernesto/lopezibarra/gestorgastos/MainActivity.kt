package jesusernesto.lopezibarra.gestorgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import jesusernesto.lopezibarra.gestorgastos.screens.MainScreen
import jesusernesto.lopezibarra.gestorgastos.screens.user.ForgotPasswordScreen
import jesusernesto.lopezibarra.gestorgastos.screens.user.LoginScreen
import jesusernesto.lopezibarra.gestorgastos.screens.user.RegisterScreen
import jesusernesto.lopezibarra.gestorgastos.ui.theme.GestorgastosTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            
            GestorgastosTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { isDarkMode = it }
                    )
                }
            }
        }
    }
}

private enum class Screen { LOGIN, REGISTER, MAIN, FORGOT_PASSWORD }

@Composable
private fun AppRoot(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    var pantallaActual by remember { mutableStateOf(Screen.LOGIN) }

    when (pantallaActual) {
        Screen.LOGIN -> LoginScreen(
            onLoginClick = { pantallaActual = Screen.MAIN },
            onRegisterClick = { pantallaActual = Screen.REGISTER },
            onForgotPasswordClick = { pantallaActual = Screen.FORGOT_PASSWORD }
        )

        Screen.REGISTER -> RegisterScreen(
            onRegisterClick = { pantallaActual = Screen.MAIN },
            onLoginClick = { pantallaActual = Screen.LOGIN }
        )

        Screen.MAIN -> MainScreen(
            onLogout = { pantallaActual = Screen.LOGIN },
            isDarkMode = isDarkMode,
            onDarkModeChange = onDarkModeChange
        )

        Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
            onBackToLogin = { pantallaActual = Screen.LOGIN }
        )
    }
}
