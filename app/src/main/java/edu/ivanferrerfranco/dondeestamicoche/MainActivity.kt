package edu.ivanferrerfranco.dondeestamicoche

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.car.ui.toolbar.MenuItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.database.SQLiteHelper
import edu.ivanferrerfranco.dondeestamicoche.databinding.ActivityMainBinding
import edu.ivanferrerfranco.dondeestamicoche.dialogs.AlarmaDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.FotoDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.GuardarAparcamientoDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.StopAlarmDialog
import edu.ivanferrerfranco.dondeestamicoche.receivers.AlarmReceiver
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.view.GravityCompat
import edu.ivanferrerfranco.dondeestamicoche.firebase.FirebaseManager
import edu.ivanferrerfranco.dondeestamicoche.utils.ConnectivityHelper

/**
 * Actividad principal de la aplicación.
 *
 * Esta actividad se encarga de configurar la interfaz principal, gestionar la navegación a través del
 * Drawer, manipular eventos de botones y manejar la lógica de obtención y guardado de la ubicación del coche,
 * así como la configuración de alarmas y eventos.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    // Se reemplaza FileHandler por SQLiteHelper
    private lateinit var sqliteHelper: SQLiteHelper

    private val REQUEST_CODE_LOCATION = 1
    private val REQUEST_IMAGE_CAPTURE = 2
    private val REQUEST_CODE_CAMERA = 1001
    private val REQUEST_CODE_STORAGE = 1002

    // Se asume que la clase UbicacionCoche incluye un campo “id: Long?”
    private var ultimaUbicacion: UbicacionCoche? = null
    private var rutaFoto: String? = null
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var mostrarDialogoDetener = false

    /**
     * Método del ciclo de vida llamado al crear la actividad.
     *
     * Inicializa la interfaz de usuario, configura la Toolbar, Drawer y NavigationView, y establece las acciones
     * para los diferentes botones. También se encarga de restaurar el estado anterior y configurar alarmas y eventos.
     *
     * @param savedInstanceState Bundle con el estado previamente guardado.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar)

        // Configurar el DrawerLayout con Toggle
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar NavigationView
        binding.navView?.setNavigationItemSelectedListener(this)

        // Restauración del estado (si se dispone de datos)
        mostrarDialogoDetener = savedInstanceState?.getBoolean("mostrarDialogoDetener", false)
            ?: intent.getBooleanExtra("mostrar_detener_alarma", false)

        savedInstanceState?.let {
            val latitud = it.getDouble("latitud", 0.0)
            val longitud = it.getDouble("longitud", 0.0)
            val direccion = it.getString("direccion")
            val fecha = it.getString("fecha")
            val hora = it.getString("hora")

            if (latitud != 0.0 && longitud != 0.0) {
                ultimaUbicacion = UbicacionCoche(
                    id = null,
                    latitud = latitud,
                    longitud = longitud,
                    direccion = direccion ?: "",
                    fecha = fecha ?: "",
                    hora = hora ?: "",
                    fotoRuta = null
                )
            }
        }

        if (mostrarDialogoDetener) {
            mostrarDialogoDetenerAlarma()
        }

        // Inicializar SQLiteHelper
        sqliteHelper = SQLiteHelper(this)

        configurarBarraPublicidad()

        binding.btnBrain.setOnClickListener {
            val dialog = GuardarAparcamientoDialog(
                onGuardar = {
                    guardarUbicacionCoche { ubicacion ->
                        ultimaUbicacion = ubicacion
                        val fotoDialog = FotoDialog(
                            onAceptar = { tomarFoto() },
                            onCancelar = {
                                rutaFoto = null
                                mostrarMensaje("Ubicación guardada sin foto.")
                            }
                        )
                        fotoDialog.show(supportFragmentManager, "FotoDialog")
                    }
                },
                onCancelar = {
                    rutaFoto = null
                    mostrarMensaje("No se guardó la ubicación.")
                }
            )
            dialog.show(supportFragmentManager, "GuardarAparcamientoDialog")
        }

        binding.btnCar.setOnClickListener {
            val ubicaciones = sqliteHelper.obtenerUbicaciones()
            if (ubicaciones.isNotEmpty()) {
                val ultimaUbicacion = ubicaciones.last()
                val intent = Intent(this, MapaActivity::class.java).apply {
                    putExtra("coche_latitud", ultimaUbicacion.latitud)
                    putExtra("coche_longitud", ultimaUbicacion.longitud)
                }
                startActivity(intent)
            } else {
                mostrarMensaje("No hay ubicaciones guardadas.")
            }
        }

        binding.btnLugares.setOnClickListener {
            startActivity(Intent(this, LugaresActivity::class.java))
        }

        binding.btnCompartir.setOnClickListener { compartirUbicacion() }

        binding.btnAparcamientos.setOnClickListener {
            obtenerUbicacionActual { latitud, longitud ->
                buscarAparcamientosCercanos(latitud, longitud)
            }
        }

        binding.btnAlarma.setOnClickListener {
            val dialog = AlarmaDialog(
                onAceptar = { calendar -> configurarAlarma(calendar) },
                onCancelar = { mostrarMensaje("No se configuró la alarma.") }
            )
            dialog.show(supportFragmentManager, "AlarmaDialog")
        }

        binding.btnCalendario.setOnClickListener {
            agregarEventoCalendario(
                titulo = "Revisar el coche",
                ubicacion = "Aparcamiento Central",
                descripcion = "Revisar el coche estacionado",
                inicio = System.currentTimeMillis() + 3600000,
                fin = System.currentTimeMillis() + 7200000
            )
        }

        binding.btnAjustes.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
        }

        configurarAnimacionLinea(binding.movingLine)
        inicializarAlarma()
    }

    /**
     * Método del ciclo de vida que se ejecuta cuando la actividad vuelve a primer plano.
     *
     * Se utiliza para sincronizar ubicaciones pendientes.
     */
    override fun onResume() {
        super.onResume()
        sincronizarPendientes()
    }

    /**
     * Sincroniza las ubicaciones pendientes almacenadas en SQLite con Firebase,
     * siempre que haya conexión a Internet.
     */
    private fun sincronizarPendientes() {
        if (ConnectivityHelper.isOnline(this)) {
            val pendientes = sqliteHelper.obtenerUbicacionesPendientes()
            for (ubi in pendientes) {
                FirebaseManager.subirUbicacion(ubi,
                    onSuccess = {
                        sqliteHelper.actualizarSincronizado(ubi.id!!, true)
                        Log.d("SYNC", "Ubicacion ${ubi.id} sincronizada")
                    },
                    onFailure = { e ->
                        Log.e("SYNC", "Error subiendo ${ubi.id}", e)
                    }
                )
            }
        }
    }

    /**
     * Guarda la ubicación actual del coche.
     *
     * Obtiene la última ubicación usando los servicios de ubicación, guarda la información en SQLite,
     * marca la ubicación como actual y, de haber conexión, la sincroniza con Firebase.
     *
     * @param callback Función que recibe la ubicación guardada.
     */
    private fun guardarUbicacionCoche(callback: (UbicacionCoche) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val ubicacionCoche = UbicacionCoche(
                    id = null,
                    latitud = location.latitude,
                    longitud = location.longitude,
                    direccion = "",
                    fecha = fechaHora.split(" ")[0],
                    hora = fechaHora.split(" ")[1],
                    fotoRuta = rutaFoto,
                    sincronizado = false,
                    esActual = false // inicialmente falso
                )

                val id = sqliteHelper.insertarUbicacion(ubicacionCoche)
                if (id != -1L) {
                    // Marca la nueva ubicación como la única actual
                    sqliteHelper.marcarComoActual(id)
                    // Actualiza el objeto para reflejar que es la actual
                    val ubicConId = ubicacionCoche.copy(id = id, esActual = true)
                    ultimaUbicacion = ubicConId
                    mostrarMensaje("Ubicación guardada correctamente.")

                    // Subida a Firebase si hay conexión (como ya tienes)
                    if (ConnectivityHelper.isOnline(this)) {
                        FirebaseManager.subirUbicacion(ubicConId,
                            onSuccess = {
                                sqliteHelper.actualizarSincronizado(id, true)
                                Log.d("SYNC", "Ubicación $id subida a Firebase")
                            },
                            onFailure = {
                                Log.e("SYNC", "Error subiendo $id", it)
                            }
                        )
                    }
                    callback(ubicConId)
                } else {
                    mostrarMensaje("Error al guardar la ubicación en SQLite.")
                }
                rutaFoto = null
            } else {
                mostrarMensaje("No se pudo obtener la ubicación actual.")
            }
        }.addOnFailureListener { exception ->
            mostrarMensaje("Error al obtener la ubicación: ${exception.localizedMessage}")
        }
    }

    /**
     * Guarda la foto asociada a la última ubicación, actualizándola en SQLite y sincronizándola con Firebase si es posible.
     */
    private fun guardarUbicacionConFoto() {
        if (ultimaUbicacion != null && rutaFoto != null && ultimaUbicacion?.id != null) {
            val rowsAffected = sqliteHelper.actualizarFoto(ultimaUbicacion!!.id!!, rutaFoto!!)
            if (rowsAffected > 0) {
                Log.d("DEBUG", "Ubicación con foto actualizada en SQLite.")
                mostrarMensaje("Ubicación con foto guardada correctamente.")

                // Actualizar la última ubicación con la ruta de la foto
                ultimaUbicacion = ultimaUbicacion!!.copy(fotoRuta = rutaFoto)

                // Si hay conexión, subir a Firebase
                if (ConnectivityHelper.isOnline(this)) {
                    FirebaseManager.subirUbicacion(ultimaUbicacion!!,
                        onSuccess = {
                            sqliteHelper.actualizarSincronizado(ultimaUbicacion!!.id!!, true)
                            Log.d("SYNC", "Ubicación con foto sincronizada con Firebase")
                        },
                        onFailure = { e ->
                            Log.e("SYNC", "Error al subir la ubicación con foto", e)
                        }
                    )
                }
            } else {
                mostrarMensaje("Error al guardar la ubicación con foto.")
            }
        } else {
            mostrarMensaje("No hay foto asociada para guardar.")
        }
        rutaFoto = null
    }

    /**
     * Inicia la captura de una foto mediante la cámara.
     *
     * Verifica permisos de cámara y lanza la actividad de captura.
     */
    private fun tomarFoto() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos()
            return
        }
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            mostrarMensaje("No se pudo acceder a la cámara.")
        }
    }

    /**
     * Guarda la imagen capturada en almacenamiento interno.
     *
     * @param bitmap Imagen capturada.
     * @return Ruta absoluta del archivo guardado o null en caso de error.
     */
    private fun guardarFoto(bitmap: Bitmap): String? {
        return try {
            val fileName = "foto_aparcamiento_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()
            Log.d("DEBUG", "Foto guardada en: ${file.absolutePath}")
            mostrarMensaje("Foto guardada como $fileName.")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("DEBUG", "Error al guardar la foto: ${e.localizedMessage}")
            mostrarMensaje("Error al guardar la foto: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Método deprecado para el manejo del resultado de la actividad de captura de imagen.
     *
     * @param requestCode Código de solicitud.
     * @param resultCode Código de resultado.
     * @param data Intent con datos de la actividad.
     */
    @Deprecated("Deprecated in favor of Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                rutaFoto = guardarFoto(imageBitmap)
                if (rutaFoto != null) {
                    Log.d("DEBUG", "Ruta de la foto guardada: $rutaFoto")
                    ultimaUbicacion?.fotoRuta = rutaFoto
                    guardarUbicacionConFoto()
                }
            } else {
                rutaFoto = null
                mostrarMensaje("No se pudo capturar la foto.")
            }
        } else {
            rutaFoto = null
        }
    }

    /**
     * Muestra un mensaje breve en pantalla.
     *
     * @param mensaje Texto a mostrar.
     */
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    /**
     * Configura una animación para mover una línea de manera infinita.
     *
     * @param view Vista que se animará.
     */
    private fun configurarAnimacionLinea(view: View) {
        val startY = resources.getDimension(R.dimen.line_movement_start)
        val endY = resources.getDimension(R.dimen.line_movement_end)

        ObjectAnimator.ofFloat(binding.movingLine, "translationY", startY, endY).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

    }

    /**
     * Solicita los permisos necesarios para la ubicación, cámara y, según la versión, almacenamiento.
     */
    private fun solicitarPermisos() {
        val permisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        permisos.add(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(
            this,
            permisos.toTypedArray(),
            REQUEST_CODE_LOCATION
        )
    }

    /**
     * Maneja la respuesta del usuario a la solicitud de permisos.
     *
     * @param requestCode Código de solicitud.
     * @param permissions Permisos solicitados.
     * @param grantResults Resultados de la solicitud.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    mostrarMensaje("Permisos de ubicación concedidos.")
                } else {
                    mostrarMensaje("Se necesitan permisos para obtener la ubicación.")
                }
            }
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    mostrarMensaje("Permiso para usar la cámara concedido.")
                    tomarFoto()
                } else {
                    mostrarMensaje("Permiso para usar la cámara denegado.")
                }
            }
            REQUEST_CODE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    mostrarMensaje("Permisos de almacenamiento concedidos.")
                } else {
                    mostrarMensaje("Permisos de almacenamiento denegados.")
                }
            }
            else -> {
                mostrarMensaje("Solicitud de permisos no reconocida.")
            }
        }
    }

    /**
     * Comparte la última ubicación guardada mediante una intención de envío.
     */
    private fun compartirUbicacion() {
        if (ultimaUbicacion != null) {
            val mensaje = """
                ¡Mira esta ubicación que guardé!
                
                Dirección: ${ultimaUbicacion?.direccion ?: "Dirección no disponible"}
                Latitud: ${ultimaUbicacion?.latitud}, Longitud: ${ultimaUbicacion?.longitud}
                Fecha: ${ultimaUbicacion?.fecha}, Hora: ${ultimaUbicacion?.hora}
                
                ¡Te espero aquí! 🚗
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Ubicación guardada")
                putExtra(Intent.EXTRA_TEXT, mensaje)
            }
            startActivity(Intent.createChooser(intent, "Compartir ubicación con..."))
        } else {
            mostrarMensaje("No hay una ubicación guardada para compartir.")
        }
    }

    /**
     * Obtiene la ubicación actual del usuario y ejecuta un callback con latitud y longitud.
     *
     * @param callback Función a ejecutar con la latitud y longitud obtenidas.
     */
    private fun obtenerUbicacionActual(callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                    callback(location.latitude, location.longitude)
                } else {
                    mostrarMensaje("No se pudo obtener la ubicación actual.")
                }
            }
        } else {
            solicitarPermisos()
        }
    }

    /**
     * Busca aparcamientos cercanos utilizando la API de Google Places.
     *
     * @param latitud Latitud de la ubicación actual.
     * @param longitud Longitud de la ubicación actual.
     */
    private fun buscarAparcamientosCercanos(latitud: Double, longitud: Double) {
        val sharedPreferences = getSharedPreferences("Ajustes", Context.MODE_PRIVATE)
        val radio = sharedPreferences.getInt("radio_busqueda", 1000)
        val apiKey = getString(R.string.google_maps_key)
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$latitud,$longitud" +
                "&radius=$radio" +
                "&type=parking" +
                "&key=$apiKey"

        Log.d("DEBUG", "URL de la solicitud: $url")

        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al buscar aparcamientos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    if (jsonResponse != null) {
                        procesarResultados(jsonResponse)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error en la respuesta de la API.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    /**
     * Procesa la respuesta JSON de la API para extraer las ubicaciones de los aparcamientos
     * y lanza la actividad que muestra el mapa con dichos marcadores.
     *
     * @param jsonResponse Cadena JSON con la respuesta de la API.
     */
    private fun procesarResultados(jsonResponse: String) {
        try {
            val jsonObject = org.json.JSONObject(jsonResponse)
            val results = jsonObject.getJSONArray("results")
            val aparcamientos = ArrayList<LatLng>()
            val nombres = ArrayList<String>()

            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val location = result.getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val nombre = result.getString("name")
                aparcamientos.add(LatLng(lat, lng))
                nombres.add(nombre)
            }

            val intent = Intent(this, AparcamientosMapaActivity::class.java).apply {
                putParcelableArrayListExtra("APARCAMIENTOS", aparcamientos)
                putStringArrayListExtra("NOMBRES", nombres)
                putExtra("USER_LAT", userLat)
                putExtra("USER_LNG", userLng)
            }
            startActivity(intent)
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
            mostrarMensaje("Error al procesar resultados.")
        }
    }

    /**
     * Configura una alarma exacta para la hora indicada.
     *
     * @param calendar Objeto [Calendar] que representa la hora de la alarma.
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun configurarAlarma(calendar: Calendar) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("show_dialog", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        mostrarMensaje(
            "Alarma configurada para las ${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                    "${calendar.get(Calendar.MINUTE)}"
        )
    }

    /**
     * Muestra un diálogo para detener la alarma en ejecución.
     */
    private fun mostrarDialogoDetenerAlarma() {
        mostrarDialogoDetener = true
        val dialog = StopAlarmDialog(
            onStopAlarm = {
                AlarmReceiver.ringtone?.stop()
                AlarmReceiver.ringtone = null
                mostrarDialogoDetener = false
                mostrarMensaje("La alarma ha sido detenida.")
            }
        )
        dialog.show(supportFragmentManager, "StopAlarmDialog")
    }

    /**
     * Guarda el estado actual de la actividad, incluyendo la última ubicación y la bandera de diálogo.
     *
     * @param outState Bundle en el que se guardan los datos.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mostrarDialogoDetener", mostrarDialogoDetener)
        ultimaUbicacion?.let {
            outState.putDouble("latitud", it.latitud)
            outState.putDouble("longitud", it.longitud)
            outState.putString("direccion", it.direccion)
            outState.putString("fecha", it.fecha)
            outState.putString("hora", it.hora)
        }
    }

    /**
     * Inicializa la alarma sin programarla, configurando el intent asociado.
     */
    private fun inicializarAlarma() {
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("show_dialog", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Agrega un evento al calendario del dispositivo.
     *
     * @param titulo Título del evento.
     * @param ubicacion Ubicación del evento.
     * @param descripcion Descripción del evento.
     * @param inicio Tiempo de inicio en milisegundos.
     * @param fin Tiempo de fin en milisegundos.
     */
    private fun agregarEventoCalendario(
        titulo: String,
        ubicacion: String,
        descripcion: String,
        inicio: Long,
        fin: Long
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            putExtra(android.provider.CalendarContract.Events.TITLE, titulo)
            putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, ubicacion)
            putExtra(android.provider.CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
            putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, fin)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de calendario", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Configura la barra de publicidad en la interfaz.
     */
    private fun configurarBarraPublicidad() {
        binding.adText.text = "¡Oferta especial! 🚗 10% de descuento en estacionamientos."
    }

    /**
     * Maneja el evento de retroceso para cerrar el Drawer si está abierto.
     */
    override fun onBackPressed() {
        // Cerrar el Drawer si está abierto
        if (binding.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Maneja la selección de ítems en el NavigationView.
     *
     * @param item Elemento del menú seleccionado.
     * @return `true` si el evento se procesó correctamente.
     */
    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_ayuda -> {
                val intent = Intent(this, AyudaIAActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_brain -> {
                // En lugar de startActivity, mostramos el diálogo:
                val dialog = GuardarAparcamientoDialog(
                    onGuardar = {
                        // Lógica que desees al pulsar en "Guardar" dentro del diálogo
                    },
                    onCancelar = {
                        // Lógica que desees al pulsar en "Cancelar" dentro del diálogo
                    }
                )
                dialog.show(supportFragmentManager, "GuardarAparcamientoDialog")
            }
            R.id.nav_car -> {
                // Ejecutar el método para encontrar coche
                startActivity(Intent(this, MapaActivity::class.java))
            }
            R.id.nav_lugares -> {
                // Abrir lista de lugares
                startActivity(Intent(this, LugaresActivity::class.java))
            }
            R.id.nav_aparcamientos -> {
                // Buscar aparcamientos cercanos
                obtenerUbicacionActual { latitud, longitud ->
                    buscarAparcamientosCercanos(latitud, longitud)
                }
            }
            R.id.nav_alarma -> {
                val dialog = AlarmaDialog(
                    onAceptar = { calendar ->
                        // Lógica cuando se confirma la hora, p. ej. configurarAlarma(calendar)
                    },
                    onCancelar = {
                        // Lógica de cancelación
                    }
                )
                dialog.show(supportFragmentManager, "AlarmaDialog")
            }
            R.id.nav_calendario -> {
                // Agregar evento al calendario
                agregarEventoCalendario(
                    titulo = "Revisar el coche",
                    ubicacion = "Aparcamiento Central",
                    descripcion = "Revisar el coche estacionado",
                    inicio = System.currentTimeMillis() + 3600000,
                    fin = System.currentTimeMillis() + 7200000
                )
            }
            R.id.nav_compartir -> {
                // Compartir ubicación
                compartirUbicacion()
            }
            R.id.nav_ajustes -> {
                // Abrir configuración
                startActivity(Intent(this, AjustesActivity::class.java))
            }
        }
        // Cerrar el Drawer después de seleccionar una opción
        binding.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

}
