package edu.ivanferrerfranco.dondeestamicoche.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import edu.ivanferrerfranco.dondeestamicoche.MainActivity

/**
 * Receptor para gestionar alarmas configuradas.
 *
 * Esta clase recibe las alarmas configuradas y ejecuta acciones específicas,
 * como reproducir un tono de alarma o iniciar una actividad para detenerla.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        /**
         * Objeto estático para gestionar el tono de alarma.
         *
         * Se utiliza para evitar que múltiples tonos se reproduzcan al mismo tiempo.
         */
        var ringtone: Ringtone? = null
    }

    /**
     * Método invocado al recibir la alarma configurada.
     *
     * Este método maneja las acciones necesarias al activarse una alarma, como:
     * - Reproducir un tono de alarma.
     * - Mostrar un mensaje de alerta.
     * - Iniciar la actividad principal con un diálogo para detener la alarma.
     *
     * @param context Contexto en el que se ejecuta el receptor.
     * @param intent Intent que contiene los datos de la alarma configurada.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Obtener la URI para el tono de alarma predeterminado
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Reproducir el tono de alarma solo si no está ya reproduciéndose
        if (ringtone == null) {
            ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()
        }

        // Verificar si se debe mostrar un diálogo para detener la alarma
        val showDialog = intent.getBooleanExtra("show_dialog", false)
        if (showDialog) {
            // Iniciar MainActivity con un diálogo para detener la alarma
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("mostrar_detener_alarma", true)
            }
            context.startActivity(mainIntent)
        } else {
            // Mostrar un mensaje de notificación al usuario
            Toast.makeText(context, "Es hora de mover el coche", Toast.LENGTH_LONG).show()
        }
    }
}
