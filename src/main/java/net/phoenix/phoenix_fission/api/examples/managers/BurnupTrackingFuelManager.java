package net.phoenix.phoenix_fission.api.examples.managers;

import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;

import java.util.HashMap;
import java.util.Map;

public class BurnupTrackingFuelManager extends FissionFuelManager {

    private final double burnupCapacityPerRod;

    private final double minOutputFraction;

    private final Map<String, Double> burnupAccumulator = new HashMap<>();

    public BurnupTrackingFuelManager(FissionWorkableElectricMultiblockMachine machine,
                                     double burnupCapacityPerRod, double minOutputFraction) {
        super(machine);
        this.burnupCapacityPerRod = Math.max(1.0, burnupCapacityPerRod);
        this.minOutputFraction = Math.max(0.0, Math.min(1.0, minOutputFraction));
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        burnupAccumulator.clear();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        burnupAccumulator.clear();
    }

    @Override
    protected double computeHeatDeltaForRodGroup(IFissionFuelRodType rod, long count, HeatContext context) {
        double base = super.computeHeatDeltaForRodGroup(rod, count, context);
        return base * getOutputFraction(rod, count);
    }

    @Override
    protected double computeFuelConsumptionForRodGroup(IFissionFuelRodType rod, long count,
                                                       double rodInteraction, double thermalScalar, double discountMult,
                                                       double reactivity) {
        double consumed = super.computeFuelConsumptionForRodGroup(
                rod, count, rodInteraction, thermalScalar, discountMult, reactivity);

        double perRodConsumed = consumed / Math.max(1, count);
        burnupAccumulator.merge(rod.getName(), perRodConsumed, Double::sum);

        return consumed;
    }

    private double getOutputFraction(IFissionFuelRodType rod, long count) {
        double totalCapacity = burnupCapacityPerRod * count;
        double consumed = burnupAccumulator.getOrDefault(rod.getName(), 0.0);
        double burnupFraction = Math.min(1.0, consumed / Math.max(1.0, totalCapacity));

        return 1.0 - burnupFraction * (1.0 - minOutputFraction);
    }

    public double getBurnupFraction(String rodName) {
        return burnupAccumulator.getOrDefault(rodName, 0.0) / burnupCapacityPerRod;
    }
}
