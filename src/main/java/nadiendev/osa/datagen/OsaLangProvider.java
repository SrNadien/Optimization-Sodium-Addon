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
        add("optimizersodiumaddon.option.armor_max", "Armor stands drawn", "Soportes de armadura dibujados");
        add("optimizersodiumaddon.option.armor_max.tooltip", "Maximum number of armor stands drawn at once (the closest ones). 0 = no limit.", "Máximo de soportes de armadura dibujados a la vez (los más cercanos). 0 = sin límite.");
        add("optimizersodiumaddon.option.armor_distance", "Armor stand distance", "Distancia de soportes de armadura");
        add("optimizersodiumaddon.option.armor_distance.tooltip", "Maximum distance in blocks to draw armor stands.", "Distancia máxima en bloques para dibujar soportes de armadura.");
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
