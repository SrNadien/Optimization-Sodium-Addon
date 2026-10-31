package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.config.OsaConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class SodiumOptions implements ConfigEntryPoint {

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OsaConfig.Client c = OsaConfig.CLIENT;

        builder.registerOwnModOptions()
                .setName("Optimizer Sodium Addon")
                .addPage(page(builder, "general")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "gpu_leak_fix", () -> c.gpu.leakFix, v -> c.gpu.leakFix = v, true))))
                .addPage(page(builder, "visuals")
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "fog_terrain", () -> c.fog.terrain, v -> c.fog.terrain = v, true))
                                .addOption(toggle(builder, "fog_water", () -> c.fog.water, v -> c.fog.water = v, true))
                                .addOption(toggle(builder, "fog_lava", () -> c.fog.lava, v -> c.fog.lava = v, true))
                                .addOption(toggle(builder, "fog_powder_snow", () -> c.fog.powderSnow, v -> c.fog.powderSnow = v, true)))
                        .addOptionGroup(group(builder)
                                .addOption(toggle(builder, "overlay_water", () -> c.overlays.water, v -> c.overlays.water = v, true))
                                .addOption(toggle(builder, "overlay_fire", () -> c.overlays.fire, v -> c.overlays.fire = v, true))))
                .addPage(page(builder, "render")
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "armor_max", () -> c.armorStands.maxRendered, v -> c.armorStands.maxRendered = v, 0, 256, 4, 32))
                                .addOption(slider(builder, "armor_distance", () -> c.armorStands.renderDistance, v -> c.armorStands.renderDistance = v, 4, 128, 2, 24))
                                .addOption(toggle(builder, "armor_hide_invisible", () -> c.armorStands.hideInvisibleEmpty, v -> c.armorStands.hideInvisibleEmpty = v, true)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "item_max", () -> c.items.maxRendered, v -> c.items.maxRendered = v, 0, 1024, 10, 150))
                                .addOption(slider(builder, "item_distance", () -> c.items.renderDistance, v -> c.items.renderDistance = v, 4, 128, 2, 32)))
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "xp_max", () -> c.xp.maxRendered, v -> c.xp.maxRendered = v, 0, 512, 10, 60))
                                .addOption(slider(builder, "xp_distance", () -> c.xp.renderDistance, v -> c.xp.renderDistance = v, 4, 128, 2, 24))))
                .addPage(page(builder, "particles")
                        .addOptionGroup(group(builder)
                                .addOption(slider(builder, "particle_total", () -> c.particles.maxTotal, v -> c.particles.maxTotal = v, 0, 16384, 100, 3000))
                                .addOption(slider(builder, "particle_per_tick", () -> c.particles.maxPerTick, v -> c.particles.maxPerTick = v, 0, 4096, 50, 400))
                                .addOption(slider(builder, "particle_distance", () -> c.particles.maxDistance, v -> c.particles.maxDistance = v, 8, 128, 2, 48))));
    }

    private static OptionPageBuilder page(ConfigBuilder builder, String key) {
        return builder.createOptionPage().setName(Component.translatable(OptimizerSodiumAddon.MODID + ".page." + key));
    }

    private static OptionGroupBuilder group(ConfigBuilder builder) {
        return builder.createOptionGroup();
    }

    private static Identifier id(String key) {
        return Identifier.fromNamespaceAndPath(OptimizerSodiumAddon.MODID, key);
    }

    private static void save() {
        OsaConfig.save();
    }

    private static BooleanOptionBuilder toggle(
            ConfigBuilder builder, String key, BooleanSupplier getter, Consumer<Boolean> setter, boolean fallback) {
        return builder.createBooleanOption(id(key))
                .setName(Component.translatable(OptimizerSodiumAddon.MODID + ".option." + key))
                .setTooltip(Component.translatable(OptimizerSodiumAddon.MODID + ".option." + key + ".tooltip"))
                .setStorageHandler(SodiumOptions::save)
                .setBinding(setter, getter::getAsBoolean)
                .setDefaultValue(fallback);
    }

    private static IntegerOptionBuilder slider(
            ConfigBuilder builder, String key, IntSupplier getter, IntConsumer setter, int min, int max, int step, int fallback) {
        return builder.createIntegerOption(id(key))
                .setName(Component.translatable(OptimizerSodiumAddon.MODID + ".option." + key))
                .setTooltip(Component.translatable(OptimizerSodiumAddon.MODID + ".option." + key + ".tooltip"))
                .setRange(min, max, step)
                .setValueFormatter(v -> v == 0 && min == 0
                        ? Component.translatable(OptimizerSodiumAddon.MODID + ".unlimited")
                        : Component.literal(Integer.toString(v)))
                .setStorageHandler(SodiumOptions::save)
                .setBinding(setter::accept, getter::getAsInt)
                .setDefaultValue(fallback);
    }
}
