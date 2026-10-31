package nadiendev.osa.server;

import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Hilos de trabajo del servidor dedicado (generacion/carga de chunks, compresion, etc).
 *
 * Vanilla usa (nucleos - 1) y deja un nucleo libre para el hilo principal del tick.
 * En modo "auto" se usan todos los nucleos: la carga y generacion de chunks va mas rapido, pero en
 * maquinas con pocos nucleos puede competir con el tick y subir el MSPT. Si pasa eso, usar "vanilla".
 * El tick principal del server sigue siendo de un solo hilo: eso no lo puede cambiar ningun mod, por
 * eso el resto del addon apunta a bajar el trabajo de ese hilo (IA lejana, armor stands, items, comandos).
 *
 * Se lee de un .properties propio porque el executor se crea antes de que NeoForge cargue los .toml.
 * Solo existe en el servidor dedicado (el mixin esta en la seccion "server" del mixins.json).
 */
public final class ServerThreads {
    private static final String FILE = "optimizersodiumaddon-threads.properties";
    private static final Logger LOG = LoggerFactory.getLogger("OSA");
    private static int cached = Integer.MIN_VALUE;

    private ServerThreads() {}

    /** Cantidad de hilos que debe usar el executor de fondo. */
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
            dir = FMLPaths.CONFIGDIR.get();
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
