package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;


import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedFissionStabilitySensorPart extends SensorHatchPartMachine {


    @SaveField
    @Getter
    @Setter
    private int minPercent = 0;
    @SaveField
    @Getter
    @Setter
    private int maxPercent = 95;
    @SaveField
    @Getter
    @Setter
    private boolean inverted = false;

    @SaveField
    @Getter
    @Setter
    private int emitStrength = 15;

    public AdvancedFissionStabilitySensorPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        var controller = getController();
        if (!(controller instanceof FissionWorkableElectricMultiblockMachine fission)) return 0;

        double maxSafe = fission.getMaxSafeHeatHU();
        if (maxSafe <= 0) return 0;

        double heatPct = (fission.getHeat() / maxSafe) * 100.0;
        boolean inRange = heatPct >= minPercent && heatPct <= maxPercent;
        boolean emit = inverted != inRange;

        return emit ? Mth.clamp(emitStrength, 1, 15) : 0;
    }
}
