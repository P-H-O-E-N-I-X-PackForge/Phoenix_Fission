package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class FissionFuelBurnEmiRecipe implements EmiRecipe {

    private static final int W = 200;
    private static final int H = 74;
    private static final int SLOT_Y = 8;
    private static final int IN_X = 12;
    private static final int OUT_X = W - 12 - 18;

    private final IFissionFuelRodType type;
    private final ResourceLocation id;

    public FissionFuelBurnEmiRecipe(IFissionFuelRodType type) {
        this.type = type;
        this.id = PhoenixFission.id("fission_fuel/" + type.getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return PhoenixFissionEmiPlugin.FUEL_BURN;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        EmiStack s = FissionEmiUtils.resolveItem(type.getFuelKey(), type.getAmountPerCycle());
        return s.isEmpty() ? List.of() : List.of(s);
    }

    @Override
    public List<EmiStack> getOutputs() {
        EmiStack s = FissionEmiUtils.resolveItem(type.getOutputKey(), 1);
        return s.isEmpty() ? List.of() : List.of(s);
    }

    @Override
    public int getDisplayWidth() {
        return W;
    }

    @Override
    public int getDisplayHeight() {
        return H;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Font font = Minecraft.getInstance().font;

        FissionEmiUtils.drawPanel(widgets, W, H);

        widgets.addSlot(FissionEmiUtils.resolveItem(type.getFuelKey(), type.getAmountPerCycle()), IN_X, SLOT_Y);
        widgets.addSlot(FissionEmiUtils.resolveItem(type.getOutputKey(), 1), OUT_X, SLOT_Y).recipeContext(this);

        EmiTexture arrow = EmiTexture.FULL_ARROW;
        int arrowX = IN_X + 18 + (OUT_X - (IN_X + 18) - arrow.width) / 2;
        widgets.addTexture(arrow, arrowX, SLOT_Y + 1);

        String name = FissionEmiUtils.formatName(type.getName());
        int nameX = W / 2 - font.width(name) / 2;
        FissionEmiUtils.text(widgets, name, nameX, 34, 0xFFFFFFFF);

        String hut = type.getBaseHeatProduction() + " HU/t";
        double sec = type.getDurationTicks() / 20.0;
        String dur = String.format(Locale.ROOT, "%.0fs cycle", sec);
        FissionEmiUtils.text(widgets, hut, 8, 46, 0xFF_FF9944);
        FissionEmiUtils.text(widgets, dur, W - font.width(dur) - 8, 46, 0xFF_AADDFF);

        String amt = "x" + type.getAmountPerCycle() + " consumed per cycle";
        FissionEmiUtils.text(widgets, amt, W / 2 - font.width(amt) / 2, 58, 0xFF_FFFFFF);
    }
}
