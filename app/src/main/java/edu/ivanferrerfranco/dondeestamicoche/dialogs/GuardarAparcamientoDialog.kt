package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.databinding.DialogGuardarAparcamientoBinding

/**
 * DialogFragment que muestra un cuadro de diálogo para confirmar si el usuario desea guardar un aparcamiento.
 *
 * @property onGuardar Callback que se ejecuta si el usuario confirma la acción de guardar.
 * @property onCancelar Callback que se ejecuta si el usuario cancela la acción.
 */
class GuardarAparcamientoDialog(
    private val onGuardar: () -> Unit,
    private val onCancelar: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogGuardarAparcamientoBinding

    /**
     * Infla el diseño del cuadro de diálogo.
     *
     * @param inflater El inflador utilizado para cargar el diseño.
     * @param container El contenedor padre en el que se alojará la vista (puede ser null).
     * @param savedInstanceState El estado previamente guardado, si existe.
     * @return La vista raíz del cuadro de diálogo.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGuardarAparcamientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura los elementos interactivos de la vista.
     *
     * Se establecen los listeners para los botones de aceptar y cancelar.
     *
     * @param view La vista raíz del cuadro de diálogo.
     * @param savedInstanceState El estado previamente guardado, si existe.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acción al pulsar el botón de aceptar
        binding.btnAceptar.setOnClickListener {
            dismiss() // Cierra el diálogo
            onGuardar() // Llama al callback de guardar
        }

        // Acción al pulsar el botón de cancelar
        binding.btnCancelar.setOnClickListener {
            dismiss() // Cierra el diálogo
            onCancelar() // Llama al callback de cancelar
        }
    }

    /**
     * Configura el estilo del cuadro de diálogo.
     *
     * Se utiliza para aplicar un fondo transparente al cuadro de diálogo.
     *
     * @param savedInstanceState El estado previamente guardado, si existe.
     * @return El cuadro de diálogo configurado.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
