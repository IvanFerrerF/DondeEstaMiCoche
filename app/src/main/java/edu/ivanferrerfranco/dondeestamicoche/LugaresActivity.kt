package edu.ivanferrerfranco.dondeestamicoche

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.ivanferrerfranco.dondeestamicoche.adapters.LugarAdapter
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.databinding.ActivityLugaresBinding
import edu.ivanferrerfranco.dondeestamicoche.utils.FileHandler
import java.util.Locale
import kotlin.concurrent.thread

/**
 * Actividad que muestra una lista de lugares previamente guardados en un archivo.
 * Incluye funcionalidades para cargar la lista de lugares, ordenarla y actualizar las direcciones
 * utilizando la clase [Geocoder].
 */
class LugaresActivity : AppCompatActivity() {

    // Enlace a las vistas del diseño de la actividad
    private lateinit var binding: ActivityLugaresBinding

    // Manejador de archivos para leer y escribir datos de ubicaciones
    private lateinit var fileHandler: FileHandler<UbicacionCoche>

    // Adaptador para el RecyclerView que muestra los lugares
    private lateinit var lugarAdapter: LugarAdapter

    /**
     * Método llamado al crear la actividad.
     * Configura la lista de lugares y su presentación en el RecyclerView.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLugaresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el manejador de archivos para las ubicaciones
        fileHandler = FileHandler(
            context = this,
            fileName = "ubicaciones.json",
            type = UbicacionCoche::class.java
        )

        // Leer los lugares guardados en el archivo
        var lugares = fileHandler.read()?.toMutableList() ?: mutableListOf()

        // Invertir el orden para mostrar los lugares más recientes primero
        lugares = lugares.asReversed()

        // Configurar el adaptador para el RecyclerView
        lugarAdapter = LugarAdapter(lugares)

        binding.recyclerViewLugares.apply {
            layoutManager = LinearLayoutManager(this@LugaresActivity)
            adapter = lugarAdapter
        }

        // Actualizar las direcciones de los lugares
        actualizarDirecciones(lugares)
    }

    /**
     * Método para actualizar las direcciones de los lugares utilizando coordenadas GPS.
     * Usa la clase [Geocoder] para convertir latitud y longitud en direcciones legibles.
     *
     * @param lugares Lista de lugares a procesar para obtener sus direcciones.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun actualizarDirecciones(lugares: List<UbicacionCoche>) {
        val geocoder = Geocoder(this, Locale.getDefault())
        thread {
            lugares.forEach { lugar ->
                try {
                    // Obtener direcciones basadas en las coordenadas del lugar
                    val direcciones: List<Address>? =
                        geocoder.getFromLocation(lugar.latitud, lugar.longitud, 1)
                    lugar.direccion = if (!direcciones.isNullOrEmpty()) {
                        direcciones[0].getAddressLine(0) ?: "Dirección no encontrada"
                    } else {
                        "Dirección desconocida"
                    }
                } catch (e: Exception) {
                    // Capturar errores al procesar direcciones
                    lugar.direccion = "Error al obtener dirección"
                }
                // Actualizar el adaptador en el hilo principal
                runOnUiThread {
                    lugarAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}
