package nadiendev.osa.mixin.common;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.server.AsyncSaver;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
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

    @Inject(method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void osa$saveOffThread(RegistryAccess registryAccess, WorldData worldData, CompoundTag hostPlayer, CallbackInfo ci) {
        if (!OsaConfig.asyncSaveEnabled()) return;
        ci.cancel();

        CompoundTag root = new CompoundTag();
        root.put("Data", worldData.createTag(registryAccess, hostPlayer));

        Path dir = this.levelDirectory.path();
        Path oldFile = this.levelDirectory.oldDataFile();
        Path dataFile = this.levelDirectory.dataFile();
        AsyncSaver.submit(() -> {
            try {
                Path temp = Files.createTempFile(dir, "level", ".dat");
                NbtIo.writeCompressed(root, temp.toFile());
                Util.safeReplaceFile(dataFile, temp, oldFile);
            } catch (Exception e) {
                OptimizerSodiumAddon.LOG.error("[OSA] No se pudo guardar level.dat en {}", dir, e);
            }
        });
    }
}
