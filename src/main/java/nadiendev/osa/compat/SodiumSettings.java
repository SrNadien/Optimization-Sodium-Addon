package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.client.Minecraft;

public final class SodiumSettings {
    private static final String MOD = "net.caffeinemc.mods.sodium.client.SodiumClientMod";
    private static final String OPTIONS = "net.caffeinemc.mods.sodium.client.gui.SodiumOptions";
    private static final String SPLITTING = "net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.QuadSplittingMode";

    private SodiumSettings() {}

    public static boolean aggressiveCulling() {
        return flag("performance", "useBlockFaceCulling") && flag("performance", "useFogOcclusion") && flag("performance", "useEntityCulling");
    }

    public static void setAggressiveCulling(boolean enabled) {
        put("performance", "useBlockFaceCulling", enabled);
        put("performance", "useFogOcclusion", enabled);
        put("performance", "useEntityCulling", enabled);
    }

    public static boolean exactTranslucency() {
        return flag("debug", "terrainSortingEnabled") && "UNLIMITED".equals(String.valueOf(read("performance", "quadSplittingMode")));
    }

    public static void setExactTranslucency(boolean enabled) {
        if (enabled) put("debug", "terrainSortingEnabled", true);
        try {
            Object mode = Class.forName(SPLITTING).getField(enabled ? "UNLIMITED" : "SAFE").get(null);
            put("performance", "quadSplittingMode", mode);
        } catch (ReflectiveOperationException | LinkageError error) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo cambiar el modo de transparencias de Sodium", error);
        }
    }

    public static void save() {
        try {
            Object options = options();
            if (options == null) return;
            Class.forName(OPTIONS).getMethod("writeToDisk", Class.forName(OPTIONS)).invoke(null, options);
        } catch (ReflectiveOperationException | LinkageError error) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo guardar la configuracion de Sodium", error);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.levelRenderer != null && minecraft.level != null) minecraft.levelRenderer.allChanged();
        });
    }

    private static Object options() {
        try {
            return Class.forName(MOD).getMethod("options").invoke(null);
        } catch (ReflectiveOperationException | LinkageError error) {
            return null;
        }
    }

    private static Object read(String group, String name) {
        try {
            Object options = options();
            if (options == null) return null;
            Object section = options.getClass().getField(group).get(options);
            return section.getClass().getField(name).get(section);
        } catch (ReflectiveOperationException error) {
            return null;
        }
    }

    private static boolean flag(String group, String name) {
        return Boolean.TRUE.equals(read(group, name));
    }

    private static void put(String group, String name, Object value) {
        try {
            Object options = options();
            if (options == null) return;
            Object section = options.getClass().getField(group).get(options);
            section.getClass().getField(name).set(section, value);
        } catch (ReflectiveOperationException error) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo cambiar {}.{} en Sodium", group, name, error);
        }
    }
}
