package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class BlockRenderGuard {
    private static final Set<ResourceLocation> REPORTED = ConcurrentHashMap.newKeySet();
    private static final AtomicLong SKIPPED = new AtomicLong();

    private BlockRenderGuard() {}

    public static void skipped(BlockState state, BlockPos pos, Throwable error) {
        SKIPPED.incrementAndGet();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (REPORTED.add(id)) {
            OptimizerSodiumAddon.LOG.error("[OSA] El bloque {} ({}) fallo al dibujarse en {}; se omite y el juego sigue. Es un fallo del mod de ese bloque.", id, state, pos.toShortString(), error);
        }
    }

    public static long skippedCount() {
        return SKIPPED.get();
    }
}
