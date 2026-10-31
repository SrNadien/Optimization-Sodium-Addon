package nadiendev.osa.mixin.client;

import nadiendev.osa.compat.CompactMachinesPreviewCleanup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Libera la memoria del preview 3D de las salas de Compact Machines.
 * Pseudo + require = 0: si Compact Machines no esta instalado, no hace nada.
 */
@Pseudo
@Mixin(targets = "dev.compactmods.machines.client.room.MachineRoomScreen", remap = false)
public abstract class MachineRoomScreenMixin extends Screen {

    protected MachineRoomScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "updateScene", at = @At("HEAD"), require = 0, remap = false)
    private void osa$releaseOnReplace(CallbackInfo ci) {
        CompactMachinesPreviewCleanup.disposeScreenRenderer(this);
    }

    @Inject(method = "onClose", at = @At("HEAD"), require = 0)
    private void osa$releaseOnClose(CallbackInfo ci) {
        CompactMachinesPreviewCleanup.disposeScreenRenderer(this);
    }
}
