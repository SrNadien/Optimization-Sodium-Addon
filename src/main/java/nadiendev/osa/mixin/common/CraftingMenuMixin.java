package nadiendev.osa.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import nadiendev.osa.config.OsaConfig;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @WrapOperation(method = "slotChangedCraftingGrid", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;"))
    @SuppressWarnings("unchecked")
    private static Optional<RecipeHolder<CraftingRecipe>> osa$tryLastRecipeFirst(
            RecipeManager manager, RecipeType<CraftingRecipe> type, RecipeInput input, Level level,
            RecipeHolder<CraftingRecipe> hint,
            Operation<Optional<RecipeHolder<CraftingRecipe>>> original,
            @Local(argsOnly = true) ResultContainer results) {
        RecipeHolder<CraftingRecipe> effective = hint;
        if (effective == null && OsaConfig.craftingRecipeHintEnabled()
                && results.getRecipeUsed() != null && results.getRecipeUsed().value() instanceof CraftingRecipe) {
            effective = (RecipeHolder<CraftingRecipe>) results.getRecipeUsed();
        }
        return original.call(manager, type, input, level, effective);
    }
}
