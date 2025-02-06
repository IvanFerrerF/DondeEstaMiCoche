# Proyecto: **¿Dónde está mi coche?** 

Aplicación móvil para Android diseñada para gestionar ubicaciones de estacionamiento, buscar aparcamientos cercanos, establecer recordatorios y mucho más.

---

## **Características Principales**

###  Funcionalidades implementadas:
1. **Gestión de ubicaciones del coche**:
   - Guardar la ubicación actual del coche con fecha, hora, lugar y coordenadas.
   - Adjuntar una foto del lugar de estacionamiento o imagen vacía si se opta por no guardar foto.
   - Visualizar la ubicación guardada en un mapa.
   - Opción para dejar de estar aparcado.
   - Compartir ubicación mediante correo electrónico.
   - Eliminar ubicación del coche mediante pulsación larga o botón.
   - Señalización mediante colores del estacionamiento activo.
   - **Guardado automático en SQLite y sincronización con Firebase** cuando la conexión esté disponible.

2. **Mapa interactivo**:
   - Integración con Google Maps API para mostrar:
     - La última ubicación guardada.
     - La ubicación actual del usuario.
     - Distancia entre el usuario y el coche.
   - Posibilidad de mostrar ubicaciones predefinidas (como la Plaza de los Luceros).

3. **Búsqueda de aparcamientos cercanos**:
   - Uso de Google Places API para buscar aparcamientos en un radio configurable.
   - Resultados mostrados en un mapa interactivo con marcadores.

4. **Menú de navegación**:
   - Interfaz con menú lateral (`DrawerLayout`) para acceder a todas las funciones de la aplicación de manera rápida y sencilla.

5. **IA de ayuda integrada**:
   - **Botón de ayuda con IA personalizada** que permite conversar con la IA de la aplicación (IvanA).
   - Explica las funcionalidades de la app y resuelve dudas del usuario sobre su uso.
   - Implementada con Cohere API para generar respuestas contextualizadas.

6. **Persistencia de datos con SQLite y Firebase**:
   - **SQLite**: Almacena todas las ubicaciones localmente para su acceso sin conexión.
   - **Firebase**: Sincroniza las ubicaciones guardadas automáticamente cuando el dispositivo recupera conexión a Internet.

7. **Sincronización automática**:
   - Si no hay conexión al guardar una ubicación, esta se almacena en SQLite como "pendiente de sincronización".
   - Cuando el dispositivo se conecta a Internet, los datos pendientes se suben automáticamente a Firebase.

8. **Alarmas**:
   - Configurar recordatorios para eventos relacionados con el coche.
   - Notificaciones para detener la alarma cuando sea necesario.

9. **Calendario**:
   - Integración con Calendar API para agregar eventos como "Revisar el coche".

10. **Lista de ubicaciones**:
   - Visualizar todas las ubicaciones guardadas en una lista interactiva.

11. **Pantalla de bienvenida (Splash Screen)**:
   - Animación de entrada con transición a la actividad principal después de 3 segundos.

12. **Compartir ubicación**:
   - Compartir la ubicación guardada a través de otras aplicaciones (WhatsApp, Email, etc.).

13. **Permisos**:
   - Gestión de historial:
     - Posibilidad de borrar todas las ubicaciones guardadas.
   - Configuración de radio de búsqueda:
     - Cambiar el radio en metros para buscar aparcamientos cercanos.
   - Gestión de permisos:
     - Acceso rápido a la configuración del sistema para gestionar permisos de ubicación y cámara.

14. **Gestión avanzada de permisos**:
   - Manejo de permisos para:
     - Acceso a la ubicación.
     - Uso de la cámara.
     - Escritura en almacenamiento.

---

## **Arquitectura del Proyecto**

El proyecto sigue una arquitectura modular que organiza las funcionalidades en actividades y utilidades.

### Actividades:
- **`MainActivity`**:
  - Punto central de la aplicación con acceso a todas las funcionalidades.
- **`MapaActivity`**:
  - Muestra las ubicaciones en un mapa interactivo con Google Maps API.
- **`SplashActivity`**:
  - Pantalla de bienvenida con animación.
- **`AjustesActivity`**:
  - Proporciona opciones para:
    - Borrar el historial.
    - Configurar el radio de búsqueda.
    - Gestionar permisos.
- **`LugaresActivity`**:
  - Visualiza una lista interactiva con las ubicaciones guardadas del coche.
- **`AparcamientosMapaActivity`**:
  - Muestra en el mapa los aparcamientos cercanos encontrados a partir de una búsqueda configurada por el usuario.
- **`AyudaIAActivity`**:
  - Implementa la IA de asistencia con la que los usuarios pueden conversar y aprender sobre las funciones de la app.

### Componentes principales:
- **Google Maps API**: Para la visualización de mapas y cálculos de distancia.
- **Google Places API**: Para buscar aparcamientos cercanos.
- **Cámara**: Para capturar imágenes del lugar de estacionamiento.
- **AlarmManager**: Para configurar recordatorios.
- **Calendar API**: Para agregar eventos directamente al calendario.
- **RecyclerView**: Para listar ubicaciones guardadas.
- **SQLite**: Para almacenar datos localmente.
- **Firebase**: Para la sincronización en la nube de ubicaciones guardadas.

---

### **Persistencia de datos con SQLite y Firebase**

- **SQLite**:
  - Se usa para almacenar todas las ubicaciones localmente.
  - Permite acceder a los datos sin conexión.
  - Si la app se cierra, las ubicaciones siguen almacenadas y accesibles.

- **Firebase**:
  - Se encarga de sincronizar los datos cuando hay conexión.
  - Si el usuario guarda una ubicación sin conexión, se almacena en SQLite como "pendiente".
  - Cuando el dispositivo recupera conexión, **los datos se suben automáticamente a Firebase**.
  - Esto garantiza que las ubicaciones estén disponibles desde cualquier dispositivo vinculado.

---

### Permisos:
- **Ubicación**:
  - Para acceder a la ubicación aproximada y precisa del dispositivo.
- **Cámara**:
  - Para capturar fotos del aparcamiento.
- **Internet**:
  - Para buscar aparcamientos cercanos y acceder a mapas interactivos.
- **Alarmas y notificaciones**:
  - Para programar alarmas exactas y mostrar notificaciones en dispositivos recientes.
- **Almacenamiento**:
  - Para leer y guardar imágenes en el dispositivo.

---

### Bibliotecas utilizadas:
- **Google Play Services**:
  - `com.google.android.gms:play-services-maps`
  - `com.google.android.gms:play-services-location`
- **OkHttp**: Para manejar solicitudes HTTP (en la búsqueda de aparcamientos).
- **Cohere API**: Implementación de IA de ayuda.
- **Firebase Realtime Database**: Para la sincronización de datos en la nube.
- **SQLite**: Base de datos local para almacenamiento offline.
- **Dokka**: Generación automática de documentación.

### Generación de documentación con Dokka:
La documentación del proyecto se genera utilizando **Dokka**. 
1. Para generarla, se ejecuta el comando:
   ```bash
   ./gradlew dokkaHtml
