package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

/** Item and fluid IO against a multiblock's hatches and busses, by way of a throwaway recipe. */
public final class HatchIO {

    private HatchIO() {}

    private static boolean run(IRecipeCapabilityHolder machine, GTRecipe dummy, IO type, boolean simulate) {
        if (type == IO.IN) {
            boolean matches = RecipeHelper.matchRecipe(machine, dummy).isSuccess();
            if (simulate || !matches) return matches;
        }
        if (simulate) return true;
        java.util.Map<RecipeCapability<?>, Object2IntMap<?>> caches =
                machine instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine logic ?
                        logic.getRecipeLogic().getChanceCaches() : new java.util.HashMap<>();
        return RecipeHelper.handleRecipeIO(machine, dummy, type, caches).isSuccess();
    }

    public static boolean fluid(IRecipeCapabilityHolder machine, FluidStack stack, IO type, boolean simulate) {
        if (stack == null || stack.isEmpty()) return false;
        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw();
        if (type == IO.IN) builder.input(FluidRecipeCapability.CAP, FluidIngredient.of(stack));
        else builder.output(FluidRecipeCapability.CAP, FluidIngredient.of(stack));
        return run(machine, builder.buildRawRecipe(), type, simulate);
    }

    public static boolean item(IRecipeCapabilityHolder machine, ItemStack stack, IO type, boolean simulate) {
        if (stack == null || stack.isEmpty()) return false;
        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw();
        if (type == IO.IN) builder.input(ItemRecipeCapability.CAP, SizedIngredient.create(stack));
        else builder.output(ItemRecipeCapability.CAP, SizedIngredient.create(stack));
        return run(machine, builder.buildRawRecipe(), type, simulate);
    }
}
