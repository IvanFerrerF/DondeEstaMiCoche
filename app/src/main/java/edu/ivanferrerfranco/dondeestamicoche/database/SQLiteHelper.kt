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
        private const val DATABASE_VERSION = 1
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
                $COLUMN_FOTO_RUTA TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UBICACIONES")
        onCreate(db)
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


}
