package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class AdvancedFissionStabilitySensorPart extends SensorHatchPartMachine implements IMuiMachine {

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

    @SaveField
    @Getter
    @Setter
    private int emitStrength = 15;

    public AdvancedFissionStabilitySensorPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        var controller = getController();
        if (!(controller instanceof FissionWorkableElectricMultiblockMachine fission)) return 0;

        double maxSafe = fission.getMaxSafeHeatHU();
        if (maxSafe <= 0) return 0;

        double heatPct = (fission.getHeat() / maxSafe) * 100.0;
        boolean inRange = heatPct >= minPercent && heatPct <= maxPercent;
        boolean emit = inverted != inRange;

        return emit ? Mth.clamp(emitStrength, 1, 15) : 0;
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 185));
        main.child(PartUi.text(10, 8, "§bAdvanced Fission Stability Sensor"));
        main.child(PartUi.dynamic(10, 24, () -> {
            var controller = getController();
            if (controller instanceof FissionWorkableElectricMultiblockMachine fission) {
                double maxSafe = fission.getMaxSafeHeatHU();
                double pct = maxSafe > 0 ? (fission.getHeat() / maxSafe) * 100.0 : 0.0;
                int sig = getOutputSignal(null);
                String sigColor = sig > 0 ? "§a" : "§c";
                return String.format(java.util.Locale.ROOT, "§7Heat: §f%.1f%%  §7Signal: %s%d", pct, sigColor, sig);
            }
            return "§7Heat: §8N/A  §7Signal: §80";
        }));
        main.child(PartUi.rule(10, 43, 180));
        main.child(PartUi.text(10, 54, "§fMin Heat Percent:"));
        main.child(PartUi.intField(115, 49, 75, 0, 200, () -> getMinPercent(), v -> setMinPercent(v)));
        main.child(PartUi.text(10, 78, "§fMax Heat Percent:"));
        main.child(PartUi.intField(115, 73, 75, 0, 200, () -> getMaxPercent(), v -> setMaxPercent(v)));
        main.child(PartUi.text(10, 102, "§fEmit Strength §7(1-15):"));
        main.child(PartUi.intField(115, 97, 75, 1, 15, () -> getEmitStrength(), v -> setEmitStrength(v)));
        main.child(PartUi.text(10, 126, "§fInvert output:"));
        main.child(PartUi.toggle(115, 122, this::isInverted, this::setInverted, "Invert Signal Output"));
        main.child(PartUi.rule(10, 151, 180));
        main.child(PartUi.text(10, 162, "Emits fixed strength on all block faces."));
        main.child(PartUi.text(10, 172, "Pair with an Advanced SCRAM Hatch."));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
