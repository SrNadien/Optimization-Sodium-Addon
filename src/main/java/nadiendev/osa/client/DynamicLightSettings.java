package nadiendev.osa.client;

import nadiendev.osa.OsaConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DynamicLightSettings {
    private static volatile boolean enabled;
    private static volatile boolean burning = true;
    private static volatile int maxSources = 24;
    private static volatile int distance = 32;
    private static volatile int interval = 2;
    private static volatile Map<ResourceLocation, Integer> items = Map.of();
    private static volatile Map<ResourceLocation, Integer> entities = Map.of();

    private DynamicLightSettings() {}

    public static boolean enabled() {
        return enabled;
    }

    public static boolean burning() {
        return burning;
    }

    public static int maxSources() {
        return maxSources;
    }

    public static int distance() {
        return distance;
    }

    public static int interval() {
        return interval;
    }

    public static Map<ResourceLocation, Integer> items() {
        return items;
    }

    public static Map<ResourceLocation, Integer> entities() {
        return entities;
    }

    public static void refresh() {
        if (!OsaConfig.clientReady()) {
            enabled = false;
            return;
        }
        OsaConfig.Client config = OsaConfig.CLIENT;
        burning = config.dynamicLightsBurning.get();
        maxSources = config.dynamicLightsMaxSources.get();
        distance = config.dynamicLightsDistance.get();
        interval = config.dynamicLightsInterval.get();
        items = parse(config.dynamicLightsItems.get());
        entities = parse(config.dynamicLightsEntities.get());
        enabled = config.dynamicLights.get();
    }

    private static Map<ResourceLocation, Integer> parse(List<? extends String> lines) {
        Map<ResourceLocation, Integer> result = new HashMap<>();
        for (String line : lines) {
            int split = line.lastIndexOf('=');
            if (split <= 0) continue;
            ResourceLocation id = ResourceLocation.tryParse(line.substring(0, split).trim());
            if (id == null) continue;
            try {
                result.put(id, Math.max(0, Math.min(15, Integer.parseInt(line.substring(split + 1).trim()))));
            } catch (NumberFormatException ignored) {
            }
        }
        return Map.copyOf(result);
    }
}
