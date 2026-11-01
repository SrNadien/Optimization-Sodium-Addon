package nadiendev.osa.client;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DynamicLights {
    private static final int REASSERT_TICKS = 40;
    private static final int MAX_LIGHT = 15;

    private static final Long2IntOpenHashMap ACTIVE = new Long2IntOpenHashMap();
    private static ClientLevel lastLevel;
    private static int ticks;

    private record Source(long pos, int level, double distance) {}

    private DynamicLights() {}

    public static int levelAt(long packedPos) {
        return ACTIVE.isEmpty() ? 0 : ACTIVE.get(packedPos);
    }

    public static void clear() {
        ACTIVE.clear();
        lastLevel = null;
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        if (level != lastLevel) {
            ACTIVE.clear();
            lastLevel = level;
        }

        if (!DynamicLightSettings.enabled()) {
            if (!ACTIVE.isEmpty()) release(level);
            return;
        }

        ticks++;
        if (ticks % DynamicLightSettings.interval() != 0) return;

        Long2IntOpenHashMap target = collect(minecraft, level);
        LongOpenHashSet changed = new LongOpenHashSet();
        for (long pos : ACTIVE.keySet()) {
            if (target.get(pos) != ACTIVE.get(pos)) changed.add(pos);
        }
        for (long pos : target.keySet()) {
            if (ACTIVE.get(pos) != target.get(pos)) changed.add(pos);
        }
        if (ticks % REASSERT_TICKS == 0) changed.addAll(ACTIVE.keySet());

        ACTIVE.clear();
        ACTIVE.putAll(target);

        LevelLightEngine engine = level.getLightEngine();
        for (long pos : changed) engine.checkBlock(BlockPos.of(pos));
    }

    private static void release(ClientLevel level) {
        LongOpenHashSet positions = new LongOpenHashSet(ACTIVE.keySet());
        ACTIVE.clear();
        LevelLightEngine engine = level.getLightEngine();
        for (long pos : positions) engine.checkBlock(BlockPos.of(pos));
    }

    private static Long2IntOpenHashMap collect(Minecraft minecraft, ClientLevel level) {
        Vec3 camera = minecraft.gameRenderer.mainCamera().position();
        double reach = DynamicLightSettings.distance();
        double reachSquared = reach * reach;

        List<Source> sources = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity.isRemoved() || entity.isSpectator()) continue;
            double distance = entity.distanceToSqr(camera);
            if (distance > reachSquared) continue;
            int light = lightOf(entity);
            if (light <= 0) continue;
            long pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()).asLong();
            sources.add(new Source(pos, light, distance));
        }
        sources.sort(Comparator.comparingDouble(Source::distance));

        Long2IntOpenHashMap target = new Long2IntOpenHashMap();
        int limit = Math.min(sources.size(), DynamicLightSettings.maxSources());
        for (int i = 0; i < limit; i++) {
            Source source = sources.get(i);
            target.put(source.pos(), Math.max(target.get(source.pos()), source.level()));
        }
        return target;
    }

    private static int lightOf(Entity entity) {
        int light = 0;
        if (entity instanceof ItemEntity item) light = stackLight(item.getItem());
        if (entity instanceof LivingEntity living) {
            light = Math.max(stackLight(living.getMainHandItem()), stackLight(living.getOffhandItem()));
        }
        if (DynamicLightSettings.burning() && entity.isOnFire()) light = MAX_LIGHT;
        Identifier type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Integer typeLight = DynamicLightSettings.entities().get(type);
        if (typeLight != null) light = Math.max(light, typeLight);
        return Math.min(light, MAX_LIGHT);
    }

    private static int stackLight(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer override = DynamicLightSettings.items().get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (override != null) return override;
        if (stack.getItem() instanceof BlockItem block) return block.getBlock().defaultBlockState().getLightEmission();
        return 0;
    }
}
