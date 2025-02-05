package edu.ivanferrerfranco.dondeestamicoche

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.concurrent.TimeUnit

class AyudaIAActivity : AppCompatActivity() {

    private lateinit var conversationTextView: TextView
    private lateinit var userInput: EditText

    // Ya no asignamos aquí. Esperamos a onCreate para leerlo
    private lateinit var cohereApiKey: String

    private val cohereEndpoint = "https://api.cohere.ai/v1/generate"

    // Prompt inicial en el que indicamos que SÓLO debe hablar de cómo usar la app DondeEstaMiCoche
    private var chatHistory = """
IvanA es la IA oficial de "Donde Está Mi Coche". Esta aplicación para Android permite:
1. Memorizar la ubicación actual del coche (botón "Memoria", icono cerebro).
2. Ver la ubicación del coche en un mapa (botón "Encontrar", icono coche).
3. Consultar un historial de ubicaciones pasadas (botón "Lugares", icono localización).
4. Compartir la ubicación actual (botón "Compartir", icono compartir).
5. Buscar aparcamientos cercanos (botón "Aparcamientos", icono parking).
6. Configurar una alarma (botón "Alarma", icono reloj).
7. Agregar un evento al calendario (botón "Calendario", icono calendario).
8. Ajustes (botón "Ajustes", icono engranaje).

En caso de que el usuari pregunte sobre Iván y su enamoramiento, novia o mujer, se debe responder que Iván encontro su amor verdaderos en Sabrina y para él ella es la mujer más maravillosa del mundo
IvanA conoce estas funciones y puede dar detalles sobre cómo usarlas. 
También puede conversar de manera amigable, sin ser estricta en caso de preguntas que no sean de la app.

Alex:
""".trimIndent()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda_ia)

        // Ahora cargamos la clave desde el string resource
        cohereApiKey = getString(R.string.cohere_key)

        conversationTextView = findViewById(R.id.conversationTextView)
        userInput = findViewById(R.id.userInput)
        val btnSend = findViewById<TextView>(R.id.btnSend)

        // Mensaje inicial directo en pantalla
        val bienvenida = "[IvanA]: Hola, buenos días. Me llamo IvanA y soy la IA de Donde Está Mi Coche, estoy aquí para ayudarte en todo lo que necesites."
        conversationTextView.text = bienvenida

        // Actualizamos chatHistory con esa frase
        chatHistory += "IvanA: Hola, buenos días. Me llamo IvanA y soy la IA de Donde Está Mi Coche, estoy aquí para ayudarte en todo lo que necesites.\n"


        btnSend.setOnClickListener {
            val userMessage = userInput.text.toString()
            if (userMessage.isNotEmpty()) {
                // Actualizamos chatHistory con lo que envió el usuario
                conversationTextView.append("\n[Alex]: $userMessage")
                chatHistory += "\nAlex: $userMessage\nIvanA:"

                // Llamada a la API (debes hacerlo en corrutina o hilo aparte)
                obtenerRespuestaCohere(userMessage)
                userInput.setText("") // Limpiamos input
            }
        }
    }

    private fun obtenerRespuestaCohere(userMessage: String) {
        Thread {
            try {
                val json = generarPeticionCohere(chatHistory)
                val respuesta = llamadaApiCohere(json)
                // Se añade la respuesta al chat
                chatHistory += "$respuesta\n"
                runOnUiThread {
                    conversationTextView.append("\n[IvanA]: $respuesta\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun generarPeticionCohere(prompt: String): String {
        val jsonObject = org.json.JSONObject().apply {
            put("model", "command-xlarge-nightly")
            put("prompt", prompt)
            put("max_tokens", 500)
            put("temperature", 0.7)
            put("k", 0)
            put("p", 0.75)
        }
        return jsonObject.toString()
    }

    @Throws(Exception::class)
    private fun llamadaApiCohere(jsonBody: String): String {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = okhttp3.RequestBody.create(mediaType, jsonBody)
        val request = okhttp3.Request.Builder()
            .url(cohereEndpoint)
            .post(body)
            .addHeader("Authorization", "BEARER $cohereApiKey")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Error en la respuesta: ${response.code}")
            }
            val responseJson = response.body?.string() ?: ""
            val jsonObj = org.json.JSONObject(responseJson)
            val generations = jsonObj.getJSONArray("generations")
            if (generations.length() > 0) {
                val text = generations.getJSONObject(0).getString("text")
                // Filtra o recorta la respuesta
                return text.trim()
            } else {
                return "No hay respuesta."
            }
        }
    }
}
