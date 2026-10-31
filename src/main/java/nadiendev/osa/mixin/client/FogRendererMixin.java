package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import nadiendev.osa.client.VisualToggles;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @WrapOperation(method = "setupFog",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static void osa$fogStart(float start, Operation<Void> original, @Local(argsOnly = true) Camera camera) {
        original.call(VisualToggles.fogStart(camera, start));
    }

    @WrapOperation(method = "setupFog",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"))
    private static void osa$fogEnd(float end, Operation<Void> original, @Local(argsOnly = true) Camera camera) {
        original.call(VisualToggles.fogEnd(camera, end));
    }
}
