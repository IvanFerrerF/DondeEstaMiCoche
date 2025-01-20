package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Fragmento de diálogo que permite al usuario seleccionar un radio de búsqueda en metros.
 *
 * @property onRadioSelected Callback que se ejecuta al seleccionar un radio, devolviendo el valor seleccionado en metros.
 */
class RadioBusquedaDialogFragment(
    private val onRadioSelected: (Int) -> Unit
) : DialogFragment() {

    /**
     * Crea el cuadro de diálogo para seleccionar un radio de búsqueda.
     *
     * @param savedInstanceState Estado previamente guardado, si existe.
     * @return El cuadro de diálogo configurado.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val radios = arrayOf("500 metros", "1000 metros", "1500 metros", "2000 metros")
        val valoresRadios = listOf(500, 1000, 1500, 2000)

        return AlertDialog.Builder(requireContext())
            .setTitle("Selecciona el radio de búsqueda") // Título del diálogo
            .setItems(radios) { _, which ->
                // Ejecuta el callback con el valor seleccionado
                onRadioSelected(valoresRadios[which])
            }
            .setNegativeButton("Cancelar", null) // Botón de cancelación
            .create()
    }
}
