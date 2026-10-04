package com.example.cajero_automatico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.cajero_automatico.data.CajeroModel
import com.example.cajero_automatico.data.SharedPreferencesManager
import com.example.cajero_automatico.ui.CajeroAppContent
import com.example.cajero_automatico.ui.CajeroViewModel
import com.example.cajero_automatico.ui.ColorAzulBanquero
import com.example.cajero_automatico.ui.ColorFondoClaro
import com.example.cajero_automatico.ui.ColorSuperficieBlanca

/**
 * Clase MainActivity
 *
 * Actividad principal de la aplicación que sirve como punto de entrada
 * para el Cajero Automático.
 *
 * Configura la persistencia con SharedPreferences, inicializa los componentes
 * POO (CajeroModel y CajeroViewModel) y renderiza la interfaz gráfica con Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar diseño de borde a borde para una interfaz moderna
        enableEdgeToEdge()

        // Inicialización de la capa de datos y modelo POO con SharedPreferences
        val prefsManager = SharedPreferencesManager(applicationContext)
        val cajeroModel = CajeroModel(prefsManager)
        val cajeroViewModel = CajeroViewModel(cajeroModel)

        setContent {
            // Aplicación del tema claro con paleta de 3 colores según requerimiento
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = ColorAzulBanquero,
                    background = ColorFondoClaro,
                    surface = ColorSuperficieBlanca
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CajeroAppContent(viewModel = cajeroViewModel)
                }
            }
        }
    }
}

/**
 * Preview de la interfaz del Cajero Automático
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCajeroApp() {
    val context = LocalContext.current
    val mockPrefs = remember { SharedPreferencesManager(context) }
    val mockModel = remember { CajeroModel(mockPrefs) }
    val mockViewModel = remember { CajeroViewModel(mockModel) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ColorAzulBanquero,
            background = ColorFondoClaro,
            surface = ColorSuperficieBlanca
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CajeroAppContent(viewModel = mockViewModel)
        }
    }
}
