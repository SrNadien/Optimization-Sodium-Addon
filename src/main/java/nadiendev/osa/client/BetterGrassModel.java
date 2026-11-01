package nadiendev.osa.client;

import nadiendev.osa.OsaConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.List;

public final class BetterGrassModel extends BakedModelWrapper<BakedModel> {
    public static final ModelProperty<Integer> SIDES = new ModelProperty<>();

    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final int VERTEX_INTS = 8;
    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;

    public BetterGrassModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        ModelData base = super.getModelData(level, pos, state, data);
        if (!OsaConfig.betterGrassEnabled()) return base;
        int mask = 0;
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (Direction side : HORIZONTAL) {
            neighbour.set(pos).move(side).move(Direction.DOWN);
            if (matches(level.getBlockState(neighbour), state)) mask |= 1 << side.get2DDataValue();
        }
        return mask == 0 ? base : base.derive().with(SIDES, mask).build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, random, data, renderType);
        if (state == null || side == null || side.getAxis().isVertical() || quads.isEmpty()) return quads;
        Integer mask = data.get(SIDES);
        if (mask == null || (mask & (1 << side.get2DDataValue())) == 0) return quads;

        List<BakedQuad> top = super.getQuads(state, Direction.UP, RandomSource.create(0L), data, renderType);
        if (top.isEmpty()) return quads;

        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (quad.getSprite().contents().name().getPath().endsWith("_overlay")) continue;
            result.add(retextured(quad, top.get(0)));
        }
        return result;
    }

    private static boolean matches(BlockState other, BlockState state) {
        if (!other.is(state.getBlock())) return false;
        return !state.hasProperty(BlockStateProperties.SNOWY)
                || other.getValue(BlockStateProperties.SNOWY).equals(state.getValue(BlockStateProperties.SNOWY));
    }

    private static BakedQuad retextured(BakedQuad quad, BakedQuad top) {
        TextureAtlasSprite from = quad.getSprite();
        TextureAtlasSprite to = top.getSprite();
        int[] vertices = quad.getVertices().clone();
        for (int i = 0; i < 4; i++) {
            int offset = i * VERTEX_INTS;
            float u = Float.intBitsToFloat(vertices[offset + U_OFFSET]);
            float v = Float.intBitsToFloat(vertices[offset + V_OFFSET]);
            float relativeU = (u - from.getU0()) / (from.getU1() - from.getU0());
            float relativeV = (v - from.getV0()) / (from.getV1() - from.getV0());
            vertices[offset + U_OFFSET] = Float.floatToRawIntBits(to.getU0() + (to.getU1() - to.getU0()) * relativeU);
            vertices[offset + V_OFFSET] = Float.floatToRawIntBits(to.getV0() + (to.getV1() - to.getV0()) * relativeV);
        }
        return new BakedQuad(vertices, top.getTintIndex(), quad.getDirection(), to, quad.isShade(), quad.hasAmbientOcclusion());
    }
}
