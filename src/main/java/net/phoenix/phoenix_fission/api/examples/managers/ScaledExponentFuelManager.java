package net.phoenix.phoenix_fission.api.examples.managers;

import net.phoenix.phoenix_fission.common.data.multiblock.fission.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.common.data.multiblock.fission.managers.FissionFuelManager;

public class ScaledExponentFuelManager extends FissionFuelManager {

    private final double heatExponent;
    private final double fuelExponent;

    public ScaledExponentFuelManager(FissionWorkableElectricMultiblockMachine machine,
                                     double heatExponent, double fuelExponent) {
        super(machine);
        this.heatExponent = heatExponent;
        this.fuelExponent = fuelExponent;
    }

    @Override
    protected double computeHeatThermalScalar(double heat, double maxSafeHeat) {
        return Math.pow(1.0 + (heat / Math.max(1.0, maxSafeHeat)), heatExponent);
    }

    @Override
    protected double computeFuelConsumptionThermalScalar(double heat, double maxSafeHeat) {
        return Math.pow(1.0 + (heat / Math.max(1.0, maxSafeHeat)), fuelExponent);
    }
}
