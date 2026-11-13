package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.phoenix.phoenix_fission.api.machine.multiblock.IStabilityMonitored;
import net.phoenix.phoenix_fission.common.FissionLang;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class FissionStabilitySensorPart extends SensorHatchPartMachine implements IMuiMachine {

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

    public FissionStabilitySensorPart(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        var controller = getController();
        if (!(controller instanceof IStabilityMonitored monitored)) return 0;

        double heatPct = monitored.getStabilityPercent();

        int strength = (int) Math.round((heatPct / 200.0) * 15.0);
        strength = Mth.clamp(strength, 0, 15);

        boolean inRange = heatPct >= minPercent && heatPct <= maxPercent;
        boolean emit = inverted != inRange;

        return emit ? strength : 0;
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 160));
        main.child(PartUi.text(10, 8, "phoenix_fission.ui.fission_stability_sensor"));
        main.child(PartUi.dynamic(10, 24, () -> {
            var controller = getController();
            if (controller instanceof IStabilityMonitored monitored) {
                double pct = monitored.getStabilityPercent();
                int sig = getOutputSignal(null);
                String sigColor = sig > 0 ? "§a" : "§c";
                return FissionLang.tr("phoenix_fission.ui.sensor_heat_signal",
                        String.format(java.util.Locale.ROOT, "%.1f", pct), sigColor, sig);
            }
            return FissionLang.tr("phoenix_fission.ui.sensor_heat_na");
        }));
        main.child(PartUi.rule(10, 43, 180));
        main.child(PartUi.text(10, 54, "phoenix_fission.ui.min_heat_percent"));
        main.child(PartUi.intField(115, 49, 75, 0, 200, () -> minPercent, v -> minPercent = v));
        main.child(PartUi.text(10, 78, "phoenix_fission.ui.max_heat_percent"));
        main.child(PartUi.intField(115, 73, 75, 0, 200, () -> maxPercent, v -> maxPercent = v));
        main.child(PartUi.text(10, 102, "phoenix_fission.ui.invert_output"));
        main.child(
                PartUi.toggle(115, 98, this::isInverted, this::setInverted, "phoenix_fission.ui.invert_signal_output"));
        main.child(PartUi.rule(10, 127, 180));
        main.child(PartUi.text(10, 138, "phoenix_fission.ui.emits_signal_on_all_block"));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
