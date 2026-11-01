package nadiendev.osa.datagen;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class OsaLangProvider extends LanguageProvider {
    private final boolean spanish;

    public OsaLangProvider(PackOutput output, String locale, boolean spanish) {
        super(output, OptimizerSodiumAddon.MODID, locale);
        this.spanish = spanish;
    }

    @Override
    protected void addTranslations() {
        add("optimizersodiumaddon.unlimited", "Unlimited", "Sin límite");
        add("optimizersodiumaddon.page.general", "General", "General");
        add("optimizersodiumaddon.page.visuals", "Fog and overlays", "Niebla y superposiciones");
        add("optimizersodiumaddon.page.render", "Render limits", "Límites de dibujado");
        add("optimizersodiumaddon.page.particles", "Particles", "Partículas");
        add("optimizersodiumaddon.option.borderless_f11", "Borderless F11", "F11 sin bordes");
        add("optimizersodiumaddon.option.borderless_f11.tooltip", "F11 switches between windowed and borderless fullscreen. If you are already in exclusive fullscreen, F11 goes back to a window as usual.", "F11 alterna entre ventana y pantalla completa sin bordes. Si ya estás en pantalla completa exclusiva, F11 vuelve a ventana como siempre.");
        add("optimizersodiumaddon.option.gpu_leak_fix", "GPU memory leak fix", "Arreglo de fuga de memoria de video");
        add("optimizersodiumaddon.option.gpu_leak_fix.tooltip", "Frees the video memory of render targets that the garbage collector discards without closing them.", "Libera la memoria de video de los RenderTarget que el recolector de basura descarta sin cerrarlos.");
        add("optimizersodiumaddon.option.compact_preview_fix", "Compact Machines preview fix", "Arreglo del preview de Compact Machines");
        add("optimizersodiumaddon.option.compact_preview_fix.tooltip", "Frees the memory of the Compact Machines 3D preview when it is closed.", "Libera la memoria del preview 3D de Compact Machines al cerrarlo.");
        add("optimizersodiumaddon.option.reload_hide_overlay", "Hide the loading screen on reload", "Ocultar la pantalla de carga al recargar");
        add("optimizersodiumaddon.option.reload_hide_overlay.tooltip", "Does not draw the Mojang logo screen while resource packs, language or F3+T reload. The game keeps showing what you were looking at. The loading screen when the game starts is not affected.", "No dibuja la pantalla del logo de Mojang mientras se recargan resource packs, idioma o F3+T. El juego sigue mostrando lo que estabas viendo. La pantalla de carga del arranque del juego no cambia.");
        add("optimizersodiumaddon.option.skip_broken_blocks", "Skip blocks that fail to render", "Omitir bloques que fallan al dibujarse");
        add("optimizersodiumaddon.option.skip_broken_blocks.tooltip", "If a block from a mod breaks while a chunk is being drawn, only that block is skipped and the error is written to the log, instead of crashing the game. The block may look missing until the mod is fixed.", "Si un bloque de un mod se rompe mientras se dibuja un chunk, se omite solo ese bloque y el error se escribe en el log, en vez de cerrar el juego. El bloque puede verse ausente hasta que se arregle el mod.");
        add("optimizersodiumaddon.option.coalesce_tints", "Group biome color refreshes", "Agrupar las limpiezas de colores de bioma");
        add("optimizersodiumaddon.option.coalesce_tints.tooltip", "Many mods clear the biome color cache every tick, which causes stutter. This turns them into a single refresh every few ticks. Colors can take a fraction of a second to update.", "Muchos mods limpian la cache de colores de bioma en cada tick, y eso provoca tirones. Con esto se agrupan en una sola limpieza cada pocos ticks. Los colores pueden tardar una fracción de segundo en actualizarse.");
        add("optimizersodiumaddon.option.better_grass", "Improved grass and snow", "Césped y nieve mejorados");
        add("optimizersodiumaddon.option.better_grass.tooltip", "When the block next to a grass, podzol or mycelium block, one level lower, is the same kind, the side uses the top texture so steps look continuous instead of showing a dirt edge. Snowy grass does the same with snow.", "Cuando el bloque de al lado de un césped, podzol o micelio, un nivel más abajo, es del mismo tipo, el lateral usa la textura de arriba y los escalones se ven continuos en vez de mostrar el borde de tierra. El césped nevado hace lo mismo con la nieve.");
        add("optimizersodiumaddon.option.entity_culling", "Entity culling", "Culling de entidades");
        add("optimizersodiumaddon.option.entity_culling.tooltip", "Stops drawing display entities (like server models) and armor stands that you cannot see: too far, outside the camera or behind blocks. It only skips drawing. They can appear a moment late when you turn. Off by default.", "Deja de dibujar las entidades Display (como los modelos de servidores) y los armor stands que no ves: muy lejos, fuera de la cámara o tapados por bloques. Solo evita dibujarlos. Pueden aparecer un instante tarde al girar. Apagado por defecto.");
        add("optimizersodiumaddon.option.entity_culling_occlusion", "Culling behind blocks", "Culling tras bloques");
        add("optimizersodiumaddon.option.entity_culling_occlusion.tooltip", "Skips entities hidden behind opaque blocks, using rays from the camera that are checked a few times per second.", "Omite las entidades tapadas por bloques opacos, con rayos desde la cámara que se comprueban unas pocas veces por segundo.");
        add("optimizersodiumaddon.option.entity_culling_distance", "Display entity distance", "Distancia de entidades Display");
        add("optimizersodiumaddon.option.entity_culling_distance.tooltip", "Maximum distance in blocks to draw display entities when entity culling is on. 0 = no limit.", "Distancia máxima en bloques para dibujar entidades Display con el culling activado. 0 = sin límite.");
        add("optimizersodiumaddon.option.aggressive_culling", "Aggressive culling", "Culling agresivo");
        add("optimizersodiumaddon.option.aggressive_culling.tooltip", "Turns on all of Sodium's culling at once: block faces that cannot be seen, chunks hidden by fog and entities inside hidden chunks. It gains FPS, but some resource packs may show holes in blocks. Turn it off if you see them.", "Activa de una vez todo el culling de Sodium: caras de bloques que no se ven, chunks tapados por la niebla y entidades dentro de chunks ocultos. Da más FPS, pero algunos resource packs pueden mostrar huecos en los bloques. Apágalo si los ves.");
        add("optimizersodiumaddon.option.exact_translucency", "Exact translucency sorting", "Orden exacto de transparencias");
        add("optimizersodiumaddon.option.exact_translucency.tooltip", "Draws glass, water and other translucent blocks in the correct order for the exact camera position, even where they intersect. It uses Sodium's own sorting with no limit on the extra geometry, so it costs some CPU in chunks with a lot of glass.", "Dibuja el vidrio, el agua y otros bloques translúcidos en el orden correcto para la posición exacta de la cámara, incluso donde se cruzan. Usa el orden propio de Sodium sin límite de geometría extra, así que cuesta algo de CPU en chunks con mucho vidrio.");
        add("optimizersodiumaddon.option.startup_timer", "Startup timer", "Contador de arranque");
        add("optimizersodiumaddon.option.startup_timer.tooltip", "Shows on the title screen, for a few seconds, how long the game took to start. The time is always written to the log.", "Muestra en la pantalla de título, durante unos segundos, cuánto tardó el juego en arrancar. El tiempo siempre se escribe en el log.");
        add("optimizersodiumaddon.startup.total", "Game started in %s", "Juego iniciado en %s");
        add("optimizersodiumaddon.startup.with_resources", "Game started in %s (resources: %s)", "Juego iniciado en %s (recursos: %s)");
        add("optimizersodiumaddon.option.reload_counter", "Reload timer", "Contador de recarga");
        add("optimizersodiumaddon.option.reload_counter.tooltip", "Shows in a corner of the screen how long a resource reload has been running and, when it ends, the total time for a few seconds. The /osareload command prints the last time in the chat; /osareload on writes the time in the chat after every reload.", "Muestra en una esquina de la pantalla cuánto lleva la recarga de recursos y, al terminar, el tiempo total durante unos segundos. El comando /osareload escribe en el chat el último tiempo; /osareload on lo escribe después de cada recarga.");
        add("optimizersodiumaddon.counter.running", "Reloading resources: %s", "Recargando recursos: %s");
        add("optimizersodiumaddon.counter.done", "Resources reloaded in %s", "Recursos recargados en %s");
        add("optimizersodiumaddon.option.reload_profile", "Resource reload report", "Informe de recarga de recursos");
        add("optimizersodiumaddon.option.reload_profile.tooltip", "Measures how long each reload listener takes and writes the slowest ones to the log and the chat, so you can see which mod slows down a resource pack reload.", "Mide cuánto tarda cada listener en una recarga y escribe los más lentos en el log y en el chat, para ver qué mod hace lenta la recarga de un resource pack.");
        add("optimizersodiumaddon.page.lights", "Dynamic lights", "Luces dinámicas");
        add("optimizersodiumaddon.option.dynamic_lights", "Dynamic lights", "Luces dinámicas");
        add("optimizersodiumaddon.option.dynamic_lights.tooltip", "Torches and other light sources light up their surroundings when you hold them, drop them or when a mob carries them, and burning entities glow. It costs a little performance because the light is recalculated when something moves.", "Las antorchas y otras fuentes de luz alumbran su alrededor cuando las llevás en la mano, las tirás o las lleva un mob, y las entidades en llamas brillan. Cuesta un poco de rendimiento porque la luz se recalcula cuando algo se mueve.");
        add("optimizersodiumaddon.option.dynamic_lights_max", "Maximum light sources", "Máximo de fuentes de luz");
        add("optimizersodiumaddon.option.dynamic_lights_max.tooltip", "How many dynamic light sources are active at once. The closest ones to the camera win.", "Cuántas fuentes de luz dinámicas hay activas a la vez. Ganan las más cercanas a la cámara.");
        add("optimizersodiumaddon.option.dynamic_lights_distance", "Light source distance", "Distancia de las fuentes de luz");
        add("optimizersodiumaddon.option.dynamic_lights_distance.tooltip", "Maximum distance in blocks from the camera for a source to light up.", "Distancia máxima en bloques a la cámara para que una fuente alumbre.");
        add("optimizersodiumaddon.option.dynamic_lights_burning", "Burning entities glow", "Las entidades en llamas brillan");
        add("optimizersodiumaddon.option.dynamic_lights_burning.tooltip", "Entities that are on fire emit light level 15.", "Las entidades que están en llamas emiten luz de nivel 15.");
        add("optimizersodiumaddon.option.auto_tune", "Automatic tuning", "Ajuste automático");
        add("optimizersodiumaddon.option.auto_tune.tooltip", "Off by default. Turning it on detects your hardware and configures your graphics settings with your profile. /osatune does the same on demand. Backups are saved next to each file as .osa-backup. If you set it to true in the config file before the first launch, it only fills in the settings you have not changed and never makes the game heavier.", "Apagado por defecto. Al encenderlo detecta tu hardware y configura tus opciones gráficas con tu perfil. /osatune hace lo mismo cuando quieras. Se guarda una copia de cada archivo como .osa-backup. Si lo pones en true en el archivo de config antes del primer arranque, solo completa lo que no cambiaste y nunca hace el juego más pesado.");
        add("optimizersodiumaddon.option.fast_math", "Fast math", "Matemáticas rápidas");
        add("optimizersodiumaddon.option.fast_math.tooltip", "Sine and cosine from a small interpolated table, only while drawing. It is more precise than vanilla, so nothing changes visually. The gain is very small.", "Seno y coseno con una tabla chica interpolada, solo al dibujar. Es más preciso que el de vanilla, así que no cambia nada visible. La ganancia es muy chica.");
        add("optimizersodiumaddon.option.chloride_all_particles", "Chloride: disable all particles", "Chloride: desactivar todas las partículas");
        add("optimizersodiumaddon.option.chloride_all_particles.tooltip", "Turns every particle off or on in Chloride at once: all the particles of every mod, plus rain, rain drops and block particles.", "Apaga o enciende de una vez todas las partículas en Chloride: las de todos los mods, además de lluvia, gotas de lluvia y partículas de bloques.");
        add("optimizersodiumaddon.option.fog_terrain", "Terrain fog", "Niebla del terreno");
        add("optimizersodiumaddon.option.fog_terrain.tooltip", "Distance haze, including the thick Nether fog. Blindness and Darkness fog is never touched.", "La neblina de distancia, incluida la espesa del Nether. La niebla de Ceguera y Oscuridad nunca se toca.");
        add("optimizersodiumaddon.option.fog_water", "Water fog", "Niebla bajo el agua");
        add("optimizersodiumaddon.option.fog_water.tooltip", "Fog while underwater.", "La niebla al estar bajo el agua.");
        add("optimizersodiumaddon.option.fog_lava", "Lava fog", "Niebla de lava");
        add("optimizersodiumaddon.option.fog_lava.tooltip", "Fog while inside lava.", "La niebla al estar dentro de lava.");
        add("optimizersodiumaddon.option.fog_powder_snow", "Powder snow fog", "Niebla de nieve en polvo");
        add("optimizersodiumaddon.option.fog_powder_snow.tooltip", "Fog while inside powder snow.", "La niebla al estar dentro de nieve en polvo.");
        add("optimizersodiumaddon.option.overlay_water", "Water overlay", "Superposición de agua");
        add("optimizersodiumaddon.option.overlay_water.tooltip", "The water texture drawn over your view while swimming.", "La textura de agua sobre la vista al nadar.");
        add("optimizersodiumaddon.option.overlay_fire", "Fire overlay", "Superposición de fuego");
        add("optimizersodiumaddon.option.overlay_fire.tooltip", "The flames drawn over your view while burning.", "Las llamas sobre la vista al arder.");
        add("optimizersodiumaddon.option.armor_hide_invisible", "Skip empty invisible armor stands", "Saltar soportes invisibles y vacíos");
        add("optimizersodiumaddon.option.armor_hide_invisible.tooltip", "Do not process invisible armor stands with nothing equipped; they look the same.", "No procesar soportes de armadura invisibles sin nada equipado; se ven igual.");
        add("optimizersodiumaddon.option.item_max", "Dropped items drawn", "Ítems del suelo dibujados");
        add("optimizersodiumaddon.option.item_max.tooltip", "Maximum number of dropped items drawn at once (the closest ones). 0 = no limit.", "Máximo de ítems del suelo dibujados a la vez (los más cercanos). 0 = sin límite.");
        add("optimizersodiumaddon.option.item_distance", "Dropped item distance", "Distancia de ítems del suelo");
        add("optimizersodiumaddon.option.item_distance.tooltip", "Maximum distance in blocks to draw dropped items.", "Distancia máxima en bloques para dibujar ítems del suelo.");
        add("optimizersodiumaddon.option.xp_max", "Experience orbs drawn", "Orbes de experiencia dibujados");
        add("optimizersodiumaddon.option.xp_max.tooltip", "Maximum number of experience orbs drawn at once (the closest ones). 0 = no limit.", "Máximo de orbes de experiencia dibujados a la vez (los más cercanos). 0 = sin límite.");
        add("optimizersodiumaddon.option.xp_distance", "Experience orb distance", "Distancia de orbes de experiencia");
        add("optimizersodiumaddon.option.xp_distance.tooltip", "Maximum distance in blocks to draw experience orbs.", "Distancia máxima en bloques para dibujar orbes de experiencia.");
        add("optimizersodiumaddon.option.particle_total", "Maximum live particles", "Máximo de partículas vivas");
        add("optimizersodiumaddon.option.particle_total.tooltip", "Maximum number of particles alive at once. 0 = no limit.", "Máximo de partículas vivas a la vez. 0 = sin límite.");
        add("optimizersodiumaddon.option.particle_per_tick", "New particles per tick", "Partículas nuevas por tick");
        add("optimizersodiumaddon.option.particle_per_tick.tooltip", "Maximum number of new particles per tick. 0 = no limit.", "Máximo de partículas nuevas por tick. 0 = sin límite.");
        add("optimizersodiumaddon.option.particle_distance", "Particle distance", "Distancia de partículas");
        add("optimizersodiumaddon.option.particle_distance.tooltip", "Particles farther than this many blocks are not created.", "No se crean partículas más lejos que estos bloques.");
    }

    private void add(String key, String english, String spanishText) {
        add(key, spanish ? spanishText : english);
    }
}
