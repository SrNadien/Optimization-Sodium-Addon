package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.client.BlockRenderGuard;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public abstract class ChunkBuilderMeshingTaskMixin {
    @WrapOperation(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"))
    private void osa$skipBrokenBlock(@Coerce Object renderer, BakedModel model, BlockState state, BlockPos pos, BlockPos origin,
                                     Operation<Void> original) {
        if (!OsaConfig.skipBrokenBlocksEnabled()) {
            original.call(renderer, model, state, pos, origin);
            return;
        }
        try {
            original.call(renderer, model, state, pos, origin);
        } catch (RuntimeException | LinkageError error) {
            BlockRenderGuard.skipped(state, pos, error);
        }
    }
}
