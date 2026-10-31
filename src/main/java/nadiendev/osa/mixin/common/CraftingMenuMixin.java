package nadiendev.osa.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import nadiendev.osa.config.OsaConfig;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
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
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private static Optional<CraftingRecipe> osa$tryLastRecipeFirst(
            RecipeManager manager, RecipeType<CraftingRecipe> type, Container container, Level level,
            Operation<Optional<CraftingRecipe>> original,
            @Local(argsOnly = true) ResultContainer results) {
        if (OsaConfig.craftingRecipeHintEnabled() && container instanceof CraftingContainer grid) {
            Recipe<?> last = results.getRecipeUsed();
            if (last instanceof CraftingRecipe crafting && crafting.matches(grid, level)) {
                return Optional.of(crafting);
            }
        }
        return original.call(manager, type, container, level);
    }
}
