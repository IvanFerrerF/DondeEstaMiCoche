package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.databinding.DialogFotoBinding

/**
 * DialogFragment que muestra una confirmación al usuario para tomar una foto que se asociará a un aparcamiento guardado.
 *
 * @property onAceptar Callback que se ejecuta si el usuario acepta tomar la foto.
 * @property onCancelar Callback que se ejecuta si el usuario cancela la acción.
 */
class FotoDialog(
    private val onAceptar: () -> Unit,
    private val onCancelar: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogFotoBinding

    /**
     * Método que infla el diseño del diálogo.
     *
     * @param inflater El inflador para cargar el diseño.
     * @param container El contenedor padre donde se alojará la vista.
     * @param savedInstanceState El estado previamente guardado del diálogo, si existe.
     * @return La vista raíz del diálogo inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configuración de la vista después de ser creada.
     *
     * Se establecen los textos en los elementos del diálogo y se configuran los eventos de los botones.
     *
     * @param view La vista raíz del diálogo.
     * @param savedInstanceState El estado previamente guardado del diálogo, si existe.
     */
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el mensaje del diálogo
        binding.tvDialogMensaje.text = "¿Deseas tomar una foto para añadir al aparcamiento?"

        // Acción al pulsar el botón Aceptar
        binding.btnAceptar.setOnClickListener {
            onAceptar()
            dismiss() // Cierra el diálogo
        }

        // Acción al pulsar el botón Cancelar
        binding.btnCancelar.setOnClickListener {
            onCancelar()
            dismiss() // Cierra el diálogo
        }
    }
}
