package edu.ivanferrerfranco.dondeestamicoche.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.ivanferrerfranco.dondeestamicoche.data.UbicacionCoche

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "donde_estami_coche.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_UBICACIONES = "ubicaciones"

        // Columnas
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATITUD = "latitud"
        private const val COLUMN_LONGITUD = "longitud"
        private const val COLUMN_DIRECCION = "direccion"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
        private const val COLUMN_FOTO_RUTA = "fotoRuta"
        private const val COLUMN_SINCRONIZADO = "sincronizado"
        private const val COLUMN_ES_ACTUAL = "esActual"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_UBICACIONES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LATITUD REAL,
                $COLUMN_LONGITUD REAL,
                $COLUMN_DIRECCION TEXT,
                $COLUMN_FECHA TEXT,
                $COLUMN_HORA TEXT,
                $COLUMN_FOTO_RUTA TEXT,
                $COLUMN_SINCRONIZADO INTEGER DEFAULT 0,
                $COLUMN_ES_ACTUAL INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si la base de datos original era de versión 1, se añade 'sincronizado'
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_UBICACIONES ADD COLUMN $COLUMN_SINCRONIZADO INTEGER DEFAULT 0")
        }
        // Si la base de datos era de versión 2, se añade 'esActual'
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_UBICACIONES ADD COLUMN $COLUMN_ES_ACTUAL INTEGER DEFAULT 0")
        }
    }

    /**
     * Inserta una nueva ubicación en la base de datos.
     *
     * @param ubicacion Datos del coche que se desea almacenar (incluyendo si esActual es true o false).
     * @return El ID autogenerado de la fila insertada. Retorna -1 si ocurre algún error.
     */
    fun insertarUbicacion(ubicacion: UbicacionCoche): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LATITUD, ubicacion.latitud)
            put(COLUMN_LONGITUD, ubicacion.longitud)
            put(COLUMN_DIRECCION, ubicacion.direccion)
            put(COLUMN_FECHA, ubicacion.fecha)
            put(COLUMN_HORA, ubicacion.hora)
            put(COLUMN_FOTO_RUTA, ubicacion.fotoRuta)
            put(COLUMN_SINCRONIZADO, if (ubicacion.sincronizado) 1 else 0)
            put(COLUMN_ES_ACTUAL, if (ubicacion.esActual) 1 else 0)
        }
        val idInsertado = db.insert(TABLE_UBICACIONES, null, values)
        db.close()
        return idInsertado
    }

    /**
     * Actualiza la foto de una ubicación existente.
     *
     * @param id        ID de la ubicación que se quiere actualizar.
     * @param fotoRuta  Nueva ruta de la foto.
     * @return Número de filas afectadas (debería ser 1 si se actualiza correctamente).
     */
    fun actualizarFoto(id: Long, fotoRuta: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOTO_RUTA, fotoRuta)
        }
        val filas = db.update(
            TABLE_UBICACIONES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return filas
    }

    /**
     * Obtiene todas las ubicaciones guardadas en la base de datos.
     *
     * @return Lista de objetos [UbicacionCoche].
     */
    fun obtenerUbicaciones(): List<UbicacionCoche> {
        val listaUbicaciones = mutableListOf<UbicacionCoche>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_UBICACIONES", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD))
            val longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD))
            val direccion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
            val hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA))
            val fotoRuta = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO_RUTA))
            val sincronizado = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)) == 1)
            val esActual = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ES_ACTUAL)) == 1)

            val ubicacion = UbicacionCoche(
                id = id,
                latitud = latitud,
                longitud = longitud,
                direccion = direccion,
                fecha = fecha,
                hora = hora,
                fotoRuta = fotoRuta,
                sincronizado = sincronizado,
                esActual = esActual
            )
            listaUbicaciones.add(ubicacion)
        }
        cursor.close()
        db.close()

        return listaUbicaciones
    }

    /**
     * Borra todo el historial (todas las ubicaciones) de la tabla.
     */
    fun borrarHistorial() {
        val db = writableDatabase
        db.delete(TABLE_UBICACIONES, null, null)
        db.close()
    }

    /**
     * Borra una ubicación en particular por su ID.
     *
     * @return Número de filas afectadas (debería ser 1 si se borra correctamente).
     */
    fun borrarUbicacion(id: Long): Int {
        val db = writableDatabase
        val filasEliminadas = db.delete(TABLE_UBICACIONES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasEliminadas
    }

    /**
     * Actualiza el campo 'sincronizado' (true/false) para el registro con id = [id].
     *
     * @param id   Identificador de la fila.
     * @param sinc Valor booleano que se convertirá a 1 o 0 en la base de datos.
     */
    fun actualizarSincronizado(id: Long, sinc: Boolean): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_SINCRONIZADO, if (sinc) 1 else 0)
        }
        val filas = db.update(
            TABLE_UBICACIONES,
            cv,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return filas
    }

    /**
     * Obtiene las ubicaciones que no han sido sincronizadas (sincronizado = 0).
     *
     * @return Lista de [UbicacionCoche] pendientes de sincronizar.
     */
    fun obtenerUbicacionesPendientes(): List<UbicacionCoche> {
        val lista = mutableListOf<UbicacionCoche>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_UBICACIONES WHERE $COLUMN_SINCRONIZADO=0", null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD))
            val dir = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
            val hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA))
            val foto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO_RUTA))
            val sinc = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)) == 1)
            val esActual = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ES_ACTUAL)) == 1)

            val ubic = UbicacionCoche(
                id = id,
                latitud = lat,
                longitud = lon,
                direccion = dir,
                fecha = fecha,
                hora = hora,
                fotoRuta = foto,
                sincronizado = sinc,
                esActual = esActual
            )
            lista.add(ubic)
        }
        cursor.close()
        db.close()
        return lista
    }

    /**
     * Actualiza la columna 'esActual' para una ubicación concreta.
     *
     * @param id       ID de la ubicación a actualizar.
     * @param esActual Valor booleano que indica si la ubicación pasa a ser la 'actual' o no.
     */
    fun actualizarEstadoAparcamiento(id: Long, esActual: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ES_ACTUAL, if (esActual) 1 else 0)
        }
        db.update(TABLE_UBICACIONES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    /**
     * Marca la ubicación con [newId] como la única 'actual' (esActual = 1),
     * poniendo el resto de registros con esActual = 0.
     *
     * @param newId El ID de la ubicación que se quiere marcar en verde (actual).
     */
    fun marcarComoActual(newId: Long) {
        val db = writableDatabase

        // Primero poner esActual = 0 a todos
        val valuesAllFalse = ContentValues().apply {
            put(COLUMN_ES_ACTUAL, 0)
        }
        db.update(TABLE_UBICACIONES, valuesAllFalse, null, null)

        // Ahora poner esActual = 1 solo a la nueva
        val valuesOneTrue = ContentValues().apply {
            put(COLUMN_ES_ACTUAL, 1)
        }
        db.update(TABLE_UBICACIONES, valuesOneTrue, "$COLUMN_ID = ?", arrayOf(newId.toString()))

        db.close()
    }
}
