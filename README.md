# Optimizer Sodium Addon

NeoForge 26.2 (26.2.0.90) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en **cliente y servidor**. No agrega bloques, items ni paquetes de red.

## Compilar

```
gradlew build
```

Requiere JDK 25. El jar queda en `build/libs/`.

## Diferencias respecto a 1.21.1

- El guardado de SavedData ya es asincrono en el propio juego, asi que solo se mueve a otro hilo `level.dat`.
- El arreglo de fuga de GPU cierra las texturas y vistas de los `RenderTarget` descartados.
- La niebla y las superposiciones usan los eventos de 26.2 (`FogData`, tipo `ATMOSPHERIC` para el terreno).
- Los hilos de fondo del servidor dedicado se aplican sobre `Util.maxAllowedExecutorThreads`.
- Los permisos de `/osa` usan el sistema de permisos nuevo (gamemaster / admin / owner).
- La pagina de opciones de Sodium usa `sodium-neoforge-api` 0.9.2+mc26.2.

Funciones, comandos y opciones de config: igual que en la version 1.21.1.
