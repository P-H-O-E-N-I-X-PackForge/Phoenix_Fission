package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.managers;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.phoenix.phoenix_fission.gtm8.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.gtm8.api.block.iface.IFissionModeratorType;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
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

    public double getFuelItemsPerTick(IFissionFuelRodType rod, long count, double reactivity) {
        if (rod.getAmountPerCycle() <= 0 || count <= 0) return 0.0;
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        int totalRods = machine.getComponentManager().getActiveFuelRods().size();
        double heatScalar = computeFuelConsumptionThermalScalar(machine.getHeat(), machine.getMaxSafeHeatHU(),
                hm.fuelConsumptionExponent);
        return computeFuelConsumptionForRodGroup(rod, count, computeRodInteractionFactor(totalRods), heatScalar,
                computeModeratorFuelDiscountMultiplier(), reactivity);
    }

    public static String formatInterval(double seconds) {
        if (!(seconds > 0) || Double.isInfinite(seconds)) return "never";
        long s = Math.round(seconds);
        if (s < 120) return s + "s";
        long m = s / 60;
        if (m < 120) return m + "m " + (s % 60) + "s";
        return (m / 60) + "h " + (m % 60) + "m";
    }

    private static final double CHARGE_EPSILON = 1.0e-9;

    private static final int MAX_WITHDRAWALS_PER_TICK = 64;

    private static int itemsLoaded(double charge) {
        return charge <= CHARGE_EPSILON ? 0 : (int) Math.ceil(charge - CHARGE_EPSILON);
    }

    public void consumeFuelTick(int ignoredMachineParallels) {
        var comp = machine.getComponentManager();
        if (comp.getActiveFuelRods().isEmpty() || machine.getReactivityFactor() <= 0.0) return;

        double reactivity = machine.getReactivityFactor();

        Map<IFissionFuelRodType, Long> fuelRodThreads = comp.getActiveFuelRods().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (var threadGroup : fuelRodThreads.entrySet()) {
            IFissionFuelRodType fuelType = threadGroup.getKey();
            long countOfThisType = threadGroup.getValue();

            if (fuelType.getAmountPerCycle() <= 0) continue;

            String itemIn = fuelType.getFuelKey();

            if (hasNoItemFuel(itemIn)) continue;

            double perTick = getFuelItemsPerTick(fuelType, countOfThisType, reactivity);
            if (perTick <= 0.0) continue;

            String key = fuelType.getName();
            double charge = machine.fuelRemainderPerType.getOrDefault(key, 0.0);

            for (int i = 0; charge < perTick && i < MAX_WITHDRAWALS_PER_TICK; i++) {
                if (!processItemConsumption(itemIn, 1)) break;
                charge += 1.0;
            }

            int loadedBeforeBurn = itemsLoaded(charge);
            charge = Math.max(0.0, charge - perTick);
            if (charge <= CHARGE_EPSILON) charge = 0.0;
            int finished = loadedBeforeBurn - itemsLoaded(charge);

            machine.fuelRemainderPerType.put(key, charge);

            if (finished > 0) outputDepletedFuel(fuelType, itemIn, finished);
        }
    }

    private void outputDepletedFuel(IFissionFuelRodType fuelType, String itemIn, int count) {
        String itemOut = fuelType.getOutputKey();
        if (itemOut.isEmpty() || "none".equalsIgnoreCase(itemOut) || itemOut.equalsIgnoreCase(itemIn)) return;
        ResourceLocation outRl = ResourceLocation.tryParse(itemOut);
        if (outRl == null || !ForgeRegistries.ITEMS.containsKey(outRl)) return;
        machine.executeItemIO(
                new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(outRl)), count),
                IO.OUT);
    }

    public boolean hasFuelAvailableForNextTick() {
        Set<String> verifiedTypes = new HashSet<>();
        for (IFissionFuelRodType rod : machine.getComponentManager().getActiveFuelRods()) {
            if (hasNoItemFuel(rod.getFuelKey()) || !verifiedTypes.add(rod.getName())) continue;
            if (machine.fuelRemainderPerType.getOrDefault(rod.getName(), 0.0) > CHARGE_EPSILON) continue;

            ResourceLocation rl = ResourceLocation.tryParse(rod.getFuelKey());
            if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl) ||
                    !machine.executeItemIO(
                            new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(rl)),
                                    1),
                            IO.IN, true))
                return false;
        }
        return true;
    }

    private static boolean hasNoItemFuel(String key) {
        if (key == null || key.isEmpty()) return true;
        ResourceLocation rl = ResourceLocation.tryParse(key);
        return rl != null && rl.equals(ForgeRegistries.ITEMS.getDefaultKey());
    }

    private boolean processItemConsumption(String id, int count) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) return false;
        return machine.executeItemIO(new ItemStack(Objects.requireNonNull(
                ForgeRegistries.ITEMS.getValue(rl)), count), IO.IN, false);
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
