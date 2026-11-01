package nadiendev.osa.datagen;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Map;

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

        Map<String, String> sodium = ForeignLangProvider.load("sodium");
        for (String locale : List.of("es_es", "es_ar", "es_mx")) {
            event.addProvider(new ForeignLangProvider(output, "sodium", locale, sodium));
        }
        Map<String, String> chloride = ForeignLangProvider.load("chloride");
        for (String locale : List.of("es_es", "es_ar")) {
            event.addProvider(new ForeignLangProvider(output, "chloride", locale, chloride));
        }
    }
}
