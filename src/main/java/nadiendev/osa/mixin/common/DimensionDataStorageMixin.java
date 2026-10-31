package nadiendev.osa.mixin.common;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.server.AsyncSaver;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageMixin {
    @Shadow
    @Final
    private Map<String, SavedData> cache;

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Shadow
    protected abstract File getDataFile(String name);

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void osa$saveOffThread(CallbackInfo ci) {
        if (!OsaConfig.asyncSaveEnabled()) return;
        ci.cancel();

        Map<File, CompoundTag> pending = new HashMap<>();
        for (Map.Entry<String, SavedData> entry : this.cache.entrySet()) {
            SavedData data = entry.getValue();
            if (data == null || !data.isDirty()) continue;
            try {
                CompoundTag tag = data.save(new CompoundTag(), this.registries);
                data.setDirty(false);
                if (tag != null) pending.put(this.getDataFile(entry.getKey()), tag);
            } catch (Exception e) {
                OptimizerSodiumAddon.LOG.error("[OSA] No se pudo serializar el dato guardado {}", entry.getKey(), e);
            }
        }
        if (pending.isEmpty()) return;

        AsyncSaver.submit(() -> pending.forEach(DimensionDataStorageMixin::osa$write));
    }

    private static void osa$write(File file, CompoundTag data) {
        try {
            CompoundTag root = new CompoundTag();
            root.put("data", data);
            NbtUtils.addCurrentDataVersion(root);
            File temp = new File(file.getParentFile(), "tmp_" + file.getName());
            temp.getParentFile().mkdirs();
            NbtIo.writeCompressed(root, temp.toPath());
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            OptimizerSodiumAddon.LOG.error("[OSA] No se pudo escribir {}", file, e);
        }
    }
}
