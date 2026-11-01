package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkParticles.Starter.class)
public abstract class FireworkStarterMixin {
    @Unique
    private boolean osa$missingSpark;

    @WrapOperation(method = "createParticle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"))
    private Particle osa$trackSpark(ParticleEngine engine, ParticleOptions options, double x, double y, double z,
                                    double dx, double dy, double dz, Operation<Particle> original) {
        Particle particle = original.call(engine, options, x, y, z, dx, dy, dz);
        this.osa$missingSpark = particle == null;
        return particle;
    }

    @Inject(method = "createParticle", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/FireworkParticles$SparkParticle;setTrail(Z)V"))
    private void osa$skipMissingSpark(double x, double y, double z, double xa, double ya, double za, IntList rgbColors,
                                      IntList fadeColors, boolean trail, boolean twinkle, CallbackInfo ci) {
        if (this.osa$missingSpark) ci.cancel();
    }
}
