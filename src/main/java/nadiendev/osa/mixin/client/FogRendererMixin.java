package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import nadiendev.osa.client.VisualToggles;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @WrapOperation(method = "setupFog",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"))
    private void osa$fog(FogRenderer self, ByteBuffer buffer, int offset, Vector4f color,
                         float environmentalStart, float environmentalEnd,
                         float renderDistanceStart, float renderDistanceEnd,
                         float skyEnd, float cloudEnd,
                         Operation<Void> original, @Local(argsOnly = true) Camera camera) {
        original.call(self, buffer, offset, color,
                VisualToggles.fogStart(camera, environmentalStart),
                VisualToggles.fogEnd(camera, environmentalEnd),
                VisualToggles.fogStart(camera, renderDistanceStart),
                VisualToggles.fogEnd(camera, renderDistanceEnd),
                skyEnd, cloudEnd);
    }
}
