package nadiendev.osa.mixin.client;

import nadiendev.osa.client.ParticleLimits;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Shadow @Final private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void osa$limit(Particle particle, CallbackInfo ci) {
        if (ParticleLimits.reject(particle, this.particles)) {
            ci.cancel();
        }
    }
}
