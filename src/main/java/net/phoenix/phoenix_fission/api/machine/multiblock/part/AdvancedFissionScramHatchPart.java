package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class AdvancedFissionScramHatchPart extends TieredPartMachine {


    @SaveField
    private int signalThreshold = 8;

    @SaveField
    private int sustainTicks = 5;

    private int sustainCounter = 0;

    @Getter
    private boolean isScrammed = false;

    public AdvancedFissionScramHatchPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateScramStatus();
    }

    @Override
    public void addedToController(@NotNull MultiblockControllerMachine controller) {
        super.addedToController(controller);
        updateScramStatus();
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return true;
    }

    @Override
    public void onNeighborChanged(@NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateScramStatus();
    }

    public void tick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        int signal = level.getBestNeighborSignal(getBlockPos());

        if (signal >= signalThreshold) {
            sustainCounter = Math.min(sustainCounter + 1, sustainTicks);
            if (sustainCounter >= sustainTicks && !isScrammed) {
                isScrammed = true;
                notifyController();
            }
        } else {
            if (sustainCounter > 0 || isScrammed) {
                sustainCounter = 0;
                isScrammed = false;
                notifyController();
            }
        }
    }

    private void updateScramStatus() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        if (level.getBestNeighborSignal(getBlockPos()) < signalThreshold) {
            sustainCounter = 0;
            if (isScrammed) {
                isScrammed = false;
                notifyController();
            }
        }
    }

    private void notifyController() {
        for (var controller : getControllers()) {
            if (controller instanceof FissionWorkableElectricMultiblockMachine fission) {
                fission.markDirty();
            }
        }
    }

}
