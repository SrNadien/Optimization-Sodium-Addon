package nadiendev.osa.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import nadiendev.osa.client.ReloadProfiler;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadInstance.class)
public abstract class SimpleReloadInstanceMixin<S> {

    @Inject(method = "create", at = @At("HEAD"))
    private static void osa$begin(ResourceManager manager, List<PreparableReloadListener> listeners, Executor background,
                                  Executor game, CompletableFuture<Unit> initialTask, boolean profiling,
                                  CallbackInfoReturnable<ReloadInstance> cir) {
        ReloadProfiler.begin();
    }

    @Inject(method = "create", at = @At("RETURN"))
    private static void osa$end(ResourceManager manager, List<PreparableReloadListener> listeners, Executor background,
                                Executor game, CompletableFuture<Unit> initialTask, boolean profiling,
                                CallbackInfoReturnable<ReloadInstance> cir) {
        ReloadProfiler.finish(cir.getReturnValue().done());
    }

    @WrapOperation(method = "prepareTasks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance$StateFactory;create(Lnet/minecraft/server/packs/resources/PreparableReloadListener$SharedState;Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;Lnet/minecraft/server/packs/resources/PreparableReloadListener;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<S> osa$watchListener(@Coerce Object factory, PreparableReloadListener.SharedState shared,
                                                   PreparableReloadListener.PreparationBarrier barrier,
                                                   PreparableReloadListener listener, Executor taskExecutor,
                                                   Executor reloadExecutor, Operation<CompletableFuture<S>> original) {
        boolean skippable = !((Object) this instanceof ProfiledReloadInstance);
        return ReloadProfiler.run(listener, barrier, skippable,
                timed -> original.call(factory, shared, timed, listener, taskExecutor, reloadExecutor));
    }
}
