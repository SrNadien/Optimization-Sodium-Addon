package nadiendev.osa.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.server.EntityRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @WrapOperation(method = "tickNonPassenger",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void osa$throttleTick(Entity entity, Operation<Void> original) {
        if (!EntityRules.shouldSkipTick(entity)) {
            original.call(entity);
        }
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void osa$filterJoin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntityRules.shouldCancelJoin((ServerLevel) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }
}
