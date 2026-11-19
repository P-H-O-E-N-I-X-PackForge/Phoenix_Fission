package net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.part;

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

final class PartUi {

    private PartUi() {}

    static TextWidget<?> text(int x, int y, String translationKey) {
        return new TextWidget<>(Component.translatable(translationKey)).pos(x, y);
    }

    static TextWidget<?> dynamic(int x, int y, Supplier<Component> text) {
        return new TextWidget<>(text).pos(x, y);
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

    static ToggleButton toggle(int x, int y, BooleanSupplier get, BooleanConsumer set, String tooltipKey) {
        String translated = net.minecraft.locale.Language.getInstance().getOrDefault(tooltipKey);
        BooleanSyncValue value = (BooleanSyncValue) new BooleanSyncValue(get, set).allowC2S();
        return new ToggleButton().pos(x, y).size(18, 18).value(value).addTooltip(true, translated)
                .addTooltip(false, translated);
    }
}
