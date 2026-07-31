package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class FissionModeratorEmiRecipe implements EmiRecipe {

    private static final int W = 200;
    private static final int H = 76;

    private final IFissionModeratorType type;
    private final ResourceLocation id;

    public FissionModeratorEmiRecipe(IFissionModeratorType type) {
        this.type = type;
        this.id = PhoenixFission.id("fission_moderator/" + type.getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return PhoenixFissionEmiPlugin.MODERATOR;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
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

        var block = PhoenixAPI.FISSION_MODERATORS.get(type);
        EmiStack stack = block != null && block.get() != null ? EmiStack.of(new ItemStack(block.get().asItem())) :
                EmiStack.EMPTY;
        widgets.addSlot(stack, 8, 8);

        String name = FissionEmiUtils.formatName(type.getName());
        FissionEmiUtils.text(widgets, name, 30, 8, 0xFF_FFFFFF);

        String eu = "+" + type.getEUBoost() + "% EU output";
        String fuel = "-" + type.getFuelDiscount() + "% fuel usage";
        FissionEmiUtils.text(widgets, eu, 8, 40, 0xFF_FFDD55);
        FissionEmiUtils.text(widgets, fuel, 8, 51, 0xFF_88FF88);

        String heat = String.format(Locale.ROOT, "x%.1f heat multiplier", type.getHeatMultiplier());
        String par = "+" + type.getParallelBonus() + " parallel";
        FissionEmiUtils.text(widgets, heat, W - font.width(heat) - 8, 40, 0xFF_FF9944);
        FissionEmiUtils.text(widgets, par, W - font.width(par) - 8, 51, 0xFF_AADDFF);

        String note = "Passive reactor bonus while installed";
        FissionEmiUtils.text(widgets, note, W / 2 - font.width(note) / 2, 64, 0xFF_FFFFFF);
    }
}
