package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.function.Predicate;

public final class BetterGrassModel extends WrapperBlockStateModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] FACES = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null
    };

    public BetterGrassModel(BlockStateModel original) {
        super(original);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        int mask = OsaConfig.betterGrassEnabled() ? sides(level, pos, state) : 0;
        if (mask == 0) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        for (BlockModelPart part : wrapped.collectParts(random)) {
            List<BakedQuad> top = part.getQuads(Direction.UP);
            TriState ambientOcclusion = part.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
            for (Direction face : FACES) {
                if (cullTest.test(face)) continue;
                boolean retexture = face != null && face.getAxis().isHorizontal() && !top.isEmpty()
                        && (mask & (1 << face.get2DDataValue())) != 0;
                for (BakedQuad quad : part.getQuads(face)) {
                    if (retexture && quad.sprite().contents().name().getPath().endsWith("_overlay")) continue;
                    emitter.cullFace(face);
                    emitter.fromBakedQuad(retexture ? retextured(quad, top.get(0)) : quad);
                    emitter.ambientOcclusion(ambientOcclusion);
                    emitter.shadeMode(ShadeMode.VANILLA);
                    emitter.emit();
                }
            }
        }
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
}
