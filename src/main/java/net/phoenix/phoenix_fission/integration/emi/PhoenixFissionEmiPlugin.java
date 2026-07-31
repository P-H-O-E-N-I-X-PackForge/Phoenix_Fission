package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.machine.multiblock.DynamicFissionReactorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;
import net.phoenix.phoenix_fission.common.PhoenixFissionMachines;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

@EmiEntrypoint
public class PhoenixFissionEmiPlugin implements EmiPlugin {

    public static EmiRecipeCategory FUEL_BURN;
    public static EmiRecipeCategory COOLANT_CYCLE;
    public static EmiRecipeCategory MODERATOR;
    public static EmiRecipeCategory BLANKET;
    public static EmiRecipeCategory MSR_LINER;

    @Override
    public void register(EmiRegistry registry) {
        FUEL_BURN = new EmiRecipeCategory(
                PhoenixFission.id("fission_fuel_burn"),
                EmiStack.of(Items.BLAZE_POWDER));

        COOLANT_CYCLE = new EmiRecipeCategory(
                PhoenixFission.id("fission_coolant_cycle"),
                EmiStack.of(Items.PACKED_ICE));

        MODERATOR = new EmiRecipeCategory(
                PhoenixFission.id("fission_moderator"),
                EmiStack.of(Items.GLOWSTONE));

        BLANKET = new EmiRecipeCategory(
                PhoenixFission.id("fission_blanket"),
                EmiStack.of(Items.IRON_BARS));

        MSR_LINER = new EmiRecipeCategory(
                PhoenixFission.id("fission_msr_liner"),
                EmiStack.of(Items.MAGMA_BLOCK));

        registry.addCategory(FUEL_BURN);
        registry.addCategory(COOLANT_CYCLE);
        registry.addCategory(MODERATOR);
        registry.addCategory(BLANKET);
        registry.addCategory(MSR_LINER);

        for (var reactor : PhoenixFissionMachines.ALL_FISSION_REACTORS) {
            if (reactor.definition() == null) continue;
            EmiStack stack = safeStack(reactor.definition().asStack());
            if (stack.isEmpty()) continue;

            Class<? extends FissionWorkableElectricMultiblockMachine> machineClass = reactor.machineClass();

            registry.addWorkstation(COOLANT_CYCLE, stack);

            if (MoltenSaltReactorMultiblockMachine.class.isAssignableFrom(machineClass)) {
                registry.addWorkstation(MSR_LINER, stack);
            } else if (DynamicFissionReactorMachine.class.isAssignableFrom(machineClass)) {
                registry.addWorkstation(FUEL_BURN, stack);
                registry.addWorkstation(MODERATOR, stack);
                registry.addWorkstation(BLANKET, stack);
            }
        }

        for (var entry : PhoenixAPI.FISSION_FUEL_RODS.entrySet()) {
            var block = entry.getValue().get();
            if (block != null) {
                EmiStack s = safeStack(new ItemStack(block.asItem()));
                if (!s.isEmpty()) registry.addWorkstation(FUEL_BURN, s);
            }
        }

        for (var entry : PhoenixAPI.FISSION_COOLERS.entrySet()) {
            var block = entry.getValue().get();
            if (block != null) {
                EmiStack s = safeStack(new ItemStack(block.asItem()));
                if (!s.isEmpty()) registry.addWorkstation(COOLANT_CYCLE, s);
            }
        }

        for (var entry : PhoenixAPI.FISSION_MODERATORS.entrySet()) {
            var block = entry.getValue().get();
            if (block != null) {
                EmiStack s = safeStack(new ItemStack(block.asItem()));
                if (!s.isEmpty()) registry.addWorkstation(MODERATOR, s);
            }
        }

        for (var entry : PhoenixAPI.FISSION_BLANKETS.entrySet()) {
            var block = entry.getValue().get();
            if (block != null) {
                EmiStack s = safeStack(new ItemStack(block.asItem()));
                if (!s.isEmpty()) registry.addWorkstation(BLANKET, s);
            }
        }

        for (var entry : PhoenixAPI.MSR_LINERS.entrySet()) {
            var block = entry.getValue().get();
            if (block != null) {
                EmiStack s = safeStack(new ItemStack(block.asItem()));
                if (!s.isEmpty()) registry.addWorkstation(MSR_LINER, s);
            }
        }

        for (var fuel : PhoenixAPI.FISSION_FUEL_RODS.keySet()) {
            try {
                registry.addRecipe(new FissionFuelBurnEmiRecipe(fuel));
            } catch (Exception ignored) {}
        }
        for (var cooler : PhoenixAPI.FISSION_COOLERS.keySet()) {
            try {
                registry.addRecipe(new FissionCoolantCycleEmiRecipe(cooler));
            } catch (Exception ignored) {}
        }
        for (var moderator : PhoenixAPI.FISSION_MODERATORS.keySet()) {
            try {
                registry.addRecipe(new FissionModeratorEmiRecipe(moderator));
            } catch (Exception ignored) {}
        }
        for (var blanket : PhoenixAPI.FISSION_BLANKETS.keySet()) {
            try {
                registry.addRecipe(new FissionBlanketEmiRecipe(blanket));
            } catch (Exception ignored) {}
        }
        for (var liner : PhoenixAPI.MSR_LINERS.keySet()) {
            try {
                registry.addRecipe(new FissionMsrLinerEmiRecipe(liner));
            } catch (Exception ignored) {}
        }
    }

    private static EmiStack safeStack(ItemStack stack) {
        return (stack == null || stack.isEmpty()) ? EmiStack.EMPTY : EmiStack.of(stack);
    }
}
