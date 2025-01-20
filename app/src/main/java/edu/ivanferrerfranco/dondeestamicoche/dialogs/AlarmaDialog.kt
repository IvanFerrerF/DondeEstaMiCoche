package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.R
import java.util.*

/**
 * Diálogo personalizado para configurar una alarma seleccionando la hora.
 *
 * @property onAceptar Función de devolución que se ejecuta al confirmar la hora seleccionada. Proporciona un objeto [Calendar] con la hora configurada.
 * @property onCancelar Función de devolución que se ejecuta al cancelar el diálogo.
 */
class AlarmaDialog(
    private val onAceptar: (Calendar) -> Unit,
    private val onCancelar: () -> Unit
) : DialogFragment() {

    /**
     * Crea y devuelve el diálogo para configurar la alarma.
     *
     * El diálogo incluye un selector de tiempo ([TimePicker]) que permite al usuario
     * seleccionar la hora y los minutos. Al pulsar "Aceptar", se envía el tiempo seleccionado
     * mediante la función [onAceptar]. Si se pulsa "Cancelar", se invoca la función [onCancelar].
     *
     * @param savedInstanceState Estado guardado del fragmento, si existe.
     * @return Un [Dialog] que muestra el selector de tiempo con las opciones de aceptar o cancelar.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.activity_alarma_dialog, null)
        val timePicker: TimePicker = view.findViewById(R.id.time_picker)

        return AlertDialog.Builder(requireContext())
            .setTitle("Configurar Alarma")
            .setView(view)
            .setPositiveButton("Aceptar") { _, _ ->
                val hora = timePicker.hour
                val minuto = timePicker.minute
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hora)
                    set(Calendar.MINUTE, minuto)
                    set(Calendar.SECOND, 0)
                }
                onAceptar(calendar)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                onCancelar()
            }
            .create()
    }
}
