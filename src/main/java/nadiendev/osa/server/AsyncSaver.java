package nadiendev.osa.server;

import nadiendev.osa.OptimizerSodiumAddon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AsyncSaver {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "OSA-AsyncSave");
        t.setDaemon(true);
        return t;
    });

    private AsyncSaver() {}

    public static void submit(Runnable task) {
        EXECUTOR.execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                OptimizerSodiumAddon.LOG.error("[OSA] Fallo el guardado asincrono", t);
            }
        });
    }

    public static void flush() {
        try {
            EXECUTOR.submit(() -> {}).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            OptimizerSodiumAddon.LOG.error("[OSA] No se pudo terminar el guardado asincrono a tiempo", e);
        }
    }
}
