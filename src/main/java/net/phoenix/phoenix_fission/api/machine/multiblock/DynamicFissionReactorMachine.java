package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;

@MethodsReturnNonnullByDefault
public class DynamicFissionReactorMachine extends FissionWorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DynamicFissionReactorMachine.class,
            FissionWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public DynamicFissionReactorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
