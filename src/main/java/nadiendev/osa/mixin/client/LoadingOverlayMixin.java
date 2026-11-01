package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import nadiendev.osa.client.ReloadTimer;
import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
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
    private Minecraft minecraft;

    @Shadow
    @Final
    private boolean fadeIn;

    @Shadow
    private long fadeInStart;

    @Shadow
    private long fadeOutStart;

    @Unique
    private boolean osa$hidden() {
        return this.fadeIn && OsaConfig.hideReloadOverlay();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void osa$drawCounter(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.fadeIn) return;
        if (osa$hidden() && osa$screenMissing()) {
            this.minecraft.screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        }
        ReloadTimer.drawLive(graphics);
    }

    @Unique
    private boolean osa$screenMissing() {
        if (this.minecraft.screen == null || this.minecraft.getOverlay() != (Object) this) return false;
        long now = Util.getMillis();
        boolean fadingIn = this.fadeInStart > -1L && now - this.fadeInStart < 500L;
        boolean fadingOut = this.fadeOutStart > -1L && now - this.fadeOutStart >= 1000L;
        return !fadingIn && !fadingOut;
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void osa$skipBackground(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color,
                                    Operation<Void> original) {
        if (!osa$hidden()) original.call(graphics, x1, y1, x2, y2, color);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V"))
    private void osa$skipLogo(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture, int x, int y,
                              float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth,
                              int textureHeight, int color, Operation<Void> original) {
        if (!osa$hidden()) original.call(graphics, pipeline, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;extractProgressBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIF)V"))
    private void osa$skipBar(LoadingOverlay overlay, GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2,
                             float alpha, Operation<Void> original) {
        if (!osa$hidden()) original.call(overlay, graphics, x1, y1, x2, y2, alpha);
    }
}
