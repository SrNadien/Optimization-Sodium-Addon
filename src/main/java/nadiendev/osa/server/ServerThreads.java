package nadiendev.osa.server;

import nadiendev.osa.loader.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ServerThreads {
    private static final String FILE = "optimizersodiumaddon-threads.properties";
    private static final Logger LOG = LoggerFactory.getLogger("OSA");
    private static int cached = Integer.MIN_VALUE;

    private ServerThreads() {}

    public static synchronized int desiredThreads() {
        if (cached != Integer.MIN_VALUE) return cached;
        int cores = Runtime.getRuntime().availableProcessors();
        String mode = readMode();
        int result;
        if (mode.equalsIgnoreCase("vanilla")) {
            result = Math.max(1, cores - 1);
        } else if (mode.equalsIgnoreCase("auto")) {
            result = Math.max(1, cores);
        } else {
            int n;
            try {
                n = Integer.parseInt(mode.trim());
            } catch (NumberFormatException e) {
                n = cores;
            }
            result = Math.max(1, Math.min(n, cores * 2));
        }
        cached = result;
        LOG.info("[OSA] Servidor dedicado: hilos de fondo = {} (modo '{}', nucleos = {})", result, mode, cores);
        return result;
    }

    private static String readMode() {
        Path dir;
        try {
            dir = Platform.configDir();
        } catch (Throwable t) {
            dir = Path.of("config");
        }
        Path file = dir.resolve(FILE);
        Properties p = new Properties();
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    p.load(r);
                }
            } else {
                Files.createDirectories(file.getParent());
                p.setProperty("threads", "auto");
                try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    p.store(w, " Optimizer Sodium Addon - solo servidor dedicado\n"
                            + " threads = auto    -> todos los nucleos (carga de chunks mas rapida)\n"
                            + " threads = vanilla -> nucleos - 1 (usalo si el MSPT sube con auto)\n"
                            + " threads = N       -> numero fijo de hilos\n"
                            + " Requiere reiniciar el servidor.");
                }
            }
        } catch (IOException e) {
            return "auto";
        }
        return p.getProperty("threads", "auto").trim();
    }
}
