package nadiendev.osa.datagen;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID)
public final class DataGenerators {
    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        event.addProvider(new OsaLangProvider(output, "en_us", false));
        event.addProvider(new OsaLangProvider(output, "es_es", true));
        event.addProvider(new OsaLangProvider(output, "es_ar", true));
        event.addProvider(new OsaLangProvider(output, "es_mx", true));
    }
}
