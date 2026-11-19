package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.phoenix.phoenix_fission.api.machine.multiblock.IStabilityMonitored;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FissionStabilitySensorPart extends SensorHatchPartMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionStabilitySensorPart.class, SensorHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private int minPercent = 0;
    @Persisted
    @Getter
    @Setter
    private int maxPercent = 95;
    @Persisted
    @Getter
    @Setter
    private boolean inverted = false;

    public FissionStabilitySensorPart(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 200, 160);

        group.addWidget(new LabelWidget(10, 8, "phoenix_fission.ui.fission_stability_sensor"));

        group.addWidget(new LabelWidget(10, 24, "phoenix_fission.ui.heat_signal") {

            @Override
            public void updateScreen() {
                super.updateScreen();
                var controller = getController();
                String text;
                if (controller instanceof IStabilityMonitored monitored) {
                    double pct = monitored.getStabilityPercent();

                    int sig = getOutputSignal(null);
                    String sigColor = sig > 0 ? "§a" : "§c";

                    String formattedPct = String.format(java.util.Locale.ROOT, "%.1f", pct);
                    text = LocalizationUtils.format("phoenix_fission.ui.sensor_heat_signal", formattedPct, sigColor,
                            sig);
                } else {
                    text = LocalizationUtils.format("phoenix_fission.ui.sensor_heat_na");
                }

                this.setComponent(net.minecraft.network.chat.Component.literal(text));
            }
        });

        group.addWidget(new Widget(10, 43, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 54, "phoenix_fission.ui.min_heat_percent"));
        group.addWidget(new IntInputWidget(115, 49, 75, 20,
                () -> minPercent,
                val -> minPercent = Mth.clamp(val, 0, 200)));

        group.addWidget(new LabelWidget(10, 78, "phoenix_fission.ui.max_heat_percent"));
        group.addWidget(new IntInputWidget(115, 73, 75, 20,
                () -> maxPercent,
                val -> maxPercent = Mth.clamp(val, 0, 200)));

        group.addWidget(new LabelWidget(10, 102, "phoenix_fission.ui.invert_output"));
        group.addWidget(new ToggleButtonWidget(
                115, 98, 18, 18,
                GuiTextures.INVERT_REDSTONE_BUTTON,
                this::isInverted,
                this::setInverted)
                .setTooltipText("phoenix_fission.ui.invert_signal_output"));

        group.addWidget(new Widget(10, 127, 180, 1).setBackground(GuiTextures.BLANK));

        group.addWidget(new LabelWidget(10, 138, "phoenix_fission.ui.emits_signal_on_all_block"));

        return group;
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
}
