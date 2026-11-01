package nadiendev.osa.client;

import nadiendev.osa.OsaConfig;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public final class BetterGrassModel implements BlockStateModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private record GeometryKey(Object inner, int mask) {}

    private final BlockStateModel wrapped;

    public BetterGrassModel(BlockStateModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        wrapped.collectParts(random, parts);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockModelPart> parts) {
        int mask = OsaConfig.betterGrassEnabled() ? sides(level, pos, state) : 0;
        if (mask == 0) {
            wrapped.collectParts(level, pos, state, random, parts);
            return;
        }
        List<BlockModelPart> original = new ArrayList<>();
        wrapped.collectParts(level, pos, state, random, original);
        for (BlockModelPart part : original) parts.add(new Part(part, mask));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        Object inner = wrapped.createGeometryKey(level, pos, state, random);
        if (inner == null || !OsaConfig.betterGrassEnabled()) return inner;
        return new GeometryKey(inner, sides(level, pos, state));
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return wrapped.particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return wrapped.particleIcon(level, pos, state);
    }

    private static int sides(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        int mask = 0;
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (Direction side : HORIZONTAL) {
            neighbour.set(pos).move(side).move(Direction.DOWN);
            if (matches(level.getBlockState(neighbour), state)) mask |= 1 << side.get2DDataValue();
        }
        return mask;
    }

    private static boolean matches(BlockState other, BlockState state) {
        if (!other.is(state.getBlock())) return false;
        return !state.hasProperty(BlockStateProperties.SNOWY)
                || other.getValue(BlockStateProperties.SNOWY).equals(state.getValue(BlockStateProperties.SNOWY));
    }

    private static BakedQuad retextured(BakedQuad quad, BakedQuad top) {
        TextureAtlasSprite from = quad.sprite();
        TextureAtlasSprite to = top.sprite();
        long[] uv = new long[BakedQuad.VERTEX_COUNT];
        for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
            long packed = quad.packedUV(i);
            float relativeU = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
            float relativeV = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
            uv[i] = UVPair.pack(to.getU0() + (to.getU1() - to.getU0()) * relativeU,
                    to.getV0() + (to.getV1() - to.getV0()) * relativeV);
        }
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                uv[0], uv[1], uv[2], uv[3], top.tintIndex(), quad.direction(), to, quad.shade(), quad.lightEmission());
    }

    private static final class Part implements BlockModelPart {
        private final BlockModelPart wrapped;
        private final int mask;
        private final BakedQuad top;

        Part(BlockModelPart wrapped, int mask) {
            this.wrapped = wrapped;
            this.mask = mask;
            List<BakedQuad> up = wrapped.getQuads(Direction.UP);
            this.top = up.isEmpty() ? null : up.get(0);
        }

        @Override
        public List<BakedQuad> getQuads(Direction side) {
            List<BakedQuad> quads = wrapped.getQuads(side);
            if (top == null || side == null || side.getAxis().isVertical() || quads.isEmpty()
                    || (mask & (1 << side.get2DDataValue())) == 0) {
                return quads;
            }
            List<BakedQuad> result = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                if (quad.sprite().contents().name().getPath().endsWith("_overlay")) continue;
                result.add(retextured(quad, top));
            }
            return result;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return wrapped.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return wrapped.particleIcon();
        }

        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            return wrapped.getRenderType(state);
        }

        @Override
        public TriState ambientOcclusion() {
            return wrapped.ambientOcclusion();
        }
    }
}
