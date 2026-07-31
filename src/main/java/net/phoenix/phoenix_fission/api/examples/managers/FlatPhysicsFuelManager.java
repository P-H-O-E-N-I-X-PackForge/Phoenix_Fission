package net.phoenix.phoenix_fission.api.examples.managers;

import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;

public class FlatPhysicsFuelManager extends FissionFuelManager {

    public FlatPhysicsFuelManager(FissionWorkableElectricMultiblockMachine machine) {
        super(machine);
    }

    @Override
    protected double computeHeatThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return 1.0;
    }

    @Override
    protected double computeFuelConsumptionThermalScalar(double heat, double maxSafeHeat, double exponent) {
        return 1.0;
    }
}
