package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.databinding.DialogEliminarAparcamientoBinding

/**
 * Diálogo personalizado para confirmar la eliminación de un aparcamiento.
 *
 * @property onConfirmar Función de callback que se ejecuta cuando el usuario confirma la eliminación.
 * @property onCancelar Función de callback que se ejecuta cuando el usuario cancela la eliminación.
 */
class EliminarAparcamientoDialog(
    private val onConfirmar: () -> Unit,
    private val onCancelar: () -> Unit
) : DialogFragment() {

    /**
     * Enlace a la vista del diálogo usando View Binding.
     */
    private lateinit var binding: DialogEliminarAparcamientoBinding

    /**
     * Infla la vista del diálogo y la retorna como la raíz del fragmento.
     *
     * @param inflater Inflador de vistas.
     * @param container Contenedor padre de la vista (puede ser nulo).
     * @param savedInstanceState Estado guardado de la instancia (puede ser nulo).
     * @return La vista raíz inflada del diálogo.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEliminarAparcamientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los elementos de la vista una vez creada.
     *
     * @param view Vista raíz del fragmento.
     * @param savedInstanceState Estado guardado de la instancia (puede ser nulo).
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mensaje que se muestra en el diálogo
        binding.tvDialogMensaje.text = "¿Deseas eliminar este aparcamiento?"

        // Acción al presionar el botón de aceptar
        binding.btnAceptar.setOnClickListener {
            onConfirmar()
            dismiss() // Cierra el diálogo
        }

        // Acción al presionar el botón de cancelar
        binding.btnCancelar.setOnClickListener {
            onCancelar()
            dismiss() // Cierra el diálogo
        }
    }

    /**
     * Crea el diálogo y le aplica un fondo transparente para un diseño más limpio.
     *
     * @param savedInstanceState Estado guardado de la instancia (puede ser nulo).
     * @return El diálogo personalizado con fondo transparente.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
