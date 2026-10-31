package nadiendev.osa.mixin.client;

import com.mojang.blaze3d.platform.Window;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.client.BorderlessWindow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Shadow
    @Final
    private long handle;

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void osa$borderless(CallbackInfo ci) {
        if (!OsaConfig.clientReady() || !OsaConfig.CLIENT.borderlessF11.get()) return;
        if (BorderlessWindow.toggle(this.handle)) ci.cancel();
    }
}
