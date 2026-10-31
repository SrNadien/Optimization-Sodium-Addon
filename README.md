# Optimizer Sodium Addon (Fabric 1.21.11)

Fabric Loader 0.20.1 · Fabric API 0.141.6+1.21.11 · Sodium 0.8.14 (opcional) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en cliente y servidor. No agrega bloques, items ni paquetes de red.

## Compilar

```
JAVA_HOME="C:/Program Files/Java/jdk-21" ./gradlew build
```

El jar queda en `build/libs/`. Loom 1.17.21 con Gradle 9.8.1 y JDK 21.

## Configuracion

Un solo archivo: `config/optimizersodiumaddon.json`, con las secciones `server`, `common` y `client` (mismos nombres y valores por defecto que la version NeoForge). El webhook de Discord va en `common.discord` y nunca se manda a los jugadores. Se lee al arrancar; `/osa reload` lo vuelve a leer. Los hilos del servidor dedicado siguen en `config/optimizersodiumaddon-threads.properties`.

Las opciones de cliente tambien se editan desde la pagina "Optimizer Sodium Addon" dentro de las opciones de video de Sodium.

## Funciones y comandos

Iguales a la version NeoForge 1.21.1: armor stands, mobs lejanos, items, orbes de XP, guardado asincrono, hilos de fondo, alertas de Discord, limites de dibujado, particulas, arreglo de fuga de GPU, F11 sin bordes, niebla y superposiciones, cronometro de entrada y `/osa` (`stats`, `tps`, `top`, `kill`, `blockentities`, `alert test`, `memory`, mas `reload`).

Diferencias con 1.21.1: el guardado asincrono de SavedData no se parchea porque Minecraft 1.21.11 ya lo hace asincrono; `asyncSave` sigue aplicandose a level.dat. La niebla se controla sobre los datos de FogRenderer (entorno y distancia de dibujado), y la fuga de GPU libera las texturas de los RenderTarget descartados.
