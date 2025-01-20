package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.R

/**
 * Fragmento de diálogo para detener la alarma.
 *
 * Este diálogo permite al usuario detener una alarma activa con un botón.
 *
 * @property onStopAlarm Callback que se ejecuta cuando el usuario detiene la alarma.
 */
class StopAlarmDialog(
    private val onStopAlarm: () -> Unit
) : DialogFragment() {

    /**
     * Crea y devuelve la vista del diálogo.
     *
     * @param inflater Inflador de vistas.
     * @param container Contenedor donde se añadirá la vista, si corresponde.
     * @param savedInstanceState Estado previamente guardado, si existe.
     * @return La vista inflada para el diálogo.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_stop_alarm, container, false)
    }

    /**
     * Configura los eventos de la vista del diálogo.
     *
     * Este método se ejecuta después de que la vista ha sido creada y permite
     * configurar los eventos asociados a los elementos del diálogo.
     *
     * @param view La vista creada para el diálogo.
     * @param savedInstanceState Estado previamente guardado, si existe.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStopAlarm = view.findViewById<TextView>(R.id.btn_stop_alarm)
        btnStopAlarm.setOnClickListener {
            onStopAlarm() // Llama al callback para detener la alarma
            dismiss() // Cierra el diálogo
        }
    }

    /**
     * Crea y personaliza el diálogo.
     *
     * Este método configura el fondo del diálogo para que sea transparente.
     *
     * @param savedInstanceState Estado previamente guardado, si existe.
     * @return El diálogo personalizado.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
