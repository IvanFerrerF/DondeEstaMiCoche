package edu.ivanferrerfranco.dondeestamicoche.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Objeto de utilidad para verificar la conectividad a Internet.
 */
object ConnectivityHelper {

    /**
     * Comprueba si el dispositivo tiene conexión a Internet.
     *
     * @param context Contexto de la aplicación, necesario para acceder a los servicios del sistema.
     * @return `true` si el dispositivo está conectado a Internet, `false` en caso contrario.
     */
    fun isOnline(context: Context): Boolean {
        // Obtiene el servicio de conectividad del sistema
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Obtiene la red activa del dispositivo, si no hay ninguna, retorna false
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Comprueba si la conexión es a través de WiFi o datos móviles
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
