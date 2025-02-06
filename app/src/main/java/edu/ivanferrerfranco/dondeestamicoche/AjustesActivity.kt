package edu.ivanferrerfranco.dondeestamicoche

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import edu.ivanferrerfranco.dondeestamicoche.database.SQLiteHelper
import edu.ivanferrerfranco.dondeestamicoche.databinding.ActivityAjustesBinding
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche
import edu.ivanferrerfranco.dondeestamicoche.dialogs.RadioBusquedaDialogFragment

/**
 * Actividad que permite configurar ajustes de la aplicación, como borrar el historial,
 * modificar el radio de búsqueda, gestionar permisos y acceder a la configuración del sistema.
 */
class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private val REQUEST_PERMISSION_CODE = 1001

    private val permisos = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    /**
     * Método del ciclo de vida llamado al crear la actividad.
     *
     * Configura la interfaz y establece los listeners para las acciones de ajustes.
     *
     * @param savedInstanceState Bundle con el estado previamente guardado.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBorrarHistorial.setOnClickListener {
            mostrarConfirmacionBorrarHistorial()
        }

        binding.btnRadioBusqueda.setOnClickListener {
            val dialog = RadioBusquedaDialogFragment { radioSeleccionado ->
                guardarRadioBusqueda(radioSeleccionado)
            }
            dialog.show(supportFragmentManager, "RadioBusquedaDialog")
        }

        binding.btnGestionPermisos.setOnClickListener {
            abrirConfiguracionDeAplicacion(this)
        }

        binding.btnCambiarIdioma.setOnClickListener {
            mostrarDialogoExplicativo()
        }
    }

    /**
     * Muestra un diálogo para confirmar el borrado del historial de lugares guardados.
     */
    private fun mostrarConfirmacionBorrarHistorial() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar acción")
            .setMessage("¿Estás seguro de que deseas borrar todo el historial de lugares guardados? Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ -> borrarHistorial() }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    /**
     * Borra el historial de ubicaciones almacenado en SQLite.
     */
    private fun borrarHistorial() {
        val sqliteHelper = SQLiteHelper(this)
        sqliteHelper.borrarHistorial()
        Toast.makeText(this, "Historial borrado correctamente.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Guarda el radio de búsqueda seleccionado en las preferencias de la aplicación.
     *
     * @param radio Valor del radio de búsqueda en metros.
     */
    private fun guardarRadioBusqueda(radio: Int) {
        val sharedPreferences = getSharedPreferences("Ajustes", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("radio_busqueda", radio).apply()
        Toast.makeText(this, "Radio de búsqueda guardado: $radio metros", Toast.LENGTH_SHORT).show()
    }

    /**
     * Solicita un permiso específico, mostrando una explicación si es necesario.
     *
     * @param permiso Permiso que se desea solicitar.
     */
    private fun pedirPermiso(permiso: String) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permiso)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("La aplicación necesita este permiso para funcionar correctamente.")
                .setPositiveButton("Conceder") { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(permiso), REQUEST_PERMISSION_CODE)
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permiso), REQUEST_PERMISSION_CODE)
        }
    }

    /**
     * Muestra un diálogo indicando cómo revocar un permiso ya concedido.
     *
     * @param permiso Permiso sobre el que se quiere mostrar la información.
     */
    private fun mostrarDialogoIrAConfiguracion(permiso: String) {
        AlertDialog.Builder(this)
            .setTitle("Revocar permiso")
            .setMessage(
                "El permiso $permiso ya está concedido. Si deseas revocarlo, puedes hacerlo manualmente desde la configuración del sistema."
            )
            .setPositiveButton("Ir a configuración") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    /**
     * Abre la pantalla de configuración de la aplicación en el sistema.
     *
     * @param context Contexto de la aplicación.
     */
    private fun abrirConfiguracionDeAplicacion(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Muestra un diálogo explicativo acerca de la funcionalidad de cambio de idioma.
     */
    private fun mostrarDialogoExplicativo() {
        val mensajeExplicativo = """
            NOTA:
            La funcionalidad para cambiar el idioma ha sido deshabilitada temporalmente.
            
            Motivo:
            Al cambiar entre orientaciones (horizontal/vertical), el idioma se restablecía automáticamente, lo que generaba una experiencia de usuario inconsistente.
            
            Próximo Paso:
            Prometo implementar esta funcionalidad en el futuro con una solución más robusta que permita conservar el idioma seleccionado independientemente de la orientación.
            
            ¡Gracias por su comprensión! 😊
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Información sobre el Cambio de Idioma")
            .setMessage(mensajeExplicativo)
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
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
        if (requestCode == REQUEST_PERMISSION_CODE) {
            permissions.forEachIndexed { index, permiso ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso concedido: $permiso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso denegado: $permiso", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
