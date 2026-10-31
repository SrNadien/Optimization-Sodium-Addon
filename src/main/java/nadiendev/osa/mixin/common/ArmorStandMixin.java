package nadiendev.osa.mixin.common;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void osa$noPush(CallbackInfo ci) {
        if (OsaConfig.serverReady() && OsaConfig.SERVER.armorStands.disablePush) {
            ci.cancel();
        }
    }
}
