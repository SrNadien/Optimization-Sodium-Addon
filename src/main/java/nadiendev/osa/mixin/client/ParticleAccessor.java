package nadiendev.osa.mixin.client;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("x")
    double osa$getX();

    @Accessor("y")
    double osa$getY();

    @Accessor("z")
    double osa$getZ();
}
