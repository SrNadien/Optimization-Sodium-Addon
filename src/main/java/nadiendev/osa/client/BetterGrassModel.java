package nadiendev.osa.client;

import nadiendev.osa.OsaConfig;
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
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BetterGrassModel extends DelegateBlockStateModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public BetterGrassModel(BlockStateModel original) {
        super(original);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int mask = OsaConfig.betterGrassEnabled() ? sideMask(level, pos, state) : 0;
        if (mask == 0) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }
        List<BlockStateModelPart> original = new ArrayList<>();
        super.collectParts(level, pos, state, random, original);
        for (BlockStateModelPart part : original) parts.add(new Part(part, mask));
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

    private record Part(BlockStateModelPart part, int mask) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            List<BakedQuad> quads = this.part.getQuads(direction);
            if (direction == null || direction.getAxis().isVertical() || quads.isEmpty()
                    || (this.mask & (1 << direction.get2DDataValue())) == 0) {
                return quads;
            }
            List<BakedQuad> top = this.part.getQuads(Direction.UP);
            if (top.isEmpty()) return quads;

            BakedQuad.MaterialInfo topInfo = top.get(0).materialInfo();
            List<BakedQuad> result = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                if (quad.materialInfo().sprite().contents().name().getPath().endsWith("_overlay")) continue;
                result.add(retextured(quad, topInfo));
            }
            return result;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.part.useAmbientOcclusion();
        }

        @Override
        public TriState ambientOcclusion() {
            return this.part.ambientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.part.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.part.materialFlags();
        }
    }

    private static BakedQuad retextured(BakedQuad quad, BakedQuad.MaterialInfo topInfo) {
        BakedQuad.MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite from = info.sprite();
        TextureAtlasSprite to = topInfo.sprite();
        long[] uv = new long[4];
        for (int i = 0; i < 4; i++) {
            long packed = quad.packedUV(i);
            float relativeU = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
            float relativeV = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
            uv[i] = UVPair.pack(to.getU0() + (to.getU1() - to.getU0()) * relativeU, to.getV0() + (to.getV1() - to.getV0()) * relativeV);
        }
        BakedQuad.MaterialInfo retextured = new BakedQuad.MaterialInfo(to, topInfo.layer(), topInfo.itemRenderType(),
                topInfo.tintIndex(), info.shade(), info.lightEmission());
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                uv[0], uv[1], uv[2], uv[3], quad.direction(), retextured);
    }
}
