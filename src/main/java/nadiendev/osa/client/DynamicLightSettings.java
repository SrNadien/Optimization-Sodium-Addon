package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DynamicLightSettings {
    private static volatile boolean enabled;
    private static volatile boolean burning = true;
    private static volatile int maxSources = 24;
    private static volatile int distance = 32;
    private static volatile int interval = 2;
    private static volatile Map<Identifier, Integer> items = Map.of();
    private static volatile Map<Identifier, Integer> entities = Map.of();

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

    public static Map<Identifier, Integer> items() {
        return items;
    }

    public static Map<Identifier, Integer> entities() {
        return entities;
    }

    public static void refresh() {
        if (!OsaConfig.clientReady()) {
            enabled = false;
            return;
        }
        OsaConfig.Client config = OsaConfig.CLIENT;
        burning = config.dynamicLights.burningEntities;
        maxSources = config.dynamicLights.maxSources;
        distance = config.dynamicLights.maxDistance;
        interval = config.dynamicLights.updateTicks;
        items = parse(config.dynamicLights.items);
        entities = parse(config.dynamicLights.entities);
        enabled = config.dynamicLights.enabled;
    }

    private static Map<Identifier, Integer> parse(List<String> lines) {
        Map<Identifier, Integer> result = new HashMap<>();
        for (String line : lines) {
            if (line == null) continue;
            int split = line.lastIndexOf('=');
            if (split <= 0) continue;
            Identifier id = Identifier.tryParse(line.substring(0, split).trim());
            if (id == null) continue;
            try {
                result.put(id, Math.max(0, Math.min(15, Integer.parseInt(line.substring(split + 1).trim()))));
            } catch (NumberFormatException ignored) {
            }
        }
        return Map.copyOf(result);
    }
}
