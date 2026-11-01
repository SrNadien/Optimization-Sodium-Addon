package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.function.Supplier;

public final class BetterGrassModel extends ForwardingBakedModel {
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] FACES = {
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null
    };
    private static final int VERTEX_INTS = 8;
    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;

    private static volatile RenderMaterial material;

    public BetterGrassModel(BakedModel original) {
        super(original);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos,
                               Supplier<RandomSource> random, RenderContext context) {
        int mask = OsaConfig.betterGrassEnabled() ? sides(level, pos, state) : 0;
        RenderMaterial standard = standardMaterial();
        if (mask == 0 || standard == null) {
            super.emitBlockQuads(level, state, pos, random, context);
            return;
        }

        List<BakedQuad> top = wrapped.getQuads(state, Direction.UP, random.get());
        QuadEmitter emitter = context.getEmitter();
        for (Direction face : FACES) {
            List<BakedQuad> quads = wrapped.getQuads(state, face, random.get());
            boolean retexture = face != null && face.getAxis().isHorizontal() && !top.isEmpty()
                    && (mask & (1 << face.get2DDataValue())) != 0;
            for (BakedQuad quad : quads) {
                if (retexture) {
                    if (quad.getSprite().contents().name().getPath().endsWith("_overlay")) continue;
                    emitter.fromVanilla(retextured(quad, top.get(0)), standard, face);
                    emitter.emit();
                } else {
                    emitter.fromVanilla(quad, standard, face);
                    emitter.emit();
                }
            }
        }
    }

    private static RenderMaterial standardMaterial() {
        RenderMaterial found = material;
        if (found == null) {
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            if (renderer == null) return null;
            found = renderer.materialFinder().find();
            material = found;
        }
        return found;
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
        return new BakedQuad(vertices, top.getTintIndex(), quad.getDirection(), to, quad.isShade());
    }
}
