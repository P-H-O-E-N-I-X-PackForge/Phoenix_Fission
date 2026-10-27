package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FissionFuelManager {

    protected final FissionWorkableElectricMultiblockMachine machine;

    public FissionFuelManager(FissionWorkableElectricMultiblockMachine machine) {
        this.machine = machine;
    }

    private static double clamp(double val, double max) {
        return Math.max(0.0, Math.min(max, val));
    }

    public static ModifierFunction buildRecipeModifier(FissionWorkableElectricMultiblockMachine m) {
        int parallels = Math.max(1, m.getFuelManager().machine.lastParallels);
        double eutMulti = 1.0 + (m.getFuelManager().calcEUBoost() / 100.0);
        double durMulti = Math.max(0.01, 1.0 - (m.getFuelManager().calcFuelDiscount() / 100.0));

        var builder = ModifierFunction.builder().eutMultiplier(eutMulti).durationMultiplier(durMulti);
        if (parallels > 1) {
            var contentMultiplier = ContentModifier.multiplier(parallels);
            builder.inputModifier(contentMultiplier).outputModifier(contentMultiplier).parallels(parallels);
        }
        return builder.build();
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
        if (PhoenixFissionConfigs.INSTANCE.fission.heatModel.thermalModel ==
                PhoenixFissionConfigs.ThermalModel.RATED) {
            return 1.0;
        }
        double ratio = heat / Math.max(1.0, maxSafeHeat);
        if (PhoenixFissionConfigs.INSTANCE.fission.heatModel.exponentialHeatGrowth) {
            return Math.exp(ratio * exponent);
        }
        return Math.pow(1.0 + ratio, exponent);
    }

    protected double computeFuelConsumptionThermalScalar(double heat, double maxSafeHeat, double exponent) {
        if (PhoenixFissionConfigs.INSTANCE.fission.heatModel.thermalModel ==
                PhoenixFissionConfigs.ThermalModel.RATED) {
            return 1.0;
        }
        double ratio = heat / Math.max(1.0, maxSafeHeat);
        if (PhoenixFissionConfigs.INSTANCE.fission.heatModel.exponentialHeatGrowth) {
            return Math.exp(ratio * exponent);
        }
        return Math.pow(1.0 + ratio, exponent);
    }

    protected double computeModeratorHeatMultiplier() {
        return 1.0 + (calcEUBoost() / 100.0);
    }

    protected double computeModeratorFuelDiscountMultiplier() {
        return Math.max(0.0, 1.0 - (calcFuelDiscount() / 100.0));
    }

    protected double computeHeatDeltaForRodGroup(IFissionFuelRodType rod,
                                                 long count,
                                                 HeatContext context) {
        return rod.getBaseHeatProduction() * count * context.rodInteraction() * context.thermalFactor() *
                context.modBonus() * context.reactivity() * context.fuelConductivity();
    }

    protected double computeFuelConsumptionForRodGroup(IFissionFuelRodType rod, long count,
                                                       double rodInteraction,
                                                       double thermalScalar,
                                                       double discountMulti,
                                                       double reactivity) {
        double baseRate = (double) rod.getAmountPerCycle() / Math.max(1, rod.getDurationTicks());
        return baseRate * rodInteraction * thermalScalar * count * discountMulti * reactivity;
    }

    public record PowerOutputConfig(
                                    double minHeat,
                                    double maxSafeHeat,
                                    double euPerHeatUnit,
                                    double powerStart,
                                    double curveExponent) {}

    protected double computePowerOutput(double activity, double currentHeat, PowerOutputConfig config) {
        double baseEU = activity * config.euPerHeatUnit();

        double heatRange = Math.max(1.0, config.maxSafeHeat() - config.minHeat());
        double heatFraction = clamp((currentHeat - config.minHeat()) / heatRange, 1.5);

        double startThreshold = clamp(config.powerStart(), 0.99);
        double curveProgress = clamp((heatFraction - startThreshold) / (1.0 - startThreshold), 1.0);

        double exponent = Math.max(0.01, config.curveExponent());
        double bonusMultiplier = 1.0 + Math.pow(curveProgress, exponent) * 1.5;

        return baseEU * bonusMultiplier;
    }

    protected record HeatContext(
                                 double rodInteraction,
                                 double thermalFactor,
                                 double modBonus,
                                 double reactivity,
                                 double fuelConductivity) {}

    public double calculateTickHeat(int ignoredMachineParallels) {
        var comp = machine.getComponentManager();
        var activeRods = comp.getActiveFuelRods();
        double reactivity = machine.getReactivityFactor();

        if (activeRods.isEmpty() || reactivity <= 0.0) return 0.0;

        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        HeatContext context = new HeatContext(
                computeRodInteractionFactor(activeRods.size()),
                computeHeatThermalScalar(machine.getHeat(), machine.getMaxSafeHeatHU(), hm.heatGenerationExponent),
                computeModeratorHeatMultiplier(),
                reactivity,
                hm.fuelConductivity);

        return activeRods.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .mapToDouble(e -> computeHeatDeltaForRodGroup(e.getKey(), e.getValue(), context))
                .sum();
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
        double discountMulti = computeModeratorFuelDiscountMultiplier();

        Map<IFissionFuelRodType, Long> fuelRodThreads = comp.getActiveFuelRods().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (var threadGroup : fuelRodThreads.entrySet()) {
            IFissionFuelRodType fuelType = threadGroup.getKey();
            long countOfThisType = threadGroup.getValue();

            if (fuelType.getAmountPerCycle() <= 0) continue;

            double perTickConsumption = computeFuelConsumptionForRodGroup(
                    fuelType, countOfThisType,
                    rodInteractionFactor, heatScalar,
                    discountMulti, reactivity);

            String key = fuelType.getName();
            double remainder = machine.fuelRemainderPerType.getOrDefault(key, 0.0) + perTickConsumption;
            int directConsume = (int) Math.floor(remainder);

            if (directConsume <= 0) {
                machine.fuelRemainderPerType.put(key, remainder);
                continue;
            }

            String itemIn = fuelType.getFuelKey();
            if (itemIn.isEmpty()) {

                machine.fuelRemainderPerType.put(key, remainder);
                continue;
            }

            if (!processItemConsumption(itemIn, directConsume)) {

                machine.fuelRemainderPerType.put(key, remainder);
                continue;
            }

            String itemOut = fuelType.getOutputKey();
            if (!itemOut.isEmpty() && !"none".equalsIgnoreCase(itemOut) && !itemOut.equalsIgnoreCase(itemIn)) {
                ResourceLocation outRl = ResourceLocation.tryParse(itemOut);
                if (outRl != null && BuiltInRegistries.ITEM.containsKey(outRl)) {
                    machine.executeItemIO(
                            new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(outRl).orElse(null)), directConsume),
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
            if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl) ||
                    !machine.executeItemIO(new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(rl).orElse(null)), 1),
                            IO.IN, true))
                return false;
        }
        return true;
    }

    private boolean processItemConsumption(String id, int count) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) return false;
        return machine.executeItemIO(new ItemStack(Objects.requireNonNull(
                BuiltInRegistries.ITEM.getOptional(rl).orElse(null)), count), IO.IN, false);
    }

    public void processPowerGeneration(boolean running) {
        var fissionConfig = PhoenixFissionConfigs.INSTANCE.fission;

        if (!fissionConfig.enableDirectEUOutput || machine.isOverCooled || !running || machine.isScramActive()) {
            machine.lastGeneratedEUt = 0;
            return;
        }

        double activity = machine.lastHeatGainedPerTick;
        if (activity <= 0.0001) {
            machine.lastGeneratedEUt = 0;
            return;
        }

        var euConfig = fissionConfig.euOutput;

        PowerOutputConfig powerConfig = new PowerOutputConfig(
                0.0,
                machine.getMaxSafeHeatHU(),
                euConfig.euPerHeatUnit,
                euConfig.powerStartFraction,
                euConfig.powerCurveExponent);

        double finalEUt = computePowerOutput(activity, machine.getHeat(), powerConfig);

        if (euConfig.maxGeneratedEUt > 0) {
            finalEUt = Math.min(finalEUt, euConfig.maxGeneratedEUt);
        }

        long outputEU = (long) Math.floor(finalEUt);
        if (euConfig.minGeneratedEUt > 0) {
            outputEU = Math.max(outputEU, euConfig.minGeneratedEUt);
        }

        if (outputEU > 0) {
            machine.lastGeneratedEUt = outputEU;
            machine.getEnergyContainer().addEnergy(outputEU);
        } else {
            machine.lastGeneratedEUt = 0;
        }
    }

    public int calcEUBoost() {
        return Math.min(
                PhoenixFissionConfigs.INSTANCE.fission.maxEUBoostPercent,
                machine.getComponentManager().getActiveModerators().stream()
                        .mapToInt(IFissionModeratorType::getEUBoost)
                        .sum());
    }

    public int calcFuelDiscount() {
        return Math.min(
                PhoenixFissionConfigs.INSTANCE.fission.maxFuelDiscountPercent,
                machine.getComponentManager().getActiveModerators().stream()
                        .mapToInt(IFissionModeratorType::getFuelDiscount)
                        .sum());
    }

    public void addDisplayText(List<Component> textList) {
        long currentEUt = machine.lastGeneratedEUt;
        int tier = GTUtil.getTierByVoltage(currentEUt);
        String tierName = GTValues.VNF[tier];
        textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick",
                FormattingUtil.formatNumbers(currentEUt), tierName));
        textList.add(Component.translatable("phoenix_fission.parallels", machine.lastParallels));
        textList.add(Component.translatable("block.phoenix_fission.fission_moderator.boost",
                calcEUBoost() + "%"));
        textList.add(Component.translatable("block.phoenix_fission.fission_moderator.fuel_discount",
                calcFuelDiscount() + "%"));
    }
}
