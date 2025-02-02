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
 * Adapter para manejar la lista de ubicaciones del coche en un RecyclerView.
 *
 * @property lugares Lista mutable de ubicaciones del coche.
 */
class LugarAdapter(
    private val lugares: MutableList<UbicacionCoche>
) : RecyclerView.Adapter<LugarAdapter.LugarViewHolder>() {

    /**
     * ViewHolder para gestionar los elementos individuales de la lista de lugares.
     *
     * @param itemView Vista correspondiente al elemento.
     */
    inner class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLugar: TextView = itemView.findViewById(R.id.tvLugar)
        val tvCoordenadas: TextView = itemView.findViewById(R.id.tvCoordenadas)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val semaforo: View = itemView.findViewById(R.id.semaforo)
    }

    /**
     * Crea la vista para un elemento de la lista.
     *
     * @param parent El contenedor padre al que se añadirá la vista.
     * @param viewType El tipo de vista.
     * @return Un ViewHolder que contiene la vista inflada.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lugar, parent, false)
        return LugarViewHolder(view)
    }

    /**
     * Vincula los datos del lugar al ViewHolder correspondiente.
     *
     * @param holder El ViewHolder para el elemento actual.
     * @param position La posición del elemento en la lista.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        val lugar = lugares[position]

        // Configurar textos para mostrar la dirección, coordenadas y fecha/hora
        holder.tvLugar.text = if (lugar.direccion?.isNotEmpty() == true) lugar.direccion else "Dirección no disponible"
        holder.tvCoordenadas.text = "Lat: ${lugar.latitud}, Lon: ${lugar.longitud}"
        holder.tvFechaHora.text = "Fecha y hora: ${lugar.fecha} ${lugar.hora}"

        // Cambiar el semáforo según el estado de 'esActual'
        val semaforoDrawable = if (lugar.esActual) R.drawable.semaforo_green else R.drawable.semaforo_red
        holder.semaforo.setBackgroundResource(semaforoDrawable)

        // Evento de clic: se muestra un diálogo con opciones de ver mapa, marcar como no aparcado o eliminar
        holder.itemView.setOnClickListener {
            val activity = holder.itemView.context as AppCompatActivity
            val dialog = DetalleLugarDialog(
                lugar = lugar,
                rutaFoto = lugar.fotoRuta,
                onNoAparcado = {
                    // Marcar como no aparcado y actualizar la vista
                    lugar.esActual = false
                    notifyItemChanged(position)
                },
                onVerMapa = {
                    // Abrir MapaActivity con las coordenadas del lugar
                    val intent = Intent(activity, MapaActivity::class.java)
                    intent.putExtra("latitud", lugar.latitud)
                    intent.putExtra("longitud", lugar.longitud)
                    activity.startActivity(intent)
                },
                onEliminar = {
                    // Eliminar el registro de la base de datos y de la lista
                    val sqliteHelper = SQLiteHelper(activity)
                    if (lugar.id != null) {
                        sqliteHelper.borrarUbicacion(lugar.id!!)
                    }
                    lugares.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(activity, "Aparcamiento eliminado", Toast.LENGTH_SHORT).show()
                }
            )
            dialog.show(activity.supportFragmentManager, "DetalleLugarDialog")
        }

        // Evento de mantener presionado: elimina el lugar directamente
        holder.itemView.setOnLongClickListener {
            val activity = holder.itemView.context as AppCompatActivity
            val sqliteHelper = SQLiteHelper(activity)
            if (lugar.id != null) {
                sqliteHelper.borrarUbicacion(lugar.id!!)
            }
            lugares.removeAt(position)
            notifyItemRemoved(position)
            Toast.makeText(activity, "Lugar eliminado", Toast.LENGTH_SHORT).show()
            true
        }
    }

    /**
     * Retorna el número de elementos en la lista.
     *
     * @return El tamaño de la lista.
     */
    override fun getItemCount() = lugares.size
}
