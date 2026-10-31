package nadiendev.osa.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import nadiendev.osa.server.ServerThreads;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Util.class)
public abstract class UtilMixin {
    @ModifyExpressionValue(method = "makeExecutor", require = 0,
            at = @At(value = "INVOKE", target = "Ljava/lang/Runtime;availableProcessors()I"))
    private static int osa$threads(int original) {
        return ServerThreads.desiredThreads() + 1;
    }
}
