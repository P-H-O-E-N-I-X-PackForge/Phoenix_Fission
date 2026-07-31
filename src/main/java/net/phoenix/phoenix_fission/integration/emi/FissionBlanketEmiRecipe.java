package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class FissionBlanketEmiRecipe implements EmiRecipe {

    private static final int W = 200;
    private static final int OUT_PER_ROW = 6;
    private static final int SLOT_STEP = 20;

    private final IFissionBlanketType type;
    private final ResourceLocation id;
    private final int outputRows;

    public FissionBlanketEmiRecipe(IFissionBlanketType type) {
        this.type = type;
        this.id = PhoenixFission.id("fission_blanket/" + type.getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
        int count = type.getOutputs().size();
        this.outputRows = Math.max(1, (count + OUT_PER_ROW - 1) / OUT_PER_ROW);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return PhoenixFissionEmiPlugin.BLANKET;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        EmiStack s = FissionEmiUtils.resolveItem(type.getInputKey(), type.getAmountPerCycle());
        return s.isEmpty() ? List.of() : List.of(s);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return type.getOutputs().stream()
                .map(o -> FissionEmiUtils.resolveItem(o.key(), 1))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public int getDisplayWidth() {
        return W;
    }

    @Override
    public int getDisplayHeight() {
        return 44 + outputRows * SLOT_STEP;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Font font = Minecraft.getInstance().font;
        int h = getDisplayHeight();

        FissionEmiUtils.drawPanel(widgets, W, h);

        String name = FissionEmiUtils.formatName(type.getName());
        FissionEmiUtils.text(widgets, name, W / 2 - font.width(name) / 2, 6, 0xFFFFFFFF);

        int inputY = 20;
        widgets.addSlot(FissionEmiUtils.resolveItem(type.getInputKey(), type.getAmountPerCycle()), 8, inputY);

        EmiTexture arrow = EmiTexture.FULL_ARROW;
        int arrowX = 8 + 18 + 4;
        widgets.addTexture(arrow, arrowX, inputY + 1);

        int gridX = arrowX + arrow.width + 6;
        int totalWeight = type.getOutputs().stream().mapToInt(IFissionBlanketType.BlanketOutput::weight).sum();

        List<IFissionBlanketType.BlanketOutput> outputs = type.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            var out = outputs.get(i);
            EmiStack stack = FissionEmiUtils.resolveItem(out.key(), 1);
            if (stack.isEmpty()) continue;
            int col = i % OUT_PER_ROW;
            int row = i / OUT_PER_ROW;
            int x = gridX + col * SLOT_STEP;
            int y = inputY + row * SLOT_STEP;
            widgets.addSlot(stack, x, y).recipeContext(this);

            double pct = totalWeight > 0 ? 100.0 * out.weight() / totalWeight : 0.0;
            String pctLabel = String.format(Locale.ROOT, "%.0f%%", pct);
            widgets.addTooltipText(
                    List.of(Component.literal(pctLabel + " chance, " + out.instability() + " instability")),
                    x, y, 18, 18);
        }

        int statsY = inputY + outputRows * SLOT_STEP + 4;
        String dur = String.format(Locale.ROOT, "%.0fs cycle", type.getDurationTicks() / 20.0);
        String amt = "x" + type.getAmountPerCycle() + " consumed";
        FissionEmiUtils.text(widgets, dur, 8, statsY, 0xFF_AADDFF);
        FissionEmiUtils.text(widgets, amt, W - font.width(amt) - 8, statsY, 0xFF_FFFFFF);
    }
}
