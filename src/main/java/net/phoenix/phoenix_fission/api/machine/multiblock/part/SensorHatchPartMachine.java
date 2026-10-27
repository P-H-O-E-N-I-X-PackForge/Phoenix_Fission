package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;


import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SensorHatchPartMachine extends TieredPartMachine {


    protected final ConditionalSubscriptionHandler signalUpdateHandler;

    public SensorHatchPartMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
        this.signalUpdateHandler = new ConditionalSubscriptionHandler(
                this,
                this::updateSignal,
                this::isAttachedToFormedController);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        signalUpdateHandler.updateSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        signalUpdateHandler.unsubscribe();
    }

    @Override
    public void addedToController(@NotNull MultiblockControllerMachine controller) {
        super.addedToController(controller);
        signalUpdateHandler.updateSubscription();
    }

    @Override
    public void removedFromController(@NotNull MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        signalUpdateHandler.updateSubscription();
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return side == getFrontFacing().getOpposite();
    }

    public abstract int getOutputSignal(@Nullable Direction direction);

    public void updateSignal() {
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().updateNeighborsAt(getBlockPos(), getHolder().getSelf().getBlockState().getBlock());
        }
    }

    public @Nullable MultiblockControllerMachine getController() {
        if (getControllers().isEmpty()) return null;
        var controller = getControllers().iterator().next();
        return (controller != null && controller.isFormed()) ? controller : null;
    }

    private boolean isAttachedToFormedController() {
        return getController() != null;
    }
}
