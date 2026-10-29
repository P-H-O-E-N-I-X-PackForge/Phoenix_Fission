package net.phoenix.phoenix_fission.api.machine.multiblock.part;

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
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

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
            if (controller instanceof FissionWorkableElectricMultiblockMachine fission) {
                fission.setChanged();
            }
        }
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 175));
        main.child(PartUi.text(10, 8, "§6Advanced Fission SCRAM Hatch"));
        main.child(PartUi.dynamic(10, 24,
                () -> isScrammed ? "§c[SCRAM] Reactor HALTED" : "§a[OK] Standby - Reactor Permitted"));
        main.child(PartUi.dynamic(10, 36, () -> {
            if (sustainCounter > 0 && !isScrammed) {
                return String.format("§eArming: %d / %d ticks", sustainCounter, sustainTicks);
            } else if (isScrammed) {
                return "§cArmed and triggered.";
            }
            return "§7Waiting for signal...";
        }));
        main.child(PartUi.rule(10, 53, 180));
        main.child(PartUi.text(10, 64, "§fMin Signal Strength §7(1-15):"));
        main.child(PartUi.intField(10, 76, 80, 1, 15, () -> signalThreshold, v -> signalThreshold = v));
        main.child(PartUi.text(10, 104, "§fSustain Ticks §7(1-100):"));
        main.child(PartUi.intField(10, 116, 80, 1, 100, () -> sustainTicks, v -> sustainTicks = v));
        main.child(PartUi.rule(10, 147, 180));
        main.child(PartUi.text(10, 156, "Signal must meet strength threshold"));
        main.child(PartUi.text(10, 166, "for the full sustain duration to SCRAM."));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
