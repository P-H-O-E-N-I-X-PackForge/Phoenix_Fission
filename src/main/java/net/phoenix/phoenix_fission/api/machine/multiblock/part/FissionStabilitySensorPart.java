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

public class FissionStabilitySensorPart extends SensorHatchPartMachine {


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

    public FissionStabilitySensorPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        var controller = getController();
        if (!(controller instanceof FissionWorkableElectricMultiblockMachine fission)) return 0;

        double maxSafe = fission.getMaxSafeHeatHU();
        if (maxSafe <= 0) return 0;

        double heatPct = (fission.getHeat() / maxSafe) * 100.0;

        int strength = (int) Math.round((heatPct / 200.0) * 15.0);
        strength = Mth.clamp(strength, 0, 15);

        boolean inRange = heatPct >= minPercent && heatPct <= maxPercent;
        boolean emit = inverted != inRange;

        return emit ? strength : 0;
    }
}
