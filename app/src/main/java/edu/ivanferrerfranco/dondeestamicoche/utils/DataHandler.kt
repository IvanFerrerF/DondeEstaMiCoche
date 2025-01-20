package edu.ivanferrerfranco.dondeestamicoche.utils

/**
 * Interfaz genérica para manejar operaciones de lectura y escritura de datos.
 *
 * Esta interfaz proporciona un conjunto de métodos para trabajar con datos genéricos
 * de tipo [T]. Es útil para abstraer operaciones comunes como lectura, escritura,
 * limpieza, adición y eliminación de elementos.
 *
 * @param T El tipo de dato que manejará la implementación de esta interfaz.
 */
interface DataHandler<T> {

    /**
     * Lee los datos almacenados.
     *
     * @return Una lista de objetos de tipo [T] si hay datos disponibles, o `null` si no hay datos.
     */
    fun read(): List<T>?

    /**
     * Escribe una lista de datos en el almacenamiento.
     *
     * @param data La lista de datos de tipo [T] que se desea almacenar.
     * @return `true` si los datos se escribieron correctamente, `false` en caso contrario.
     */
    fun write(data: List<T>): Boolean

    /**
     * Limpia todos los datos almacenados.
     *
     * @return `true` si los datos se limpiaron correctamente, `false` en caso contrario.
     */
    fun clean(): Boolean

    /**
     * Agrega un objeto al almacenamiento.
     *
     * @param objeto El objeto de tipo [T] que se desea agregar.
     * @return `true` si el objeto se agregó correctamente, `false` en caso contrario.
     */
    fun add(objeto: T): Boolean

    /**
     * Elimina un objeto del almacenamiento.
     *
     * @param objeto El objeto de tipo [T] que se desea eliminar.
     * @return `true` si el objeto se eliminó correctamente, `false` en caso contrario.
     */
    fun delete(objeto: T): Boolean
}
