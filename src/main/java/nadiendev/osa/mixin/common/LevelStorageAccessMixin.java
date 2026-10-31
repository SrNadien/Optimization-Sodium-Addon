package nadiendev.osa.mixin.common;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.server.AsyncSaver;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class LevelStorageAccessMixin {
    @Shadow
    @Final
    LevelStorageSource.LevelDirectory levelDirectory;

    @Inject(method = "saveLevelData", at = @At("HEAD"), cancellable = true)
    private void osa$saveOffThread(CompoundTag tag, CallbackInfo ci) {
        if (!OsaConfig.asyncSaveEnabled()) return;
        ci.cancel();

        Path dir = this.levelDirectory.path();
        Path oldFile = this.levelDirectory.oldDataFile();
        Path dataFile = this.levelDirectory.dataFile();
        AsyncSaver.submit(() -> {
            try {
                Path temp = Files.createTempFile(dir, "level", ".dat");
                NbtIo.writeCompressed(tag, temp);
                Util.safeReplaceFile(dataFile, temp, oldFile);
            } catch (Exception e) {
                OptimizerSodiumAddon.LOG.error("[OSA] No se pudo guardar level.dat en {}", dir, e);
            }
        });
    }
}
