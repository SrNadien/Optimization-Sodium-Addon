package nadiendev.osa.mixin.client;

import nadiendev.osa.compat.ImpactSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.config.search.BigramSearchIndex$BigramSearchQuerySession")
public abstract class SodiumSearchMixin {
    @Inject(method = "getSearchResults", at = @At("HEAD"), cancellable = true, require = 0)
    private void osa$filterByImpact(String query, CallbackInfoReturnable<List<?>> cir) {
        List<Object> filtered = ImpactSearch.filter(this, query);
        if (filtered != null) cir.setReturnValue(filtered);
    }
}
