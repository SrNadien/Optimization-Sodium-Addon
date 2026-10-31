package nadiendev.osa.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import nadiendev.osa.mixin.common.LevelTickersAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChunkReport {
    public static final class ChunkStats {
        public final String dimension;
        public final ChunkPos pos;
        public int entities;
        public int tickingBlockEntities;
        public final Map<String, Integer> byType = new HashMap<>();

        ChunkStats(String dimension, ChunkPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        public int score() {
            return entities + tickingBlockEntities * 2;
        }

        public String topType() {
            return byType.entrySet().stream().max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey() + " x" + e.getValue()).orElse("-");
        }

        public String coords() {
            return "x " + pos.getMiddleBlockX() + ", z " + pos.getMiddleBlockZ();
        }
    }

    private ChunkReport() {}

    public static List<ChunkStats> scan(ServerLevel level) {
        String dim = level.dimension().identifier().toString();
        Long2ObjectOpenHashMap<ChunkStats> map = new Long2ObjectOpenHashMap<>();

        for (Entity e : level.getAllEntities()) {
            if (e == null) continue;
            ChunkPos cp = e.chunkPosition();
            ChunkStats s = map.computeIfAbsent(cp.pack(), k -> new ChunkStats(dim, cp));
            s.entities++;
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            s.byType.merge(id == null ? "?" : id.toString(), 1, Integer::sum);
        }

        List<TickingBlockEntity> tickers = new ArrayList<>(((LevelTickersAccessor) level).osa$getBlockEntityTickers());
        for (TickingBlockEntity t : tickers) {
            if (t == null || t.isRemoved()) continue;
            BlockPos p = t.getPos();
            ChunkPos cp = ChunkPos.containing(p);
            ChunkStats s = map.computeIfAbsent(cp.pack(), k -> new ChunkStats(dim, cp));
            s.tickingBlockEntities++;
            s.byType.merge(t.getType(), 1, Integer::sum);
        }
        return new ArrayList<>(map.values());
    }

    public static List<ChunkStats> top(MinecraftServer server, int count) {
        List<ChunkStats> all = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            all.addAll(scan(level));
        }
        all.sort(Comparator.comparingInt(ChunkStats::score).reversed());
        return all.size() > count ? new ArrayList<>(all.subList(0, count)) : all;
    }

    public static List<ChunkStats> top(ServerLevel level, int count) {
        List<ChunkStats> all = scan(level);
        all.sort(Comparator.comparingInt(ChunkStats::score).reversed());
        return all.size() > count ? new ArrayList<>(all.subList(0, count)) : all;
    }

    public static int tickingBlockEntities(ServerLevel level) {
        return ((LevelTickersAccessor) level).osa$getBlockEntityTickers().size();
    }
}
