package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public final class BetterGrassModels implements ModelLoadingPlugin {
    @Override
    public void onInitializeModelLoader(Context context) {
        Set<ModelResourceLocation> targets = new HashSet<>();
        for (ResourceLocation id : OsaConfig.betterGrassBlocks()) {
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (block == null) continue;
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                targets.add(BlockModelShaper.stateToModelLocation(state));
            }
        }
        if (targets.isEmpty()) return;
        context.modifyModelAfterBake().register((model, modelContext) -> {
            ModelResourceLocation key = modelContext.topLevelId();
            return model != null && key != null && targets.contains(key) ? new BetterGrassModel(model) : model;
        });
    }
}
