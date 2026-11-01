package nadiendev.osa.datagen;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ForeignLangProvider extends LanguageProvider {
    private static final Type TABLE = new TypeToken<LinkedHashMap<String, String>>() {}.getType();

    private final Map<String, String> table;

    public ForeignLangProvider(PackOutput output, String namespace, String locale, Map<String, String> table) {
        super(output, namespace, locale);
        this.table = table;
    }

    public static Map<String, String> load(String name) {
        String path = "/datagen/spanish/" + name + ".json";
        try (InputStream stream = ForeignLangProvider.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Falta la tabla de traducciones " + path);
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, TABLE);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + path, e);
        }
    }

    @Override
    protected void addTranslations() {
        table.forEach(this::add);
    }
}
