package nadiendev.osa.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.client.GpuLeakCleaner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {
    @Shadow
    protected GpuTexture colorTexture;

    @Shadow
    protected GpuTextureView colorTextureView;

    @Shadow
    protected GpuTexture depthTexture;

    @Shadow
    protected GpuTextureView depthTextureView;

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    protected void finalize() throws Throwable {
        try {
            if (OsaConfig.gpuLeakFixEnabled()
                    && (this.colorTexture != null || this.colorTextureView != null
                    || this.depthTexture != null || this.depthTextureView != null)) {
                GpuLeakCleaner.enqueue(this.colorTextureView, this.depthTextureView, this.colorTexture, this.depthTexture);
            }
        } catch (Throwable t) {
            OptimizerSodiumAddon.LOG.error("[OSA] Error liberando un RenderTarget descartado", t);
        } finally {
            super.finalize();
        }
    }
}
