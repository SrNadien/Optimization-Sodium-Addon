package nadiendev.osa.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class GpuLeakCleaner {
    private static final int FRAMEBUFFER_TARGET = 36160;
    private static final int PER_TICK = 20;
    private static final ConcurrentLinkedQueue<int[]> ORPHANS = new ConcurrentLinkedQueue<>();

    private GpuLeakCleaner() {}

    public static void enqueue(int depthId, int colorId, int frameBufferId) {
        ORPHANS.add(new int[]{depthId, colorId, frameBufferId});
    }

    public static void tick() {
        for (int i = 0; i < PER_TICK; i++) {
            int[] ids = ORPHANS.poll();
            if (ids == null) return;
            GlStateManager._bindTexture(0);
            GlStateManager._glBindFramebuffer(FRAMEBUFFER_TARGET, 0);
            if (ids[0] > -1) TextureUtil.releaseTextureId(ids[0]);
            if (ids[1] > -1) TextureUtil.releaseTextureId(ids[1]);
            if (ids[2] > -1) GlStateManager._glDeleteFramebuffers(ids[2]);
        }
    }
}
