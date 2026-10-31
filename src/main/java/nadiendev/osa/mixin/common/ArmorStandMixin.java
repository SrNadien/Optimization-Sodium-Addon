package nadiendev.osa.mixin.common;

import nadiendev.osa.OsaConfig;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cada armor stand busca minecarts en su caja de colision en todos los ticks para empujarlos.
 * Con 200 armor stands en una dungeon son 200 busquedas de entidades por tick que no sirven para nada.
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void osa$noPush(CallbackInfo ci) {
        if (OsaConfig.serverReady() && OsaConfig.SERVER.armorStandNoPush.get()) {
            ci.cancel();
        }
    }
}
