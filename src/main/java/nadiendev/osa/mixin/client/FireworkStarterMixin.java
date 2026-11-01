package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.client.InertParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkParticles.Starter.class)
public abstract class FireworkStarterMixin extends NoRenderParticle {
    protected FireworkStarterMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @WrapOperation(method = "tick", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"))
    private Particle osa$flashMayBeDisabled(ParticleEngine engine, ParticleOptions options, double x, double y, double z,
                                            double dx, double dy, double dz, Operation<Particle> original) {
        Particle particle = original.call(engine, options, x, y, z, dx, dy, dz);
        return particle != null ? particle : new InertParticle(this.level, x, y, z);
    }
}
