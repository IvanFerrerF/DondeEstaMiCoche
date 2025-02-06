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
 */
class EliminarAparcamientoDialog(
    private val onConfirmar: () -> Unit,
    private val onCancelar: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogEliminarAparcamientoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEliminarAparcamientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDialogMensaje.text = "¿Deseas eliminar este aparcamiento?"
        binding.btnAceptar.setOnClickListener {
            onConfirmar()
            dismiss()
        }
        binding.btnCancelar.setOnClickListener {
            onCancelar()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
