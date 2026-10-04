package com.example.cajero_automatico.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cajero_automatico.data.ReciboTransaccion

// PALETA DE 3 COLORES CLAROS SEGÚN EL PROMPT:
// Color 1 (Fondo general): Azul muy claro / Blanco hielo
val ColorFondoClaro = Color(0xFFF4F7FA)
// Color 2 (Tarjetas y contenedores): Blanco puro
val ColorSuperficieBlanca = Color(0xFFFFFFFF)
// Color 3 (Acentos y botones principales): Azul suave profesional
val ColorAzulBanquero = Color(0xFF1976D2)
val ColorAzulOscuroTexto = Color(0xFF0D47A1)
val ColorGrisBorde = Color(0xFFB0BEC5)
val ColorRojoAlerta = Color(0xFFD32F2F)

/**
 * CajeroAppContent
 *
 * Composable principal que actúa como contenedor y enrutador de pantallas
 * para la simulación del cajero electrónico.
 */
@Composable
fun CajeroAppContent(viewModel: CajeroViewModel) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorFondoClaro
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado del Cajero Electrónico
                HeaderCajero()

                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje de notificación / error (como "Fondos insuficientes para este retiro")
                viewModel.mensajeNotificacion?.let { mensaje ->
                    NotificacionBanner(
                        mensaje = mensaje,
                        esError = viewModel.esErrorNotificacion,
                        onDescartar = { viewModel.descartarNotificacion() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Renderizado condicional según la pantalla activa
                when (viewModel.pantallaActual) {
                    PantallaCajero.LOGIN -> LoginScreen(viewModel)
                    PantallaCajero.MENU_PRINCIPAL -> MenuPrincipalScreen(viewModel)
                    PantallaCajero.CONSIGNAR_INGRESO_MONTO,
                    PantallaCajero.CONSIGNAR_RANURA_CONFIRMACION -> ConsignarScreen(viewModel)
                    PantallaCajero.RETIRAR_INGRESO_MONTO,
                    PantallaCajero.RETIRAR_INGRESO_PIN,
                    PantallaCajero.RETIRAR_EXITO_OPCIONES -> RetirarScreen(viewModel)
                    PantallaCajero.VISUALIZAR_SALDO -> VisualizarSaldoScreen(viewModel)
                    PantallaCajero.CAMBIAR_CLAVE -> CambiarClaveScreen(viewModel)
                }
            }

            // Diálogo Modal de Despedida: "Gracias por utilizar nuestros servicios"
            if (viewModel.mostrarMensajeDespedida) {
                DialogoDespedida(
                    onConfirmar = { viewModel.cerrarSesionYSalir() }
                )
            }

            // Diálogo Modal de Recibo Impreso
            if (viewModel.mostrarReciboDialog) {
                DialogoRecibo(
                    recibo = viewModel.reciboParaMostrar,
                    onCerrar = { viewModel.mostrarReciboDialog = false }
                )
            }
        }
    }
}

/**
 * HeaderCajero
 * Muestra el título institucional del banco y del cajero electrónico.
 */
@Composable
fun HeaderCajero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CAJERO ELECTRÓNICO",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )
            Text(
                text = "Banco Nacional - Red de Servicios",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * LoginScreen
 *
 * Pantalla inicial de autenticación.
 * Solicita si se va a utilizar Tarjeta Débito o PIN de 5 dígitos de la aplicación del banco.
 */
@Composable
fun LoginScreen(viewModel: CajeroViewModel) {
    var esTarjetaSeleccionada by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a su Cajero Automático",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorAzulOscuroTexto
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Seleccione el método de acceso:",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Opciones de selección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = esTarjetaSeleccionada,
                    onClick = { esTarjetaSeleccionada = true },
                    colors = RadioButtonDefaults.colors(selectedColor = ColorAzulBanquero)
                )
                Text(text = "Tarjeta Débito", fontSize = 15.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = !esTarjetaSeleccionada,
                    onClick = { esTarjetaSeleccionada = false },
                    colors = RadioButtonDefaults.colors(selectedColor = ColorAzulBanquero)
                )
                Text(text = "PIN de 5 dígitos de la App", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para PIN de 5 dígitos (Flotante y transparente)
            AnimatedVisibility(visible = !esTarjetaSeleccionada) {
                OutlinedTextFieldTransparente(
                    value = viewModel.inputPinAcceso,
                    onValueChange = { if (it.length <= 5) viewModel.inputPinAcceso = it },
                    label = "Ingrese PIN de 5 dígitos",
                    isPassword = true,
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.iniciarSesion(
                        esTarjeta = esTarjetaSeleccionada,
                        pinIngresado = viewModel.inputPinAcceso
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Ingresar al Cajero", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

/**
 * MenuPrincipalScreen
 *
 * Menú con las opciones principales del cajero:
 * - Consignar Dinero
 * - Retirar Dinero
 * - Visualizar Saldo
 * - Cambiar Clave
 * - Salir
 */
@Composable
fun MenuPrincipalScreen(viewModel: CajeroViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Menú Principal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )
            Text(
                text = "Seleccione la operación que desea realizar:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Consignar Dinero
            BotonMenuOpcion(
                texto = "Botón Consignar Dinero",
                onClick = { viewModel.irA(PantallaCajero.CONSIGNAR_INGRESO_MONTO) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Retirar Dinero
            BotonMenuOpcion(
                texto = "Botón Retirar Dinero",
                onClick = { viewModel.irA(PantallaCajero.RETIRAR_INGRESO_MONTO) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Visualizar Saldo
            BotonMenuOpcion(
                texto = "Botón Visualizar Saldo",
                onClick = { viewModel.irA(PantallaCajero.VISUALIZAR_SALDO) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Cambiar Clave
            BotonMenuOpcion(
                texto = "Cambiar Clave",
                onClick = { viewModel.irA(PantallaCajero.CAMBIAR_CLAVE) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Salir
            OutlinedButton(
                onClick = { viewModel.activarSalir() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Salir", fontSize = 16.sp, color = ColorRojoAlerta)
            }
        }
    }
}

/**
 * ConsignarScreen
 *
 * Implementa el flujo completo de consignación:
 * - Campo texto (flotante y transparente) Valor a consignar
 * - Insertar Dinero en ranura
 * - Botón Aceptar -> Confirmar Consignación
 * - Opciones: Mostrar Saldo / Salir ("Gracias por utilizar nuestros servicios") / Cancelar y Volver
 */
@Composable
fun ConsignarScreen(viewModel: CajeroViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Consignar Dinero",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.pantallaActual == PantallaCajero.CONSIGNAR_INGRESO_MONTO) {
                // Campo texto (flotante y transparente) Valor a consignar
                OutlinedTextFieldTransparente(
                    value = viewModel.inputMontoConsignar,
                    onValueChange = { viewModel.inputMontoConsignar = it },
                    label = "Valor a consignar ($)",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.prepararConsignacion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
                ) {
                    Text(text = "Botón Aceptar")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.irAMenuPrincipal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Botón Cancelar y volver")
                }

            } else {
                // Paso 2: Insertar Dinero en ranura y Confirmar Consignación
                Text(
                    text = "Monto a consignar: ${viewModel.model.formatearMoneda(viewModel.inputMontoConsignar.toDoubleOrNull() ?: 0.0)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Indicador visual simulación "Insertar Dinero en ranura"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorFondoClaro, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, ColorGrisBorde, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📥 Insertar Dinero en ranura",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAzulBanquero
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.confirmarConsignacion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
                ) {
                    Text(text = "Confirmar Consignación (Aceptar)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Opciones posteriores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.irA(PantallaCajero.VISUALIZAR_SALDO) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulOscuroTexto)
                    ) {
                        Text(text = "Mostrar Saldo", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.activarSalir() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Botón Salir", fontSize = 12.sp, color = ColorRojoAlerta)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.irAMenuPrincipal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(text = "Botón Cancelar y volver", fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * RetirarScreen
 *
 * Implementa el flujo de retiro de dinero con las reglas de negocio del prompt:
 * - Campo texto (flotante y transparente) Valor a retirar
 * - Digite Clave o PIN de la aplicación (Campo texto flotante y transparente)
 * - Confirmar valor del retiro
 * - Validar si el retiro supera el saldo: "Fondos insuficientes para este retiro"
 * - Opciones: Imprimir recibo, Mostrar Saldo Restante, Salir, Cancelar y volver.
 */
@Composable
fun RetirarScreen(viewModel: CajeroViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Retirar Dinero",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (viewModel.pantallaActual) {
                PantallaCajero.RETIRAR_INGRESO_MONTO -> {
                    // Campo texto (flotante y transparente) Valor a retirar
                    OutlinedTextFieldTransparente(
                        value = viewModel.inputMontoRetirar,
                        onValueChange = { viewModel.inputMontoRetirar = it },
                        label = "Valor a retirar ($)",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.prepararRetiro() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
                    ) {
                        Text(text = "Botón Aceptar")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.irAMenuPrincipal() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Botón Cancelar y volver")
                    }
                }

                PantallaCajero.RETIRAR_INGRESO_PIN -> {
                    Text(
                        text = "Valor a retirar: ${viewModel.model.formatearMoneda(viewModel.inputMontoRetirar.toDoubleOrNull() ?: 0.0)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Digite Clave o PIN de la aplicación Campo texto (flotante y transparente)
                    OutlinedTextFieldTransparente(
                        value = viewModel.inputClaveTransaccion,
                        onValueChange = { viewModel.inputClaveTransaccion = it },
                        label = "Digite Clave o PIN de la aplicación",
                        isPassword = true,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.confirmarRetiro() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
                    ) {
                        Text(text = "Confirmar valor del retiro")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.irAMenuPrincipal() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Botón Cancelar y volver")
                    }
                }

                PantallaCajero.RETIRAR_EXITO_OPCIONES -> {
                    Text(
                        text = "¡Retiro Procesado con Éxito!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAzulOscuroTexto
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Imprimir recibo
                    Button(
                        onClick = { viewModel.solicitarImprimirRecibo() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
                    ) {
                        Text(text = "Botón Imprimir recibo")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón Mostrar Saldo Restante
                    Button(
                        onClick = { viewModel.irA(PantallaCajero.VISUALIZAR_SALDO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulOscuroTexto)
                    ) {
                        Text(text = "Botón Mostrar Saldo Restante")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Salir -> Mensaje "Gracias por utilizar nuestros servicios"
                    OutlinedButton(
                        onClick = { viewModel.activarSalir() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Salir", color = ColorRojoAlerta)
                    }
                }

                else -> {}
            }
        }
    }
}

/**
 * VisualizarSaldoScreen
 *
 * Muestra el saldo actual disponible del usuario.
 * - Mostrar Saldo
 * - Botón Volver a Menú anterior
 * - Botón Salir -> Mensaje "Gracias por utilizar nuestros servicios"
 */
@Composable
fun VisualizarSaldoScreen(viewModel: CajeroViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Consulta de Saldo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta contenedora de Saldo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorFondoClaro, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, ColorAzulBanquero, shape = RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Saldo Disponible:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.model.formatearMoneda(viewModel.model.saldoActual),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAzulOscuroTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Volver a Menú anterior
            Button(
                onClick = { viewModel.irAMenuPrincipal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
            ) {
                Text(text = "Botón Volver a Menú anterior")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Salir -> Mensaje "Gracias por utilizar nuestros servicios"
            OutlinedButton(
                onClick = { viewModel.activarSalir() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Botón Salir", color = ColorRojoAlerta)
            }
        }
    }
}

/**
 * CambiarClaveScreen
 *
 * Permite la modificación de la clave de 4 dígitos.
 * - Digite Clave Actual (4 Dígitos) - Campo Texto
 * - Digite Nueva Clave (4 Dígitos) - Campo texto
 * - Confirmar
 * - Botón Volver a Menú Anterior
 * - Botón Salir -> Mensaje "Gracias por utilizar nuestros servicios"
 */
@Composable
fun CambiarClaveScreen(viewModel: CajeroViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSuperficieBlanca),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cambiar Clave",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Digite Clave Actual (4 Dígitos) - Campo Texto
            OutlinedTextFieldTransparente(
                value = viewModel.inputClaveActual,
                onValueChange = { if (it.length <= 4) viewModel.inputClaveActual = it },
                label = "Digite Clave Actual (4 Dígitos)",
                isPassword = true,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Digite Nueva Clave (4 Dígitos) - Campo texto
            OutlinedTextFieldTransparente(
                value = viewModel.inputClaveNueva,
                onValueChange = { if (it.length <= 4) viewModel.inputClaveNueva = it },
                label = "Digite Nueva Clave (4 Dígitos)",
                isPassword = true,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirmar
            Button(
                onClick = { viewModel.ejecutarCambioClave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
            ) {
                Text(text = "Confirmar")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Volver a Menú Anterior
            OutlinedButton(
                onClick = { viewModel.irAMenuPrincipal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Botón Volver a Menú Anterior")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Salir -> Mensaje "Gracias por utilizar nuestros servicios"
            OutlinedButton(
                onClick = { viewModel.activarSalir() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Botón Salir", color = ColorRojoAlerta)
            }
        }
    }
}

/**
 * OutlinedTextFieldTransparente
 *
 * Reutilizable: Crea un campo de texto con estilo transparente y flotante
 * conforme a la solicitud del prompt ("Campo texto flotante y transparente").
 */
@Composable
fun OutlinedTextFieldTransparente(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color.Gray) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = ColorAzulBanquero,
            unfocusedBorderColor = ColorGrisBorde
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * BotonMenuOpcion
 * Botón estandarizado para las opciones del menú principal.
 */
@Composable
fun BotonMenuOpcion(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Banner flotante para mostrar mensajes informativos o de error.
 */
@Composable
fun NotificacionBanner(
    mensaje: String,
    esError: Boolean,
    onDescartar: () -> Unit
) {
    val colorFondo = if (esError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val colorTexto = if (esError) ColorRojoAlerta else Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mensaje,
                color = colorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDescartar) {
                Text(text = "OK", color = colorTexto, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * DialogoDespedida
 *
 * Diálogo modal para la opción Salir.
 * Cumple con el requerimiento de mostrar el mensaje:
 * "Gracias por utilizar nuestros servicios".
 */
@Composable
fun DialogoDespedida(onConfirmar: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onConfirmar() },
        title = {
            Text(
                text = "Cajero Automático",
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )
        },
        text = {
            Text(
                text = "Gracias por utilizar nuestros servicios",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
            ) {
                Text("Aceptar")
            }
        },
        containerColor = ColorSuperficieBlanca
    )
}

/**
 * DialogoRecibo
 * Muestra el recibo impreso de la transacción realizada.
 */
@Composable
fun DialogoRecibo(
    recibo: ReciboTransaccion?,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = "🧾 Recibo de Transacción",
                fontWeight = FontWeight.Bold,
                color = ColorAzulOscuroTexto
            )
        },
        text = {
            if (recibo != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Fecha / Hora: ${recibo.fechaHora}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Operación: ${recibo.tipoOperacion}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Monto: $${recibo.monto}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Saldo Restante: $${recibo.saldoRestante}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text("No hay recibo disponible.")
            }
        },
        confirmButton = {
            Button(
                onClick = onCerrar,
                colors = ButtonDefaults.buttonColors(containerColor = ColorAzulBanquero)
            ) {
                Text("Cerrar")
            }
        },
        containerColor = ColorSuperficieBlanca
    )
}
