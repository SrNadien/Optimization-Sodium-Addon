## 1.0.3 - 2026-11-02

- Fixed a black screen while joining a server that sends a resource pack: with the loading screen hidden, the screen underneath is now drawn during the reload.
- Armor stands are no longer limited by amount, because many servers use them as models and only the entities near you are loaded anyway. They now follow the general entity culling, by distance and behind blocks.
- Entity culling (off by default): display entities, like the models some servers use, and armor stands are no longer drawn when they are too far, outside the camera or behind blocks. /osacull shows the counters.
- Automatic tuning is now off by default and never raises graphics settings on its own: only the low profile changes them, so it never makes the game heavier.
- Sodium settings: every option of this mod shows its performance impact, and searching #low, #medium, #high or #varies (or #bajo, #medio, #alto, #variable) lists the options with that impact from every mod.
- The title screen shows how long the game took to start, and the log records it.
- Spanish translations for the Sodium and Chloride settings that were missing.

## 1.0.1 - 2026-11-01

- Resource reloads: the Mojang loading screen is no longer drawn on reloads (resource packs, language, F3+T), a timer shows how long the reload takes, and a report in the log lists the slowest listeners. /osareload prints the last time in the chat.
- Automatic tuning: detects your hardware on first launch and fills in only the graphics settings you have not changed. /osatune applies a profile on demand.
- Dynamic lights: torches and other light sources light up around them in hand, dropped or carried by mobs. Off by default.
- Fast math: sine and cosine from a precise interpolated table while drawing. Off by default.
- Stability: a block from a mod that fails to render is skipped instead of crashing the game, and biome color cache refreshes are grouped to avoid stutter.
- Fixed a crash with firework particles when their flash particle is disabled.
- Sodium shortcuts: aggressive culling and exact translucency sorting.
- Improved grass and snow: steps of grass, podzol and mycelium join visually. Off by default.

## 1.0.0 - 2026-10-31

First release for NeoForge 26.2.

- Armor stands: no push checks, and they tick less when no player is near.
- Far-away mobs tick one in four ticks, with a whitelist.
- Dropped items merge from a wider radius and despawn sooner.
- Nearby XP orbs merge into one.
- Asynchronous saving and background threads on dedicated servers.
- Client render limits for armor stands, items and XP orbs, plus particle caps.
- Discord lag alerts and the /osa admin commands.
- Options page inside Sodium video settings.
