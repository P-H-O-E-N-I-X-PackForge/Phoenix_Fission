package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
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

public class AdvancedFissionScramHatchPart extends TieredPartMachine implements IMuiMachine {

    @SaveField
    private int signalThreshold = 8;

    @SaveField
    private int sustainTicks = 5;

    @SyncToClient
    private int sustainCounter = 0;

    @SyncToClient
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
            controller.setChanged();
        }
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 175));
        main.child(PartUi.text(10, 8, "phoenix_fission.ui.advanced_fission_scram_hatch"));
        main.child(PartUi.dynamic(10, 24, () -> isScrammed ? FissionLang.tr("phoenix_fission.ui.scram_halted") :
                FissionLang.tr("phoenix_fission.ui.scram_standby")));
        main.child(PartUi.dynamic(10, 36, () -> {
            if (sustainCounter > 0 && !isScrammed) {
                return FissionLang.tr("phoenix_fission.ui.scram_arming", sustainCounter, sustainTicks);
            } else if (isScrammed) {
                return FissionLang.tr("phoenix_fission.ui.scram_triggered");
            }
            return FissionLang.tr("phoenix_fission.ui.scram_waiting");
        }));
        main.child(PartUi.rule(10, 53, 180));
        main.child(PartUi.text(10, 64, "phoenix_fission.ui.min_signal_strength_1_15"));
        main.child(PartUi.intField(10, 76, 80, 1, 15, () -> signalThreshold, v -> signalThreshold = v));
        main.child(PartUi.text(10, 104, "phoenix_fission.ui.sustain_ticks_1_100"));
        main.child(PartUi.intField(10, 116, 80, 1, 100, () -> sustainTicks, v -> sustainTicks = v));
        main.child(PartUi.rule(10, 147, 180));
        main.child(PartUi.text(10, 156, "gui.phoenix_fission.advanced_scram.hint1"));
        main.child(PartUi.text(10, 166, "gui.phoenix_fission.advanced_scram.hint2"));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
