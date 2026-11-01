package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class BlockRenderGuard {
    private static final Set<ResourceLocation> REPORTED = ConcurrentHashMap.newKeySet();
    private static final AtomicLong SKIPPED = new AtomicLong();

    private static volatile Method stateGetter;
    private static volatile Method posGetter;

    private BlockRenderGuard() {}

    public static void skipped(Object context, Throwable error) {
        SKIPPED.incrementAndGet();
        BlockState state = read(context, "state", BlockState.class);
        BlockPos pos = read(context, "pos", BlockPos.class);
        if (state == null || pos == null) {
            OptimizerSodiumAddon.LOG.error("[OSA] Un bloque fallo al dibujarse; se omite y el juego sigue. Es un fallo del mod de ese bloque.", error);
            return;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (REPORTED.add(id)) {
            OptimizerSodiumAddon.LOG.error("[OSA] El bloque {} ({}) fallo al dibujarse en {}; se omite y el juego sigue. Es un fallo del mod de ese bloque.", id, state, pos.toShortString(), error);
        }
    }

    public static long skippedCount() {
        return SKIPPED.get();
    }

    private static <T> T read(Object context, String name, Class<T> type) {
        try {
            Method method = name.equals("state") ? stateGetter : posGetter;
            if (method == null) {
                method = context.getClass().getMethod(name);
                if (name.equals("state")) stateGetter = method;
                else posGetter = method;
            }
            return type.cast(method.invoke(context));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
