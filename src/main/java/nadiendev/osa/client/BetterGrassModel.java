package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BetterGrassModel extends WrapperBlockStateModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private volatile Probe probe;

    private record Probe(TextureAtlasSprite top, int topTint, List<TextureAtlasSprite> sides) {}

    public BetterGrassModel(BlockStateModel original) {
        super(original);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        int mask = OsaConfig.betterGrassEnabled() ? sideMask(level, pos, state) : 0;
        Probe probe = mask == 0 ? null : probe();
        if (probe == null) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }
        emitter.pushTransform(quad -> retexture(quad, mask, probe));
        try {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
        } finally {
            emitter.popTransform();
        }
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (OsaConfig.betterGrassEnabled() && sideMask(level, pos, state) != 0) return null;
        return super.createGeometryKey(level, pos, state, random);
    }

    private static int sideMask(BlockAndTintGetter level, BlockPos pos, BlockState state) {
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

    private Probe probe() {
        Probe cached = probe;
        if (cached != null) return cached;
        List<BlockStateModelPart> parts = new ArrayList<>();
        this.wrapped.collectParts(RandomSource.create(0L), parts);
        TextureAtlasSprite top = null;
        int topTint = -1;
        List<TextureAtlasSprite> sides = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(Direction.UP)) {
                if (top == null) {
                    top = quad.materialInfo().sprite();
                    topTint = quad.materialInfo().tintIndex();
                }
            }
            for (Direction side : HORIZONTAL) {
                for (BakedQuad quad : part.getQuads(side)) {
                    TextureAtlasSprite sprite = quad.materialInfo().sprite();
                    if (!sides.contains(sprite)) sides.add(sprite);
                }
            }
        }
        if (top == null || sides.isEmpty()) return null;
        cached = new Probe(top, topTint, List.copyOf(sides));
        probe = cached;
        return cached;
    }

    private static boolean retexture(MutableQuadView quad, int mask, Probe probe) {
        Direction face = quad.cullFace();
        if (face == null || face.getAxis().isVertical() || (mask & (1 << face.get2DDataValue())) == 0) return true;

        float centerU = (quad.u(0) + quad.u(1) + quad.u(2) + quad.u(3)) / 4.0F;
        float centerV = (quad.v(0) + quad.v(1) + quad.v(2) + quad.v(3)) / 4.0F;
        TextureAtlasSprite from = null;
        for (TextureAtlasSprite sprite : probe.sides()) {
            if (centerU >= Math.min(sprite.getU0(), sprite.getU1()) && centerU <= Math.max(sprite.getU0(), sprite.getU1())
                    && centerV >= Math.min(sprite.getV0(), sprite.getV1()) && centerV <= Math.max(sprite.getV0(), sprite.getV1())) {
                from = sprite;
                break;
            }
        }
        if (from == null) return true;
        if (from.contents().name().getPath().endsWith("_overlay")) return false;

        TextureAtlasSprite to = probe.top();
        for (int i = 0; i < 4; i++) {
            float relativeU = (quad.u(i) - from.getU0()) / (from.getU1() - from.getU0());
            float relativeV = (quad.v(i) - from.getV0()) / (from.getV1() - from.getV0());
            quad.uv(i, to.getU0() + (to.getU1() - to.getU0()) * relativeU, to.getV0() + (to.getV1() - to.getV0()) * relativeV);
        }
        quad.tintIndex(probe.topTint());
        return true;
    }
}
