package edu.ivanferrerfranco.dondeestamicoche.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

/**
 * Clase genérica para manejar archivos de almacenamiento utilizando JSON.
 *
 * Esta clase implementa la interfaz [DataHandler] para realizar operaciones de lectura, escritura,
 * limpieza, adición y eliminación de datos almacenados en un archivo JSON.
 *
 * @param T El tipo de dato que manejará esta clase.
 * @param context El contexto de la aplicación, necesario para acceder al directorio de archivos.
 * @param fileName El nombre del archivo en el que se almacenarán los datos.
 * @param type La clase del tipo de dato [T] para deserializar correctamente los datos.
 */
class FileHandler<T>(
    private val context: Context,
    private val fileName: String,
    private val type: Class<T>
) : DataHandler<T> {

    private val gson = Gson()

    /**
     * Lee los datos del archivo JSON.
     *
     * @return Una lista de objetos de tipo [T] si se pudo leer el archivo, o una lista vacía si no existen datos o ocurrió un error.
     */
    override fun read(): List<T>? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val json = file.readText()
                val listType = TypeToken.getParameterized(List::class.java, type).type
                gson.fromJson(json, listType) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Limpia el contenido del archivo JSON.
     *
     * @return `true` si se limpió el archivo correctamente, o `false` si ocurrió un error o el archivo no existe.
     */
    override fun clean(): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.writeText("")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Escribe una lista de datos en el archivo JSON.
     *
     * @param data La lista de datos de tipo [T] que se desea almacenar.
     * @return `true` si los datos se escribieron correctamente, o `false` si ocurrió un error.
     */
    override fun write(data: List<T>): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            val json = gson.toJson(data)
            file.writeText(json)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Agrega un objeto al archivo JSON.
     *
     * @param objeto El objeto de tipo [T] que se desea agregar.
     * @return `true` si el objeto se agregó correctamente, o `false` si ocurrió un error.
     */
    override fun add(objeto: T): Boolean {
        val list = read() ?: emptyList()
        val newList = list + objeto
        return write(newList)
    }

    /**
     * Elimina un objeto del archivo JSON.
     *
     * @param objeto El objeto de tipo [T] que se desea eliminar.
     * @return `true` si el objeto se eliminó correctamente, o `false` si ocurrió un error.
     */
    override fun delete(objeto: T): Boolean {
        val list = read() ?: emptyList()
        val newList = list.filter { it != objeto }
        return write(newList)
    }
}
