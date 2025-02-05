package edu.ivanferrerfranco.dondeestamicoche.firebase

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche

object FirebaseManager {

    private val db = Firebase.firestore
    private val ubicacionesRef = db.collection("ubicaciones")

    /**
     * Sube una ubicación a Firebase.
     * @param ubicacion UbicacionCoche a subir.
     * @param onSuccess Callback si todo va bien.
     * @param onFailure Callback si algo falla.
     */
    fun subirUbicacion(ubicacion: UbicacionCoche,
                       onSuccess: () -> Unit,
                       onFailure: (Exception) -> Unit) {

        // Podemos usar el id local como ID del documento,
        // o dejar que Firestore genere uno con .add(...)
        val docId = ubicacion.id?.toString() ?: ubicacionesRef.document().id

        // Convertir UbicacionCoche a mapa o usar .set(ubicacion) si es un data class
        ubicacionesRef.document(docId)
            .set(ubicacion)
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Ubicación subida con éxito (docId=$docId)")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "Error al subir ubicación", e)
                onFailure(e)
            }
    }
}
