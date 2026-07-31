package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IMSRCoreLinerType;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class FissionMsrLinerEmiRecipe implements EmiRecipe {

    private static final int W = 200;
    private static final int H = 74;

    private final IMSRCoreLinerType type;
    private final ResourceLocation id;

    public FissionMsrLinerEmiRecipe(IMSRCoreLinerType type) {
        this.type = type;
        this.id = PhoenixFission.id("fission_msr_liner/" + type.getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return PhoenixFissionEmiPlugin.MSR_LINER;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        EmiStack s = FissionEmiUtils.resolveFluid(type.getInputFluidId(), type.getFluidFlowRate());
        return s.isEmpty() ? List.of() : List.of(s);
    }

    @Override
    public List<EmiStack> getOutputs() {
        String outId = type.getOutputFluidId();
        if (outId.equals(type.getInputFluidId())) return List.of();
        EmiStack s = FissionEmiUtils.resolveFluid(outId, type.getFluidFlowRate());
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

        String name = FissionEmiUtils.formatName(type.getName());
        FissionEmiUtils.text(widgets, name, W / 2 - font.width(name) / 2, 8, 0xFF_FFFFFF);

        EmiStack inFluid = FissionEmiUtils.resolveFluid(type.getInputFluidId(), type.getFluidFlowRate());
        String outFluidId = type.getOutputFluidId();
        boolean sameFluid = outFluidId.equals(type.getInputFluidId());

        int slotY = 22;
        EmiTexture arrow = EmiTexture.FULL_ARROW;

        int outWidth = sameFluid ? font.width("(same fluid)") : 18;
        int groupWidth = 18 + 6 + arrow.width + 6 + outWidth;
        int groupX = W / 2 - groupWidth / 2;

        widgets.addSlot(inFluid, groupX, slotY);
        widgets.addTexture(arrow, groupX + 18 + 6, slotY + 1);

        int outX = groupX + 18 + 6 + arrow.width + 6;
        if (!sameFluid) {
            EmiStack outFluid = FissionEmiUtils.resolveFluid(outFluidId, type.getFluidFlowRate());
            widgets.addSlot(outFluid, outX, slotY).recipeContext(this);
        } else {
            FissionEmiUtils.text(widgets, "(same fluid)", outX, slotY + 6, 0xFF_FFFFFF);
        }

        String heat = String.format(Locale.ROOT, "%.1f HU per mB", type.getHeatPerMb());
        String flow = type.getFluidFlowRate() + " mB/t flow";
        FissionEmiUtils.text(widgets, heat, 8, 46, 0xFF_FF9944);
        FissionEmiUtils.text(widgets, flow, W - font.width(flow) - 8, 46, 0xFF_AADDFF);

        String cond = "Transfers reactor heat into the salt loop";
        FissionEmiUtils.text(widgets, cond, W / 2 - font.width(cond) / 2, 60, 0xFF_FFFFFF);
    }
}
