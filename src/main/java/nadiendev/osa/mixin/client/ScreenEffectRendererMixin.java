package nadiendev.osa.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import nadiendev.osa.client.VisualToggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "submitWater", at = @At("HEAD"), cancellable = true)
    private static void osa$hideWater(Minecraft minecraft, PoseStack poseStack, SubmitNodeCollector collector, CallbackInfo ci) {
        if (VisualToggles.hideWaterOverlay()) ci.cancel();
    }

    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
    private static void osa$hideFire(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (VisualToggles.hideFireOverlay()) ci.cancel();
    }
}
