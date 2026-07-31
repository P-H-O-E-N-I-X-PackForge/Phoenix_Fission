package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class AdvancedFissionScramHatchPart extends TieredPartMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedFissionScramHatchPart.class, TieredPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int signalThreshold = 8;

    @Persisted
    private int sustainTicks = 5;

    private int sustainCounter = 0;

    @Getter
    private boolean isScrammed = false;

    public AdvancedFissionScramHatchPart(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateScramStatus();
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
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

        int signal = level.getBestNeighborSignal(getPos());

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
        if (level.getBestNeighborSignal(getPos()) < signalThreshold) {
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

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 200, 175);

        group.addWidget(new LabelWidget(10, 8, "§6Advanced Fission SCRAM Hatch"));

        group.addWidget(new LabelWidget(10, 24,
                () -> isScrammed ? "§c[SCRAM] Reactor HALTED" : "§a[OK] Standby - Reactor Permitted"));

        group.addWidget(new LabelWidget(10, 36, () -> {
            if (sustainCounter > 0 && !isScrammed) {
                return String.format("§eArming: %d / %d ticks", sustainCounter, sustainTicks);
            } else if (isScrammed) {
                return "§cArmed and triggered.";
            }
            return "§7Waiting for signal...";
        }));

        group.addWidget(new Widget(10, 53, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 64, "§fMin Signal Strength §7(1-15):"));
        group.addWidget(new IntInputWidget(10, 76, 80, 20,
                () -> signalThreshold,
                val -> signalThreshold = Mth.clamp(val, 1, 15)));

        group.addWidget(new LabelWidget(10, 104, "§fSustain Ticks §7(1-100):"));
        group.addWidget(new IntInputWidget(10, 116, 80, 20,
                () -> sustainTicks,
                val -> sustainTicks = Mth.clamp(val, 1, 100)));

        group.addWidget(new Widget(10, 147, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 156, "Signal must meet strength threshold"));
        group.addWidget(new LabelWidget(10, 166, "for the full sustain duration to SCRAM."));

        return group;
    }
}
