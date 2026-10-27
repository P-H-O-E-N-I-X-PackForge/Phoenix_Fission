package net.phoenix.phoenix_fission.api.examples.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.List;
import java.util.Objects;

public class LegacyPhysicsFuelManager extends FissionFuelManager {

    private final double baseHeatPerTick;

    private final double heatPerParallel;

    private final boolean fuelScalesWithRodCount;

    private final boolean fuelScalesWithParallels;

    public LegacyPhysicsFuelManager(FissionWorkableElectricMultiblockMachine machine) {
        this(machine, 0.0, 0.0, true, false);
    }

    public LegacyPhysicsFuelManager(FissionWorkableElectricMultiblockMachine machine,
                                    double baseHeatPerTick, double heatPerParallel,
                                    boolean fuelScalesWithRodCount, boolean fuelScalesWithParallels) {
        super(machine);
        this.baseHeatPerTick = Math.max(0.0, baseHeatPerTick);
        this.heatPerParallel = Math.max(0.0, heatPerParallel);
        this.fuelScalesWithRodCount = fuelScalesWithRodCount;
        this.fuelScalesWithParallels = fuelScalesWithParallels;
    }

    @Override
    public void recalcRuntimeParallels() {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        int rodCount = machine.getComponentManager().getActiveFuelRods().size();

        int base = cfg.parallelsPerFuelRod * rodCount;

        int modBonus = machine.getComponentManager().getActiveModerators().stream()
                .mapToInt(IFissionModeratorType::getParallelBonus)
                .sum();

        int heatBonus = 0;
        if (heatPerParallel > 0.0) {
            heatBonus = (int) Math.floor(Math.max(0.0, machine.getHeat()) / heatPerParallel);
        }

        int total = base + modBonus + heatBonus;
        machine.lastParallels = Math.max(1, Math.min(total, cfg.maxParallels));
    }

    @Override
    public double calculateTickHeat(int machineParallels) {
        List<IFissionFuelRodType> rods = machine.getComponentManager().getActiveFuelRods();
        if (rods.isEmpty() || machine.getReactivityFactor() <= 0.0) return 0.0;

        double totalRodHeat = rods.stream()
                .mapToDouble(IFissionFuelRodType::getBaseHeatProduction)
                .sum();

        double modAdd = machine.getComponentManager().getActiveModerators().stream()
                .mapToDouble(IFissionModeratorType::getHeatMultiplier)
                .sum();
        double moderatorMulti = 0.1 + (modAdd / 10.0);

        double parallels = Math.max(1.0, machine.lastParallels);
        double burnMulti = computeBurnMultiplier();
        double reactivity = machine.getReactivityFactor();

        return (baseHeatPerTick + totalRodHeat * moderatorMulti) * parallels * burnMulti * reactivity;
    }

    @Override
    public void consumeFuelTick(int machineParallels) {
        var comp = machine.getComponentManager();
        if (comp.getActiveFuelRods().isEmpty() || machine.getReactivityFactor() <= 0.0) return;

        IFissionFuelRodType fuelType = comp.getPrimaryFuelRodType();
        if (fuelType == null) fuelType = comp.getActiveFuelRods().get(0);

        int amountPerCycle = Math.max(0, fuelType.getAmountPerCycle());
        if (amountPerCycle == 0) return;

        double totalPerCycle = amountPerCycle;
        if (fuelScalesWithRodCount) totalPerCycle *= Math.max(1, comp.getActiveFuelRods().size());
        if (fuelScalesWithParallels) totalPerCycle *= Math.max(1, machine.lastParallels);

        double discountMult = computeModeratorFuelDiscountMultiplier();
        totalPerCycle *= discountMult;

        double perTick = totalPerCycle / Math.max(1, fuelType.getDurationTicks());

        String key = fuelType.getName();
        double remainder = machine.fuelRemainderPerType.getOrDefault(key, 0.0) + perTick;
        int toConsume = (int) Math.floor(remainder);

        if (toConsume <= 0) {
            machine.fuelRemainderPerType.put(key, remainder);
            return;
        }
        machine.fuelRemainderPerType.put(key, remainder - toConsume);

        String itemIn = fuelType.getFuelKey();
        if (itemIn.isEmpty()) return;

        ResourceLocation inRl = ResourceLocation.tryParse(itemIn);
        if (inRl == null || !BuiltInRegistries.ITEM.containsKey(inRl)) return;

        ItemStack inStack = new ItemStack(
                Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(inRl).orElse(null)), toConsume);

        if (!machine.executeItemIO(inStack, IO.IN, false)) {
            machine.setHeat(Math.max(machine.getHeat(),
                    machine.getMaxSafeHeatHU() + 1.0));
            return;
        }

        String itemOut = fuelType.getOutputKey();
        if (!itemOut.isEmpty() && !"none".equalsIgnoreCase(itemOut) && !itemOut.equalsIgnoreCase(itemIn)) {
            ResourceLocation outRl = ResourceLocation.tryParse(itemOut);
            if (outRl != null && BuiltInRegistries.ITEM.containsKey(outRl)) {
                machine.executeItemIO(
                        new ItemStack(
                                Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(outRl).orElse(null)), toConsume),
                        IO.OUT);
            }
        }
    }

    @Override
    protected double computePowerOutput(double activity, double currentHeat, PowerOutputConfig config) {
        double base = super.computePowerOutput(activity, currentHeat, config);
        return base * computeBurnMultiplier();
    }

    @Override
    protected double computeHeatThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return 1.0;
    }

    @Override
    protected double computeFuelConsumptionThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return 1.0;
    }

    protected double computeBurnMultiplier() {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        double maxPct = Math.max(0.0, cfg.burnBonusMaxPercent);
        double rampTicks = Math.max(1.0, cfg.burnBonusRampSeconds * 20.0);
        double t = Math.min(1.0, machine.continuousBurnTicks / rampTicks);
        return 1.0 + (maxPct / 100.0) * t;
    }
}
