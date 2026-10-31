package nadiendev.osa.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.mixin.common.LevelTickersAccessor;
import nadiendev.osa.server.ChunkReport;
import nadiendev.osa.server.EntityRules;
import nadiendev.osa.server.LagAlerts;
import nadiendev.osa.server.MemoryTools;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class OsaCommands {
    private static final int MAX_BE_RADIUS = 128;

    private static final SuggestionProvider<CommandSourceStack> ENTITY_TYPES =
            (ctx, b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b);
    private static final SuggestionProvider<CommandSourceStack> BLOCK_ENTITY_TYPES =
            (ctx, b) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet(), b);

    private OsaCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("osa").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        root.then(Commands.literal("stats").executes(OsaCommands::stats));
        root.then(Commands.literal("tps").executes(OsaCommands::tps));
        root.then(Commands.literal("top")
                .executes(c -> top(c, 10))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 50))
                        .executes(c -> top(c, IntegerArgumentType.getInteger(c, "count")))));

        LiteralArgumentBuilder<CommandSourceStack> kill = Commands.literal("kill").requires(Commands.hasPermission(Commands.LEVEL_ADMINS));
        addKillFilter(kill, "items", e -> e instanceof ItemEntity);
        addKillFilter(kill, "xp", e -> e instanceof ExperienceOrb);
        addKillFilter(kill, "armorstands", e -> e instanceof ArmorStand);
        addKillFilter(kill, "hostile", e -> e instanceof Enemy && safeMob(e));
        addKillFilter(kill, "passive", e -> e instanceof Animal && safeMob(e));
        addKillFilter(kill, "mobs", e -> e instanceof Mob && safeMob(e));
        kill.then(Commands.literal("type")
                .then(Commands.argument("entity", IdentifierArgument.id()).suggests(ENTITY_TYPES)
                        .executes(c -> killType(c, -1))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 10000))
                                .executes(c -> killType(c, IntegerArgumentType.getInteger(c, "radius"))))));
        root.then(kill);

        root.then(Commands.literal("blockentities")
                .then(Commands.literal("list")
                        .executes(c -> beList(c, -1))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_BE_RADIUS))
                                .executes(c -> beList(c, IntegerArgumentType.getInteger(c, "radius")))))
                .then(Commands.literal("remove").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("type", IdentifierArgument.id()).suggests(BLOCK_ENTITY_TYPES)
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_BE_RADIUS))
                                        .executes(c -> {
                                            c.getSource().sendFailure(Component.literal(
                                                    "Esto BORRA los bloques. Repeti el comando terminando en 'confirm'."));
                                            return 0;
                                        })
                                        .then(Commands.literal("confirm").executes(OsaCommands::beRemove))))));

        root.then(Commands.literal("alert").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("test").executes(OsaCommands::alertTest)));

        root.then(Commands.literal("reload").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)).executes(OsaCommands::reload));

        root.then(Commands.literal("memory")
                .executes(c -> memory(c, MemoryTools.summary()))
                .then(Commands.literal("gc").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(c -> memory(c, MemoryTools.gc())))
                .then(Commands.literal("heapdump").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(OsaCommands::heapDump)));

        d.register(root);
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        OsaConfig.load();
        c.getSource().sendSuccess(() -> Component.literal("[OSA] Configuracion recargada desde " + OsaConfig.file().getFileName()).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int memory(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int heapDump(CommandContext<CommandSourceStack> c) {
        try {
            String file = MemoryTools.heapDump().toString();
            c.getSource().sendSuccess(() -> Component.literal("Heap dump guardado en " + file), true);
            return 1;
        } catch (Exception e) {
            c.getSource().sendFailure(Component.literal("No se pudo crear el heap dump: " + e.getMessage()));
            return 0;
        }
    }

    private static int stats(CommandContext<CommandSourceStack> c) {
        MinecraftServer server = c.getSource().getServer();
        double mspt = LagAlerts.mspt(server);
        send(c, header(String.format("MSPT %.1f ms | TPS %.1f", mspt, LagAlerts.tps(mspt))));
        for (ServerLevel level : server.getAllLevels()) {
            int total = 0, items = 0, xp = 0, mobs = 0, stands = 0;
            for (Entity e : level.getAllEntities()) {
                if (e == null) continue;
                total++;
                if (e instanceof ItemEntity) items++;
                else if (e instanceof ExperienceOrb) xp++;
                else if (e instanceof ArmorStand) stands++;
                else if (e instanceof Mob) mobs++;
            }
            int bes = ChunkReport.tickingBlockEntities(level);
            int chunks = level.getChunkSource().getLoadedChunksCount();
            send(c, Component.literal(String.format("%s: %d chunks, %d entidades (%d mobs, %d items, %d xp, %d armor stands), %d block entities que tickean",
                    level.dimension().identifier(), chunks, total, mobs, items, xp, stands, bes)));
        }
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        send(c, Component.literal("RAM: " + used + " / " + rt.maxMemory() / 1048576L + " MB").withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int tps(CommandContext<CommandSourceStack> c) {
        double mspt = LagAlerts.mspt(c.getSource().getServer());
        ChatFormatting color = mspt < 40 ? ChatFormatting.GREEN : mspt < 50 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        send(c, Component.literal(String.format("MSPT %.1f ms | TPS %.1f", mspt, LagAlerts.tps(mspt))).withStyle(color));
        return 1;
    }

    private static int top(CommandContext<CommandSourceStack> c, int count) {
        List<ChunkReport.ChunkStats> list = ChunkReport.top(c.getSource().getLevel(), count);
        send(c, header("Chunks mas pesados en " + c.getSource().getLevel().dimension().identifier()));
        int i = 1;
        for (ChunkReport.ChunkStats s : list) {
            send(c, Component.literal(String.format("%d. %s — %d entidades, %d block entities — %s",
                    i++, s.coords(), s.entities, s.tickingBlockEntities, s.topType())));
        }
        if (list.isEmpty()) send(c, Component.literal("Nada cargado."));
        return list.size();
    }

    private static void addKillFilter(LiteralArgumentBuilder<CommandSourceStack> kill, String name, Predicate<Entity> filter) {
        kill.then(Commands.literal(name)
                .executes(c -> kill(c, filter, -1, name))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 10000))
                        .executes(c -> kill(c, filter, IntegerArgumentType.getInteger(c, "radius"), name))));
    }

    private static boolean safeMob(Entity e) {
        if (e instanceof Player) return false;
        if (e.hasCustomName() || e.isPassenger() || e.isVehicle()) return false;
        if (e.typeHolder().is(EntityRules.BOSSES)) return false;
        if (e instanceof OwnableEntity own && own.getOwnerReference() != null) return false;
        return !(e instanceof Mob mob && mob.isLeashed());
    }

    private static int killType(CommandContext<CommandSourceStack> c, int radius) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(c, "entity");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            c.getSource().sendFailure(Component.literal("No existe la entidad " + id));
            return 0;
        }
        if (type == EntityType.PLAYER) {
            c.getSource().sendFailure(Component.literal("No se pueden borrar jugadores."));
            return 0;
        }
        return kill(c, e -> e.getType() == type && (e instanceof Mob ? safeMob(e) : !e.hasCustomName()), radius, id.toString());
    }

    private static int kill(CommandContext<CommandSourceStack> c, Predicate<Entity> filter, int radius, String label) {
        ServerLevel level = c.getSource().getLevel();
        Vec3 origin = c.getSource().getPosition();
        double r2 = radius > 0 ? (double) radius * radius : -1;

        List<Entity> targets = new ArrayList<>();
        for (Entity e : level.getAllEntities()) {
            if (e == null || !e.isAlive() || e instanceof Player) continue;
            if (r2 > 0 && e.distanceToSqr(origin) > r2) continue;
            if (filter.test(e)) targets.add(e);
        }
        for (Entity e : targets) e.discard();

        String where = radius > 0 ? "en " + radius + " bloques" : "en toda la dimension";
        int n = targets.size();
        c.getSource().sendSuccess(() -> Component.literal("Borradas " + n + " entidades (" + label + ") " + where + ".")
                .withStyle(ChatFormatting.GREEN), true);
        return n;
    }

    private static int beList(CommandContext<CommandSourceStack> c, int radius) {
        ServerLevel level = c.getSource().getLevel();
        Map<String, Integer> counts = new HashMap<>();

        if (radius <= 0) {
            List<TickingBlockEntity> tickers = new ArrayList<>(((LevelTickersAccessor) level).osa$getBlockEntityTickers());
            for (TickingBlockEntity t : tickers) {
                if (t != null && !t.isRemoved()) counts.merge(t.getType(), 1, Integer::sum);
            }
            send(c, header("Block entities que tickean en " + level.dimension().identifier()));
        } else {
            for (BlockEntity be : blockEntitiesInRadius(level, BlockPos.containing(c.getSource().getPosition()), radius)) {
                Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
                counts.merge(id == null ? "?" : id.toString(), 1, Integer::sum);
            }
            send(c, header("Block entities en " + radius + " bloques"));
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));
        int shown = 0;
        for (Map.Entry<String, Integer> e : sorted) {
            if (shown++ >= 20) break;
            send(c, Component.literal(e.getValue() + "x " + e.getKey()));
        }
        if (sorted.isEmpty()) send(c, Component.literal("Ninguna."));
        return sorted.size();
    }

    private static int beRemove(CommandContext<CommandSourceStack> c) {
        ServerLevel level = c.getSource().getLevel();
        Identifier id = IdentifierArgument.getId(c, "type");
        int radius = IntegerArgumentType.getInteger(c, "radius");
        BlockPos center = BlockPos.containing(c.getSource().getPosition());

        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockEntity be : blockEntitiesInRadius(level, center, radius)) {
            if (id.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()))) {
                toRemove.add(be.getBlockPos().immutable());
            }
        }
        int ok = 0, failed = 0;
        for (BlockPos pos : toRemove) {
            try {
                level.removeBlockEntity(pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                ok++;
            } catch (Throwable t) {
                failed++;
                OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo borrar {} en {}: {}", id, pos, t.toString());
            }
        }
        if (failed > 0) {
            int f = failed;
            c.getSource().sendFailure(Component.literal(f + " bloques no se pudieron borrar (mira el log). "
                    + "Ojo con multibloques: puede quedar una parte suelta."));
        }
        int n = ok;
        c.getSource().sendSuccess(() -> Component.literal("Borrados " + n + " bloques de " + id + " en " + radius + " bloques.")
                .withStyle(ChatFormatting.GOLD), true);
        return n;
    }

    private static List<BlockEntity> blockEntitiesInRadius(ServerLevel level, BlockPos center, int radius) {
        List<BlockEntity> out = new ArrayList<>();
        double r2 = (double) radius * radius;
        int minCx = (center.getX() - radius) >> 4, maxCx = (center.getX() + radius) >> 4;
        int minCz = (center.getZ() - radius) >> 4, maxCz = (center.getZ() + radius) >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.getBlockPos().distSqr(center) <= r2) out.add(be);
                }
            }
        }
        return out;
    }

    private static int alertTest(CommandContext<CommandSourceStack> c) {
        CommandSourceStack src = c.getSource();
        LagAlerts.sendTest(src.getServer()).thenAccept(code -> src.getServer().execute(() -> {
            if (code == -1) src.sendFailure(Component.literal("No hay webhook configurado o fallo el envio (mira el log)."));
            else if (code >= 200 && code < 300) src.sendSuccess(() -> Component.literal("Alerta enviada a Discord.").withStyle(ChatFormatting.GREEN), false);
            else src.sendFailure(Component.literal("Discord respondio con error HTTP " + code));
        }));
        return 1;
    }

    private static Component header(String text) {
        return Component.literal("[OSA] " + text).withStyle(ChatFormatting.AQUA);
    }

    private static void send(CommandContext<CommandSourceStack> c, Component msg) {
        c.getSource().sendSuccess(() -> msg, false);
    }
}
