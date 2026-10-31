package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.mixin.client.ParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

public final class ParticleLimits {
    private static int spawnedThisTick;

    private ParticleLimits() {}

    public static void resetTick() {
        spawnedThisTick = 0;
    }

    public static boolean reject(Particle p, Map<?, ? extends Queue<Particle>> live) {
        if (!OsaConfig.clientReady()) return false;

        int perTick = OsaConfig.CLIENT.particles.maxPerTick;
        if (perTick > 0 && spawnedThisTick >= perTick) return true;

        int maxTotal = OsaConfig.CLIENT.particles.maxTotal;
        if (maxTotal > 0) {
            int total = 0;
            for (Collection<Particle> q : live.values()) total += q.size();
            if (total >= maxTotal) return true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            ParticleAccessor a = (ParticleAccessor) p;
            double dx = a.osa$getX() - cam.x, dy = a.osa$getY() - cam.y, dz = a.osa$getZ() - cam.z;
            double max = OsaConfig.CLIENT.particles.maxDistance;
            if (dx * dx + dy * dy + dz * dz > max * max) return true;
        }

        spawnedThisTick++;
        return false;
    }
}
