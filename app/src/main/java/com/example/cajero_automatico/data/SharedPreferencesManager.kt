package com.example.cajero_automatico.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Clase SharedPreferencesManager
 *
 * Se encarga de administrar el almacenamiento local y la recuperación de información
 * del cajero automático utilizando la API de SharedPreferences de Android.
 *
 * Cumple con el requerimiento de guardar el saldo existente y las claves de acceso
 * antes de salir de la aplicación para mantener el estado en sesiones posteriores.
 *
 * @param context Contexto de la aplicación necesario para acceder a SharedPreferences.
 */
class SharedPreferencesManager(context: Context) {

    // Instancia privada de SharedPreferences con nombre único para la aplicación
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        // Clave del archivo de preferencias
        private const val PREFS_NAME = "cajero_prefs"

        // Claves para almacenar los valores individuales
        private const val KEY_SALDO = "key_saldo"
        private const val KEY_PIN_5 = "key_pin_5"
        private const val KEY_CLAVE_4 = "key_clave_4"

        // Valores por defecto iniciales
        private const val DEFAULT_SALDO = 500000.0f // Saldo inicial por defecto: $500,000 COP
        private const val DEFAULT_PIN_5 = "12345"    // PIN de 5 dígitos por defecto
        private const val DEFAULT_CLAVE_4 = "1234"  // Clave de 4 dígitos por defecto
    }

    /**
     * Guarda el saldo actual del usuario en SharedPreferences.
     *
     * @param saldo Valor numérico del saldo a almacenar.
     */
    fun guardarSaldo(saldo: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_SALDO, saldo.toFloat())
            .apply()
    }

    /**
     * Obtiene el saldo actual almacenado. Si es la primera vez que se ejecuta la app,
     * retorna el saldo por defecto.
     *
     * @return El saldo almacenado como Double.
     */
    fun obtenerSaldo(): Double {
        return sharedPreferences.getFloat(KEY_SALDO, DEFAULT_SALDO).toDouble()
    }

    /**
     * Guarda el PIN de 5 dígitos de la aplicación del banco.
     *
     * @param pin Nuevo PIN de 5 dígitos.
     */
    fun guardarPin5(pin: String) {
        sharedPreferences.edit()
            .putString(KEY_PIN_5, pin)
            .apply()
    }

    /**
     * Obtiene el PIN de 5 dígitos actual.
     *
     * @return El PIN de 5 dígitos almacenado.
     */
    fun obtenerPin5(): String {
        return sharedPreferences.getString(KEY_PIN_5, DEFAULT_PIN_5) ?: DEFAULT_PIN_5
    }

    /**
     * Guarda la clave de 4 dígitos para operaciones y retiros.
     *
     * @param clave Nueva clave de 4 dígitos.
     */
    fun guardarClave4(clave: String) {
        sharedPreferences.edit()
            .putString(KEY_CLAVE_4, clave)
            .apply()
    }

    /**
     * Obtiene la clave de 4 dígitos almacenada.
     *
     * @return La clave de 4 dígitos.
     */
    fun obtenerClave4(): String {
        return sharedPreferences.getString(KEY_CLAVE_4, DEFAULT_CLAVE_4) ?: DEFAULT_CLAVE_4
    }
}
