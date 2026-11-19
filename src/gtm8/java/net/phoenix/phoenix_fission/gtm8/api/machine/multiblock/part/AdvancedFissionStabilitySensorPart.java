package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.IStabilityMonitored;
import net.phoenix.phoenix_fission.gtm8.common.FissionLang;

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
        if (!(controller instanceof IStabilityMonitored monitored)) return 0;

        double heatPct = monitored.getStabilityPercent();
        boolean inRange = heatPct >= minPercent && heatPct <= maxPercent;
        boolean emit = inverted != inRange;

        return emit ? Mth.clamp(emitStrength, 1, 15) : 0;
    }

    @Override
    public void buildMainUI(ParentWidget<?> main, PosGuiData data, PanelSyncManager syncManager,
                            UISettings settings) {
        main.child(new Widget<>().pos(0, 0).size(200, 185));
        main.child(PartUi.text(10, 8, "phoenix_fission.ui.advanced_fission_stability_sensor"));
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
        main.child(PartUi.intField(115, 49, 75, 0, 200, () -> getMinPercent(), v -> setMinPercent(v)));
        main.child(PartUi.text(10, 78, "phoenix_fission.ui.max_heat_percent"));
        main.child(PartUi.intField(115, 73, 75, 0, 200, () -> getMaxPercent(), v -> setMaxPercent(v)));
        main.child(PartUi.text(10, 102, "phoenix_fission.ui.emit_strength_1_15"));
        main.child(PartUi.intField(115, 97, 75, 1, 15, () -> getEmitStrength(), v -> setEmitStrength(v)));
        main.child(PartUi.text(10, 126, "phoenix_fission.ui.invert_output"));
        main.child(PartUi.toggle(115, 122, this::isInverted, this::setInverted,
                "phoenix_fission.ui.invert_signal_output"));
        main.child(PartUi.rule(10, 151, 180));
        main.child(PartUi.text(10, 162, "phoenix_fission.ui.emits_fixed_strength_on_all"));
        main.child(PartUi.text(10, 172, "gui.phoenix_fission.advanced_stability_sensor.hint2"));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return IMuiMachine.super.getPanelBuilder(data, syncManager, settings).attachInventory(false)
                .addTitleBar(false).drawGTLogo(false);
    }
}
