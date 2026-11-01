package nadiendev.osa.client;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import nadiendev.osa.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public final class EntityCulling {
    private static final long WINDOW_NANOS = 50_000_000L;
    private static final long PURGE_EVERY_NANOS = 10_000_000_000L;
    private static final long PURGE_AFTER_NANOS = 5_000_000_000L;
    private static final double SIZE_FACTOR = 1.5;
    private static final double[][] SAMPLES = {
            {0.5, 0.5, 0.5},
            {0.5, 0.95, 0.5},
            {0.5, 0.05, 0.5},
            {0.05, 0.5, 0.5},
            {0.95, 0.5, 0.5},
            {0.5, 0.5, 0.05},
            {0.5, 0.5, 0.95}
    };

    private static final class Entry {
        boolean hidden;
        long checkedAt;
    }

    private static final class Extent {
        Object source;
        double value;
        long usedAt;
    }

    private static final Long2ObjectOpenHashMap<Entry> CACHE = new Long2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<Extent> EXTENTS = new Int2ObjectOpenHashMap<>();
    private static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();

    private static long windowStart;
    private static int windowUsed;
    private static long lastPurge;

    private static long seen;
    private static long culledDistance;
    private static long culledFrustum;
    private static long culledOcclusion;

    private EntityCulling() {}

    public static boolean shouldSkip(Entity entity, Frustum frustum, double camX, double camY, double camZ) {
        if (!OsaConfig.entityCullingEnabled()) return false;
        OsaConfig.Client config = OsaConfig.CLIENT;

        boolean display = entity instanceof Display && config.cullDisplays.get();
        boolean stand = entity instanceof ArmorStand armorStand && config.cullArmorStands.get()
                && !(armorStand.hasCustomName() && armorStand.isCustomNameVisible());
        if (!display && !stand) return false;

        seen++;
        double dx = entity.getX() - camX;
        double dy = entity.getY() - camY;
        double dz = entity.getZ() - camZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        double near = config.cullNeverWithin.get();
        if (distanceSq < near * near) return false;

        double max = entity instanceof Display.TextDisplay ? config.cullTextMaxDistance.get() : config.cullMaxDistance.get();
        if (max > 0 && distanceSq > max * max) {
            culledDistance++;
            return true;
        }

        if (display) {
            Display model = (Display) entity;
            if (entity.noCulling && !frustum.isVisible(new AABB(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getX(), entity.getY(), entity.getZ()).inflate(extentOf(model) + config.cullFrustumMargin.get()))) {
                culledFrustum++;
                return true;
            }
        }

        double minOcclusion = config.cullOcclusionMinDistance.get();
        if (config.cullOcclusion.get() && distanceSq >= minOcclusion * minOcclusion && hiddenBehindBlocks(entity, camX, camY, camZ)) {
            culledOcclusion++;
            return true;
        }
        return false;
    }

    public static void clear() {
        CACHE.clear();
        EXTENTS.clear();
    }

    public static String summary() {
        long culled = culledDistance + culledFrustum + culledOcclusion;
        return "vistas " + seen + ", descartadas " + culled + " (distancia " + culledDistance + ", camara " + culledFrustum
                + ", muros " + culledOcclusion + ")";
    }

    public static void reset() {
        seen = 0;
        culledDistance = 0;
        culledFrustum = 0;
        culledOcclusion = 0;
    }

    private static double extentOf(Display display) {
        Display.RenderState state = display.renderState();
        if (state == null) return 1.0;
        Object source = state.transformation();
        long now = System.nanoTime();
        Extent cached = EXTENTS.get(display.getId());
        if (cached != null && cached.source == source) {
            cached.usedAt = now;
            return cached.value;
        }
        Transformation transformation = state.transformation().get(1.0F);
        Vector3f scale = transformation.getScale();
        Vector3f translation = transformation.getTranslation();
        double size = Math.max(Math.abs(scale.x()), Math.max(Math.abs(scale.y()), Math.abs(scale.z()))) * SIZE_FACTOR;
        double offset = Math.max(Math.abs(translation.x()), Math.max(Math.abs(translation.y()), Math.abs(translation.z())));
        if (cached == null) {
            cached = new Extent();
            EXTENTS.put(display.getId(), cached);
        }
        cached.source = source;
        cached.value = Math.max(0.25, size + offset);
        cached.usedAt = now;
        return cached.value;
    }

    private static boolean hiddenBehindBlocks(Entity entity, double camX, double camY, double camZ) {
        int blockX = floor(entity.getX());
        int blockY = floor(entity.getY());
        int blockZ = floor(entity.getZ());
        long key = BlockPos.asLong(blockX, blockY, blockZ);
        long now = System.nanoTime();
        Entry entry = CACHE.get(key);
        long recheck = OsaConfig.CLIENT.cullOcclusionRecheckMs.get() * 1_000_000L;
        if (entry != null && now - entry.checkedAt < recheck) return entry.hidden;

        if (now - windowStart > WINDOW_NANOS) {
            windowStart = now;
            windowUsed = 0;
        }
        if (windowUsed >= OsaConfig.CLIENT.cullOcclusionBudget.get()) return entry != null && entry.hidden;
        windowUsed++;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        if (entry == null) {
            entry = new Entry();
            CACHE.put(key, entry);
        }

        boolean hidden = true;
        for (double[] sample : SAMPLES) {
            if (rayClear(level, camX, camY, camZ, blockX + sample[0], blockY + sample[1], blockZ + sample[2])) {
                hidden = false;
                break;
            }
        }
        entry.hidden = hidden;
        entry.checkedAt = now;

        if (now - lastPurge > PURGE_EVERY_NANOS) {
            lastPurge = now;
            CACHE.values().removeIf(e -> now - e.checkedAt > PURGE_AFTER_NANOS);
            EXTENTS.values().removeIf(e -> now - e.usedAt > PURGE_AFTER_NANOS);
        }
        return hidden;
    }

    private static boolean rayClear(ClientLevel level, double x0, double y0, double z0, double x1, double y1, double z1) {
        int ix = floor(x0);
        int iy = floor(y0);
        int iz = floor(z0);
        int tx = floor(x1);
        int ty = floor(y1);
        int tz = floor(z1);
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        int sx = dx > 0 ? 1 : -1;
        int sy = dy > 0 ? 1 : -1;
        int sz = dz > 0 ? 1 : -1;
        double deltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double deltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double deltaZ = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
        double maxX = dx == 0 ? Double.POSITIVE_INFINITY : (dx > 0 ? ix + 1 - x0 : x0 - ix) * deltaX;
        double maxY = dy == 0 ? Double.POSITIVE_INFINITY : (dy > 0 ? iy + 1 - y0 : y0 - iy) * deltaY;
        double maxZ = dz == 0 ? Double.POSITIVE_INFINITY : (dz > 0 ? iz + 1 - z0 : z0 - iz) * deltaZ;
        int steps = Math.abs(tx - ix) + Math.abs(ty - iy) + Math.abs(tz - iz) + 2;

        for (int i = 0; i < steps; i++) {
            if (maxX < maxY) {
                if (maxX < maxZ) {
                    ix += sx;
                    maxX += deltaX;
                } else {
                    iz += sz;
                    maxZ += deltaZ;
                }
            } else if (maxY < maxZ) {
                iy += sy;
                maxY += deltaY;
            } else {
                iz += sz;
                maxZ += deltaZ;
            }
            if (ix == tx && iy == ty && iz == tz) return true;
            POS.set(ix, iy, iz);
            BlockState state = level.getBlockState(POS);
            if (state.isSolidRender(level, POS)) return false;
        }
        return true;
    }

    private static int floor(double value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
