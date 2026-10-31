package nadiendev.osa.server;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.server.MinecraftServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manda un aviso al webhook de Discord cuando el servidor se laggea.
 * Todo el envio es asincronico: nunca frena el tick del servidor.
 */
public final class LagAlerts {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private static int secondsOver = 0;
    private static long lastAlertMs = 0;
    private static long lastChunkCheckTick = 0;

    private LagAlerts() {}

    /** Se llama al arrancar cada servidor/mundo para no arrastrar contadores del anterior. */
    static void reset() {
        secondsOver = 0;
        lastAlertMs = 0;
        lastChunkCheckTick = 0;
    }

    public static double mspt(MinecraftServer server) {
        return server.getAverageTickTimeNanos() / 1_000_000.0;
    }

    public static double tps(double mspt) {
        return Math.min(20.0, 1000.0 / Math.max(mspt, 1.0));
    }

    static void tick(MinecraftServer server) {
        if (!OsaConfig.serverReady() || !OsaConfig.commonReady() || !OsaConfig.COMMON.alertsEnabled.get()) return;
        String url = OsaConfig.COMMON.webhookUrl.get();
        if (url == null || url.isBlank()) return;

        int tickCount = server.getTickCount();
        if (tickCount % 20 != 0) return;

        double mspt = mspt(server);
        if (mspt >= OsaConfig.SERVER.alertMspt.get()) secondsOver++;
        else secondsOver = 0;

        long now = System.currentTimeMillis();
        boolean cooldownOk = now - lastAlertMs >= OsaConfig.SERVER.alertCooldownSeconds.get() * 1000L;
        if (!cooldownOk) return;

        if (secondsOver >= OsaConfig.SERVER.alertSustainSeconds.get()) {
            lastAlertMs = now;
            secondsOver = 0;
            List<ChunkReport.ChunkStats> top = ChunkReport.top(server, 5);
            send(url, buildLagMessage(mspt, top, "El servidor esta laggeado"), 0xE74C3C);
            return;
        }

        int chunkLimit = OsaConfig.SERVER.alertChunkEntities.get();
        if (tickCount < lastChunkCheckTick) lastChunkCheckTick = 0;
        if (chunkLimit > 0 && tickCount - lastChunkCheckTick >= 20 * 60) {
            lastChunkCheckTick = tickCount;
            List<ChunkReport.ChunkStats> top = ChunkReport.top(server, 5);
            if (!top.isEmpty() && top.get(0).entities >= chunkLimit) {
                lastAlertMs = now;
                send(url, buildLagMessage(mspt, top, "Chunk con demasiadas entidades"), 0xF39C12);
            }
        }
    }

    public static CompletableFuture<Integer> sendTest(MinecraftServer server) {
        if (!OsaConfig.commonReady()) return CompletableFuture.completedFuture(-1);
        String url = OsaConfig.COMMON.webhookUrl.get();
        if (url == null || url.isBlank()) return CompletableFuture.completedFuture(-1);
        double mspt = mspt(server);
        return send(url, buildLagMessage(mspt, ChunkReport.top(server, 5), "Prueba de alerta"), 0x3498DB);
    }

    private static String buildLagMessage(double mspt, List<ChunkReport.ChunkStats> top, String title) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("**MSPT:** %.1f ms  |  **TPS:** %.1f%n%n", mspt, tps(mspt)));
        desc.append("**Chunks mas pesados:**\n");
        if (top.isEmpty()) desc.append("(sin datos)\n");
        for (ChunkReport.ChunkStats s : top) {
            desc.append(String.format("- `%s` %s — %d entidades, %d block entities — mas comun: %s%n",
                    s.dimension, s.coords(), s.entities, s.tickingBlockEntities, s.topType()));
        }
        desc.append("\nUsa `/osa top` y `/osa kill` en el juego para limpiar.");
        return "{\"username\":" + json(OsaConfig.COMMON.serverName.get() + " - Lag")
                + ",\"embeds\":[{\"title\":" + json(title)
                + ",\"description\":" + json(desc.toString())
                + ",\"color\":%COLOR%}]}";
    }

    private static CompletableFuture<Integer> send(String url, String body, int color) {
        String payload = body.replace("%COLOR%", Integer.toString(color));
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "OptimizerSodiumAddon")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            return HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .thenApply(HttpResponse::statusCode)
                    .exceptionally(t -> {
                        OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo mandar la alerta a Discord: {}", t.toString());
                        return -1;
                    });
        } catch (IllegalArgumentException e) {
            OptimizerSodiumAddon.LOG.warn("[OSA] URL de webhook invalida: {}", url);
            return CompletableFuture.completedFuture(-1);
        }
    }

    private static String json(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }
}
