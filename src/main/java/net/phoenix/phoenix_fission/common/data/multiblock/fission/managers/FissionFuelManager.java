package net.phoenix.phoenix_fission.common.data.multiblock.fission.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.block.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.IFissionModeratorType;
import net.phoenix.phoenix_fission.common.data.multiblock.fission.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FissionFuelManager {

    protected final FissionWorkableElectricMultiblockMachine machine;

    public FissionFuelManager(FissionWorkableElectricMultiblockMachine machine) {
        this.machine = machine;
    }

    public void onStructureFormed() {
        recalcRuntimeParallels();
    }

    public void onStructureInvalid() {
        machine.lastParallels = 1;
        machine.lastGeneratedEUt = 0;
    }

    public void recalcRuntimeParallels() {
        int totalRods = machine.getComponentManager().getActiveFuelRods().size();
        machine.lastParallels = Math.max(1, totalRods);
    }

    protected double computeRodInteractionFactor(int totalRods) {
        return (totalRods + 1.0) / 2.0;
    }

    protected double computeHeatThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return Math.pow(1.0 + (heat / Math.max(1.0, maxSafeHeat)), exponent);
    }

    protected double computeFuelConsumptionThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return Math.pow(1.0 + (heat / Math.max(1.0, maxSafeHeat)), exponent);
    }

    protected double computeModeratorHeatMultiplier() {
        return 1.0 + (getModeratorEUBoostClamped() / 100.0);
    }

    protected double computeModeratorFuelDiscountMultiplier() {
        return Math.max(0.0, 1.0 - (getModeratorFuelDiscountClamped() / 100.0));
    }

    protected double computeHeatDeltaForRodGroup(IFissionFuelRodType rod, long count,
                                                 double rodInteraction, double thermalScalar,
                                                 double moderatorMultiplier,
                                                 double reactivity, double fuelConductivity) {
        return rod.getBaseHeatProduction() * rodInteraction * thermalScalar * count * moderatorMultiplier * reactivity *
                fuelConductivity;
    }

    protected double computeFuelConsumptionForRodGroup(IFissionFuelRodType rod, long count,
                                                       double rodInteraction, double thermalScalar, double discountMult,
                                                       double reactivity) {
        double baseRate = (double) rod.getAmountPerCycle() / Math.max(1, rod.getDurationTicks());
        return baseRate * rodInteraction * thermalScalar * count * discountMult * reactivity;
    }

    protected double computePowerOutput(double activity, double currentHeat, double minHeat,
                                        double maxSafeHeat, double euPerHeatUnit, double powerStart,
                                        double curveExponent) {
        double baseEU = activity * euPerHeatUnit;

        double range = Math.max(1.0, maxSafeHeat - minHeat);
        double fraction = Math.max(0.0, Math.min(1.5, (currentHeat - minHeat) / range));
        double start = Math.max(0.0, Math.min(0.99, powerStart));
        double targetCurve = Math.max(0.0, Math.min(1.0, (fraction - start) / Math.max(1e-9, 1.0 - start)));
        double bonus = 1.0 + Math.pow(targetCurve, Math.max(0.01, curveExponent)) * 1.5;
        return baseEU * bonus;
    }

    public double calculateTickHeat(int ignoredMachineParallels) {
        var comp = machine.getComponentManager();
        if (comp.getActiveFuelRods().isEmpty() || machine.getReactivityFactor() <= 0.0) return 0.0;
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;

        int totalRods = comp.getActiveFuelRods().size();
        double rodInteractionFactor = computeRodInteractionFactor(totalRods);
        double thermalFactor = computeHeatThermalScalar(machine.getHeat(), machine.getMaxSafeHeatHU(),
                hm.heatGenerationExponent);
        double reactivity = machine.getReactivityFactor();
        double modBonus = computeModeratorHeatMultiplier();

        Map<IFissionFuelRodType, Long> fuelRodThreads = comp.getActiveFuelRods().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double aggregateReactorHeatDelta = 0.0;

        for (var threadGroup : fuelRodThreads.entrySet()) {
            aggregateReactorHeatDelta += computeHeatDeltaForRodGroup(
                    threadGroup.getKey(), threadGroup.getValue(),
                    rodInteractionFactor, thermalFactor, modBonus,
                    reactivity, hm.fuelConductivity);
        }

        return aggregateReactorHeatDelta;
    }

    public void consumeFuelTick(int ignoredMachineParallels) {
        var comp = machine.getComponentManager();
        if (comp.getActiveFuelRods().isEmpty() || machine.getReactivityFactor() <= 0.0) return;

        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        int totalRods = comp.getActiveFuelRods().size();
        double rodInteractionFactor = computeRodInteractionFactor(totalRods);
        double heatScalar = computeFuelConsumptionThermalScalar(machine.getHeat(), machine.getMaxSafeHeatHU(),
                hm.fuelConsumptionExponent);
        double reactivity = machine.getReactivityFactor();
        double discountMult = computeModeratorFuelDiscountMultiplier();

        Map<IFissionFuelRodType, Long> fuelRodThreads = comp.getActiveFuelRods().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (var threadGroup : fuelRodThreads.entrySet()) {
            IFissionFuelRodType fuelType = threadGroup.getKey();
            long countOfThisType = threadGroup.getValue();

            if (fuelType.getAmountPerCycle() <= 0) continue;

            double perTickConsumption = computeFuelConsumptionForRodGroup(
                    fuelType, countOfThisType,
                    rodInteractionFactor, heatScalar,
                    discountMult, reactivity);

            String key = fuelType.getName();
            double remainder = machine.fuelRemainderPerType.getOrDefault(key, 0.0) + perTickConsumption;
            int directConsume = (int) Math.floor(remainder);

            if (directConsume <= 0) {
                machine.fuelRemainderPerType.put(key, remainder);
                continue;
            }
            machine.fuelRemainderPerType.put(key, remainder - directConsume);

            String itemIn = fuelType.getFuelKey();
            if (itemIn.isEmpty() || !processItemConsumption(itemIn, directConsume)) {
                machine.setHeat(Math.max(machine.getHeat(), machine.getMaxSafeHeatHU() + 1.0));
                continue;
            }

            String itemOut = fuelType.getOutputKey();
            if (!itemOut.isEmpty() && !"none".equalsIgnoreCase(itemOut) && !itemOut.equalsIgnoreCase(itemIn)) {
                ResourceLocation outRl = ResourceLocation.tryParse(itemOut);
                if (outRl != null && ForgeRegistries.ITEMS.containsKey(outRl)) {
                    machine.executeItemIO(
                            new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(outRl)), directConsume),
                            IO.OUT);
                }
            }
        }
    }

    public boolean hasFuelAvailableForNextTick() {
        Set<String> verifiedTypes = new HashSet<>();
        for (IFissionFuelRodType rod : machine.getComponentManager().getActiveFuelRods()) {
            if (rod.getFuelKey().isEmpty() || verifiedTypes.contains(rod.getFuelKey())) continue;
            verifiedTypes.add(rod.getFuelKey());
            ResourceLocation rl = ResourceLocation.tryParse(rod.getFuelKey());
            if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl) ||
                    !machine.executeItemIO(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(rl)), 1),
                            IO.IN, true))
                return false;
        }
        return true;
    }

    private boolean processItemConsumption(String id, int count) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) return false;
        return machine.executeItemIO(new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(rl)), count),
                IO.IN, false);
    }

    public void processPowerGeneration(boolean running) {
        var eu = PhoenixFissionConfigs.INSTANCE.fission.euOutput;
        if (!PhoenixFissionConfigs.INSTANCE.fission.enableDirectEUOutput) return;
        if (machine.isOverCooled) return;

        double ambientHU = PhoenixFissionConfigs.INSTANCE.fission.heatModel.ambientTemperatureHU;
        double activity = (running && machine.lastHeatGainedPerTick > 0.0) ? machine.lastHeatGainedPerTick :
                Math.max(0.0, machine.getHeat() - ambientHU);
        if (activity <= 0.0001) return;

        double finalEUt = computePowerOutput(activity, machine.getHeat(),
                PhoenixFissionConfigs.INSTANCE.fission.heatModel.ambientTemperatureHU,
                machine.getMaxSafeHeatHU(),
                eu.euPerHeatUnit, eu.powerStartFraction, eu.powerCurveExponent);

        if (eu.maxGeneratedEUt > 0) finalEUt = Math.min(finalEUt, eu.maxGeneratedEUt);
        long outputEU = (long) Math.floor(finalEUt);
        if (running && eu.minGeneratedEUt > 0) outputEU = Math.max(outputEU, eu.minGeneratedEUt);

        if (outputEU > 0) {
            machine.lastGeneratedEUt = outputEU;
            machine.getEnergyContainer();
            machine.getEnergyContainer().addEnergy(outputEU);
        }
    }

    public static ModifierFunction buildRecipeModifier(FissionWorkableElectricMultiblockMachine m) {
        int parallels = Math.max(1, m.getFuelManager().machine.lastParallels);
        double eutMult = 1.0 + (m.getFuelManager().getModeratorEUBoostClamped() / 100.0);
        double durMult = Math.max(0.01, 1.0 - (m.getFuelManager().getModeratorFuelDiscountClamped() / 100.0));

        var builder = ModifierFunction.builder().eutMultiplier(eutMult).durationMultiplier(durMult);
        if (parallels > 1) {
            var contentMultiplier = com.gregtechceu.gtceu.api.recipe.content.ContentModifier.multiplier(parallels);
            builder.inputModifier(contentMultiplier).outputModifier(contentMultiplier).parallels(parallels);
        }
        return builder.build();
    }

    public int getModeratorEUBoostClamped() {
        return Math.min(machine.getComponentManager().getActiveModerators().stream()
                .mapToInt(IFissionModeratorType::getEUBoost).sum(),
                PhoenixFissionConfigs.INSTANCE.fission.maxEUBoostPercent);
    }

    public int getModeratorFuelDiscountClamped() {
        return Math.min(
                machine.getComponentManager().getActiveModerators().stream()
                        .mapToInt(IFissionModeratorType::getFuelDiscount).sum(),
                PhoenixFissionConfigs.INSTANCE.fission.maxFuelDiscountPercent);
    }

    public void addDisplayText(List<Component> textList) {
        long currentEUt = machine.lastGeneratedEUt;
        int tier = GTUtil.getTierByVoltage(currentEUt);
        String tierName = com.gregtechceu.gtceu.api.GTValues.VNF[tier];
        textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick",
                FormattingUtil.formatNumbers(currentEUt), tierName));
        textList.add(Component.translatable("phoenix_fission.parallels", machine.lastParallels));
        textList.add(Component.translatable("block.phoenix_fission.fission_moderator.boost",
                getModeratorEUBoostClamped() + "%"));
        textList.add(Component.translatable("block.phoenix_fission.fission_moderator.fuel_discount",
                getModeratorFuelDiscountClamped() + "%"));
    }
}
