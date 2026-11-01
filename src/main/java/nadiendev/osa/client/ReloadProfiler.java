package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReloadProfiler {
    public interface Call<T> {
        CompletableFuture<T> apply(PreparableReloadListener.PreparationBarrier barrier);
    }

    private static final class Entry {
        final String name;
        final String jar;
        volatile long startAt;
        volatile long syncNanos;
        volatile long prepAt;
        volatile long doneAt;
        volatile boolean skipped;

        Entry(PreparableReloadListener listener) {
            this.name = listener.getClass().getName();
            this.jar = jarOf(listener.getClass());
        }

        String label() {
            return jar.isEmpty() ? name : name + " [" + jar + "]";
        }

        String shortLabel() {
            int dot = name.lastIndexOf('.');
            String simple = dot >= 0 ? name.substring(dot + 1) : name;
            return jar.isEmpty() ? simple : simple + " [" + jar + "]";
        }
    }

    private static final class Session {
        final int id;
        final long start;
        final List<Entry> entries = new CopyOnWriteArrayList<>();

        Session(int id, long start) {
            this.id = id;
            this.start = start;
        }

        boolean initial() {
            return id == 1;
        }
    }

    private static final AtomicInteger RELOADS = new AtomicInteger();
    private static volatile Session current;

    private ReloadProfiler() {}

    public static void begin() {
        Session session = new Session(RELOADS.incrementAndGet(), System.nanoTime());
        current = session;
        ReloadTimer.begin(session.id, session.start);
    }

    public static <T> CompletableFuture<T> run(PreparableReloadListener listener, PreparableReloadListener.PreparationBarrier barrier, boolean skippable, Call<T> call) {
        Session session = current;
        if (session == null || !OsaConfig.reloadWatched()) return call.apply(barrier);

        Entry entry = new Entry(listener);
        session.entries.add(entry);

        if (skippable && !session.initial() && skipped(entry.name)) {
            entry.skipped = true;
            entry.prepAt = System.nanoTime();
            entry.doneAt = entry.prepAt;
            return barrier.wait(Unit.INSTANCE).thenApply(unit -> null);
        }

        PreparableReloadListener.PreparationBarrier timed = new PreparableReloadListener.PreparationBarrier() {
            @Override
            public <T> CompletableFuture<T> wait(T value) {
                entry.prepAt = System.nanoTime();
                return barrier.wait(value);
            }
        };
        entry.startAt = System.nanoTime();
        CompletableFuture<T> future = call.apply(timed);
        entry.syncNanos = System.nanoTime() - entry.startAt;
        return future.whenComplete((result, error) -> entry.doneAt = System.nanoTime());
    }

    public static void finish(CompletableFuture<?> done) {
        Session session = current;
        if (session == null) return;
        done.whenComplete((result, error) -> {
            long end = System.nanoTime();
            ReloadTimer.end(end - session.start);
            if (OsaConfig.clientReady() && OsaConfig.CLIENT.reloadProfile.get()) report(session, end);
        });
    }

    private static boolean skipped(String className) {
        for (String prefix : OsaConfig.CLIENT.reloadSkip.get()) {
            if (prefix == null) continue;
            String p = prefix.trim();
            if (!p.isEmpty() && className.startsWith(p)) return true;
        }
        return false;
    }

    private static void report(Session session, long end) {
        List<Entry> entries = new ArrayList<>(session.entries);
        long total = end - session.start;
        int top = OsaConfig.CLIENT.reloadTopCount.get();

        long allPrepared = session.start;
        for (Entry e : entries) allPrepared = Math.max(allPrepared, e.prepAt);

        long[] sync = new long[entries.size()];
        long[] prep = new long[entries.size()];
        long[] apply = new long[entries.size()];
        long previousDone = session.start;
        int skippedCount = 0;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.skipped) skippedCount++;
            sync[i] = e.syncNanos;
            prep[i] = e.prepAt == 0 || e.startAt == 0 ? 0 : Math.max(0, e.prepAt - e.startAt);
            long applyStart = Math.max(previousDone, allPrepared);
            apply[i] = e.doneAt == 0 ? 0 : Math.max(0, e.doneAt - applyStart);
            previousDone = Math.max(previousDone, e.doneAt);
        }

        List<String> lines = new ArrayList<>();
        lines.add("Recarga de recursos #" + session.id + ": " + fmt(total) + " (" + entries.size() + " listeners"
                + (skippedCount > 0 ? ", " + skippedCount + " omitidos" : "") + ")");
        lines.add("  Bloqueo al arrancar la recarga (lo que tarda reload() en volver):");
        addTop(lines, entries, sync, top);
        lines.add("  Preparacion, desde que arranca cada uno hasta que termina de preparar:");
        addTop(lines, entries, prep, top);
        lines.add("  Aplicacion, tiempo propio de cada uno (corren en cadena):");
        addTop(lines, entries, apply, top);
        for (String line : lines) OptimizerSodiumAddon.LOG.info("[OSA] {}", line);

        if (session.initial() || total < OsaConfig.CLIENT.reloadReportSeconds.get() * 1_000_000_000L) return;

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) order.add(i);
        order.sort(Comparator.comparingLong((Integer i) -> Math.max(sync[i], Math.max(prep[i], apply[i]))).reversed());
        StringBuilder slow = new StringBuilder();
        for (int i = 0; i < Math.min(3, order.size()); i++) {
            int idx = order.get(i);
            if (i > 0) slow.append(", ");
            slow.append(entries.get(idx).shortLabel()).append(" (")
                    .append(fmt(Math.max(sync[idx], Math.max(prep[idx], apply[idx])))).append(")");
        }
        String message = "[OSA] Recarga de recursos: " + fmt(total) + ". Los mas lentos: " + slow + ". Detalle en logs/latest.log";
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.level != null) minecraft.gui.getChat().addClientSystemMessage(Component.literal(message));
        });
    }

    private static void addTop(List<String> lines, List<Entry> entries, long[] values, int top) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) order.add(i);
        order.sort(Comparator.comparingLong((Integer i) -> values[i]).reversed());
        for (int i = 0; i < Math.min(top, order.size()); i++) {
            int idx = order.get(i);
            Entry e = entries.get(idx);
            lines.add("    " + (i + 1) + ") " + fmt(values[idx]) + "  " + e.label() + (e.skipped ? "  (omitido)" : ""));
        }
    }

    static String fmt(long nanos) {
        long ms = nanos / 1_000_000L;
        if (ms < 1000) return ms + " ms";
        if (ms < 60_000) return String.format("%.1f s", ms / 1000.0);
        return (ms / 60_000) + " min " + ((ms % 60_000) / 1000) + " s";
    }

    private static String jarOf(Class<?> type) {
        try {
            if (type.getProtectionDomain().getCodeSource() == null) return "";
            String path = type.getProtectionDomain().getCodeSource().getLocation().getPath();
            int bang = path.indexOf('!');
            if (bang >= 0) path = path.substring(0, bang);
            int slash = path.lastIndexOf('/');
            String name = slash >= 0 ? path.substring(slash + 1) : path;
            return name.endsWith(".jar") ? name : "";
        } catch (Throwable t) {
            return "";
        }
    }
}
