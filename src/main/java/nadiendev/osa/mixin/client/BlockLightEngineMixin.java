package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import nadiendev.osa.client.DynamicLights;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
    @Unique
    private int osa$clientEngine = -1;

    @ModifyReturnValue(method = "getEmission", at = @At("RETURN"))
    private int osa$addDynamicLight(int original, long packedPos, BlockState state) {
        if (this.osa$clientEngine < 0) {
            boolean client = ((LightEngineAccessor) (Object) this).osa$chunkSource().getLevel() instanceof ClientLevel;
            this.osa$clientEngine = client ? 1 : 0;
        }
        if (this.osa$clientEngine == 0) return original;
        return Math.max(original, DynamicLights.levelAt(packedPos));
    }
}
