# Optimizer Sodium Addon (Fabric 1.20.1)

Fabric Loader 0.20.1 · Fabric API 0.92.12+1.20.1 · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en cliente y servidor. No agrega bloques, items ni paquetes de red.

## Compilar

```
JAVA_HOME="C:/Program Files/Java/jdk-21" ./gradlew build
```

El jar queda en `build/libs/`. Loom 1.17.21 con Gradle 9.8.1 y JDK 21 (compila a Java 17).

## Configuracion

Un solo archivo: `config/optimizersodiumaddon.json`, con las secciones `server`, `common` y `client` (mismos nombres y valores por defecto que la version NeoForge). El webhook de Discord va en `common.discord` y nunca se manda a los jugadores. Se lee al arrancar; `/osa reload` lo vuelve a leer. Los hilos del servidor dedicado siguen en `config/optimizersodiumaddon-threads.properties`.

Sodium 0.5.x para 1.20.1 no tiene API de configuracion, asi que no hay pagina dentro de Sodium: las opciones de cliente se editan en el JSON.

## Funciones y comandos

Iguales a la version NeoForge 1.21.1: armor stands, mobs lejanos, items, orbes de XP, guardado asincrono, hilos de fondo, alertas de Discord, limites de dibujado, particulas, arreglo de fuga de GPU, F11 sin bordes, niebla y superposiciones, cronometro de entrada y `/osa` (`stats`, `tps`, `top`, `kill`, `blockentities`, `alert test`, `memory`, mas `reload`).

Diferencias con 1.21.1: la pista de ultima receta prueba primero la receta anterior antes de recorrer el resto, y el guardado asincrono de level.dat se engancha en `saveDataTag`.
