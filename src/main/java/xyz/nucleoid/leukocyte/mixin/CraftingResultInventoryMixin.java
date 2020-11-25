package xyz.nucleoid.leukocyte.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import xyz.nucleoid.leukocyte.ProtectionManager;
import xyz.nucleoid.leukocyte.RuleQuery;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;

@Mixin(CraftingResultInventory.class)
public abstract class CraftingResultInventoryMixin implements RecipeUnlocker {
    @Override
    public boolean shouldCraftRecipe(World world, ServerPlayerEntity player, Recipe<?> recipe) {
        ProtectionManager protection = ProtectionManager.get(player.server);

        RuleQuery query = RuleQuery.forPlayer(player);
        if (protection.denies(query, ProtectionRule.CRAFTING)) {
            return false;
        }

        // [VanillaCopy]
        if (recipe.isIgnoredInRecipeBook() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
            this.setLastRecipe(recipe);
            return true;
        } else {
            return false;
        }
    }
}
