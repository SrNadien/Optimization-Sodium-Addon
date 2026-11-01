package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.client.ReloadTimer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Shadow
    @Final
    private boolean fadeIn;

    @Unique
    private boolean osa$hidden() {
        return this.fadeIn && OsaConfig.hideReloadOverlay();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void osa$drawCounter(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.fadeIn) ReloadTimer.drawLive(graphics);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private void osa$skipBackground(GuiGraphics graphics, RenderType type, int x1, int y1, int x2, int y2, int color,
                                    Operation<Void> original) {
        if (!osa$hidden()) original.call(graphics, type, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private void osa$skipLogo(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                              float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight,
                              Operation<Void> original) {
        if (!osa$hidden()) original.call(graphics, texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
    private void osa$skipBar(LoadingOverlay overlay, GuiGraphics graphics, int x1, int y1, int x2, int y2, float alpha,
                             Operation<Void> original) {
        if (!osa$hidden()) original.call(overlay, graphics, x1, y1, x2, y2, alpha);
    }
}
