package edu.ivanferrerfranco.dondeestamicoche

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.ivanferrerfranco.dondeestamicoche.adapters.LugarAdapter
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.database.SQLiteHelper
import edu.ivanferrerfranco.dondeestamicoche.databinding.ActivityLugaresBinding
import java.util.Locale
import kotlin.concurrent.thread

/**
 * Actividad que muestra una lista de lugares previamente guardados en SQLite.
 * Incluye funcionalidades para cargar la lista de lugares, ordenarla y actualizar las direcciones
 * utilizando la clase [Geocoder].
 */
class LugaresActivity : AppCompatActivity() {

    // Enlace a las vistas del layout de la actividad
    private lateinit var binding: ActivityLugaresBinding

    // Adaptador para el RecyclerView que muestra los lugares
    private lateinit var lugarAdapter: LugarAdapter

    // Instancia del helper de SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLugaresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el helper de SQLite para las ubicaciones
        sqliteHelper = SQLiteHelper(this)

        // Leer los lugares guardados en la base de datos SQLite
        var lugares = sqliteHelper.obtenerUbicaciones().toMutableList()

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
     * Actualiza las direcciones de los lugares utilizando el Geocoder.
     * Para cada lugar se obtienen los datos de la dirección basados en sus coordenadas.
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
                    // En caso de error, se asigna un mensaje de error
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
