package edu.ivanferrerfranco.dondeestamicoche.data

/**
 * Representa la información relacionada con la ubicación del coche.
 *
 * @property id Identificador único de la ubicación (generado por SQLite).
 *              Es nulo cuando se crea una nueva instancia y se asigna al insertarla en la base de datos.
 * @property latitud Coordenada de latitud de la ubicación del coche.
 * @property longitud Coordenada de longitud de la ubicación del coche.
 * @property fecha Fecha en la que se registró la ubicación (formato dd/MM/yyyy).
 * @property hora Hora en la que se registró la ubicación (formato HH:mm).
 * @property direccion Dirección asociada a la ubicación (puede ser nula si no está disponible).
 * @property fotoRuta Ruta del archivo de la foto asociada a la ubicación (puede ser nula).
 * @property esActual Indica si esta ubicación es la más reciente en la que se aparcó el coche.
 * @property fechaHoraSalida Fecha y hora en la que se registró la salida de la ubicación (puede ser nula).
 */
data class UbicacionCoche(
    val id: Long? = null,
    val latitud: Double,
    val longitud: Double,
    val fecha: String,
    val hora: String,
    var direccion: String? = null,
    var fotoRuta: String? = null,
    var esActual: Boolean = false,
    var fechaHoraSalida: String? = null
)
