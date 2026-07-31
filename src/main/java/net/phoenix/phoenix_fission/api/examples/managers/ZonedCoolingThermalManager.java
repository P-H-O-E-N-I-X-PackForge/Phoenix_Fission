package net.phoenix.phoenix_fission.api.examples.managers;

import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;

public class ZonedCoolingThermalManager extends FissionThermalManager {

    private final double coldThreshold;
    private final double hotThreshold;

    private final double coldPassiveMult;
    private final double hotPassiveMult;

    private final double coldActiveMult;
    private final double hotActiveMult;

    public ZonedCoolingThermalManager(FissionWorkableElectricMultiblockMachine machine,
                                      double coldThreshold, double hotThreshold,
                                      double coldPassiveMult, double hotPassiveMult,
                                      double coldActiveMult, double hotActiveMult) {
        super(machine);
        this.coldThreshold = Math.max(0.0, Math.min(0.99, coldThreshold));
        this.hotThreshold = Math.max(coldThreshold, Math.min(1.0, hotThreshold));
        this.coldPassiveMult = Math.max(0.0, coldPassiveMult);
        this.hotPassiveMult = Math.max(0.0, hotPassiveMult);
        this.coldActiveMult = Math.max(0.0, coldActiveMult);
        this.hotActiveMult = Math.max(0.0, hotActiveMult);
    }

    @Override
    protected double computePassiveCoolingDelta(double heat, double ambientTemp, double conductivity) {
        double adjusted = conductivity * getPassiveZoneMultiplier(heat);
        return (ambientTemp - heat) * adjusted;
    }

    @Override
    protected double computeActiveCoolerHeatRemoval(IFissionCoolerType cooler, double heat,
                                                    long count, double conductivity) {
        double adjusted = conductivity * getActiveZoneMultiplier(heat);
        double delta = (cooler.getCoolerTemperature() - heat) * count * adjusted;
        return delta < 0 ? -delta : 0.0;
    }

    @Override
    protected int computeMeltdownGracePeriodTicks(double heat, double maxSafeHeat,
                                                  double minGrace, double baseGrace, double severity) {
        if (heat <= maxSafeHeat) return (int) (baseGrace * 20.0);

        double overheatRatio = heat / Math.max(1.0, maxSafeHeat);
        double doublings = Math.log(overheatRatio) / Math.log(2.0);

        double grace = Math.max(minGrace, baseGrace / Math.pow(2.0, doublings * severity));
        return (int) Math.max(1, Math.floor(grace * 20.0));
    }

    private double getPassiveZoneMultiplier(double heat) {
        return switch (getZone(heat)) {
            case COLD -> coldPassiveMult;
            case NOMINAL -> 1.0;
            case HOT -> hotPassiveMult;
        };
    }

    private double getActiveZoneMultiplier(double heat) {
        return switch (getZone(heat)) {
            case COLD -> coldActiveMult;
            case NOMINAL -> 1.0;
            case HOT -> hotActiveMult;
        };
    }

    private Zone getZone(double heat) {
        double fraction = heat / Math.max(1.0, machine.getMaxSafeHeatHU());
        if (fraction < coldThreshold) return Zone.COLD;
        if (fraction > hotThreshold) return Zone.HOT;
        return Zone.NOMINAL;
    }

    private enum Zone {
        COLD,
        NOMINAL,
        HOT
    }
}
