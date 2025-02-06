package edu.ivanferrerfranco.dondeestamicoche.data

/**
 * Representa la información relacionada con la ubicación del coche.
 * Esta clase modela cada punto de aparcamiento registrado en la aplicación.
 *
 * @property id Identificador único de la ubicación, generado automáticamente por SQLite.
 *              Es nulo cuando se crea una nueva instancia y se asigna al insertarla en la base de datos.
 * @property latitud Coordenada de latitud de la ubicación del coche. Valores válidos entre -90 y 90.
 * @property longitud Coordenada de longitud de la ubicación del coche. Valores válidos entre -180 y 180.
 * @property fecha Fecha en la que se registró la ubicación, almacenada en formato `dd/MM/yyyy`.
 * @property hora Hora en la que se registró la ubicación, almacenada en formato `HH:mm`.
 * @property direccion Dirección asociada a la ubicación (puede ser nula si no está disponible).
 *                     Se obtiene a partir de las coordenadas mediante geocodificación inversa.
 * @property fotoRuta Ruta del archivo de la foto asociada a la ubicación (puede ser nula si no se tomó ninguna foto).
 *                    Se almacena la ruta absoluta del archivo en el almacenamiento interno del dispositivo.
 * @property esActual Indica si esta ubicación es la más reciente en la que se aparcó el coche.
 *                    Solo una ubicación puede estar marcada como `true` a la vez.
 * @property fechaHoraSalida Fecha y hora en la que se registró la salida de la ubicación, si aplica.
 *                           Se usa para calcular el tiempo de estacionamiento y generar alertas.
 * @property sincronizado Indica si esta ubicación ha sido sincronizada con un servidor o respaldo en la nube.
 *                        Si es `false`, significa que aún no ha sido subida o hay cambios pendientes.
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
    var fechaHoraSalida: String? = null,
    var sincronizado: Boolean = false
)
