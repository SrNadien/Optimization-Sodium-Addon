package nadiendev.osa.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;

public final class InertParticle extends NoRenderParticle {
    public InertParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }
}
