## 1.0.1 - 2026-11-01

- Resource reloads: the Mojang loading screen is no longer drawn on reloads (resource packs, language, F3+T), a timer shows how long the reload takes, and a report in the log lists the slowest listeners. /osareload prints the last time in the chat.
- Automatic tuning: detects your hardware on first launch and fills in only the graphics settings you have not changed. /osatune applies a profile on demand.
- Dynamic lights: torches and other light sources light up around them in hand, dropped or carried by mobs. Off by default.
- Fast math: sine and cosine from a precise interpolated table while drawing. Off by default.
- Stability: a block from a mod that fails to render is skipped instead of crashing the game, and biome color cache refreshes are grouped to avoid stutter.
- Sodium shortcuts: aggressive culling and exact translucency sorting.
- Improved grass and snow: steps of grass, podzol and mycelium join visually. Off by default.

## 1.0.0 - 2026-10-31

First release for NeoForge 1.21.11.

- Armor stands: no push checks, and they tick less when no player is near.
- Far-away mobs tick one in four ticks, with a whitelist.
- Dropped items merge from a wider radius and despawn sooner.
- Nearby XP orbs merge into one.
- Asynchronous saving and background threads on dedicated servers.
- Client render limits for armor stands, items and XP orbs, plus particle caps.
- Discord lag alerts and the /osa admin commands.
- Options page inside Sodium video settings.
