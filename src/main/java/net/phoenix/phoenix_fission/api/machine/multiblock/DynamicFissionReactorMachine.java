package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;

@MethodsReturnNonnullByDefault
public class DynamicFissionReactorMachine extends FissionWorkableElectricMultiblockMachine {


    public DynamicFissionReactorMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof FissionWorkableElectricMultiblockMachine m)) {
            return RecipeModifier
                    .nullWrongType(FissionWorkableElectricMultiblockMachine.class, machine);
        }
        if (!m.isFormed()) return ModifierFunction.IDENTITY;

        return FissionFuelManager.buildRecipeModifier(m);
    }
}
