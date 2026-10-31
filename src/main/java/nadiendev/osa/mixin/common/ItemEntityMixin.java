package nadiendev.osa.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.server.EntityRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @WrapOperation(method = "mergeWithNeighbours",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB osa$mergeRadius(AABB box, double x, double y, double z, Operation<AABB> original) {
        if (OsaConfig.serverReady()) {
            double r = OsaConfig.SERVER.items.mergeRadius;
            if (r > 0) {
                return original.call(box, r, Math.max(y, r * 0.5), r);
            }
        }
        return original.call(box, x, y, z);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6000))
    private int osa$lifespan(int vanilla) {
        return EntityRules.itemLifespan(vanilla);
    }
}
