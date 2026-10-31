package nadiendev.osa.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.OsaConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Agranda el radio en el que un item busca otros items iguales para juntarse (vanilla 0.5). */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @WrapOperation(method = "mergeWithNeighbours",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB osa$mergeRadius(AABB box, double x, double y, double z, Operation<AABB> original) {
        if (OsaConfig.serverReady()) {
            double r = OsaConfig.SERVER.itemMergeRadius.get();
            if (r > 0) {
                return original.call(box, r, Math.max(y, r * 0.5), r);
            }
        }
        return original.call(box, x, y, z);
    }
}
