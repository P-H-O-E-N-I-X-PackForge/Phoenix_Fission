package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.part;

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
import net.phoenix.phoenix_fission.gtm8.common.FissionLang;

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
            controller.setChanged();
        }
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 145));
        main.child(PartUi.text(10, 8, "phoenix_fission.ui.fission_scram_hatch"));
        main.child(PartUi.dynamic(10, 24, () -> isScrammed ? FissionLang.tr("phoenix_fission.ui.scram_halted") :
                FissionLang.tr("phoenix_fission.ui.scram_standby")));
        main.child(PartUi.rule(10, 43, 180));
        main.child(PartUi.text(10, 52, "phoenix_fission.ui.how_it_works"));
        main.child(PartUi.text(10, 63, "phoenix_fission.ui.any_redstone_signal_on_any"));
        main.child(PartUi.text(10, 73, "phoenix_fission.ui.halts_the_reactor_immediately"));
        main.child(PartUi.text(10, 83, "phoenix_fission.ui.removing_the_signal_resumes_it"));
        main.child(PartUi.rule(10, 102, 180));
        main.child(PartUi.text(10, 111, "phoenix_fission.ui.hint_not_every_redstone_source"));
        main.child(PartUi.text(10, 121, "phoenix_fission.ui.behaves_the_way_you_expect"));
        main.child(PartUi.text(10, 131, "phoenix_fission.ui.think_carefully_about_signal_timing"));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
