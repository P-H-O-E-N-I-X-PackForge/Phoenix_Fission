package net.phoenix.phoenix_fission.integration.kubejs;

import net.minecraft.core.registries.Registries;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.machine.multiblock.BreederWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.DynamicFissionReactorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;
import net.phoenix.phoenix_fission.api.pattern.PhoenixFissionPredicates;
import net.phoenix.phoenix_fission.common.PhoenixFissionMachines;
import net.phoenix.phoenix_fission.common.data.PhoenixRecipeTypes;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;
import net.phoenix.phoenix_fission.integration.kubejs.builders.*;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class PhoenixKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("net.phoenix.phoenix_fission");
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("PhoenixFissionConfigs", PhoenixFissionConfigs.class);
        bindings.add("PhoenixFissionBlocks", PhoenixFissionBlocks.class);
        bindings.add("PhoenixFissionMachines", PhoenixFissionMachines.class);
        bindings.add("PhoenixFissionRecipeTypes", PhoenixRecipeTypes.class);
        bindings.add("FissionWorkableElectricMultiblockMachine", FissionWorkableElectricMultiblockMachine.class);
        bindings.add("BreederWorkableElectricMultiblockMachine", BreederWorkableElectricMultiblockMachine.class);
        bindings.add("DynamicFissionReactorMachine", DynamicFissionReactorMachine.class);
        bindings.add("MoltenSaltReactorMultiblockMachine", MoltenSaltReactorMultiblockMachine.class);
        bindings.add("PhoenixFission", PhoenixFission.class);
        bindings.add("PhoenixFissionPredicates", PhoenixFissionPredicates.class);
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        // Registered under the kubejs namespace so scripts keep writing event.create('id', 'fission_cooler').
        registry.of(Registries.BLOCK, callback -> {
            callback.add("fission_fuel_rod", FissionFuelRodBlockBuilder.class, FissionFuelRodBlockBuilder::new);
            callback.add("fission_cooler", FissionCoolerBlockBuilder.class, FissionCoolerBlockBuilder::new);
            callback.add("fission_moderator", FissionModeratorBlockBuilder.class, FissionModeratorBlockBuilder::new);
            callback.add("fission_blanket", FissionBlanketRodBlockBuilder.class, FissionBlanketRodBlockBuilder::new);
            callback.add("msr_core_liner", MSRCoreLinerBlockBuilder.class, MSRCoreLinerBlockBuilder::new);
        });
        registry.of(Registries.ITEM, callback -> callback.add("rtg_cell", RtgCellItemBuilder.class,
                RtgCellItemBuilder::new));
    }
}
