package nadiendev.osa.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.compat.ChlorideBridge;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.loader.Platform;
import nadiendev.osa.mixin.client.OptionInstanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AutoTune {
    private static boolean checked;

    private AutoTune() {}

    public static void tick(Minecraft minecraft) {
        if (checked || !OsaConfig.clientReady() || minecraft.gui.overlay() != null) return;
        checked = true;
        OsaConfig.Client config = OsaConfig.CLIENT;
        if (!config.autoTune.enabled || config.autoTune.applied) return;
        String summary = apply(config.autoTune.profile, minecraft, false);
        minecraft.gui.hud.getChat().addClientSystemMessage(Component.literal("[OSA] " + summary));
    }

    public static void applyManually() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            String summary = apply(OsaConfig.CLIENT.autoTune.profile, minecraft, true);
            minecraft.gui.hud.getChat().addClientSystemMessage(Component.literal("[OSA] " + summary));
        });
    }

    public static String describeHardware() {
        HardwareProfile hardware = HardwareProfile.detect();
        return hardware.describe() + ". Perfil recomendado: " + label(hardware.tier()) + ".";
    }

    public static String apply(TuneProfile requested, Minecraft minecraft, boolean force) {
        OsaConfig.Client config = OsaConfig.CLIENT;
        HardwareProfile hardware = HardwareProfile.detect();
        TuneProfile tier = requested == TuneProfile.AUTO ? hardware.tier() : requested;
        TunePreset preset = TunePreset.of(tier, hardware);

        boolean lightenOnly = force || tier == TuneProfile.LOW;
        int graphics = config.autoTune.graphics && lightenOnly ? applyGraphics(minecraft, preset, force) : 0;
        int addon = config.autoTune.addon ? applyAddon(preset, force) : 0;
        int sodium = config.autoTune.sodium ? applySodium(preset, force) : 0;
        int chloride = config.autoTune.chloride && preset.chlorideLow() && ChlorideBridge.available() ? applyChloride(force) : 0;

        config.autoTune.applied = true;
        OsaConfig.save();

        StringBuilder done = new StringBuilder();
        append(done, "graficos de Minecraft", graphics);
        append(done, "limites del addon", addon);
        append(done, "Sodium, al reiniciar el juego", sodium);
        append(done, "Chloride", chloride);

        String summary = "Perfil " + label(tier) + (requested == TuneProfile.AUTO ? " (detectado)" : " (elegido)") + ". "
                + hardware.describe() + ". "
                + (done.isEmpty() ? "No cambie nada: tu configuracion ya estaba personalizada."
                : force ? "Configure todo con el perfil: " + done + ". Se guardo una copia de cada archivo como .osa-backup."
                : "Complete lo que estaba en su valor por defecto: " + done + ". Lo que ya tenias cambiado quedo igual.");
        OptimizerSodiumAddon.LOG.info("[OSA] {}", summary);
        return summary;
    }

    private static void append(StringBuilder text, String area, int changed) {
        if (changed <= 0) return;
        if (!text.isEmpty()) text.append("; ");
        text.append(area).append(" (").append(changed).append(")");
    }

    private static int applyGraphics(Minecraft minecraft, TunePreset preset, boolean force) {
        Options options = minecraft.options;
        Path file = minecraft.gameDirectory.toPath().resolve("options.txt");
        int changed = 0;
        changed += fill(options.renderDistance(), preset.renderDistance(), force);
        changed += fill(options.simulationDistance(), preset.simulationDistance(), force);
        changed += fill(options.cutoutLeaves(), preset.cutoutLeaves(), force);
        changed += fill(options.weatherRadius(), preset.weatherRadius(), force);
        changed += fill(options.ambientOcclusion(), preset.ambientOcclusion(), force);
        changed += fill(options.biomeBlendRadius(), preset.biomeBlend(), force);
        changed += fill(options.enableVsync(), preset.vsync(), force);
        changed += fill(options.entityDistanceScaling(), preset.entityDistance(), force);
        changed += fill(options.entityShadows(), preset.entityShadows(), force);
        changed += fill(options.mipmapLevels(), preset.mipmaps(), force);
        changed += fill(options.particles(), preset.particles(), force);
        changed += fill(options.cloudStatus(), preset.clouds(), force);
        changed += fill(options.framerateLimit(), preset.maxFps(), force);
        changed += fill(options.menuBackgroundBlurriness(), preset.menuBlur(), force);
        if (changed > 0) {
            backup(file);
            options.save();
        }
        return changed;
    }

    private static <T> int fill(OptionInstance<T> option, T wanted, boolean force) {
        T current = option.get();
        if (Objects.equals(current, wanted)) return 0;
        @SuppressWarnings("unchecked")
        T initial = ((OptionInstanceAccessor<T>) (Object) option).osa$initialValue();
        if (!force && !Objects.equals(current, initial)) return 0;
        option.set(wanted);
        return 1;
    }

    private static int applyAddon(TunePreset preset, boolean force) {
        OsaConfig.Client config = OsaConfig.CLIENT;
        OsaConfig.Client defaults = new OsaConfig.Client();
        int changed = 0;
        changed += fill(() -> config.items.maxRendered, v -> config.items.maxRendered = v, defaults.items.maxRendered, preset.itemMax(), force);
        changed += fill(() -> config.items.renderDistance, v -> config.items.renderDistance = v, defaults.items.renderDistance, preset.itemDistance(), force);
        changed += fill(() -> config.xp.maxRendered, v -> config.xp.maxRendered = v, defaults.xp.maxRendered, preset.xpMax(), force);
        changed += fill(() -> config.xp.renderDistance, v -> config.xp.renderDistance = v, defaults.xp.renderDistance, preset.xpDistance(), force);
        changed += fill(() -> config.particles.maxTotal, v -> config.particles.maxTotal = v, defaults.particles.maxTotal, preset.particleTotal(), force);
        changed += fill(() -> config.particles.maxPerTick, v -> config.particles.maxPerTick = v, defaults.particles.maxPerTick, preset.particlePerTick(), force);
        changed += fill(() -> config.particles.maxDistance, v -> config.particles.maxDistance = v, defaults.particles.maxDistance, preset.particleDistance(), force);
        changed += fill(() -> config.math.fastMath, v -> config.math.fastMath = v, defaults.math.fastMath, preset.fastMath(), force);
        return changed;
    }

    private static <T> int fill(Supplier<T> getter, Consumer<T> setter, T vanilla, T wanted, boolean force) {
        T current = getter.get();
        if (Objects.equals(current, wanted)) return 0;
        if (!force && !Objects.equals(current, vanilla)) return 0;
        setter.accept(wanted);
        return 1;
    }

    private static int applySodium(TunePreset preset, boolean force) {
        Path file = Platform.configDir().resolve("sodium-options.json");
        if (!Platform.isModLoaded("sodium") && !Files.exists(file)) return 0;
        try {
            JsonObject root = Files.exists(file) ? JsonParser.parseString(Files.readString(file)).getAsJsonObject() : new JsonObject();
            TunePreset.SodiumValues values = preset.sodium();
            int changed = 0;

            JsonObject performance = section(root, "performance");
            changed += fill(performance, "animate_only_visible_textures", true, values.animateVisibleOnly(), force);
            changed += fill(performance, "use_entity_culling", true, values.entityCulling(), force);
            changed += fill(performance, "use_fog_occlusion", true, values.fogOcclusion(), force);
            changed += fill(performance, "use_block_face_culling", true, values.blockFaceCulling(), force);
            changed += fill(performance, "use_no_error_g_l_context", true, values.noErrorContext(), force);

            if (changed > 0) {
                backup(file);
                Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));
            }
            return changed;
        } catch (IOException | RuntimeException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo ajustar Sodium: {}", e.toString());
            return 0;
        }
    }

    private static int fill(JsonObject section, String key, Object vanilla, Object wanted, boolean force) {
        JsonElement current = section.get(key);
        JsonPrimitive target = primitive(wanted);
        if (current != null && current.equals(target)) return 0;
        if (!force && current != null && !current.equals(primitive(vanilla))) return 0;
        section.add(key, target);
        return 1;
    }

    private static JsonPrimitive primitive(Object value) {
        if (value instanceof Boolean flag) return new JsonPrimitive(flag);
        if (value instanceof Number number) return new JsonPrimitive(number);
        return new JsonPrimitive(String.valueOf(value));
    }

    private static int applyChloride(boolean force) {
        int changed = 0;
        for (String flag : new String[]{"enabled", "onOverworld", "onNether", "onEnd", "blueBand"}) {
            changed += chlorideFlag("fog", flag);
        }
        for (String flag : new String[]{"rain", "rainDrops", "blockCracking", "blockDestroyed"}) {
            changed += chlorideFlag("particles", flag);
        }
        if (force || ChlorideBridge.disabledParticlesEmpty()) {
            if (ChlorideBridge.addAllParticles()) changed++;
        }
        if (changed > 0) ChlorideBridge.write();
        return changed;
    }

    private static int chlorideFlag(String section, String field) {
        Boolean current = ChlorideBridge.getFlag(section, field);
        if (current == null || !current) return 0;
        return ChlorideBridge.setFlag(section, field, false) ? 1 : 0;
    }

    private static JsonObject section(JsonObject root, String name) {
        if (!root.has(name) || !root.get(name).isJsonObject()) root.add(name, new JsonObject());
        return root.getAsJsonObject(name);
    }

    private static void backup(Path file) {
        try {
            Path copy = file.resolveSibling(file.getFileName() + ".osa-backup");
            if (Files.exists(file) && !Files.exists(copy)) Files.copy(file, copy);
        } catch (IOException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo copiar {}: {}", file, e.toString());
        }
    }

    private static String label(TuneProfile tier) {
        return switch (tier) {
            case LOW -> "BAJO";
            case MEDIUM -> "MEDIO";
            case HIGH -> "ALTO";
            default -> "AUTOMATICO";
        };
    }
}
