package nadiendev.osa;

import nadiendev.osa.client.DynamicLightSettings;
import nadiendev.osa.client.FastMath;
import nadiendev.osa.client.TuneProfile;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    public static boolean fastMathConfigured() {
        return clientReady() && CLIENT.fastMath.get();
    }

    public static boolean entityCullingEnabled() {
        return clientReady() && CLIENT.entityCulling.get();
    }

    public static boolean startupTimerEnabled() {
        return clientReady() && CLIENT.startupTimer.get();
    }

    public static boolean hideReloadOverlay() {
        return clientReady() && CLIENT.reloadHideOverlay.get();
    }

    public static boolean betterGrassEnabled() {
        return clientReady() && CLIENT.betterGrass.get();
    }

    public static List<Identifier> betterGrassBlocks() {
        List<? extends String> names = clientReady()
                ? CLIENT.betterGrassBlocks.get()
                : List.of("minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium");
        return names.stream().map(Identifier::tryParse).filter(Objects::nonNull).toList();
    }

    public static boolean skipBrokenBlocksEnabled() {
        return clientReady() && CLIENT.skipBrokenBlocks.get();
    }

    public static boolean coalesceTintsEnabled() {
        return clientReady() && CLIENT.coalesceTints.get();
    }

    public static int tintIntervalTicks() {
        return clientReady() ? CLIENT.tintIntervalTicks.get() : 1;
    }

    public static boolean reloadCounterEnabled() {
        return clientReady() && CLIENT.reloadCounter.get();
    }

    public static boolean reloadWatched() {
        return clientReady() && (CLIENT.reloadProfile.get() || !CLIENT.reloadSkip.get().isEmpty());
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

        public final ModConfigSpec.BooleanValue startupTimer;
        public final ModConfigSpec.BooleanValue reloadProfile;
        public final ModConfigSpec.BooleanValue entityCulling;
        public final ModConfigSpec.BooleanValue cullDisplays;
        public final ModConfigSpec.BooleanValue cullArmorStands;
        public final ModConfigSpec.IntValue cullMaxDistance;
        public final ModConfigSpec.IntValue cullTextMaxDistance;
        public final ModConfigSpec.IntValue cullNeverWithin;
        public final ModConfigSpec.DoubleValue cullFrustumMargin;
        public final ModConfigSpec.BooleanValue cullOcclusion;
        public final ModConfigSpec.IntValue cullOcclusionMinDistance;
        public final ModConfigSpec.IntValue cullOcclusionRecheckMs;
        public final ModConfigSpec.IntValue cullOcclusionBudget;
        public final ModConfigSpec.BooleanValue betterGrass;
        public final ModConfigSpec.ConfigValue<List<? extends String>> betterGrassBlocks;
        public final ModConfigSpec.BooleanValue skipBrokenBlocks;
        public final ModConfigSpec.BooleanValue coalesceTints;
        public final ModConfigSpec.IntValue tintIntervalTicks;
        public final ModConfigSpec.BooleanValue reloadHideOverlay;
        public final ModConfigSpec.BooleanValue reloadCounter;
        public final ModConfigSpec.IntValue reloadReportSeconds;
        public final ModConfigSpec.IntValue reloadTopCount;
        public final ModConfigSpec.ConfigValue<List<? extends String>> reloadSkip;

        public final ModConfigSpec.BooleanValue tuneEnabled;
        public final ModConfigSpec.EnumValue<TuneProfile> tuneProfile;
        public final ModConfigSpec.BooleanValue tuneApplied;
        public final ModConfigSpec.BooleanValue tuneGraphics;
        public final ModConfigSpec.BooleanValue tuneSodium;
        public final ModConfigSpec.BooleanValue tuneChloride;
        public final ModConfigSpec.BooleanValue tuneAddon;
        public final ModConfigSpec.IntValue tuneRenderDistanceLow;
        public final ModConfigSpec.IntValue tuneRenderDistanceMedium;
        public final ModConfigSpec.IntValue tuneRenderDistanceHigh;
        public final ModConfigSpec.BooleanValue fastMath;

        public final ModConfigSpec.BooleanValue dynamicLights;
        public final ModConfigSpec.IntValue dynamicLightsMaxSources;
        public final ModConfigSpec.IntValue dynamicLightsDistance;
        public final ModConfigSpec.IntValue dynamicLightsInterval;
        public final ModConfigSpec.BooleanValue dynamicLightsBurning;
        public final ModConfigSpec.ConfigValue<List<? extends String>> dynamicLightsItems;
        public final ModConfigSpec.ConfigValue<List<? extends String>> dynamicLightsEntities;

        Client(ModConfigSpec.Builder b) {
            b.comment("Armor stands").push("armorStands");
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

            b.comment("Recarga de recursos (resource packs, idioma, F3+T)").push("reload");
            reloadProfile = b.comment("Medir cuanto tarda cada listener en cada recarga y escribir los mas lentos en el log y en el chat. Sirve para saber que mod hace lenta la recarga.")
                    .define("profile", true);
            reloadHideOverlay = b.comment("No mostrar la pantalla de carga de Mojang (fondo, logo y barra) al recargar recursos: resource packs, idioma, F3+T. La del arranque del juego se ve igual.")
                    .define("hideOverlay", true);
            reloadCounter = b.comment("Mostrar en una esquina de la pantalla el tiempo que lleva la recarga y, al terminar, el tiempo total durante unos segundos.")
                    .define("counter", true);
            reloadReportSeconds = b.comment("Solo avisar en el chat si la recarga tardo al menos estos segundos. El log siempre lo registra.")
                    .defineInRange("reportSeconds", 10, 0, 3600);
            reloadTopCount = b.comment("Cuantos listeners mostrar en cada lista del informe.")
                    .defineInRange("topCount", 8, 1, 30);
            reloadSkip = b.comment("Listeners que NO se recargan en las recargas posteriores a la primera (resource packs, F3+T). Se compara por el comienzo del nombre de la clase, tal como sale en el informe.",
                            "Empieza vacia. Saltar un listener que el juego necesita rompe texturas, modelos o sonidos: usalo solo para los que el informe marque como lentos y no dependan de los recursos.")
                    .defineListAllowEmpty("skipListeners", List.of(), () -> "", o -> o instanceof String);
            b.pop();

            b.comment("Arranque del juego").push("startup");
            startupTimer = b.comment("Mostrar en la pantalla de titulo cuanto tardo el juego en arrancar, durante unos segundos. El tiempo siempre se escribe en el log.")
                    .define("timer", true);
            b.pop();

            b.comment("Terreno").push("terrain");
            betterGrass = b.comment("Cesped y nieve mejorados: si el bloque de al lado y un nivel mas abajo es del mismo tipo, el lateral usa la textura de arriba y los escalones se ven continuos.")
                    .define("betterGrass", false);
            betterGrassBlocks = b.comment("Bloques a los que se aplica. Un cambio en esta lista se nota al recargar recursos (F3+T).")
                    .defineListAllowEmpty("betterGrassBlocks", List.of("minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium"), () -> "", o -> o instanceof String);
            b.pop();

            b.comment("Culling de entidades que no ves: modelos hechos con entidades Display y armor stands. Solo evita dibujarlas.").push("entityCulling");
            entityCulling = b.comment("Activa el culling. Apagado por defecto: las entidades que no se ven dejan de dibujarse y pueden aparecer con un pequeno retraso al girar.")
                    .define("enabled", false);
            cullDisplays = b.comment("Aplicar a entidades Display (item, bloque y texto), como las de los modelos de ModelEngine.")
                    .define("displays", true);
            cullArmorStands = b.comment("Aplicar el culling a los armor stands por distancia y por muros, sin limite por cantidad (los que tienen nombre visible nunca se descartan).")
                    .define("armorStands", true);
            cullMaxDistance = b.comment("Distancia maxima (bloques) a la que se dibuja una entidad Display o un armor stand. 0 = sin limite.")
                    .defineInRange("maxDistance", 64, 0, 512);
            cullTextMaxDistance = b.comment("Distancia maxima (bloques) para entidades Display de texto. 0 = sin limite.")
                    .defineInRange("textMaxDistance", 32, 0, 512);
            cullNeverWithin = b.comment("Dentro de esta distancia (bloques) nunca se descarta nada.")
                    .defineInRange("neverCullWithin", 12, 0, 128);
            cullFrustumMargin = b.comment("Margen (bloques) alrededor de una Display al comprobar si esta dentro de la camara. Mas margen = menos descartes y menos pop-in.")
                    .defineInRange("frustumMargin", 2.0, 0.0, 16.0);
            cullOcclusion = b.comment("Descartar las entidades que quedan tapadas por bloques opacos entre la camara y la entidad.")
                    .define("occlusion", true);
            cullOcclusionMinDistance = b.comment("Distancia minima (bloques) para comprobar muros; mas cerca no se comprueba.")
                    .defineInRange("occlusionMinDistance", 6, 0, 128);
            cullOcclusionRecheckMs = b.comment("Cada cuantos milisegundos se vuelve a comprobar si una entidad esta tapada.")
                    .defineInRange("occlusionRecheckMs", 200, 50, 5000);
            cullOcclusionBudget = b.comment("Maximo de comprobaciones de muros cada 50 ms. Reparte el coste entre fotogramas.")
                    .defineInRange("occlusionBudget", 150, 10, 2000);
            b.pop();

            b.comment("Estabilidad y tirones al dibujar").push("stability");
            skipBrokenBlocks = b.comment("Si un bloque de un mod falla al dibujarse en un chunk, se omite solo ese bloque y se avisa en el log, en vez de cerrar el juego.")
                    .define("skipBrokenBlocks", true);
            coalesceTints = b.comment("Muchos mods limpian la cache de colores de bioma en cada tick y eso provoca tirones. Con esto se agrupan en una sola limpieza cada pocos ticks.")
                    .define("coalesceTints", true);
            tintIntervalTicks = b.comment("Cada cuantos ticks como maximo se hace la limpieza de colores de bioma.")
                    .defineInRange("tintIntervalTicks", 4, 1, 40);
            b.pop();

            b.comment("Ajuste automatico segun tu PC. Solo toca la configuracion grafica.").push("autoTune");
            tuneEnabled = b.comment("Apagado por defecto. Si lo dejas en true antes del primer arranque, detecta el hardware y completa solo lo que no cambiaste, sin hacer el juego mas pesado.",
                            "Encenderlo a mano desde las opciones (o usar /osatune) configura todo con el perfil.")
                    .define("enabled", false);
            tuneProfile = b.comment("AUTO elige segun el hardware. LOW, MEDIUM o HIGH fuerzan ese perfil.")
                    .defineEnum("profile", TuneProfile.AUTO);
            tuneApplied = b.comment("Lo marca el mod al aplicar el perfil. Ponelo en false para que se aplique otra vez al proximo arranque.")
                    .define("applied", false);
            tuneGraphics = b.comment("Ajustar las opciones graficas de Minecraft (distancia de dibujado, nubes, particulas...). Se guarda una copia en options.txt.osa-backup.")
                    .define("graphics", true);
            tuneSodium = b.comment("Ajustar Sodium (sodium-options.json). Se aplica al reiniciar el juego. Copia en sodium-options.json.osa-backup.")
                    .define("sodium", true);
            tuneChloride = b.comment("Ajustar Chloride si esta instalado (niebla, particulas, culling).")
                    .define("chloride", true);
            tuneAddon = b.comment("Ajustar los limites de este mod (armor stands, items, orbes, particulas).")
                    .define("addon", true);
            tuneRenderDistanceLow = b.comment("Distancia de dibujado (chunks) del perfil LOW.")
                    .defineInRange("renderDistanceLow", 2, 2, 32);
            tuneRenderDistanceMedium = b.comment("Distancia de dibujado (chunks) del perfil MEDIUM.")
                    .defineInRange("renderDistanceMedium", 10, 2, 32);
            tuneRenderDistanceHigh = b.comment("Distancia de dibujado (chunks) del perfil HIGH.")
                    .defineInRange("renderDistanceHigh", 16, 2, 32);
            b.pop();

            b.comment("Luces dinamicas: lo que llevas en la mano, lo que tiras al suelo y los mobs que brillan alumbran a su alrededor. Solo en el cliente.").push("dynamicLights");
            dynamicLights = b.comment("Activar las luces dinamicas. Cuesta un poco de rendimiento porque hay que recalcular la luz cuando algo se mueve.")
                    .define("enabled", false);
            dynamicLightsMaxSources = b.comment("Maximo de fuentes de luz dinamicas a la vez (las mas cercanas a la camara).")
                    .defineInRange("maxSources", 24, 1, 256);
            dynamicLightsDistance = b.comment("Distancia maxima (bloques) a la camara para que una fuente alumbre.")
                    .defineInRange("maxDistance", 32, 8, 128);
            dynamicLightsInterval = b.comment("Cada cuantos ticks se recalculan las fuentes. Mas alto cuesta menos pero la luz se actualiza mas tarde.")
                    .defineInRange("updateTicks", 2, 1, 20);
            dynamicLightsBurning = b.comment("Las entidades en llamas alumbran con luz 15.")
                    .define("burningEntities", true);
            dynamicLightsItems = b.comment("Luz de items que no son bloques (los bloques como antorchas, faroles o piedra luminosa ya usan la suya). Formato modid:item=nivel.")
                    .defineListAllowEmpty("items", List.of("minecraft:lava_bucket=15", "minecraft:blaze_rod=10"), () -> "", o -> o instanceof String);
            dynamicLightsEntities = b.comment("Luz de tipos de entidad. Formato modid:entidad=nivel.")
                    .defineListAllowEmpty("entities", List.of("minecraft:blaze=10", "minecraft:fireball=12", "minecraft:small_fireball=12", "minecraft:magma_cube=8"), () -> "", o -> o instanceof String);
            b.pop();

            b.comment("Matematicas").push("math");
            fastMath = b.comment("Seno y coseno con una tabla chica interpolada, solo al dibujar. Es mas preciso que el de vanilla, asi que no cambia nada visible. La ganancia es muy chica.")
                    .define("fastMath", false);
            b.pop();
        }
    }

    private static volatile Set<String> aiWhitelistExact;
    private static volatile Set<String> aiWhitelistNamespaces;

    static void invalidateCaches() {
        aiWhitelistExact = null;
        aiWhitelistNamespaces = null;
        FastMath.setEnabled(fastMathConfigured());
        DynamicLightSettings.refresh();
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
