# Optimizer Sodium Addon (Fabric 26.1.2)

Fabric Loader 0.20.1 · Fabric API 0.155.3+26.1.2 · Sodium 0.9.2 (API de configuracion, opcional) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en cliente y servidor. No agrega bloques, items ni paquetes de red.

## Compilar

```
JAVA_HOME="C:/Program Files/Java/jdk-25.0.2" ./gradlew build
```

El jar queda en `build/libs/`. Fabric Loom 1.19.3 (Minecraft sin ofuscar) con Gradle 9.8.1 y JDK 25.

## Configuracion

Un solo archivo: `config/optimizersodiumaddon.json`, con las secciones `server`, `common` y `client` (mismos nombres y valores por defecto que la version NeoForge). El webhook de Discord va en `common.discord` y nunca se manda a los jugadores. Se lee al arrancar; `/osa reload` lo vuelve a leer. Los hilos del servidor dedicado siguen en `config/optimizersodiumaddon-threads.properties`.

Las opciones de cliente tambien se editan desde la pagina "Optimizer Sodium Addon" dentro de las opciones de video de Sodium.

## Diferencias con la version 1.21.1

- El guardado de `SavedData` ya es asincrono en el juego base, asi que solo se guarda `level.dat` en otro hilo (`performance.asyncSave`).
- La fuga de memoria de GPU se corrige cerrando las texturas y vistas de los `RenderTarget` descartados.
- Las opciones de niebla modifican los datos de niebla (la de Ceguera y Oscuridad no se toca).
- Los hilos de fondo del servidor dedicado se cambian en `Util.maxAllowedExecutorThreads`.

El resto de funciones, opciones y comandos `/osa` son los mismos que en 1.21.1.
