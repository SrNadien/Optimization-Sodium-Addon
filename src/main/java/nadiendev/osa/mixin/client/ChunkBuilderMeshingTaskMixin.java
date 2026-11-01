package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.client.BlockRenderGuard;
import nadiendev.osa.config.OsaConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public abstract class ChunkBuilderMeshingTaskMixin {
    @WrapOperation(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            require = 0, remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V"))
    private void osa$skipBrokenBlock(@Coerce Object renderer, @Coerce Object context, @Coerce Object buffers,
                                     Operation<Void> original) {
        if (!OsaConfig.skipBrokenBlocksEnabled()) {
            original.call(renderer, context, buffers);
            return;
        }
        try {
            original.call(renderer, context, buffers);
        } catch (RuntimeException | LinkageError error) {
            BlockRenderGuard.skipped(context, error);
        }
    }
}
