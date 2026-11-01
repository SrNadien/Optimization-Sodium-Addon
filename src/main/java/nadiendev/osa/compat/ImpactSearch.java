package nadiendev.osa.compat;

import nadiendev.osa.OptimizerSodiumAddon;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ImpactSearch {
    private static final String OPTION = "net.caffeinemc.mods.sodium.client.config.structure.Option";
    private static final String TEXT_SOURCE = "net.caffeinemc.mods.sodium.client.config.search.TextSource";

    private static final Map<OptionImpact, List<String>> WORDS = Map.of(
            OptionImpact.LOW, List.of("low", "bajo"),
            OptionImpact.MEDIUM, List.of("medium", "medio"),
            OptionImpact.HIGH, List.of("high", "alto"),
            OptionImpact.VARIES, List.of("varies", "variable")
    );

    private static boolean failed;

    private ImpactSearch() {}

    public static List<Object> filter(Object session, String query) {
        if (query == null) return null;
        String text = query.trim();
        if (!text.startsWith("#")) return null;
        String word = normalize(text.substring(1));
        if (word.isEmpty()) return null;

        OptionImpact wanted = parse(word);
        if (wanted == null) return List.of();

        try {
            Field outer = session.getClass().getDeclaredField("this$0");
            outer.setAccessible(true);
            Object index = outer.get(session);
            Field sourcesField = index.getClass().getSuperclass().getDeclaredField("sources");
            sourcesField.setAccessible(true);
            List<?> sources = (List<?>) sourcesField.get(index);

            ClassLoader loader = session.getClass().getClassLoader();
            Method impactOf = Class.forName(OPTION, false, loader).getMethod("getImpact");
            Method setIndex = Class.forName(TEXT_SOURCE, false, loader).getMethod("setResultIndex", int.class);

            List<Object> result = new ArrayList<>();
            for (Object source : sources) {
                if (!source.getClass().getName().endsWith("OptionNameSource")) continue;
                Object option = source.getClass().getMethod("getOption").invoke(source);
                if (impactOf.invoke(option) != wanted) continue;
                setIndex.invoke(source, result.size());
                result.add(source);
            }
            return result;
        } catch (ReflectiveOperationException | RuntimeException e) {
            if (!failed) {
                failed = true;
                OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo filtrar la busqueda de Sodium por impacto", e);
            }
            return null;
        }
    }

    private static OptionImpact parse(String word) {
        for (OptionImpact impact : OptionImpact.values()) {
            for (String candidate : WORDS.getOrDefault(impact, List.of())) {
                if (candidate.startsWith(word)) return impact;
            }
        }
        return null;
    }

    private static String normalize(String text) {
        String decomposed = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }
}
