package nadiendev.osa.mixin.client;

import nadiendev.osa.client.RenderLimits;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void osa$limit(E entity, Frustum frustum, double camX, double camY, double camZ,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (!RenderLimits.shouldRender(entity, camX, camY, camZ)) {
            cir.setReturnValue(false);
        }
    }
}
