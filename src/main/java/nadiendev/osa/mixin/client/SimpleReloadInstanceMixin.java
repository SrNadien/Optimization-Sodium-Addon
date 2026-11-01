package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.client.ReloadProfiler;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadInstance.class)
public abstract class SimpleReloadInstanceMixin {

    @Inject(method = "of", at = @At("HEAD"))
    private static void osa$begin(ResourceManager manager, List<PreparableReloadListener> listeners, Executor background,
                                  Executor game, CompletableFuture<Unit> waitedFor,
                                  CallbackInfoReturnable<SimpleReloadInstance<Void>> cir) {
        ReloadProfiler.begin();
    }

    @Inject(method = "of", at = @At("RETURN"))
    private static void osa$end(ResourceManager manager, List<PreparableReloadListener> listeners, Executor background,
                                Executor game, CompletableFuture<Unit> waitedFor,
                                CallbackInfoReturnable<SimpleReloadInstance<Void>> cir) {
        ReloadProfiler.finish(cir.getReturnValue().done());
    }

    @WrapOperation(method = "lambda$of$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/PreparableReloadListener;reload(Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<Void> osa$watchListener(PreparableReloadListener listener,
                                                             PreparableReloadListener.PreparationBarrier barrier,
                                                             ResourceManager manager, ProfilerFiller preparation,
                                                             ProfilerFiller reload, Executor background, Executor game,
                                                             Operation<CompletableFuture<Void>> original) {
        return ReloadProfiler.run(listener, barrier,
                timed -> original.call(listener, timed, manager, preparation, reload, background, game));
    }
}
