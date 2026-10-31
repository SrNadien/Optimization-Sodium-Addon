package nadiendev.osa.mixin.client;

import nadiendev.osa.client.JoinTimer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleUpdateRecipes", at = @At("HEAD"))
    private void osa$recipesBegin(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        JoinTimer.recipesBegin();
    }

    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void osa$recipesEnd(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        JoinTimer.recipesEnd();
    }

    @Inject(method = "handleUpdateTags", at = @At("HEAD"))
    private void osa$tagsBegin(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        JoinTimer.tagsBegin();
    }

    @Inject(method = "handleUpdateTags", at = @At("RETURN"))
    private void osa$tagsEnd(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        JoinTimer.tagsEnd();
    }
}
