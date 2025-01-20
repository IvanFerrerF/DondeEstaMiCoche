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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.databinding.ActivityMainBinding
import edu.ivanferrerfranco.dondeestamicoche.dialogs.AlarmaDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.FotoDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.GuardarAparcamientoDialog
import edu.ivanferrerfranco.dondeestamicoche.dialogs.StopAlarmDialog
import edu.ivanferrerfranco.dondeestamicoche.receivers.AlarmReceiver
import edu.ivanferrerfranco.dondeestamicoche.utils.FileHandler
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Clase principal de la aplicación que gestiona la mayoría de las interacciones del usuario.
 *
 * Esta actividad es el punto de entrada de la aplicación y se encarga de:
 * - Guardar y gestionar ubicaciones de estacionamiento del coche, con opción de adjuntar una foto.
 * - Mostrar un mapa con la última ubicación guardada.
 * - Buscar aparcamientos cercanos utilizando la API de Google Places.
 * - Configurar alarmas relacionadas con el coche (por ejemplo, recordatorios).
 * - Compartir la ubicación actual del coche a través de otras aplicaciones.
 * - Interactuar con un calendario para programar eventos.
 * - Administrar una lista de ubicaciones guardadas previamente.
 * - Ofrecer configuraciones adicionales mediante la pantalla de ajustes.
 *
 * Contiene integración con funcionalidades avanzadas como:
 * - Google Maps API para la geolocalización y búsqueda de lugares.
 * - Cámara para capturar imágenes del lugar de estacionamiento.
 * - Calendar API para crear eventos asociados al coche.
 * - Animaciones en la interfaz para mejorar la experiencia del usuario.
 */
class MainActivity : AppCompatActivity() {

    /** ViewBinding para acceder a los elementos del layout de la actividad principal. */
    private lateinit var binding: ActivityMainBinding

    /** Manejador de archivos para gestionar la persistencia de datos relacionados con las ubicaciones del coche. */
    private lateinit var fileHandler: FileHandler<UbicacionCoche>

    /** Código de solicitud para permisos de ubicación. */
    private val REQUEST_CODE_LOCATION = 1

    /** Código de solicitud para capturar una imagen. */
    private val REQUEST_IMAGE_CAPTURE = 2

    /** Código de solicitud para permisos de cámara. */
    private val REQUEST_CODE_CAMERA = 1001

    /** Código de solicitud para permisos de almacenamiento. */
    private val REQUEST_CODE_STORAGE = 1002

    /** Última ubicación guardada del coche. */
    private var ultimaUbicacion: UbicacionCoche? = null

    /** Ruta de la foto capturada para asociarla con la ubicación. */
    private var rutaFoto: String? = null

    /** Latitud de la ubicación actual del usuario. */
    private var userLat: Double = 0.0

    /** Longitud de la ubicación actual del usuario. */
    private var userLng: Double = 0.0

    /** Bandera para determinar si se debe mostrar el diálogo para detener la alarma. */
    private var mostrarDialogoDetener = false

    /**
     * Método que se ejecuta al crear la actividad.
     * Configura los elementos iniciales y define los listeners para los botones de la interfaz.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar el estado del diálogo de detener alarma
        mostrarDialogoDetener = savedInstanceState?.getBoolean("mostrarDialogoDetener", false)
            ?: intent.getBooleanExtra("mostrar_detener_alarma", false)

        // Restaurar última ubicación guardada
        savedInstanceState?.let {
            val latitud = it.getDouble("latitud", 0.0)
            val longitud = it.getDouble("longitud", 0.0)
            val direccion = it.getString("direccion")
            val fecha = it.getString("fecha")
            val hora = it.getString("hora")

            if (latitud != 0.0 && longitud != 0.0) {
                ultimaUbicacion = UbicacionCoche(
                    latitud = latitud,
                    longitud = longitud,
                    direccion = direccion ?: "",
                    fecha = fecha ?: "",
                    hora = hora ?: "",
                    esActual = true
                )
            }
        }

        // Mostrar el diálogo si corresponde
        if (mostrarDialogoDetener) {
            mostrarDialogoDetenerAlarma()
        }

        // Inicializar el manejador de archivos
        fileHandler = FileHandler(
            context = this,
            fileName = "ubicaciones.json",
            type = UbicacionCoche::class.java
        )

        // Configurar barra de publicidad
        configurarBarraPublicidad()

        // Configurar listener para guardar una ubicación
        binding.btnBrain.setOnClickListener {
            val dialog = GuardarAparcamientoDialog(
                onGuardar = {
                    guardarUbicacionCoche { ubicacion ->
                        ultimaUbicacion = ubicacion

                        val fotoDialog = FotoDialog(
                            onAceptar = {
                                tomarFoto()
                            },
                            onCancelar = {
                                rutaFoto = null // Reinicia la variable rutaFoto
                                mostrarMensaje("Ubicación guardada sin foto.")
                            }
                        )
                        fotoDialog.show(supportFragmentManager, "FotoDialog")
                    }
                },
                onCancelar = {
                    rutaFoto = null // Limpia la variable si se cancela
                    mostrarMensaje("No se guardó la ubicación.")
                }
            )
            dialog.show(supportFragmentManager, "GuardarAparcamientoDialog")
        }

        // Configurar listener para mostrar la última ubicación guardada
        binding.btnCar.setOnClickListener {
            val ubicaciones = fileHandler.read()
            if (!ubicaciones.isNullOrEmpty()) {
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

        // Configurar listener para mostrar la lista de lugares guardados
        binding.btnLugares.setOnClickListener {
            startActivity(Intent(this, LugaresActivity::class.java))
        }

        // Configurar listener para compartir la última ubicación guardada
        binding.btnCompartir.setOnClickListener {
            compartirUbicacion()
        }

        // Configurar listener para buscar aparcamientos cercanos
        binding.btnAparcamientos.setOnClickListener {
            obtenerUbicacionActual { latitud, longitud ->
                buscarAparcamientosCercanos(latitud, longitud)
            }
        }

        // Configurar listener para establecer una alarma
        binding.btnAlarma.setOnClickListener {
            val dialog = AlarmaDialog(
                onAceptar = { calendar ->
                    configurarAlarma(calendar)
                },
                onCancelar = {
                    mostrarMensaje("No se configuró la alarma.")
                }
            )
            dialog.show(supportFragmentManager, "AlarmaDialog")
        }

        // Configurar listener para agregar un evento al calendario
        binding.btnCalendario.setOnClickListener {
            agregarEventoCalendario(
                titulo = "Revisar el coche",
                ubicacion = "Aparcamiento Central",
                descripcion = "Revisar el coche estacionado",
                inicio = System.currentTimeMillis() + 3600000, // 1 hora desde ahora
                fin = System.currentTimeMillis() + 7200000    // 2 horas desde ahora
            )
        }

        // Configurar listener para acceder a los ajustes
        binding.btnAjustes.setOnClickListener {
            val intent = Intent(this, AjustesActivity::class.java)
            startActivity(intent)
        }

        // Configurar la animación de la línea
        configurarAnimacionLinea(binding.movingLine)

        // Inicializar la alarma
        inicializarAlarma()
    }


    /**
     * Guarda la ubicación actual del coche.
     * @param callback Función que se ejecuta con la ubicación guardada.
     */
    private fun guardarUbicacionCoche(callback: (UbicacionCoche) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificar permisos de ubicación antes de proceder
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos()
            return
        }

        // Obtener la última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val fechaHora =
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val ubicacionCoche = UbicacionCoche(
                    latitud = location.latitude,
                    longitud = location.longitude,
                    fecha = fechaHora.split(" ")[0],
                    hora = fechaHora.split(" ")[1],
                    esActual = true,
                    fotoRuta = rutaFoto // Asocia la foto capturada, si existe
                )

                val lugares = fileHandler.read()?.toMutableList() ?: mutableListOf()
                lugares.forEach {
                    it.esActual = false
                } // Marcar todas las ubicaciones como no actuales
                lugares.add(ubicacionCoche)

                if (fileHandler.write(lugares)) {
                    mostrarMensaje("Ubicación guardada correctamente.")
                    callback(ubicacionCoche)
                } else {
                    mostrarMensaje("Error al guardar la ubicación.")
                }

                // Reinicia rutaFoto después de guardar
                rutaFoto = null
            } else {
                mostrarMensaje("No se pudo obtener la ubicación actual.")
            }
        }.addOnFailureListener { exception ->
            mostrarMensaje("Error al obtener la ubicación: ${exception.localizedMessage}")
        }
    }

    /**
     * Guarda la última ubicación registrada del coche junto con la foto asociada, si existe.
     */
    private fun guardarUbicacionConFoto() {
        if (ultimaUbicacion != null && rutaFoto != null) {
            ultimaUbicacion?.fotoRuta = rutaFoto // Asocia la foto con la ubicación
            val lugares = fileHandler.read()?.toMutableList() ?: mutableListOf()

            // Actualizar la lista de ubicaciones
            lugares.removeAll { it.esActual } // Elimina las ubicaciones marcadas como actuales
            lugares.add(ultimaUbicacion!!) // Añade la nueva ubicación

            if (fileHandler.write(lugares)) {
                Log.d("DEBUG", "Ubicación con foto guardada en JSON.")
                mostrarMensaje("Ubicación con foto guardada correctamente.")
            } else {
                mostrarMensaje("Error al guardar la ubicación con foto.")
            }
        } else {
            mostrarMensaje("No hay foto asociada para guardar.")
        }

        // Limpia rutaFoto después de procesar
        rutaFoto = null
    }

    /**
     * Inicia el proceso de captura de foto para asociarla con una ubicación.
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
     * Guarda una foto capturada en el almacenamiento interno y devuelve su ruta.
     * @param bitmap La imagen capturada como un objeto Bitmap.
     * @return Ruta del archivo guardado o null si ocurre un error.
     */
    private fun guardarFoto(bitmap: Bitmap): String? {
        return try {
            val fileName = "foto_aparcamiento_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()

            // Confirmar guardado con logs y mensajes
            Log.d("DEBUG", "Foto guardada en: ${file.absolutePath}")
            mostrarMensaje("Foto guardada como $fileName.")

            file.absolutePath // Devuelve la ruta del archivo
        } catch (e: Exception) {
            // Captura y notifica errores
            Log.e("DEBUG", "Error al guardar la foto: ${e.localizedMessage}")
            mostrarMensaje("Error al guardar la foto: ${e.localizedMessage}")
            null
        }
    }


    /**
     * Callback para manejar resultados de actividades iniciadas. Deprecado en favor de Activity Result API.
     * @param requestCode Código de solicitud que identifica la actividad.
     * @param resultCode Código de resultado indicando el estado de la actividad.
     * @param data Intent que contiene los datos devueltos por la actividad.
     */
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
                // Si no hay foto, limpia la ruta
                rutaFoto = null
                mostrarMensaje("No se pudo capturar la foto.")
            }
        } else {
            // Si el resultado no es exitoso, limpia la ruta
            rutaFoto = null
        }
    }

    /**
     * Muestra un mensaje en pantalla usando un Toast.
     * @param mensaje Mensaje a mostrar.
     */
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    /**
     * Configura una animación infinita para una vista que simula el movimiento de una línea.
     * @param view Vista a animar.
     */
    private fun configurarAnimacionLinea(view: View) {
        ObjectAnimator.ofFloat(view, "translationY", 0f, 1800f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    /**
     * Solicita los permisos necesarios para la aplicación, incluyendo ubicación y cámara.
     */
    private fun solicitarPermisos() {
        val permisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Añade permiso de almacenamiento solo si la versión es menor a Android Q (Android 10)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Añade permiso para la cámara
        permisos.add(Manifest.permission.CAMERA)

        // Solicita todos los permisos en una sola llamada
        ActivityCompat.requestPermissions(
            this,
            permisos.toTypedArray(),
            REQUEST_CODE_LOCATION
        )
    }

    /**
     * Callback para manejar los resultados de las solicitudes de permisos.
     * @param requestCode Código que identifica la solicitud de permisos.
     * @param permissions Array de permisos solicitados.
     * @param grantResults Resultados de los permisos concedidos o denegados.
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
                    tomarFoto() // Llama a la función de captura de foto si el permiso fue otorgado
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
     * Comparte la última ubicación guardada mediante aplicaciones disponibles en el dispositivo.
     * Si no hay una ubicación guardada, muestra un mensaje de error.
     */
    private fun compartirUbicacion() {
        // Verificar que haya una ubicación guardada
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

            // Mostrar el selector de aplicaciones
            startActivity(Intent.createChooser(intent, "Compartir ubicación con..."))
        } else {
            mostrarMensaje("No hay una ubicación guardada para compartir.")
        }
    }

    /**
     * Obtiene la ubicación actual del usuario y ejecuta un callback con las coordenadas obtenidas.
     * Si los permisos de ubicación no están concedidos, solicita los permisos.
     * @param callback Función que recibe las coordenadas de la ubicación actual.
     */
    private fun obtenerUbicacionActual(callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLat = location.latitude // Actualiza la variable global
                    userLng = location.longitude // Actualiza la variable global
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
     * Busca aparcamientos cercanos a las coordenadas especificadas utilizando la API de Google Places.
     * Los resultados se basan en un radio de búsqueda configurado en las preferencias compartidas.
     * @param latitud Latitud de la ubicación actual.
     * @param longitud Longitud de la ubicación actual.
     */
    private fun buscarAparcamientosCercanos(latitud: Double, longitud: Double) {
        // Leer el radio desde SharedPreferences
        val sharedPreferences = getSharedPreferences("Ajustes", Context.MODE_PRIVATE)
        val radio = sharedPreferences.getInt("radio_busqueda", 1000) // Default: 1000 metros

        val apiKey = getString(R.string.google_maps_key) // Tu API Key
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$latitud,$longitud" +
                "&radius=$radio" +
                "&type=parking" +
                "&key=$apiKey"

        Log.d("DEBUG", "URL de la solicitud: $url") // Muestra el URL para depuración

        // Realizar una solicitud HTTP usando OkHttp
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            /**
             * Maneja los errores al realizar la solicitud HTTP.
             * Muestra un mensaje en la interfaz de usuario.
             * @param call Llamada de la solicitud HTTP.
             * @param e Excepción ocurrida durante la solicitud.
             */
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Manejo del error al hacer la solicitud
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al buscar aparcamientos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            /**
             * Procesa la respuesta HTTP recibida de la API de Google Places.
             * Si la respuesta es válida, se llama a la función para procesar los resultados.
             * @param call Llamada de la solicitud HTTP.
             * @param response Respuesta recibida de la API.
             */
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    // Procesar la respuesta si es válida
                    val jsonResponse = response.body?.string()
                    if (jsonResponse != null) {
                        procesarResultados(jsonResponse)
                    }
                } else {
                    // Manejo de error en la respuesta de la API
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
     * Procesa los resultados de la API de Google Places y los prepara para mostrarlos en una nueva actividad.
     * Extrae las coordenadas y nombres de los aparcamientos cercanos desde la respuesta JSON.
     * @param jsonResponse Respuesta en formato JSON obtenida de la API.
     */
    private fun procesarResultados(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)
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

            // Pasar los resultados a AparcamientosMapaActivity
            val intent = Intent(this, AparcamientosMapaActivity::class.java).apply {
                putParcelableArrayListExtra("APARCAMIENTOS", aparcamientos)
                putStringArrayListExtra("NOMBRES", nombres)
                putExtra("USER_LAT", userLat) // Latitud del usuario
                putExtra("USER_LNG", userLng) // Longitud del usuario
            }
            startActivity(intent)

        } catch (e: JSONException) {
            e.printStackTrace()
            mostrarMensaje("Error al procesar resultados.")
        }
    }

    /**
     * Configura una alarma utilizando el servicio AlarmManager.
     * La alarma mostrará un diálogo cuando se active.
     * @param calendar Objeto Calendar que contiene la fecha y hora de la alarma.
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun configurarAlarma(calendar: Calendar) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("show_dialog", true) // Asegúrate de incluir esta bandera
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        mostrarMensaje(
            "Alarma configurada para las ${calendar.get(Calendar.HOUR_OF_DAY)}:${
                calendar.get(
                    Calendar.MINUTE
                )
            }"
        )
    }

    /**
     * Muestra un diálogo para detener la alarma.
     * Llama a un método estático en AlarmReceiver para detener el tono de la alarma.
     */
    private fun mostrarDialogoDetenerAlarma() {
        mostrarDialogoDetener = true
        val dialog = StopAlarmDialog(
            onStopAlarm = {
                // Llama al método estático de AlarmReceiver para detener el tono
                AlarmReceiver.ringtone?.stop()
                AlarmReceiver.ringtone = null
                mostrarDialogoDetener = false
                mostrarMensaje("La alarma ha sido detenida.")
            }
        )
        dialog.show(supportFragmentManager, "StopAlarmDialog")
    }

    /**
     * Guarda el estado actual de la actividad en un objeto `Bundle`.
     * Esto permite preservar información importante durante cambios de configuración,
     * como la rotación de pantalla.
     *
     * @param outState Objeto `Bundle` donde se guarda el estado de la actividad.
     * Contiene:
     * - `mostrarDialogoDetener`: Un booleano que indica si el diálogo de detener alarma está activo.
     * - `latitud`: Coordenada de latitud de la última ubicación guardada, si existe.
     * - `longitud`: Coordenada de longitud de la última ubicación guardada, si existe.
     * - `direccion`: Dirección asociada a la última ubicación guardada.
     * - `fecha`: Fecha en la que se guardó la última ubicación.
     * - `hora`: Hora en la que se guardó la última ubicación.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mostrarDialogoDetener", mostrarDialogoDetener)
        // Guardar última ubicación, si existe
        ultimaUbicacion?.let {
            outState.putDouble("latitud", it.latitud)
            outState.putDouble("longitud", it.longitud)
            outState.putString("direccion", it.direccion)
            outState.putString("fecha", it.fecha)
            outState.putString("hora", it.hora)
        }
    }


    /**
     * Inicializa el intent de alarma con una bandera para mostrar un diálogo al activarse.
     */
    private fun inicializarAlarma() {
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("show_dialog", true) // Bandera para mostrar el diálogo
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Agrega un evento al calendario utilizando un Intent.
     * @param titulo Título del evento.
     * @param ubicacion Ubicación del evento.
     * @param descripcion Descripción del evento.
     * @param inicio Hora de inicio del evento en milisegundos.
     * @param fin Hora de fin del evento en milisegundos.
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

        // Verifica si hay una app capaz de manejar el intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de calendario", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Configura y muestra una barra de publicidad con contenido específico.
     */
    @SuppressLint("SetTextI18n")
    private fun configurarBarraPublicidad() {
        // Establecer contenido de la publicidad
        binding.adText.text = "¡Oferta especial! 🚗 10% de descuento en estacionamientos."
    }
}

