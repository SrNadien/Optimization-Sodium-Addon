package nadiendev.osa.mixin.client;

import nadiendev.osa.client.TintCoalescer;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "clearTintCaches", at = @At("HEAD"), cancellable = true)
    private void osa$coalesceTintClears(CallbackInfo ci) {
        if (TintCoalescer.defer()) ci.cancel();
    }
}
