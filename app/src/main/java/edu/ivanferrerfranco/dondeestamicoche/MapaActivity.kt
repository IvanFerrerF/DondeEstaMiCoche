package edu.ivanferrerfranco.dondeestamicoche

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationServices

/**
 * Actividad que muestra un mapa con la ubicación del coche, del usuario, o un punto de referencia predeterminado.
 *
 * @constructor Crea una instancia de la actividad del mapa.
 */
class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val REQUEST_CODE_LOCATION = 1 // Código para la solicitud de permisos

    /**
     * Método llamado al crear la actividad.
     *
     * @param savedInstanceState Estado guardado anteriormente, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        Log.d("MapaActivity", "onCreate ejecutado correctamente")

        // Inicializar el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Método llamado cuando el mapa está listo para usarse.
     *
     * @param map Objeto GoogleMap inicializado.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("MapaActivity", "Google Map está listo")

        // Recuperar las coordenadas del coche desde el intent
        val cocheLatitud = intent.getDoubleExtra("coche_latitud", 0.0)
        val cocheLongitud = intent.getDoubleExtra("coche_longitud", 0.0)

        // Mostrar marcador del coche o de la Plaza de los Luceros
        if (cocheLatitud != 0.0 && cocheLongitud != 0.0) {
            mostrarUbicacionCoche(cocheLatitud, cocheLongitud)
        } else {
            mostrarPlazaLuceros()
        }

        // Verificar y solicitar permisos antes de acceder a la ubicación del usuario
        if (verificarPermisos()) {
            mostrarUbicacionActual()
        } else {
            solicitarPermisos()
        }
    }

    /**
     * Muestra la ubicación del coche en el mapa.
     *
     * @param latitud Latitud de la ubicación del coche.
     * @param longitud Longitud de la ubicación del coche.
     */
    private fun mostrarUbicacionCoche(latitud: Double, longitud: Double) {
        val ubicacionCoche = LatLng(latitud, longitud)
        googleMap.addMarker(
            MarkerOptions()
                .position(ubicacionCoche)
                .title("Ubicación del coche")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCoche, 15f))
    }

    /**
     * Muestra la Plaza de los Luceros en el mapa como ubicación predeterminada.
     */
    private fun mostrarPlazaLuceros() {
        val plazaLuceros = LatLng(38.3452, -0.4845) // Coordenadas de la Plaza de los Luceros
        googleMap.addMarker(
            MarkerOptions()
                .position(plazaLuceros)
                .title("Plaza de los Luceros")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(plazaLuceros, 15f))
    }

    /**
     * Muestra la ubicación actual del usuario en el mapa.
     */
    private fun mostrarUbicacionActual() {
        if (!verificarPermisos()) {
            solicitarPermisos()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE_LOCATION
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val ubicacionActual = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(ubicacionActual)
                        .title("Tu ubicación actual")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))

                val cocheLatitud = intent.getDoubleExtra("coche_latitud", 0.0)
                val cocheLongitud = intent.getDoubleExtra("coche_longitud", 0.0)

                if (cocheLatitud != 0.0 && cocheLongitud != 0.0) {
                    val distancia = FloatArray(1)
                    android.location.Location.distanceBetween(
                        location.latitude, location.longitude,
                        cocheLatitud, cocheLongitud,
                        distancia
                    )

                    if (distancia[0] > 0) {
                        Toast.makeText(
                            this,
                            "Distancia al coche: %.2f metros".format(distancia[0]),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Ya estás en la ubicación guardada.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se pudo obtener tu ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener la ubicación: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verifica si los permisos de ubicación están concedidos.
     *
     * @return `true` si los permisos están concedidos, `false` en caso contrario.
     */
    private fun verificarPermisos(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita los permisos necesarios para acceder a la ubicación.
     */
    private fun solicitarPermisos() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_CODE_LOCATION
        )
    }

    /**
     * Maneja la respuesta del usuario al solicitar permisos.
     *
     * @param requestCode Código de solicitud.
     * @param permissions Lista de permisos solicitados.
     * @param grantResults Resultados de la solicitud.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarUbicacionActual()
            } else {
                Toast.makeText(
                    this,
                    "Se necesitan permisos para mostrar tu ubicación actual",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
