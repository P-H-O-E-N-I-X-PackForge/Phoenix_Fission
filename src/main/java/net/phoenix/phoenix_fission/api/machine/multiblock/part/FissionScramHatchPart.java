package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

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
            if (controller instanceof com.gregtechceu.gtceu.api.machine.MetaMachine machine) machine.markDirty();
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 200, 145);

        group.addWidget(new LabelWidget(10, 8, "phoenix_fission.ui.fission_scram_hatch"));

        group.addWidget(new LabelWidget(10, 24,
                () -> isScrammed ? LocalizationUtils.format("phoenix_fission.ui.scram_halted") :
                        LocalizationUtils.format("phoenix_fission.ui.scram_standby")));

        group.addWidget(new Widget(10, 43, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 52, "phoenix_fission.ui.how_it_works"));
        group.addWidget(new LabelWidget(10, 63, "phoenix_fission.ui.any_redstone_signal_on_any"));
        group.addWidget(new LabelWidget(10, 73, "phoenix_fission.ui.halts_the_reactor_immediately"));
        group.addWidget(new LabelWidget(10, 83, "phoenix_fission.ui.removing_the_signal_resumes_it"));

        group.addWidget(new Widget(10, 102, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 111, "phoenix_fission.ui.hint_not_every_redstone_source"));
        group.addWidget(new LabelWidget(10, 121, "phoenix_fission.ui.behaves_the_way_you_expect"));
        group.addWidget(new LabelWidget(10, 131, "phoenix_fission.ui.think_carefully_about_signal_timing"));

        return group;
    }
}
