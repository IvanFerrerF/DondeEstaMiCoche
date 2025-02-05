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
        private const val DATABASE_VERSION = 2
        private const val TABLE_UBICACIONES = "ubicaciones"

        // Columnas
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATITUD = "latitud"
        private const val COLUMN_LONGITUD = "longitud"
        private const val COLUMN_DIRECCION = "direccion"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
        private const val COLUMN_FOTO_RUTA = "fotoRuta"
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
                sincronizado INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_UBICACIONES ADD COLUMN sincronizado INTEGER DEFAULT 0")
        }
    }

    // Método para insertar una nueva ubicación
    fun insertarUbicacion(ubicacion: UbicacionCoche): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LATITUD, ubicacion.latitud)
            put(COLUMN_LONGITUD, ubicacion.longitud)
            put(COLUMN_DIRECCION, ubicacion.direccion)
            put(COLUMN_FECHA, ubicacion.fecha)
            put(COLUMN_HORA, ubicacion.hora)
            put(COLUMN_FOTO_RUTA, ubicacion.fotoRuta)
        }
        return db.insert(TABLE_UBICACIONES, null, values)
    }

    // Método para actualizar la foto de una ubicación existente
    fun actualizarFoto(id: Long, fotoRuta: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FOTO_RUTA, fotoRuta)
        }
        return db.update(TABLE_UBICACIONES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Método para obtener todas las ubicaciones guardadas
    fun obtenerUbicaciones(): List<UbicacionCoche> {
        val listaUbicaciones = mutableListOf<UbicacionCoche>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_UBICACIONES", null)

        while (cursor.moveToNext()) {
            val ubicacion = UbicacionCoche(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                latitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD)),
                longitud = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD)),
                direccion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)),
                hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)),
                fotoRuta = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO_RUTA))
            )
            listaUbicaciones.add(ubicacion)
        }
        cursor.close()
        return listaUbicaciones
    }

    fun borrarHistorial() {
        val db = writableDatabase
        val filasEliminadas = db.delete(TABLE_UBICACIONES, null, null)
        db.close()

    }

    fun borrarUbicacion(id: Long): Int {
        val db = writableDatabase
        val filasEliminadas = db.delete(TABLE_UBICACIONES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()

        return filasEliminadas
    }

    fun actualizarSincronizado(id: Long, sinc: Boolean): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("sincronizado", if (sinc) 1 else 0)
        }
        return db.update(TABLE_UBICACIONES, cv, "id=?", arrayOf(id.toString()))
    }

    fun obtenerUbicacionesPendientes(): List<UbicacionCoche> {
        val lista = mutableListOf<UbicacionCoche>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_UBICACIONES WHERE sincronizado=0", null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD))
            val dir = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA))
            val hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA))
            val foto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO_RUTA))
            val sinc = cursor.getInt(cursor.getColumnIndexOrThrow("sincronizado")) == 1

            val ubic = UbicacionCoche(
                id = id,
                latitud = lat,
                longitud = lon,
                direccion = dir,
                fecha = fecha,
                hora = hora,
                fotoRuta = foto,
                sincronizado = sinc
            )
            lista.add(ubic)
        }
        cursor.close()
        return lista
    }


}
