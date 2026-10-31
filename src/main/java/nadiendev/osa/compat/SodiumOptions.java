package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

@ConfigEntryPointForge(OptimizerSodiumAddon.MODID)
public class SodiumOptions implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OsaConfig.Client c = OsaConfig.CLIENT;

        builder.registerOwnModOptions()
                .setName("Optimizer Sodium Addon")
                .addPage(page(builder, "general")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "borderless_f11", c.borderlessF11, true))
                                .addOption(toggle(builder, "gpu_leak_fix", c.gpuLeakFix, true))))
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
                                .addOption(slider(builder, "armor_max", c.armorStandMax, 0, 256, 4, 32))
                                .addOption(slider(builder, "armor_distance", c.armorStandDistance, 4, 128, 2, 24))
                                .addOption(toggle(builder, "armor_hide_invisible", c.armorStandHideInvisibleEmpty, true)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "item_max", c.itemMax, 0, 1024, 10, 150))
                                .addOption(slider(builder, "item_distance", c.itemDistance, 4, 128, 2, 32)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "xp_max", c.xpMax, 0, 512, 10, 60))
                                .addOption(slider(builder, "xp_distance", c.xpDistance, 4, 128, 2, 24))))
                .addPage(page(builder, "particles")
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "particle_total", c.particleMaxTotal, 0, 16384, 100, 3000))
                                .addOption(slider(builder, "particle_per_tick", c.particleMaxPerTick, 0, 4096, 50, 400))
                                .addOption(slider(builder, "particle_distance", c.particleDistance, 8, 128, 2, 48))));
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

    private static void save() {
        OsaConfig.CLIENT_SPEC.save();
    }

    private static BooleanOptionBuilder toggle(
            ConfigBuilder builder, String key, ModConfigSpec.BooleanValue value, boolean fallback) {
        return builder.createBooleanOption(id(key))
                .setName(Component.translatable("optimizersodiumaddon.option." + key))
                .setTooltip(Component.translatable("optimizersodiumaddon.option." + key + ".tooltip"))
                .setStorageHandler(SodiumOptions::save)
                .setBinding(value::set, value::get)
                .setDefaultValue(fallback);
    }

    private static IntegerOptionBuilder slider(
            ConfigBuilder builder, String key, ModConfigSpec.IntValue value, int min, int max, int step, int fallback) {
        return builder.createIntegerOption(id(key))
                .setName(Component.translatable("optimizersodiumaddon.option." + key))
                .setTooltip(Component.translatable("optimizersodiumaddon.option." + key + ".tooltip"))
                .setRange(min, max, step)
                .setValueFormatter(v -> v == 0 && min == 0
                        ? Component.translatable("optimizersodiumaddon.unlimited")
                        : Component.literal(Integer.toString(v)))
                .setStorageHandler(SodiumOptions::save)
                .setBinding(value::set, value::get)
                .setDefaultValue(fallback);
    }
}
