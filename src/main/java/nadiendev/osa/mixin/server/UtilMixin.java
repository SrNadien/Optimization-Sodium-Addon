package nadiendev.osa.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import nadiendev.osa.server.ServerThreads;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla: threads = clamp(availableProcessors() - 1, 1, maxThreads).
 * Cambiamos el valor de availableProcessors() para que el resultado sea el configurado.
 * require = 0: si la firma cambia en otra version, el server arranca igual con el valor vanilla.
 */
@Mixin(Util.class)
public abstract class UtilMixin {

    @ModifyExpressionValue(method = "makeExecutor", require = 0,
            at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;availableProcessors()I"))
    private static int osa$threads(int original) {
        return ServerThreads.desiredThreads() + 1;
    }
}
