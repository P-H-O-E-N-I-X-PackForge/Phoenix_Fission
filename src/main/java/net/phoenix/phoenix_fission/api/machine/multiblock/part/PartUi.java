package net.phoenix.phoenix_fission.api.machine.multiblock.part;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import brachy.modularui.drawable.Rectangle;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Tiny layout helpers for the fission hatch/sensor screens (absolute positions, like the old LDLib layouts). */
final class PartUi {

    private PartUi() {}

    /** Text with legacy section-sign colour codes, as the old labels used. */
    static TextWidget<?> text(int x, int y, String legacyText) {
        return new TextWidget<>(Component.literal(legacyText)).pos(x, y);
    }

    static TextWidget<?> dynamic(int x, int y, Supplier<String> legacyText) {
        return new TextWidget<>(() -> Component.literal(legacyText.get())).pos(x, y);
    }

    static Widget<?> rule(int x, int y, int width) {
        return new Widget<>().pos(x, y).size(width, 1).background(new Rectangle().color(0x55_FFFFFF));
    }

    static TextFieldWidget intField(int x, int y, int width, int min, int max, IntSupplier get, IntConsumer set) {
        IntSyncValue value = (IntSyncValue) new IntSyncValue(get, v -> set.accept(Mth.clamp(v, min, max)))
                .allowC2S();
        return new TextFieldWidget().pos(x, y).size(width, 18).setNumbers(min, max).value(value)
                .setDefaultNumber(min);
    }

    static ToggleButton toggle(int x, int y, BooleanSupplier get, BooleanConsumer set, String tooltip) {
        BooleanSyncValue value = (BooleanSyncValue) new BooleanSyncValue(get, set).allowC2S();
        return new ToggleButton().pos(x, y).size(18, 18).value(value).addTooltip(true, tooltip)
                .addTooltip(false, tooltip);
    }
}
