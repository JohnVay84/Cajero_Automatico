package com.example.cajero_automatico.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cajero_automatico.data.CajeroModel
import com.example.cajero_automatico.data.ReciboTransaccion
import com.example.cajero_automatico.data.ResultadoOperacion

/**
 * PantallaCajero
 *
 * Enum que define los diferentes estados o pantallas de la aplicación del cajero.
 */
enum class PantallaCajero {
    LOGIN,
    MENU_PRINCIPAL,
    CONSIGNAR_INGRESO_MONTO,
    CONSIGNAR_RANURA_CONFIRMACION,
    RETIRAR_INGRESO_MONTO,
    RETIRAR_INGRESO_PIN,
    RETIRAR_EXITO_OPCIONES,
    VISUALIZAR_SALDO,
    CAMBIAR_CLAVE
}

/**
 * Clase CajeroViewModel
 *
 * ViewModel encargado de mantener el estado de la interfaz de usuario (UI),
 * procesar la navegación entre pantallas y conectar la vista con la capa
 * de negocio (CajeroModel).
 *
 * @param model Instancia de CajeroModel que ejecuta las transacciones.
 */
class CajeroViewModel(val model: CajeroModel) : ViewModel() {

    // Pantalla activa actualmente en el cajero
    var pantallaActual by mutableStateOf(PantallaCajero.LOGIN)
        private set

    // Tipo de autenticación seleccionada: "Tarjeta Débito" o "PIN 5 dígitos"
    var metodoAutenticacion by mutableStateOf("Tarjeta Débito")
        private set

    // Entradas de texto para formularios (campos flotantes/transparentes)
    var inputMontoConsignar by mutableStateOf("")
    var inputMontoRetirar by mutableStateOf("")
    var inputPinAcceso by mutableStateOf("")
    var inputClaveTransaccion by mutableStateOf("")
    var inputClaveActual by mutableStateOf("")
    var inputClaveNueva by mutableStateOf("")

    // Estado para avisos y diálogos modales
    var mensajeNotificacion by mutableStateOf<String?>(null)
    var esErrorNotificacion by mutableStateOf(false)
    var mostrarMensajeDespedida by mutableStateOf(false)
    var mostrarReciboDialog by mutableStateOf(false)

    // Recibo activo para visualización
    var reciboParaMostrar by mutableStateOf<ReciboTransaccion?>(null)
        private set

    /**
     * Inicia sesión seleccionando método (Tarjeta Débito o PIN de 5 dígitos).
     *
     * @param esTarjeta True si elige Tarjeta Débito, False si usa PIN de 5 dígitos.
     * @param pinIngresado PIN proporcionado si seleccionó PIN de 5 dígitos.
     */
    fun iniciarSesion(esTarjeta: Boolean, pinIngresado: String) {
        if (esTarjeta) {
            metodoAutenticacion = "Tarjeta Débito"
            limpiarCampos()
            pantallaActual = PantallaCajero.MENU_PRINCIPAL
        } else {
            metodoAutenticacion = "PIN 5 dígitos"
            if (model.validarPin5(pinIngresado)) {
                limpiarCampos()
                pantallaActual = PantallaCajero.MENU_PRINCIPAL
            } else {
                mostrarNotificacion("PIN de 5 dígitos incorrecto. Verifique e intente de nuevo.", esError = true)
            }
        }
    }

    /**
     * Navega a la pantalla seleccionada del menú principal.
     */
    fun irA(pantalla: PantallaCajero) {
        limpiarCampos()
        pantallaActual = pantalla
    }

    /**
     * Paso 1 de consignación: Valida el monto ingresado antes de insertar dinero en la ranura.
     */
    fun prepararConsignacion() {
        val monto = inputMontoConsignar.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            mostrarNotificacion("Ingrese un valor válido a consignar.", esError = true)
            return
        }
        pantallaActual = PantallaCajero.CONSIGNAR_RANURA_CONFIRMACION
    }

    /**
     * Paso 2 de consignación: Inserta el dinero en la ranura y confirma la transacción.
     */
    fun confirmarConsignacion() {
        val monto = inputMontoConsignar.toDoubleOrNull() ?: 0.0
        when (val resultado = model.consignarDinero(monto)) {
            is ResultadoOperacion.Exito -> {
                reciboParaMostrar = model.ultimoRecibo
                mostrarNotificacion(resultado.mensaje, esError = false)
            }
            is ResultadoOperacion.Error -> {
                mostrarNotificacion(resultado.mensaje, esError = true)
            }
        }
    }

    /**
     * Paso 1 de retiro: Valida el monto a retirar y solicita la clave/PIN.
     */
    fun prepararRetiro() {
        val monto = inputMontoRetirar.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            mostrarNotificacion("Ingrese un valor válido a retirar.", esError = true)
            return
        }

        // Validación anticipada de fondos suficientes
        if (monto > model.saldoActual) {
            mostrarNotificacion("Fondos insuficientes para este retiro", esError = true)
            return
        }

        pantallaActual = PantallaCajero.RETIRAR_INGRESO_PIN
    }

    /**
     * Paso 2 de retiro: Confirma el valor del retiro verificando la clave.
     * Muestra "Fondos insuficientes para este retiro" si el monto supera el saldo.
     */
    fun confirmarRetiro() {
        val monto = inputMontoRetirar.toDoubleOrNull() ?: 0.0
        when (val resultado = model.retirarDinero(monto, inputClaveTransaccion)) {
            is ResultadoOperacion.Exito -> {
                reciboParaMostrar = model.ultimoRecibo
                pantallaActual = PantallaCajero.RETIRAR_EXITO_OPCIONES
                mostrarNotificacion("Retiro realizado exitosamente.", esError = false)
            }
            is ResultadoOperacion.Error -> {
                mostrarNotificacion(resultado.mensaje, esError = true)
            }
        }
    }

    /**
     * Ejecuta el cambio de clave de 4 dígitos.
     */
    fun ejecutarCambioClave() {
        when (val resultado = model.cambiarClave(inputClaveActual, inputClaveNueva)) {
            is ResultadoOperacion.Exito -> {
                mostrarNotificacion(resultado.mensaje, esError = false)
                irAMenuPrincipal()
            }
            is ResultadoOperacion.Error -> {
                mostrarNotificacion(resultado.mensaje, esError = true)
            }
        }
    }

    /**
     * Muestra el recibo impreso si está disponible.
     */
    fun solicitarImprimirRecibo() {
        if (reciboParaMostrar != null) {
            mostrarReciboDialog = true
        } else {
            mostrarNotificacion("No hay un recibo disponible para imprimir.", esError = true)
        }
    }

    /**
     * Regresa al menú principal del cajero.
     */
    fun irAMenuPrincipal() {
        limpiarCampos()
        pantallaActual = PantallaCajero.MENU_PRINCIPAL
    }

    /**
     * Activa el diálogo modal de despedida con el mensaje "Gracias por utilizar nuestros servicios".
     */
    fun activarSalir() {
        mostrarMensajeDespedida = true
    }

    /**
     * Cierra la sesión y regresa a la pantalla de inicio de sesión.
     */
    fun cerrarSesionYSalir() {
        mostrarMensajeDespedida = false
        limpiarCampos()
        pantallaActual = PantallaCajero.LOGIN
    }

    /**
     * Muestra un mensaje flotante de notificación.
     */
    fun mostrarNotificacion(mensaje: String, esError: Boolean) {
        mensajeNotificacion = mensaje
        esErrorNotificacion = esError
    }

    /**
     * Limpia la notificación activa.
     */
    fun descartarNotificacion() {
        mensajeNotificacion = null
    }

    /**
     * Resetea todos los campos de entrada de la UI.
     */
    private fun limpiarCampos() {
        inputMontoConsignar = ""
        inputMontoRetirar = ""
        inputPinAcceso = ""
        inputClaveTransaccion = ""
        inputClaveActual = ""
        inputClaveNueva = ""
    }
}
