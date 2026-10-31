package nadiendev.osa.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.client.GpuLeakCleaner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {
    @Shadow
    protected int depthBufferId;

    @Shadow
    protected int colorTextureId;

    @Shadow
    public int frameBufferId;

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    protected void finalize() throws Throwable {
        try {
            if (OsaConfig.gpuLeakFixEnabled()
                    && (this.depthBufferId > -1 || this.colorTextureId > -1 || this.frameBufferId > -1)) {
                GpuLeakCleaner.enqueue(this.depthBufferId, this.colorTextureId, this.frameBufferId);
            }
        } catch (Throwable t) {
            OptimizerSodiumAddon.LOG.error("[OSA] Error liberando un RenderTarget descartado", t);
        } finally {
            super.finalize();
        }
    }
}
