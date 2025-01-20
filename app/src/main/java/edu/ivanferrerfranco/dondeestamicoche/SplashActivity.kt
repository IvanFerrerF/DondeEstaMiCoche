package edu.ivanferrerfranco.dondeestamicoche

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * Actividad que muestra una pantalla de bienvenida (Splash Screen) al iniciar la aplicación.
 *
 * Esta pantalla incluye una animación y redirige al usuario a la actividad principal (`MainActivity`)
 * después de un breve período de tiempo.
 */
@SuppressLint("CustomSplashScreen") // Desactiva el aviso del uso de SplashScreen personalizado
class SplashActivity : AppCompatActivity() {

    /**
     * Método llamado al crear la actividad.
     *
     * @param savedInstanceState Estado guardado anteriormente, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Configuración de la animación del logo
        val logo = findViewById<ImageView>(R.id.logo) // Referencia al logo en el layout
        val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in) // Cargar animación desde recursos
        logo.startAnimation(animation) // Iniciar animación

        // Configurar transición a la actividad principal después de 3 segundos
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java) // Crear intent para MainActivity
            startActivity(intent) // Iniciar actividad principal
            finish() // Finalizar la SplashActivity
        }, 3000) // 3000 ms = 3 segundos
    }
}
