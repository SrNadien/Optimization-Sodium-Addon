package nadiendev.osa.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import nadiendev.osa.client.FastMath;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mth.class)
public abstract class MthMixin {

    @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
    private static void osa$sin(double value, CallbackInfoReturnable<Float> cir) {
        if (FastMath.enabled() && RenderSystem.isOnRenderThread()) cir.setReturnValue(FastMath.sin((float) value));
    }

    @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
    private static void osa$cos(double value, CallbackInfoReturnable<Float> cir) {
        if (FastMath.enabled() && RenderSystem.isOnRenderThread()) cir.setReturnValue(FastMath.cos((float) value));
    }
}
