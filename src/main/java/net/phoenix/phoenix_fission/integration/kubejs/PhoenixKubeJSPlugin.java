package net.phoenix.phoenix_fission.integration.kubejs;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

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
import net.phoenix.phoenix_fission.integration.kubejs.recipe.PhoenixRecipeSchema;

import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;

public class PhoenixKubeJSPlugin extends dev.latvian.mods.kubejs.KubeJSPlugin {

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        filter.allow("net.phoenix.phoenix_fission");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), PhoenixRecipeSchema.SCHEMA);
        }
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("PhoenixFissionConfigs", PhoenixFissionConfigs.class);
        event.add("PhoenixFissionBlocks", PhoenixFissionBlocks.class);
        event.add("PhoenixFissionMachines", PhoenixFissionMachines.class);
        event.add("PhoenixFissionRecipeTypes", PhoenixRecipeTypes.class);
        event.add("FissionWorkableElectricMultiblockMachine", FissionWorkableElectricMultiblockMachine.class);
        event.add("BreederWorkableElectricMultiblockMachine", BreederWorkableElectricMultiblockMachine.class);
        event.add("DynamicFissionReactorMachine", DynamicFissionReactorMachine.class);
        event.add("MoltenSaltReactorMultiblockMachine", MoltenSaltReactorMultiblockMachine.class);
        event.add("PhoenixFission", PhoenixFission.class);
        event.add("PhoenixFissionPredicates", PhoenixFissionPredicates.class);
    }

    @Override
    public void init() {
        super.init();
        RegistryInfo.BLOCK.addType("fission_fuel_rod", FissionFuelRodBlockBuilder.class,
                FissionFuelRodBlockBuilder::new);
        RegistryInfo.BLOCK.addType("fission_cooler", FissionCoolerBlockBuilder.class,
                FissionCoolerBlockBuilder::new);
        RegistryInfo.BLOCK.addType("fission_moderator", FissionModeratorBlockBuilder.class,
                FissionModeratorBlockBuilder::new);
        RegistryInfo.BLOCK.addType("fission_blanket", FissionBlanketRodBlockBuilder.class,
                FissionBlanketRodBlockBuilder::new);
        RegistryInfo.BLOCK.addType("msr_core_liner", MSRCoreLinerBlockBuilder.class,
                MSRCoreLinerBlockBuilder::new);
        RegistryInfo.ITEM.addType("rtg_cell", RtgCellItemBuilder.class, RtgCellItemBuilder::new);
    }
}
