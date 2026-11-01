package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BetterGrassModel extends WrapperBlockStateModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public BetterGrassModel(BlockStateModel original) {
        super(original);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<Direction> cullTest) {
        int mask = OsaConfig.betterGrassEnabled() ? sideMask(level, pos, state) : 0;
        if (mask == 0) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }
        new Masked(this.wrapped, mask).emitQuads(emitter, level, pos, state, random, cullTest);
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

    private static BakedQuad retextured(BakedQuad quad, BakedQuad top) {
        TextureAtlasSprite from = quad.materialInfo().sprite();
        TextureAtlasSprite to = top.materialInfo().sprite();
        long[] uvs = new long[4];
        for (int i = 0; i < 4; i++) {
            long packed = quad.packedUV(i);
            float relativeU = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
            float relativeV = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
            uvs[i] = UVPair.pack(to.getU0() + (to.getU1() - to.getU0()) * relativeU, to.getV0() + (to.getV1() - to.getV0()) * relativeV);
        }
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                uvs[0], uvs[1], uvs[2], uvs[3], quad.direction(), top.materialInfo());
    }

    private record Masked(BlockStateModel original, int mask) implements BlockStateModel {
        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            this.original.collectParts(random, output);
            for (int i = 0; i < output.size(); i++) {
                output.set(i, new Part(output.get(i), this.mask));
            }
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.original.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.original.materialFlags();
        }
    }

    private record Part(BlockStateModelPart original, int mask) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            List<BakedQuad> quads = this.original.getQuads(direction);
            if (direction == null || direction.getAxis().isVertical() || quads.isEmpty()) return quads;
            if ((this.mask & (1 << direction.get2DDataValue())) == 0) return quads;

            List<BakedQuad> top = this.original.getQuads(Direction.UP);
            if (top.isEmpty()) return quads;

            List<BakedQuad> result = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                if (quad.materialInfo().sprite().contents().name().getPath().endsWith("_overlay")) continue;
                result.add(retextured(quad, top.get(0)));
            }
            return result;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.original.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.original.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.original.materialFlags();
        }
    }
}
