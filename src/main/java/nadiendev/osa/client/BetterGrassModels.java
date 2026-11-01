package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class BetterGrassModels {
    private BetterGrassModels() {}

    public static void register() {
        ModelLoadingPlugin.register(context -> {
            Set<Identifier> blocks = new HashSet<>(OsaConfig.betterGrassBlocks());
            context.modifyBlockModelAfterBake().register((model, modifier) ->
                    blocks.contains(BuiltInRegistries.BLOCK.getKey(modifier.state().getBlock())) ? new BetterGrassModel(model) : model);
        });
    }
}
