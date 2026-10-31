package nadiendev.osa.client;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class GpuLeakCleaner {
    private static final int PER_TICK = 20;
    private static final ConcurrentLinkedQueue<AutoCloseable[]> ORPHANS = new ConcurrentLinkedQueue<>();

    private GpuLeakCleaner() {}

    public static void enqueue(AutoCloseable... resources) {
        ORPHANS.add(resources);
    }

    public static void tick() {
        for (int i = 0; i < PER_TICK; i++) {
            AutoCloseable[] resources = ORPHANS.poll();
            if (resources == null) return;
            for (AutoCloseable resource : resources) {
                if (resource == null) continue;
                try {
                    resource.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
