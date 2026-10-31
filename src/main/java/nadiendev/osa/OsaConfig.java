package nadiendev.osa;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OsaConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Server, ModConfigSpec> s = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = s.getLeft();
        SERVER_SPEC = s.getRight();
        Pair<Client, ModConfigSpec> c = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = c.getLeft();
        CLIENT_SPEC = c.getRight();
        Pair<Common, ModConfigSpec> m = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = m.getLeft();
        COMMON_SPEC = m.getRight();
    }

    private OsaConfig() {}

    public static boolean serverReady() {
        return SERVER_SPEC.isLoaded();
    }

    public static boolean clientReady() {
        return CLIENT_SPEC.isLoaded();
    }

    public static boolean commonReady() {
        return COMMON_SPEC.isLoaded();
    }

    public static boolean asyncSaveEnabled() {
        return commonReady() && COMMON.asyncSave.get();
    }

    public static boolean craftingRecipeHintEnabled() {
        return !commonReady() || COMMON.craftingRecipeHint.get();
    }

    public static boolean ignoreMoveSpeedLimit() {
        return commonReady() && COMMON.ignoreMoveSpeedLimit.get();
    }

    public static boolean gpuLeakFixEnabled() {
        return !clientReady() || CLIENT.gpuLeakFix.get();
    }

    public static final class Server {
        public final ModConfigSpec.BooleanValue armorStandNoPush;
        public final ModConfigSpec.BooleanValue armorStandThrottle;

        public final ModConfigSpec.DoubleValue itemMergeRadius;
        public final ModConfigSpec.IntValue itemLifespanTicks;

        public final ModConfigSpec.BooleanValue xpMerge;
        public final ModConfigSpec.DoubleValue xpMergeRadius;

        public final ModConfigSpec.BooleanValue aiThrottle;
        public final ModConfigSpec.IntValue aiActivationRange;
        public final ModConfigSpec.IntValue aiTickInterval;
        public final ModConfigSpec.ConfigValue<List<? extends String>> aiWhitelist;

        public final ModConfigSpec.IntValue alertMspt;
        public final ModConfigSpec.IntValue alertSustainSeconds;
        public final ModConfigSpec.IntValue alertCooldownSeconds;
        public final ModConfigSpec.IntValue alertChunkEntities;

        Server(ModConfigSpec.Builder b) {
            b.comment("Armor stands (por ejemplo los cientos que hay en algunas dungeons)").push("armorStands");
            armorStandNoPush = b.comment("No buscar minecarts/entidades para empujar en cada tick. Vanilla lo hace en todos los armor stands, aunque esten quietos.")
                    .define("disablePush", true);
            armorStandThrottle = b.comment("Los armor stands lejos de cualquier jugador tickean igual que los mobs lejanos (ver aiThrottle).")
                    .define("throttleWhenFar", true);
            b.pop();

            b.comment("Items en el suelo").push("items");
            itemMergeRadius = b.comment("Radio (bloques) para juntar items iguales en un solo stack. Vanilla = 0.5. 0 = no tocar.")
                    .defineInRange("mergeRadius", 1.5, 0.0, 8.0);
            itemLifespanTicks = b.comment("Ticks hasta que desaparece un item tirado (vanilla 6000 = 5 min). Solo se aplica a items con la duracion vanilla.")
                    .defineInRange("lifespanTicks", 3600, 200, 6000);
            b.pop();

            b.comment("Orbes de experiencia").push("xp");
            xpMerge = b.comment("Juntar orbes nuevos con orbes cercanos (aunque tengan distinto valor).")
                    .define("merge", true);
            xpMergeRadius = b.defineInRange("mergeRadius", 2.0, 0.5, 8.0);
            b.pop();

            b.comment("IA de mobs lejanos").push("ai");
            aiThrottle = b.comment("Los mobs sin jugadores cerca solo tickean cada 'tickInterval' ticks. Ahorra mucho TPS con muchos mobs y granjas.",
                            "OJO: las granjas con chunks cargados sin nadie cerca van mas lentas. Agrega esos mobs a la whitelist si molesta.")
                    .define("throttleFarMobs", true);
            aiActivationRange = b.comment("Distancia (bloques) a un jugador para tickear normal.")
                    .defineInRange("activationRange", 48, 16, 256);
            aiTickInterval = b.comment("Cada cuantos ticks se procesa un mob lejano (4 = 1/4 del costo).")
                    .defineInRange("tickInterval", 4, 2, 20);
            aiWhitelist = b.comment("Entidades que nunca se frenan. Acepta 'modid:entidad' o 'modid:*'.")
                    .defineListAllowEmpty("whitelist", List.of("minecraft:wither", "minecraft:ender_dragon", "minecraft:villager"),
                            () -> "", o -> o instanceof String);
            b.pop();

            b.comment("Limites de las alertas de lag (el webhook va en config/optimizersodiumaddon-common.toml)").push("alerts");
            alertMspt = b.comment("Avisar cuando el tick promedio pase estos milisegundos (50 = el server ya no llega a 20 TPS).")
                    .defineInRange("msptThreshold", 50, 20, 1000);
            alertSustainSeconds = b.comment("Segundos seguidos por encima del limite antes de avisar.")
                    .defineInRange("sustainSeconds", 10, 1, 600);
            alertCooldownSeconds = b.comment("Tiempo minimo entre dos alertas.")
                    .defineInRange("cooldownSeconds", 300, 30, 86400);
            alertChunkEntities = b.comment("Avisar si un solo chunk tiene mas de estas entidades (0 = desactivado).")
                    .defineInRange("chunkEntityThreshold", 300, 0, 100000);
            b.pop();
        }
    }

    public static final class Common {
        public final ModConfigSpec.BooleanValue alertsEnabled;
        public final ModConfigSpec.ConfigValue<String> webhookUrl;
        public final ModConfigSpec.ConfigValue<String> serverName;
        public final ModConfigSpec.BooleanValue asyncSave;
        public final ModConfigSpec.BooleanValue craftingRecipeHint;
        public final ModConfigSpec.BooleanValue ignoreMoveSpeedLimit;

        Common(ModConfigSpec.Builder b) {
            b.comment("Alertas de lag por Discord. Solo lo lee el servidor; nunca se manda a los jugadores.").push("discord");
            alertsEnabled = b.define("enabled", false);
            webhookUrl = b.comment("URL del webhook de Discord (Ajustes del canal > Integraciones > Webhooks).")
                    .define("webhookUrl", "");
            serverName = b.define("serverName", "Endless World");
            b.pop();

            b.comment("Rendimiento del servidor (se leen al arrancar, no hace falta que el mundo este cargado)").push("performance");
            asyncSave = b.comment("Escribir los datos guardados (SavedData) y level.dat en otro hilo, para que el guardado no congele el tick.")
                    .define("asyncSave", true);
            craftingRecipeHint = b.comment("Al cambiar la grilla de crafteo, probar primero la ultima receta usada antes de buscar entre todas. Con miles de recetas ahorra mucho.")
                    .define("craftingRecipeHint", true);
            ignoreMoveSpeedLimit = b.comment("Quitar el aviso y la correccion de Moved too quickly. Solo para servidores con teletransportes o velocidades muy altas. Desactiva una proteccion anti trampas.")
                    .define("ignoreMoveSpeedLimit", false);
            b.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.IntValue armorStandMax;
        public final ModConfigSpec.IntValue armorStandDistance;
        public final ModConfigSpec.BooleanValue armorStandHideInvisibleEmpty;

        public final ModConfigSpec.IntValue itemMax;
        public final ModConfigSpec.IntValue itemDistance;

        public final ModConfigSpec.IntValue xpMax;
        public final ModConfigSpec.IntValue xpDistance;

        public final ModConfigSpec.IntValue particleMaxTotal;
        public final ModConfigSpec.IntValue particleMaxPerTick;
        public final ModConfigSpec.IntValue particleDistance;

        public final ModConfigSpec.BooleanValue gpuLeakFix;
        public final ModConfigSpec.BooleanValue borderlessF11;
        public final ModConfigSpec.BooleanValue terrainFog;
        public final ModConfigSpec.BooleanValue waterFog;
        public final ModConfigSpec.BooleanValue lavaFog;
        public final ModConfigSpec.BooleanValue powderSnowFog;
        public final ModConfigSpec.BooleanValue waterOverlay;
        public final ModConfigSpec.BooleanValue fireOverlay;

        Client(ModConfigSpec.Builder b) {
            b.comment("Armor stands").push("armorStands");
            armorStandMax = b.comment("Maximo de armor stands dibujados a la vez (los mas cercanos). 0 = sin limite.")
                    .defineInRange("maxRendered", 32, 0, 4096);
            armorStandDistance = b.comment("Distancia maxima (bloques) para dibujar armor stands.")
                    .defineInRange("renderDistance", 24, 4, 256);
            armorStandHideInvisibleEmpty = b.comment("No procesar armor stands invisibles sin nada equipado (no se ven igual).")
                    .define("hideInvisibleEmpty", true);
            b.pop();

            b.comment("Items en el suelo").push("items");
            itemMax = b.comment("Maximo de items del suelo dibujados (los mas cercanos). 0 = sin limite.")
                    .defineInRange("maxRendered", 150, 0, 4096);
            itemDistance = b.defineInRange("renderDistance", 32, 4, 256);
            b.pop();

            b.comment("Orbes de experiencia").push("xp");
            xpMax = b.defineInRange("maxRendered", 60, 0, 4096);
            xpDistance = b.defineInRange("renderDistance", 24, 4, 256);
            b.pop();

            b.comment("Particulas").push("particles");
            particleMaxTotal = b.comment("Maximo de particulas vivas a la vez (vanilla permite 16384 por tipo). 0 = sin limite.")
                    .defineInRange("maxTotal", 3000, 0, 65536);
            particleMaxPerTick = b.comment("Maximo de particulas nuevas por tick. 0 = sin limite.")
                    .defineInRange("maxPerTick", 400, 0, 65536);
            particleDistance = b.comment("No crear particulas mas lejos que esto (bloques).")
                    .defineInRange("maxDistance", 48, 8, 256);
            b.pop();

            b.comment("Memoria de video").push("gpu");
            gpuLeakFix = b.comment("Liberar las texturas y framebuffers de la GPU de los RenderTarget que el recolector de basura descarta sin cerrarlos (fuga de memoria de video).")
                    .define("leakFix", true);
            b.pop();

            b.comment("Ventana").push("window");
            borderlessF11 = b.comment("F11 alterna entre ventana y pantalla completa sin bordes. Si ya estas en pantalla completa exclusiva, F11 vuelve a ventana como siempre.")
                    .define("borderlessF11", true);
            b.pop();

            b.comment("Niebla (la de Ceguera y Oscuridad nunca se toca)").push("fog");
            terrainFog = b.comment("Niebla de distancia del terreno, incluida la espesa del Nether.").define("terrain", true);
            waterFog = b.define("water", true);
            lavaFog = b.define("lava", true);
            powderSnowFog = b.define("powderSnow", true);
            b.pop();

            b.comment("Superposiciones de pantalla (la de estar dentro de un bloque solido nunca se toca)").push("overlays");
            waterOverlay = b.comment("Textura de agua sobre la vista al nadar.").define("water", true);
            fireOverlay = b.comment("Llamas sobre la vista al arder.").define("fire", true);
            b.pop();
        }
    }

    private static volatile Set<String> aiWhitelistExact;
    private static volatile Set<String> aiWhitelistNamespaces;

    static void invalidateCaches() {
        aiWhitelistExact = null;
        aiWhitelistNamespaces = null;
    }

    public static boolean isAiWhitelisted(Identifier id) {
        if (id == null) return false;
        Set<String> exact = aiWhitelistExact;
        Set<String> ns = aiWhitelistNamespaces;
        if (exact == null || ns == null) {
            exact = new HashSet<>();
            ns = new HashSet<>();
            for (String raw : SERVER.aiWhitelist.get()) {
                String s = raw.trim();
                if (s.endsWith(":*")) ns.add(s.substring(0, s.length() - 2));
                else if (!s.isEmpty()) exact.add(s);
            }
            aiWhitelistExact = exact;
            aiWhitelistNamespaces = ns;
        }
        return exact.contains(id.toString()) || ns.contains(id.getNamespace());
    }
}
