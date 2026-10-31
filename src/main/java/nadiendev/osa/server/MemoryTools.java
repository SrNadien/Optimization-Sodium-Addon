package nadiendev.osa.server;

import com.sun.management.HotSpotDiagnosticMXBean;
import net.neoforged.fml.loading.FMLPaths;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class MemoryTools {
    private static final long MB = 1024L * 1024L;
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private MemoryTools() {}

    public static String summary() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / MB;
        long total = rt.totalMemory() / MB;
        long max = rt.maxMemory() / MB;
        long collections = 0;
        long millis = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            collections += Math.max(0, gc.getCollectionCount());
            millis += Math.max(0, gc.getCollectionTime());
        }
        return "RAM " + used + " / " + total + " MB (maximo " + max + " MB) | GC: " + collections + " pasadas, " + millis + " ms";
    }

    public static String gc() {
        long before = usedMb();
        System.gc();
        long after = usedMb();
        return "GC ejecutado: " + before + " MB -> " + after + " MB (liberados " + Math.max(0, before - after) + " MB)";
    }

    public static Path heapDump() throws Exception {
        Path dir = FMLPaths.GAMEDIR.get().resolve("heap_dump");
        Files.createDirectories(dir);
        Path file = dir.resolve(LocalDateTime.now().format(STAMP) + ".hprof");
        HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(
                ManagementFactory.getPlatformMBeanServer(), "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        bean.dumpHeap(file.toString(), true);
        return file;
    }

    private static long usedMb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / MB;
    }
}
