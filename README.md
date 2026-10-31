# Optimizer Sodium Addon

NeoForge 1.21.1 (21.1.264) · modid `optimizersodiumaddon` · paquete `nadiendev.osa` · autor NadienDev

Se instala en **cliente y servidor**. No agrega bloques, ítems ni paquetes de red, así que un cliente sin el mod puede entrar a un server que lo tiene, y al revés.

## Compilar

```
gradlew build
```
El jar queda en `build/libs/`. La primera vez Gradle baja NeoForge y Minecraft, así que tarda.

## Qué hace

### Servidor
| Función | Config |
|---|---|
| Armor stands: no buscan minecarts para empujar en cada tick | `armorStands.disablePush` |
| Armor stands lejos de jugadores tickean menos | `armorStands.throttleWhenFar` |
| Mobs lejos de jugadores tickean 1 de cada N ticks | `ai.*` |
| Items iguales se juntan desde más lejos | `items.mergeRadius` |
| Items tirados duran menos (3 min por defecto) | `items.lifespanTicks` |
| Orbes de XP se juntan aunque tengan distinto valor | `xp.*` |
| Alertas de lag a Discord por webhook | `alerts.*` |
| Hilos de fondo = todos los núcleos (solo server dedicado) | `config/optimizersodiumaddon-threads.properties` |
| Guardado de datos del mundo y level.dat en otro hilo | `performance.asyncSave` (common) |
| Crafteo: prueba primero la última receta usada | `performance.craftingRecipeHint` (common) |
| Quitar el límite de "Moved too quickly" (desactivado por defecto) | `performance.ignoreMoveSpeedLimit` (common) |

Config del server: `world/serverconfig/optimizersodiumaddon-server.toml`. El webhook de Discord va en `config/optimizersodiumaddon-common.toml`. En un server dedicado: `world/serverconfig/`. Para que se aplique a mundos nuevos, copialo a `defaultconfigs/`.

### Cliente
| Función | Config |
|---|---|
| Solo se dibujan los N armor stands más cercanos | `armorStands.*` |
| Límite de ítems y orbes dibujados | `items.*`, `xp.*` |
| Tope de partículas por tick, total y por distancia | `particles.*` |
| Libera la memoria del preview 3D de Compact Machines | `compat.compactMachinesPreviewFix` |
| Libera la memoria de video de los RenderTarget descartados | `gpu.leakFix` |
| F11 alterna ventana / pantalla completa sin bordes | `window.borderlessF11` |
| Quitar niebla de terreno, agua, lava y nieve en polvo | `fog.*` |
| Quitar la textura de agua y las llamas sobre la vista | `overlays.*` |
| Cronómetro de entrada al server: dice en el chat y en el log cuánto tardó cada etapa | siempre activo |

Config del cliente: `config/optimizersodiumaddon-client.toml`.

## Comandos (`/osa`, nivel OP 2+)

| Comando | Qué hace |
|---|---|
| `/osa stats` | Entidades, items, xp, armor stands, block entities y chunks por dimensión + RAM |
| `/osa tps` | MSPT y TPS |
| `/osa top [n]` | Los chunks más pesados de tu dimensión, con coordenadas |
| `/osa kill items\|xp\|armorstands\|hostile\|passive\|mobs [radio]` | Borra entidades (OP 3) |
| `/osa kill type <entidad> [radio]` | Borra un tipo concreto |
| `/osa blockentities list [radio]` | Block entities por tipo |
| `/osa blockentities remove <tipo> <radio> confirm` | **Borra esos bloques** (OP 4, radio máx. 128) |
| `/osa alert test` | Manda una alerta de prueba a Discord |
| `/osa memory` | RAM y GC; `gc` fuerza un GC (OP 3), `heapdump` vuelca el heap a `heap_dump/` (OP 4) |

`kill` nunca toca jugadores, mobs con nombre, mascotas, atados, montados ni jefes. Usa `discard()`: no suelta drops ni XP.

## Alertas de Discord

1. En Discord: Ajustes del canal → Integraciones → Webhooks → Nuevo webhook → Copiar URL.
2. En `config/optimizersodiumaddon-common.toml` (no se manda a los jugadores, así nadie puede ver la URL):
   ```toml
   [discord]
   enabled = true
   webhookUrl = "https://discord.com/api/webhooks/..."
   ```
3. `/reload` no recarga este archivo; NeoForge lo recarga solo al guardarlo. Probalo con `/osa alert test`.

Avisa cuando el MSPT pasa `msptThreshold` durante `sustainSeconds` seguidos, o cuando un chunk supera `chunkEntityThreshold` entidades. Manda los 5 chunks más pesados con coordenadas y el tipo de entidad o block entity más común.

## Compatibilidad con otros mods

- Con Clumps instalado, el addon deja de juntar orbes de XP en el servidor.
- Entity Culling, Spark y ModernFix no se duplican: el addon no hace nada de lo que ya hacen.
- Sodium y Chloride son solo dependencias de compilación opcionales; el addon funciona sin ellos.

## Los hilos solo existen en el server dedicado

`optimizersodiumaddon-threads.properties` solo se aplica en un servidor dedicado. En un cliente o en un mundo de un jugador no hace nada, a propósito. Para verlo: `gradlew runServer` y buscar en el log `hilos de fondo =`.
