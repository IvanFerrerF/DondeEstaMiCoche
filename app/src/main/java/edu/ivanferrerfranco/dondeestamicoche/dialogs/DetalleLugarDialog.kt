package edu.ivanferrerfranco.dondeestamicoche.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import edu.ivanferrerfranco.dondeestamicoche.R
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.databinding.DialogDetalleLugarBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DialogFragment que muestra los detalles de una ubicación guardada, permitiendo
 * interactuar con la información o realizar acciones específicas como compartir la ubicación,
 * visualizarla en el mapa o eliminarla.
 *
 * @property lugar La ubicación de tipo [UbicacionCoche] cuyos detalles se mostrarán.
 * @property rutaFoto Ruta de la foto asociada a la ubicación, si existe.
 * @property onNoAparcado Callback que se ejecuta cuando el usuario marca que ya no está aparcado.
 * @property onVerMapa Callback que se ejecuta para abrir la ubicación en el mapa.
 * @property onEliminar Callback que se ejecuta cuando se elimina la ubicación.
 */
class DetalleLugarDialog(
    private val lugar: UbicacionCoche,
    private val rutaFoto: String?,
    private val onNoAparcado: () -> Unit,
    private val onVerMapa: () -> Unit,
    private val onEliminar: () -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogDetalleLugarBinding

    /**
     * Inflar la vista del diálogo.
     *
     * @param inflater El inflador de vistas para cargar el diseño del diálogo.
     * @param container El contenedor en el que se insertará la vista.
     * @param savedInstanceState Estado previamente guardado del fragmento.
     * @return La vista inflada para el diálogo.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDetalleLugarBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configuración de la vista tras su creación.
     *
     * Se configuran los textos iniciales, las imágenes y los listeners de los botones.
     *
     * @param view La vista raíz del diálogo.
     * @param savedInstanceState Estado previamente guardado del fragmento.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarVista()
        configurarListeners()
    }

    /**
     * Configura los elementos visuales del diálogo, incluyendo los textos y la imagen asociada.
     */
    @SuppressLint("SetTextI18n")
    private fun configurarVista() {
        binding.tvDireccion.text = lugar.direccion
        binding.tvCoordenadas.text = "Lat: ${lugar.latitud}, Lon: ${lugar.longitud}"
        binding.tvFechaHora.text = "Fecha: ${lugar.fecha}, Hora: ${lugar.hora}"

        if (lugar.fechaHoraSalida.isNullOrEmpty()) {
            binding.tvFechaHoraSalida.text = "Fecha y hora de salida: No registrada"
        } else {
            binding.tvFechaHoraSalida.text = "Fecha y hora de salida: ${lugar.fechaHoraSalida}"
        }

        if (!lugar.fotoRuta.isNullOrEmpty()) {
            val fotoFile = File(lugar.fotoRuta)
            if (fotoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(fotoFile.absolutePath)
                binding.imgFoto.setImageBitmap(bitmap)
            } else {
                binding.imgFoto.setImageResource(R.drawable.no_photo)
            }
        } else {
            binding.imgFoto.setImageResource(R.drawable.no_photo)
        }
    }

    /**
     * Configura los eventos asociados a los botones de la interfaz.
     */
    private fun configurarListeners() {
        binding.btnNoAparcado.setOnClickListener {
            lugar.fechaHoraSalida = obtenerFechaHoraActual()
            onNoAparcado()
            dismiss()
        }

        binding.btnVerEnMapa.setOnClickListener { onVerMapa() }
        binding.btnEliminar.setOnClickListener {
            onEliminar()
            dismiss()
        }
        binding.btnCerrar.setOnClickListener { dismiss() }
        binding.btnCompartirUbicacion.setOnClickListener { compartirUbicacion() }
    }

    /**
     * Comparte la información de la ubicación a través de un correo electrónico.
     */
    private fun compartirUbicacion() {
        val direccion = lugar.direccion ?: "Dirección no disponible"
        val latitud = lugar.latitud.takeIf { it != 0.0 } ?: "Latitud no disponible"
        val longitud = lugar.longitud.takeIf { it != 0.0 } ?: "Longitud no disponible"
        val fechaSalida = lugar.fechaHoraSalida ?: "No registrada"

        val mensaje = """
            ¡Hola! Aquí tienes la ubicación de mi coche:

            Dirección: $direccion
            Coordenadas: Latitud $latitud, Longitud $longitud
            Fecha de entrada: ${lugar.fecha}, Hora de entrada: ${lugar.hora}
            Fecha de salida: $fechaSalida

            ¡Nos vemos!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "Ubicación de mi coche")
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar ubicación a través de..."))
        } catch (e: Exception) {
            Toast.makeText(context, "No se encontró ninguna aplicación de correo.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Obtiene la fecha y hora actual en formato "dd/MM/yyyy HH:mm".
     *
     * @return La fecha y hora actual como cadena.
     */
    private fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Configura el tamaño del diálogo al crearlo.
     *
     * @param savedInstanceState Estado previamente guardado del fragmento.
     * @return Un diálogo con tamaño ajustado.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
