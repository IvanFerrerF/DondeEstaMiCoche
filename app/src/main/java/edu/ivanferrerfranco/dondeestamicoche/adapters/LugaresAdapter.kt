package edu.ivanferrerfranco.dondeestamicoche.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import edu.ivanferrerfranco.dondeestamicoche.MapaActivity
import edu.ivanferrerfranco.dondeestamicoche.R
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.database.SQLiteHelper
import edu.ivanferrerfranco.dondeestamicoche.dialogs.DetalleLugarDialog

/**
 * Adaptador para gestionar la lista de ubicaciones del coche en un RecyclerView.
 * Permite visualizar, actualizar y eliminar ubicaciones guardadas en la base de datos.
 *
 * @property lugares Lista mutable de ubicaciones del coche almacenadas.
 */
class LugarAdapter(
    private val lugares: MutableList<UbicacionCoche>
) : RecyclerView.Adapter<LugarAdapter.LugarViewHolder>() {

    /**
     * ViewHolder para gestionar los elementos individuales de la lista de lugares.
     *
     * @param itemView Vista correspondiente al elemento en la lista.
     */
    inner class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLugar: TextView = itemView.findViewById(R.id.tvLugar) // Muestra la dirección del lugar.
        val tvCoordenadas: TextView = itemView.findViewById(R.id.tvCoordenadas) // Muestra las coordenadas (latitud y longitud).
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora) // Muestra la fecha y hora de registro.
        val semaforo: View = itemView.findViewById(R.id.semaforo) // Indica si el lugar es el aparcamiento actual.
    }

    /**
     * Crea la vista para un elemento de la lista inflándola desde el layout correspondiente.
     *
     * @param parent El contenedor padre al que se añadirá la vista.
     * @param viewType El tipo de vista, aunque no se usa ya que es un solo tipo de item.
     * @return Un ViewHolder que contiene la vista inflada.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lugar, parent, false)
        return LugarViewHolder(view)
    }

    /**
     * Vincula los datos de una ubicación específica al ViewHolder correspondiente.
     *
     * @param holder El ViewHolder que representa el elemento actual en la lista.
     * @param position La posición del elemento dentro de la lista.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        val lugar = lugares[position]

        // Muestra la dirección o un mensaje alternativo si no hay datos disponibles.
        holder.tvLugar.text = if (lugar.direccion?.isNotEmpty() == true) lugar.direccion else "Dirección no disponible"

        // Muestra las coordenadas en formato Latitud / Longitud.
        holder.tvCoordenadas.text = "Lat: ${lugar.latitud}, Lon: ${lugar.longitud}"

        // Muestra la fecha y hora de registro de la ubicación.
        holder.tvFechaHora.text = "Fecha y hora: ${lugar.fecha} ${lugar.hora}"

        // Modifica el color del semáforo según si es el lugar actual de aparcamiento.
        val semaforoDrawable = if (lugar.esActual) R.drawable.semaforo_green else R.drawable.semaforo_red
        holder.semaforo.setBackgroundResource(semaforoDrawable)

        // Obtener la actividad asociada al contexto y la base de datos.
        val activity = holder.itemView.context as AppCompatActivity
        val sqliteHelper = SQLiteHelper(activity)

        /**
         * Evento de clic en el elemento de la lista.
         * Muestra un diálogo con opciones para ver la ubicación en el mapa,
         * marcar como no aparcado o eliminar el registro.
         */
        holder.itemView.setOnClickListener {
            val dialog = DetalleLugarDialog(
                lugar = lugar,
                rutaFoto = lugar.fotoRuta,
                onNoAparcado = {
                    // Si la ubicación tiene un ID, se actualiza su estado en la BD a 'no aparcado'.
                    if (lugar.id != null) {
                        sqliteHelper.actualizarEstadoAparcamiento(lugar.id!!, false)
                    }
                    lugar.esActual = false // Cambia la bandera en la lista actual.
                    notifyItemChanged(position) // Notifica el cambio en la vista.
                },
                onVerMapa = {
                    // Abre la actividad del mapa mostrando la ubicación seleccionada.
                    val intent = Intent(activity, MapaActivity::class.java).apply {
                        putExtra("latitud", lugar.latitud)
                        putExtra("longitud", lugar.longitud)
                    }
                    activity.startActivity(intent)
                },
                onEliminar = {
                    // Si la ubicación tiene un ID, se elimina de la BD.
                    if (lugar.id != null) {
                        sqliteHelper.borrarUbicacion(lugar.id!!)
                    }
                    // Se elimina también de la lista de lugares y se notifica al adaptador.
                    lugares.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(activity, "Aparcamiento eliminado", Toast.LENGTH_SHORT).show()
                }
            )
            dialog.show(activity.supportFragmentManager, "DetalleLugarDialog")
        }

        /**
         * Evento de mantener presionado un elemento en la lista.
         * Elimina el registro directamente sin mostrar el diálogo de opciones.
         */
        holder.itemView.setOnLongClickListener {
            if (lugar.id != null) {
                sqliteHelper.borrarUbicacion(lugar.id!!) // Elimina de la base de datos.
            }
            lugares.removeAt(position) // Elimina de la lista en memoria.
            notifyItemRemoved(position) // Notifica el cambio al RecyclerView.
            Toast.makeText(activity, "Lugar eliminado", Toast.LENGTH_SHORT).show()
            true // Indica que el evento ha sido manejado.
        }
    }

    /**
     * Retorna el número de elementos en la lista de ubicaciones.
     *
     * @return Cantidad total de lugares registrados.
     */
    override fun getItemCount() = lugares.size
}
