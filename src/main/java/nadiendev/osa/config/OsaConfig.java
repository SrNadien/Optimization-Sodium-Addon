package nadiendev.osa.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.client.DynamicLightSettings;
import nadiendev.osa.client.FastMath;
import nadiendev.osa.client.TuneProfile;
import nadiendev.osa.loader.Platform;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class OsaConfig {
    private static final String FILE = "optimizersodiumaddon.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final Server SERVER = new Server();
    public static final Common COMMON = new Common();
    public static final Client CLIENT = new Client();

    private static volatile boolean loaded;
    private static volatile Set<String> aiWhitelistExact;
    private static volatile Set<String> aiWhitelistNamespaces;

    private OsaConfig() {}

    public static boolean serverReady() {
        return loaded;
    }

    public static boolean clientReady() {
        return loaded;
    }

    public static boolean commonReady() {
        return loaded;
    }

    public static boolean asyncSaveEnabled() {
        return commonReady() && COMMON.performance.asyncSave;
    }

    public static boolean craftingRecipeHintEnabled() {
        return !commonReady() || COMMON.performance.craftingRecipeHint;
    }

    public static boolean ignoreMoveSpeedLimit() {
        return commonReady() && COMMON.performance.ignoreMoveSpeedLimit;
    }

    public static boolean gpuLeakFixEnabled() {
        return !clientReady() || CLIENT.gpu.leakFix;
    }

    public static boolean fastMathConfigured() {
        return clientReady() && CLIENT.math.fastMath;
    }

    public static boolean hideReloadOverlay() {
        return clientReady() && CLIENT.reload.hideOverlay;
    }

    public static boolean betterGrassEnabled() {
        return clientReady() && CLIENT.terrain.betterGrass;
    }

    public static List<Identifier> betterGrassBlocks() {
        List<String> names = clientReady()
                ? CLIENT.terrain.betterGrassBlocks
                : List.of("minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium");
        return names.stream().map(Identifier::tryParse).filter(Objects::nonNull).toList();
    }

    public static boolean skipBrokenBlocksEnabled() {
        return clientReady() && CLIENT.stability.skipBrokenBlocks;
    }

    public static boolean coalesceTintsEnabled() {
        return clientReady() && CLIENT.stability.coalesceTints;
    }

    public static int tintIntervalTicks() {
        return clientReady() ? CLIENT.stability.tintIntervalTicks : 1;
    }

    public static boolean reloadCounterEnabled() {
        return clientReady() && CLIENT.reload.counter;
    }

    public static boolean reloadWatched() {
        return clientReady() && (CLIENT.reload.profile || !CLIENT.reload.skipListeners.isEmpty());
    }

    public static Path file() {
        return Platform.configDir().resolve(FILE);
    }

    public static synchronized void load() {
        Path path = file();
        Root root = new Root();
        boolean readable = true;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Root parsed = GSON.fromJson(reader, Root.class);
                if (parsed != null) root = parsed;
            } catch (Exception e) {
                readable = false;
                root = new Root();
                OptimizerSodiumAddon.LOG.error("[OSA] No se pudo leer {}; se usan los valores por defecto y el archivo no se toca", path, e);
            }
        }
        root.fillMissing();
        root.server.sanitize();
        root.common.sanitize();
        root.client.sanitize();
        SERVER.copyFrom(root.server);
        COMMON.copyFrom(root.common);
        CLIENT.copyFrom(root.client);
        invalidateCaches();
        loaded = true;
        if (readable) save();
    }

    public static synchronized void save() {
        Root root = new Root();
        root.server = SERVER;
        root.common = COMMON;
        root.client = CLIENT;
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            OptimizerSodiumAddon.LOG.error("[OSA] No se pudo guardar {}", path, e);
        }
        invalidateCaches();
    }

    private static void invalidateCaches() {
        aiWhitelistExact = null;
        aiWhitelistNamespaces = null;
        FastMath.setEnabled(fastMathConfigured());
        DynamicLightSettings.refresh();
    }

    public static boolean isAiWhitelisted(Identifier id) {
        if (id == null) return false;
        Set<String> exact = aiWhitelistExact;
        Set<String> ns = aiWhitelistNamespaces;
        if (exact == null || ns == null) {
            exact = new HashSet<>();
            ns = new HashSet<>();
            for (String raw : SERVER.ai.whitelist) {
                if (raw == null) continue;
                String s = raw.trim();
                if (s.endsWith(":*")) ns.add(s.substring(0, s.length() - 2));
                else if (!s.isEmpty()) exact.add(s);
            }
            aiWhitelistExact = exact;
            aiWhitelistNamespaces = ns;
        }
        return exact.contains(id.toString()) || ns.contains(id.getNamespace());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    static final class Root {
        Server server = new Server();
        Common common = new Common();
        Client client = new Client();

        void fillMissing() {
            if (server == null) server = new Server();
            if (common == null) common = new Common();
            if (client == null) client = new Client();
            server.fillMissing();
            common.fillMissing();
            client.fillMissing();
        }
    }

    public static final class Server {
        public ArmorStands armorStands = new ArmorStands();
        public Items items = new Items();
        public Xp xp = new Xp();
        public Ai ai = new Ai();
        public Alerts alerts = new Alerts();

        public static final class ArmorStands {
            public boolean disablePush = true;
            public boolean throttleWhenFar = true;
        }

        public static final class Items {
            public double mergeRadius = 1.5;
            public int lifespanTicks = 3600;
        }

        public static final class Xp {
            public boolean merge = true;
            public double mergeRadius = 2.0;
        }

        public static final class Ai {
            public boolean throttleFarMobs = true;
            public int activationRange = 48;
            public int tickInterval = 4;
            public List<String> whitelist = new ArrayList<>(List.of("minecraft:wither", "minecraft:ender_dragon", "minecraft:villager"));
        }

        public static final class Alerts {
            public int msptThreshold = 50;
            public int sustainSeconds = 10;
            public int cooldownSeconds = 300;
            public int chunkEntityThreshold = 300;
        }

        void fillMissing() {
            if (armorStands == null) armorStands = new ArmorStands();
            if (items == null) items = new Items();
            if (xp == null) xp = new Xp();
            if (ai == null) ai = new Ai();
            if (ai.whitelist == null) ai.whitelist = new ArrayList<>();
            if (alerts == null) alerts = new Alerts();
        }

        void sanitize() {
            items.mergeRadius = clamp(items.mergeRadius, 0.0, 8.0);
            items.lifespanTicks = clamp(items.lifespanTicks, 200, 6000);
            xp.mergeRadius = clamp(xp.mergeRadius, 0.5, 8.0);
            ai.activationRange = clamp(ai.activationRange, 16, 256);
            ai.tickInterval = clamp(ai.tickInterval, 2, 20);
            alerts.msptThreshold = clamp(alerts.msptThreshold, 20, 1000);
            alerts.sustainSeconds = clamp(alerts.sustainSeconds, 1, 600);
            alerts.cooldownSeconds = clamp(alerts.cooldownSeconds, 30, 86400);
            alerts.chunkEntityThreshold = clamp(alerts.chunkEntityThreshold, 0, 100000);
        }

        void copyFrom(Server other) {
            armorStands = other.armorStands;
            items = other.items;
            xp = other.xp;
            ai = other.ai;
            alerts = other.alerts;
        }
    }

    public static final class Common {
        public Discord discord = new Discord();
        public Performance performance = new Performance();

        public static final class Discord {
            public boolean enabled = false;
            public String webhookUrl = "";
            public String serverName = "Endless World";
        }

        public static final class Performance {
            public boolean asyncSave = true;
            public boolean craftingRecipeHint = true;
            public boolean ignoreMoveSpeedLimit = false;
        }

        void fillMissing() {
            if (discord == null) discord = new Discord();
            if (discord.webhookUrl == null) discord.webhookUrl = "";
            if (discord.serverName == null) discord.serverName = "Endless World";
            if (performance == null) performance = new Performance();
        }

        void sanitize() {
        }

        void copyFrom(Common other) {
            discord = other.discord;
            performance = other.performance;
        }
    }

    public static final class Client {
        public ArmorStands armorStands = new ArmorStands();
        public Items items = new Items();
        public Xp xp = new Xp();
        public Particles particles = new Particles();
        public Gpu gpu = new Gpu();
        public Window window = new Window();
        public Fog fog = new Fog();
        public Overlays overlays = new Overlays();
        public Reload reload = new Reload();
        public Terrain terrain = new Terrain();
        public Stability stability = new Stability();
        public AutoTune autoTune = new AutoTune();
        public DynamicLights dynamicLights = new DynamicLights();
        public MathOptions math = new MathOptions();

        public static final class Reload {
            public boolean profile = true;
            public boolean hideOverlay = true;
            public boolean counter = true;
            public int reportSeconds = 10;
            public int topCount = 8;
            public List<String> skipListeners = new ArrayList<>();
        }

        public static final class Terrain {
            public boolean betterGrass = false;
            public List<String> betterGrassBlocks = new ArrayList<>(List.of("minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium"));
        }

        public static final class Stability {
            public boolean skipBrokenBlocks = true;
            public boolean coalesceTints = true;
            public int tintIntervalTicks = 4;
        }

        public static final class AutoTune {
            public boolean enabled = true;
            public TuneProfile profile = TuneProfile.AUTO;
            public boolean applied = false;
            public boolean graphics = true;
            public boolean sodium = true;
            public boolean addon = true;
            public int renderDistanceLow = 2;
            public int renderDistanceMedium = 10;
            public int renderDistanceHigh = 16;
        }

        public static final class DynamicLights {
            public boolean enabled = false;
            public int maxSources = 24;
            public int maxDistance = 32;
            public int updateTicks = 2;
            public boolean burningEntities = true;
            public List<String> items = new ArrayList<>(List.of("minecraft:lava_bucket=15", "minecraft:blaze_rod=10"));
            public List<String> entities = new ArrayList<>(List.of("minecraft:blaze=10", "minecraft:fireball=12", "minecraft:small_fireball=12", "minecraft:magma_cube=8"));
        }

        public static final class MathOptions {
            public boolean fastMath = false;
        }

        public static final class ArmorStands {
            public int maxRendered = 32;
            public int renderDistance = 24;
            public boolean hideInvisibleEmpty = true;
        }

        public static final class Items {
            public int maxRendered = 150;
            public int renderDistance = 32;
        }

        public static final class Xp {
            public int maxRendered = 60;
            public int renderDistance = 24;
        }

        public static final class Particles {
            public int maxTotal = 3000;
            public int maxPerTick = 400;
            public int maxDistance = 48;
        }

        public static final class Gpu {
            public boolean leakFix = true;
        }

        public static final class Window {
            public boolean borderlessF11 = true;
        }

        public static final class Fog {
            public boolean terrain = true;
            public boolean water = true;
            public boolean lava = true;
            public boolean powderSnow = true;
        }

        public static final class Overlays {
            public boolean water = true;
            public boolean fire = true;
        }

        void fillMissing() {
            if (armorStands == null) armorStands = new ArmorStands();
            if (items == null) items = new Items();
            if (xp == null) xp = new Xp();
            if (particles == null) particles = new Particles();
            if (gpu == null) gpu = new Gpu();
            if (window == null) window = new Window();
            if (fog == null) fog = new Fog();
            if (overlays == null) overlays = new Overlays();
            if (reload == null) reload = new Reload();
            if (reload.skipListeners == null) reload.skipListeners = new ArrayList<>();
            if (terrain == null) terrain = new Terrain();
            if (terrain.betterGrassBlocks == null) terrain.betterGrassBlocks = new ArrayList<>();
            if (stability == null) stability = new Stability();
            if (autoTune == null) autoTune = new AutoTune();
            if (autoTune.profile == null) autoTune.profile = TuneProfile.AUTO;
            if (dynamicLights == null) dynamicLights = new DynamicLights();
            if (dynamicLights.items == null) dynamicLights.items = new ArrayList<>();
            if (dynamicLights.entities == null) dynamicLights.entities = new ArrayList<>();
            if (math == null) math = new MathOptions();
        }

        void sanitize() {
            reload.reportSeconds = clamp(reload.reportSeconds, 0, 3600);
            reload.topCount = clamp(reload.topCount, 1, 30);
            stability.tintIntervalTicks = clamp(stability.tintIntervalTicks, 1, 40);
            autoTune.renderDistanceLow = clamp(autoTune.renderDistanceLow, 2, 32);
            autoTune.renderDistanceMedium = clamp(autoTune.renderDistanceMedium, 2, 32);
            autoTune.renderDistanceHigh = clamp(autoTune.renderDistanceHigh, 2, 32);
            dynamicLights.maxSources = clamp(dynamicLights.maxSources, 1, 256);
            dynamicLights.maxDistance = clamp(dynamicLights.maxDistance, 8, 128);
            dynamicLights.updateTicks = clamp(dynamicLights.updateTicks, 1, 20);
            armorStands.maxRendered = clamp(armorStands.maxRendered, 0, 4096);
            armorStands.renderDistance = clamp(armorStands.renderDistance, 4, 256);
            items.maxRendered = clamp(items.maxRendered, 0, 4096);
            items.renderDistance = clamp(items.renderDistance, 4, 256);
            xp.maxRendered = clamp(xp.maxRendered, 0, 4096);
            xp.renderDistance = clamp(xp.renderDistance, 4, 256);
            particles.maxTotal = clamp(particles.maxTotal, 0, 65536);
            particles.maxPerTick = clamp(particles.maxPerTick, 0, 65536);
            particles.maxDistance = clamp(particles.maxDistance, 8, 256);
        }

        void copyFrom(Client other) {
            armorStands = other.armorStands;
            items = other.items;
            xp = other.xp;
            particles = other.particles;
            gpu = other.gpu;
            window = other.window;
            fog = other.fog;
            overlays = other.overlays;
            reload = other.reload;
            terrain = other.terrain;
            stability = other.stability;
            autoTune = other.autoTune;
            dynamicLights = other.dynamicLights;
            math = other.math;
        }
    }
}
