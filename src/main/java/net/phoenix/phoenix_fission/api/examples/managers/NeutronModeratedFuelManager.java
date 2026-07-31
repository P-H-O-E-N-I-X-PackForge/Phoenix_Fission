package net.phoenix.phoenix_fission.api.examples.managers;

import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;

public class NeutronModeratedFuelManager extends FissionFuelManager {

    private final double maxModeratorRatio;

    public NeutronModeratedFuelManager(FissionWorkableElectricMultiblockMachine machine,
                                       double maxModeratorRatio) {
        super(machine);
        this.maxModeratorRatio = Math.max(1.0, maxModeratorRatio);
    }

    @Override
    protected double computeRodInteractionFactor(int totalRods) {
        if (totalRods <= 0) return 1.0;

        int moderatorCount = machine.getComponentManager().getActiveModerators().size();
        double moderatorRatio = Math.min((double) moderatorCount / totalRods, maxModeratorRatio);
        double moderatorBonus = moderatorRatio / maxModeratorRatio;

        return Math.sqrt((totalRods + 1.0) / 2.0) * (1.0 + moderatorBonus);
    }
}
