# Optimizer Sodium Addon

NeoForge 26.3 (26.3.0.76-beta) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en **cliente y servidor**. No agrega bloques, items ni paquetes de red.

## Compilar

```
gradlew build
```

Requiere JDK 25. El jar queda en `build/libs/`.

## Diferencias respecto a 1.21.1

- El guardado de SavedData ya es asincrono en el propio juego, asi que solo se mueve a otro hilo `level.dat`.
- El arreglo de fuga de GPU cierra las texturas y vistas de los `RenderTarget` descartados.
- La niebla y las superposiciones usan los eventos de 26.3 (`FogData`, tipo `ATMOSPHERIC` para el terreno, `ExtractBlockScreenEffectEvent`).
- Los hilos de fondo del servidor dedicado se aplican sobre `Util.maxAllowedExecutorThreads`.
- Los permisos de `/osa` usan el sistema de permisos nuevo (gamemaster / admin / owner).
- Los tipos de config cambiaron: lo que era SERVER ahora es `SYNCED` (`optimizersodiumaddon-synced.toml`) y lo que era COMMON ahora es `LOCAL` (`config/optimizersodiumaddon-local.toml`, aqui va el webhook de Discord).
- Se quita la opcion `borderlessF11`: el cliente de 26.3 ya alterna ventana y pantalla completa sin bordes por defecto.
- La pagina de opciones de Sodium usa `sodium-neoforge-api` 0.9.2+mc26.3.

Funciones, comandos y resto de opciones de config: igual que en la version 1.21.1.
