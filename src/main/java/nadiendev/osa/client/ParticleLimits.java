package nadiendev.osa.client;

import nadiendev.osa.OsaConfig;
import nadiendev.osa.mixin.client.ParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public final class ParticleLimits {
    private static int spawnedThisTick;

    private ParticleLimits() {}

    static void resetTick() {
        spawnedThisTick = 0;
    }

    public static boolean reject(Particle p, Map<?, ? extends ParticleGroup<?>> live) {
        if (!OsaConfig.clientReady()) return false;

        int perTick = OsaConfig.CLIENT.particleMaxPerTick.get();
        if (perTick > 0 && spawnedThisTick >= perTick) return true;

        int maxTotal = OsaConfig.CLIENT.particleMaxTotal.get();
        if (maxTotal > 0) {
            int total = 0;
            for (ParticleGroup<?> group : live.values()) total += group.size();
            if (total >= maxTotal) return true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            Vec3 cam = mc.gameRenderer.getMainCamera().position();
            ParticleAccessor a = (ParticleAccessor) p;
            double dx = a.osa$getX() - cam.x, dy = a.osa$getY() - cam.y, dz = a.osa$getZ() - cam.z;
            double max = OsaConfig.CLIENT.particleDistance.get();
            if (dx * dx + dy * dy + dz * dz > max * max) return true;
        }

        spawnedThisTick++;
        return false;
    }
}
