package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class FissionScramHatchPart extends TieredPartMachine implements IMuiMachine {

    @SyncToClient
    @Getter
    private boolean isScrammed = false;

    public FissionScramHatchPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateScramStatus();
    }

    @Override
    public void addedToController(@NotNull MultiblockControllerMachine controller, String substructureName) {
        super.addedToController(controller, substructureName);
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

    private void updateScramStatus() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        boolean newScrammed = level.getBestNeighborSignal(getBlockPos()) > 0;
        if (newScrammed != isScrammed) {
            isScrammed = newScrammed;
            notifyController();
        }
    }

    private void notifyController() {
        for (var controller : getControllers()) {
            if (controller instanceof FissionWorkableElectricMultiblockMachine fission) {
                fission.setChanged();
            }
        }
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 145));
        main.child(PartUi.text(10, 8, "§cFission SCRAM Hatch"));
        main.child(PartUi.dynamic(10, 24,
                () -> isScrammed ? "§c[SCRAM] Reactor HALTED" : "§a[OK] Standby - Reactor Permitted"));
        main.child(PartUi.rule(10, 43, 180));
        main.child(PartUi.text(10, 52, "§eHow it works:"));
        main.child(PartUi.text(10, 63, "§7Any redstone signal on any face"));
        main.child(PartUi.text(10, 73, "§7halts the reactor immediately."));
        main.child(PartUi.text(10, 83, "§7Removing the signal resumes it."));
        main.child(PartUi.rule(10, 102, 180));
        main.child(PartUi.text(10, 111, "Hint: not every redstone source"));
        main.child(PartUi.text(10, 121, "behaves the way you expect."));
        main.child(PartUi.text(10, 131, "Think carefully about signal timing."));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
