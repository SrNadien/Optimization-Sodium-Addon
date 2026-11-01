package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.client.AutoTune;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ConfigEntryPointForge(OptimizerSodiumAddon.MODID)
public class SodiumOptions implements ConfigEntryPoint {
    private static final Map<String, OptionImpact> IMPACTS = Map.ofEntries(
            Map.entry("better_grass", OptionImpact.MEDIUM),
            Map.entry("exact_translucency", OptionImpact.MEDIUM),
            Map.entry("auto_tune", OptionImpact.VARIES),
            Map.entry("item_max", OptionImpact.VARIES),
            Map.entry("item_distance", OptionImpact.VARIES),
            Map.entry("xp_max", OptionImpact.VARIES),
            Map.entry("xp_distance", OptionImpact.VARIES),
            Map.entry("particle_total", OptionImpact.VARIES),
            Map.entry("particle_per_tick", OptionImpact.VARIES),
            Map.entry("particle_distance", OptionImpact.VARIES),
            Map.entry("dynamic_lights", OptionImpact.MEDIUM),
            Map.entry("dynamic_lights_max", OptionImpact.MEDIUM),
            Map.entry("dynamic_lights_distance", OptionImpact.MEDIUM)
    );

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OsaConfig.Client c = OsaConfig.CLIENT;

        OptionPageBuilder particles = page(builder, "particles")
                .addOptionGroup(group(builder)
                        .addOption(slider(builder, "particle_total", c.particleMaxTotal, 0, 16384, 100, 3000))
                        .addOption(slider(builder, "particle_per_tick", c.particleMaxPerTick, 0, 4096, 50, 400))
                        .addOption(slider(builder, "particle_distance", c.particleDistance, 8, 128, 2, 48)));
        if (ChlorideBridge.available()) {
            particles.addOptionGroup(group(builder)
                    .addOption(toggle(builder, "chloride_all_particles", ChlorideBridge::allParticlesDisabled, ChlorideBridge::setAllParticles, false, ChlorideBridge::write)));
        }

        builder.registerOwnModOptions()
                .setName("Optimizer Sodium Addon")
                .addPage(page(builder, "general")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "borderless_f11", c.borderlessF11, true))
                                .addOption(toggle(builder, "gpu_leak_fix", c.gpuLeakFix, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "startup_timer", c.startupTimer, true))
                                .addOption(toggle(builder, "reload_hide_overlay", c.reloadHideOverlay, true))
                                .addOption(toggle(builder, "reload_counter", c.reloadCounter, true))
                                .addOption(toggle(builder, "reload_profile", c.reloadProfile, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "skip_broken_blocks", c.skipBrokenBlocks, true))
                                .addOption(toggle(builder, "coalesce_tints", c.coalesceTints, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "better_grass", c.betterGrass::get, c.betterGrass::set, false, SodiumOptions::saveAndRefresh)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "aggressive_culling", SodiumSettings::aggressiveCulling, SodiumSettings::setAggressiveCulling, false, SodiumSettings::save))
                                .addOption(toggle(builder, "exact_translucency", SodiumSettings::exactTranslucency, SodiumSettings::setExactTranslucency, false, SodiumSettings::save)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "auto_tune", c.tuneEnabled::get, SodiumOptions::setAutoTune, false, SodiumOptions::save))
                                .addOption(toggle(builder, "fast_math", c.fastMath, false))))
                .addPage(page(builder, "visuals")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "fog_terrain", c.terrainFog, true))
                                .addOption(toggle(builder, "fog_water", c.waterFog, true))
                                .addOption(toggle(builder, "fog_lava", c.lavaFog, true))
                                .addOption(toggle(builder, "fog_powder_snow", c.powderSnowFog, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "overlay_water", c.waterOverlay, true))
                                .addOption(toggle(builder, "overlay_fire", c.fireOverlay, true))))
                .addPage(page(builder, "render")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "armor_hide_invisible", c.armorStandHideInvisibleEmpty, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "entity_culling", c.entityCulling, false))
                                .addOption(toggle(builder, "entity_culling_occlusion", c.cullOcclusion, true))
                                .addOption(slider(builder, "entity_culling_distance", c.cullMaxDistance, 0, 256, 8, 64)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "item_max", c.itemMax, 0, 1024, 10, 150))
                                .addOption(slider(builder, "item_distance", c.itemDistance, 4, 128, 2, 32)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "xp_max", c.xpMax, 0, 512, 10, 60))
                                .addOption(slider(builder, "xp_distance", c.xpDistance, 4, 128, 2, 24))))
                .addPage(page(builder, "lights")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "dynamic_lights", c.dynamicLights, false))
                                .addOption(slider(builder, "dynamic_lights_max", c.dynamicLightsMaxSources, 1, 256, 1, 24))
                                .addOption(slider(builder, "dynamic_lights_distance", c.dynamicLightsDistance, 8, 128, 4, 32))
                                .addOption(toggle(builder, "dynamic_lights_burning", c.dynamicLightsBurning, true))))
                .addPage(particles);
    }

    private static OptionImpact impactOf(String key) {
        return IMPACTS.getOrDefault(key, OptionImpact.LOW);
    }

    private static OptionPageBuilder page(ConfigBuilder builder, String key) {
        return builder.createOptionPage().setName(Component.translatable("optimizersodiumaddon.page." + key));
    }

    private static OptionGroupBuilder group(ConfigBuilder builder) {
        return builder.createOptionGroup();
    }

    private static Identifier id(String key) {
        return Identifier.fromNamespaceAndPath(OptimizerSodiumAddon.MODID, key);
    }

    private static void setAutoTune(boolean enabled) {
        boolean wasEnabled = OsaConfig.CLIENT.tuneEnabled.get();
        OsaConfig.CLIENT.tuneEnabled.set(enabled);
        if (enabled && !wasEnabled) AutoTune.applyManually();
    }

    private static void save() {
        OsaConfig.CLIENT_SPEC.save();
    }

    private static void saveAndRefresh() {
        save();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.levelRenderer != null && minecraft.level != null) minecraft.levelRenderer.allChanged();
        });
    }

    private static BooleanOptionBuilder toggle(
            ConfigBuilder builder, String key, ModConfigSpec.BooleanValue value, boolean fallback) {
        return toggle(builder, key, value::get, value::set, fallback, SodiumOptions::save);
    }

    private static BooleanOptionBuilder toggle(
            ConfigBuilder builder, String key, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean fallback,
            StorageEventHandler storage) {
        return builder.createBooleanOption(id(key))
                .setName(Component.translatable("optimizersodiumaddon.option." + key))
                .setTooltip(Component.translatable("optimizersodiumaddon.option." + key + ".tooltip"))
                .setImpact(impactOf(key))
                .setStorageHandler(storage)
                .setBinding(setter, getter)
                .setDefaultValue(fallback);
    }

    private static IntegerOptionBuilder slider(
            ConfigBuilder builder, String key, ModConfigSpec.IntValue value, int min, int max, int step, int fallback) {
        return builder.createIntegerOption(id(key))
                .setName(Component.translatable("optimizersodiumaddon.option." + key))
                .setTooltip(Component.translatable("optimizersodiumaddon.option." + key + ".tooltip"))
                .setImpact(impactOf(key))
                .setRange(min, max, step)
                .setValueFormatter(v -> v == 0 && min == 0
                        ? Component.translatable("optimizersodiumaddon.unlimited")
                        : Component.literal(Integer.toString(v)))
                .setStorageHandler(SodiumOptions::save)
                .setBinding(value::set, value::get)
                .setDefaultValue(fallback);
    }
}
