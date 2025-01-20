package edu.ivanferrerfranco.dondeestamicoche

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Actividad que muestra un mapa con la ubicación del usuario y los aparcamientos cercanos.
 * Implementa [OnMapReadyCallback] para configurar y manejar el mapa una vez que esté listo.
 */
class AparcamientosMapaActivity : AppCompatActivity(), OnMapReadyCallback {

    // Mapa de Google utilizado para mostrar las ubicaciones
    private lateinit var googleMap: GoogleMap

    // Lista de coordenadas de los aparcamientos cercanos
    private var aparcamientos: ArrayList<LatLng> = arrayListOf()

    // Lista de nombres asociados a los aparcamientos
    private var nombres: ArrayList<String> = arrayListOf()

    // Latitud y longitud de la ubicación del usuario
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0

    /**
     * Configura la actividad al ser creada.
     * Recupera las listas de aparcamientos, nombres y la ubicación del usuario desde el intent.
     * Inicia la configuración del fragmento de mapa.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aparcamientos_mapa)

        // Recupera los datos pasados desde el intent
        aparcamientos = intent.getParcelableArrayListExtra("APARCAMIENTOS") ?: arrayListOf()
        nombres = intent.getStringArrayListExtra("NOMBRES") ?: arrayListOf()
        userLat = intent.getDoubleExtra("USER_LAT", 0.0)
        userLng = intent.getDoubleExtra("USER_LNG", 0.0)

        // Logs para depuración
        Log.d("DEBUG", "Aparcamientos recibidos: ${aparcamientos.size}")
        Log.d("DEBUG", "Nombres recibidos: ${nombres.size}")
        Log.d("DEBUG", "Ubicación del usuario: [$userLat, $userLng]")

        // Configura el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Método que se llama cuando el mapa está listo para ser utilizado.
     * Configura el mapa centrado en la posición del usuario y añade marcadores para los aparcamientos.
     *
     * @param map Instancia de [GoogleMap] proporcionada cuando el mapa está listo.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Centrar el mapa en la posición del usuario
        val userLocation = LatLng(userLat, userLng)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        // Agregar un marcador para la posición del usuario
        googleMap.addMarker(
            MarkerOptions()
                .position(userLocation)
                .title("Tu ubicación")
        )

        // Agregar marcadores para los aparcamientos
        for (i in aparcamientos.indices) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(aparcamientos[i])
                    .title(nombres[i])
            )
        }
    }
}
