# Optimizer Sodium Addon

NeoForge 26.1.2 (26.1.2.112) · Sodium 0.9.2 (API de configuracion) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en **cliente y servidor**. No agrega bloques, items ni paquetes de red.

## Compilar

```
JAVA_HOME="C:/Program Files/Java/jdk-25.0.2" ./gradlew build
```
El jar queda en `build/libs/`.

## Diferencias con la version 1.21.1

- El guardado de `SavedData` ya es asincrono en el juego base, asi que solo se guarda `level.dat` en otro hilo (`performance.asyncSave`).
- La fuga de memoria de GPU se corrige cerrando las texturas y vistas de los `RenderTarget` descartados.
- Las opciones de niebla modifican los datos de niebla del evento de render (la de Ceguera y Oscuridad no se toca).
- Los hilos de fondo del servidor dedicado se cambian en `Util.maxAllowedExecutorThreads`.

El resto de funciones, opciones de configuracion y comandos `/osa` son los mismos que en 1.21.1.
