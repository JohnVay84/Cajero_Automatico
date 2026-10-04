package com.example.cajero_automatico.data

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ResultadoOperacion
 *
 * Clase sellada que representa el resultado de ejecutar una transacción
 * en el cajero automático. Permite manejar de forma limpia y segura
 * las respuestas de éxito o error.
 */
sealed class ResultadoOperacion {
    data class Exito(val mensaje: String, val nuevoSaldo: Double) : ResultadoOperacion()
    data class Error(val mensaje: String) : ResultadoOperacion()
}

/**
 * ReciboTransaccion
 *
 * Modelo de datos POO que contiene la información requerida
 * para imprimir el recibo de una transacción realizada.
 */
data class ReciboTransaccion(
    val tipoOperacion: String,
    val monto: Double,
    val saldoRestante: Double,
    val fechaHora: String
)

/**
 * Clase CajeroModel
 *
 * Contiene la lógica de negocio orientada a objetos (POO) del Cajero Automático.
 * Administra las reglas para consignaciones, retiros, cambio de claves
 * y la validación obligatoria de fondos suficientes.
 *
 * @param prefsManager Manejador de SharedPreferences para persistencia.
 */
class CajeroModel(private val prefsManager: SharedPreferencesManager) {

    // Saldo actual cargado desde SharedPreferences
    var saldoActual: Double = prefsManager.obtenerSaldo()
        private set

    // PIN de 5 dígitos para acceso por aplicación del banco
    var pin5Digitos: String = prefsManager.obtenerPin5()
        private set

    // Clave de 4 dígitos para retiros y operaciones
    var clave4Digitos: String = prefsManager.obtenerClave4()
        private set

    // Último recibo generado durante la sesión
    var ultimoRecibo: ReciboTransaccion? = null
        private set

    /**
     * Valida si el PIN de 5 dígitos ingresado es correcto.
     *
     * @param pin PIN a verificar.
     * @return true si coincide con el PIN almacenado, false en caso contrario.
     */
    fun validarPin5(pin: String): Boolean {
        return pin == pin5Digitos
    }

    /**
     * Valida si la clave de 4 dígitos ingresada es correcta.
     *
     * @param clave Clave a verificar.
     * @return true si coincide con la clave almacenada, false en caso contrario.
     */
    fun validarClave4(clave: String): Boolean {
        return clave == clave4Digitos
    }

    /**
     * Realiza una consignación de dinero aumentando el saldo disponible.
     *
     * @param monto Cantidad de dinero a ingresar.
     * @return ResultadoOperacion indicando el éxito o el error si el monto es inválido.
     */
    fun consignarDinero(monto: Double): ResultadoOperacion {
        if (monto <= 0) {
            return ResultadoOperacion.Error("El monto a consignar debe ser mayor a cero.")
        }

        saldoActual += monto
        prefsManager.guardarSaldo(saldoActual)

        // Registrar el recibo de consignación
        ultimoRecibo = ReciboTransaccion(
            tipoOperacion = "Consignación de Dinero",
            monto = monto,
            saldoRestante = saldoActual,
            fechaHora = obtenerFechaHoraActual()
        )

        return ResultadoOperacion.Exito(
            mensaje = "Consignación realizada con éxito. Nuevo saldo: ${formatearMoneda(saldoActual)}",
            nuevoSaldo = saldoActual
        )
    }

    /**
     * Realiza el retiro de dinero aplicando la validación de fondos suficientes.
     *
     * Cumple con la regla del prompt: "Si se pretende hacer un retiro de una cantidad
     * mayor al saldo existente, esta transacción se debe considerar como inválida,
     * mostrar un mensaje informando: 'Fondos insuficientes para este retiro'".
     *
     * @param monto Cantidad de dinero a retirar.
     * @param claveIngresada Clave de 4 dígitos proporcionada por el usuario.
     * @return ResultadoOperacion con el resultado del retiro.
     */
    fun retirarDinero(monto: Double, claveIngresada: String): ResultadoOperacion {
        if (monto <= 0) {
            return ResultadoOperacion.Error("El monto a retirar debe ser mayor a cero.")
        }

        // Validación de Clave de 4 dígitos / PIN
        if (!validarClave4(claveIngresada) && !validarPin5(claveIngresada)) {
            return ResultadoOperacion.Error("La clave o PIN ingresado es incorrecto.")
        }

        // VALIDACIÓN OBLIGATORIA DE FONDOS INSUFICIENTES
        if (monto > saldoActual) {
            return ResultadoOperacion.Error("Fondos insuficientes para este retiro")
        }

        saldoActual -= monto
        prefsManager.guardarSaldo(saldoActual)

        // Registrar recibo de retiro
        ultimoRecibo = ReciboTransaccion(
            tipoOperacion = "Retiro de Dinero",
            monto = monto,
            saldoRestante = saldoActual,
            fechaHora = obtenerFechaHoraActual()
        )

        return ResultadoOperacion.Exito(
            mensaje = "Retiro realizado con éxito. Saldo restante: ${formatearMoneda(saldoActual)}",
            nuevoSaldo = saldoActual
        )
    }

    /**
     * Cambia la clave de 4 dígitos verificando la clave actual.
     *
     * @param claveActual Clave de 4 dígitos actual del usuario.
     * @param nuevaClave Nueva clave de 4 dígitos deseada.
     * @return ResultadoOperacion indicando si se realizó el cambio correctamente.
     */
    fun cambiarClave(claveActual: String, nuevaClave: String): ResultadoOperacion {
        if (!validarClave4(claveActual)) {
            return ResultadoOperacion.Error("La clave actual ingresada es incorrecta.")
        }

        if (nuevaClave.length != 4 || !nuevaClave.all { it.isDigit() }) {
            return ResultadoOperacion.Error("La nueva clave debe tener exactamente 4 dígitos numéricos.")
        }

        clave4Digitos = nuevaClave
        prefsManager.guardarClave4(nuevaClave)

        return ResultadoOperacion.Exito(
            mensaje = "Su clave ha sido modificada con éxito.",
            nuevoSaldo = saldoActual
        )
    }

    /**
     * Formatea un valor numérico a representación de moneda en pesos (COP/USD).
     *
     * @param valor Monto numérico.
     * @return String formateado como moneda.
     */
    fun formatearMoneda(valor: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CO").build())
        return formato.format(valor)
    }

    /**
     * Obtiene la fecha y hora actual formateada para el recibo.
     *
     * @return String con la fecha y hora actual.
     */
    private fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
