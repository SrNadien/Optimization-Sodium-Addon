package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ChlorideBridge {
    private static final String CONFIG = "me.srrapero720.chloride.ChlorideConfig";
    private static final String[] PARTICLE_FLAGS = {"rain", "rainDrops", "blockCracking", "blockDestroyed"};

    private ChlorideBridge() {}

    public static boolean available() {
        return ModList.get().isLoaded("chloride");
    }

    public static boolean setFlag(String section, String field, boolean value) {
        try {
            Object target = section(section);
            target.getClass().getField(field).setBoolean(target, value);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo cambiar {}.{} en Chloride: {}", section, field, e.toString());
            return false;
        }
    }

    public static Boolean getFlag(String section, String field) {
        try {
            Object target = section(section);
            return target.getClass().getField(field).getBoolean(target);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static boolean disabledParticlesEmpty() {
        try {
            return disabled().isEmpty();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    public static boolean addAllParticles() {
        try {
            List<ResourceLocation> disabled = disabled();
            disabled.removeAll(allNamespaces());
            boolean added = false;
            for (ResourceLocation id : allParticleIds()) {
                if (!disabled.contains(id)) {
                    disabled.add(id);
                    added = true;
                }
            }
            return added;
        } catch (ReflectiveOperationException | RuntimeException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo ampliar la lista de particulas de Chloride: {}", e.toString());
            return false;
        }
    }

    public static boolean allParticlesDisabled() {
        try {
            return disabled().containsAll(allParticleIds());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    public static void setAllParticles(boolean disable) {
        try {
            List<ResourceLocation> disabled = disabled();
            disabled.removeAll(allNamespaces());
            if (disable) {
                for (ResourceLocation id : allParticleIds()) {
                    if (!disabled.contains(id)) disabled.add(id);
                }
            } else {
                disabled.clear();
            }
            for (String flag : PARTICLE_FLAGS) setFlag("particles", flag, !disable);
            write();
        } catch (ReflectiveOperationException | RuntimeException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudieron cambiar las particulas de Chloride: {}", e.toString());
        }
    }

    public static void write() {
        try {
            Class.forName(CONFIG, true, ChlorideBridge.class.getClassLoader()).getMethod("write").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo guardar la config de Chloride: {}", e.toString());
        }
    }

    private static Set<ResourceLocation> allParticleIds() {
        return new LinkedHashSet<>(BuiltInRegistries.PARTICLE_TYPE.keySet());
    }

    private static Set<ResourceLocation> allNamespaces() {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (ResourceLocation key : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
            ids.add(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "all"));
        }
        return ids;
    }

    private static Object section(String name) throws ReflectiveOperationException {
        return Class.forName(CONFIG, true, ChlorideBridge.class.getClassLoader()).getField(name).get(null);
    }

    @SuppressWarnings("unchecked")
    private static List<ResourceLocation> disabled() throws ReflectiveOperationException {
        Object particles = section("particles");
        return (List<ResourceLocation>) particles.getClass().getField("disabled").get(particles);
    }
}
