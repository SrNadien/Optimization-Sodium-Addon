package nadiendev.osa.loader;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class Platform {
    private Platform() {}

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
