package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class FissionScramHatchPart extends TieredPartMachine {

    @Getter
    private boolean isScrammed = false;

    public FissionScramHatchPart(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
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

    private void updateScramStatus() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        boolean newScrammed = level.getBestNeighborSignal(getPos()) > 0;
        if (newScrammed != isScrammed) {
            isScrammed = newScrammed;
            notifyController();
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
        WidgetGroup group = new WidgetGroup(0, 0, 200, 145);

        group.addWidget(new LabelWidget(10, 8, "§cFission SCRAM Hatch"));

        group.addWidget(new LabelWidget(10, 24,
                () -> isScrammed ? "§c[SCRAM] Reactor HALTED" : "§a[OK] Standby - Reactor Permitted"));

        group.addWidget(new Widget(10, 43, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 52, "§eHow it works:"));
        group.addWidget(new LabelWidget(10, 63, "§7Any redstone signal on any face"));
        group.addWidget(new LabelWidget(10, 73, "§7halts the reactor immediately."));
        group.addWidget(new LabelWidget(10, 83, "§7Removing the signal resumes it."));

        group.addWidget(new Widget(10, 102, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 111, "Hint: not every redstone source"));
        group.addWidget(new LabelWidget(10, 121, "behaves the way you expect."));
        group.addWidget(new LabelWidget(10, 131, "Think carefully about signal timing."));

        return group;
    }
}
