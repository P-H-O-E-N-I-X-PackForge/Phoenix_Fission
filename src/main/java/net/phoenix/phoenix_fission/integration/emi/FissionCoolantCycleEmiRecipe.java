package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FissionCoolantCycleEmiRecipe implements EmiRecipe {

    private static final int W = 200;

    private final IFissionCoolerType type;
    private final ResourceLocation id;

    public FissionCoolantCycleEmiRecipe(IFissionCoolerType type) {
        this.type = type;
        this.id = PhoenixFission.id("fission_coolant/" + type.getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return PhoenixFissionEmiPlugin.COOLANT_CYCLE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        if (type.isPassive()) return List.of();
        List<EmiIngredient> inputs = new ArrayList<>();
        if (type.hasFluidCoolant()) {
            EmiStack s = FissionEmiUtils.resolveFluid(type.getInputCoolantFluidId(), type.getCoolantPerTick());
            if (!s.isEmpty()) inputs.add(s);
        }
        if (type.hasItemCoolant()) {
            EmiStack s = FissionEmiUtils.resolveItem(type.getItemCoolantId(), 1);
            if (!s.isEmpty()) inputs.add(s);
        }
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        if (type.isPassive()) return List.of();
        List<EmiStack> outputs = new ArrayList<>();
        String outId = type.getOutputCoolantFluidId();
        if (type.hasFluidCoolant() && !outId.equals(type.getInputCoolantFluidId())) {
            EmiStack s = FissionEmiUtils.resolveFluid(outId, type.getCoolantPerTick());
            if (!s.isEmpty()) outputs.add(s);
        }
        if (type.hasItemCoolant() && !type.getItemCoolantOutputId().isEmpty()) {
            EmiStack s = FissionEmiUtils.resolveItem(type.getItemCoolantOutputId(), 1);
            if (!s.isEmpty()) outputs.add(s);
        }
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return W;
    }

    @Override
    public int getDisplayHeight() {
        if (type.isPassive()) return 58;
        // Fluid cooler layout is 74 tall; an item-coolant row adds one slot row, an item-only cooler is shorter.
        return type.hasFluidCoolant() ? (type.hasItemCoolant() ? 100 : 74) : 74;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Font font = Minecraft.getInstance().font;
        int h = getDisplayHeight();

        FissionEmiUtils.drawPanel(widgets, W, h);

        String name = FissionEmiUtils.formatName(type.getName());
        FissionEmiUtils.text(widgets, name, W / 2 - font.width(name) / 2, 8, 0xFFFFFFFF);
        if (type.isPassive()) {
            String label = "Passive Cooler: no coolant needed";
            FissionEmiUtils.text(widgets, label, W / 2 - font.width(label) / 2, 22, 0xFF_88FF88);

            String flat = String.format(Locale.ROOT, "%.0f HU/t (always active)", type.getFlatCoolingHUt());
            FissionEmiUtils.text(widgets, flat, W / 2 - font.width(flat) / 2, 34, 0xFF_55FFFF);

            String note = "Active even during coolant outage";
            FissionEmiUtils.text(widgets, note, W / 2 - font.width(note) / 2, 46, 0xFF_FFFFFF);
        } else if (!type.hasFluidCoolant()) {
            drawItemRow(widgets, font, 22);

            String thresh = coolingLabel();
            String rate = String.format(Locale.ROOT, "1 item / %.2f s", type.getItemCoolantSeconds());
            FissionEmiUtils.text(widgets, thresh, 8, 46, 0xFF_55FFFF);
            FissionEmiUtils.text(widgets, rate, W - font.width(rate) - 8, 46, 0xFF_AADDFF);

            String cond = "Cools when reactor heat > threshold";
            FissionEmiUtils.text(widgets, cond, W / 2 - font.width(cond) / 2, 60, 0xFF_FFFFFF);
        } else {
            EmiStack inFluid = FissionEmiUtils.resolveFluid(type.getInputCoolantFluidId(), type.getCoolantPerTick());
            String outFluidId = type.getOutputCoolantFluidId();
            boolean sameFluid = outFluidId.equals(type.getInputCoolantFluidId());

            int slotY = 22;
            EmiTexture arrow = EmiTexture.FULL_ARROW;

            int outWidth = sameFluid ? font.width("(same fluid)") : 18;
            int groupWidth = 18 + 6 + arrow.width + 6 + outWidth;
            int groupX = W / 2 - groupWidth / 2;

            widgets.addSlot(inFluid, groupX, slotY);
            widgets.addTexture(arrow, groupX + 18 + 6, slotY + 1);

            int outX = groupX + 18 + 6 + arrow.width + 6;
            if (!sameFluid) {
                EmiStack outFluid = FissionEmiUtils.resolveFluid(outFluidId, type.getCoolantPerTick());
                widgets.addSlot(outFluid, outX, slotY).recipeContext(this);
            } else {
                FissionEmiUtils.text(widgets, "(same fluid)", outX, slotY + 6, 0xFF_FFFFFF);
            }

            String thresh = coolingLabel();
            String mbt = type.getCoolantPerTick() + " mB/t";
            FissionEmiUtils.text(widgets, thresh, 8, 46, 0xFF_55FFFF);
            FissionEmiUtils.text(widgets, mbt, W - font.width(mbt) - 8, 46, 0xFF_AADDFF);

            String cond = "Cools when reactor heat > threshold";
            FissionEmiUtils.text(widgets, cond, W / 2 - font.width(cond) / 2, 60, 0xFF_FFFFFF);

            if (type.hasItemCoolant()) {
                drawItemRow(widgets, font, 76);
            }
        }
    }

    private String coolingLabel() {
        boolean rated = net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs.INSTANCE.fission.heatModel
                .thermalModel == net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs.ThermalModel.RATED;
        if (rated && type.getRatedCoolingHUt() >= 0) {
            return String.format(Locale.ROOT, "%.0f HU/t rated", type.getRatedCoolingHUt());
        }
        return type.getCoolerTemperature() + " K threshold";
    }

    private void drawItemRow(WidgetHolder widgets, Font font, int slotY) {
        String outItemId = type.getItemCoolantOutputId();
        boolean hasOut = !outItemId.isEmpty() && !"none".equalsIgnoreCase(outItemId);
        int outWidth = hasOut ? 18 : font.width("(consumed)");
        EmiTexture arrow = EmiTexture.FULL_ARROW;
        int groupWidth = 18 + 6 + arrow.width + 6 + outWidth;
        int groupX = W / 2 - groupWidth / 2;

        widgets.addSlot(FissionEmiUtils.resolveItem(type.getItemCoolantId(), 1), groupX, slotY);
        widgets.addTexture(arrow, groupX + 18 + 6, slotY + 1);

        int outX = groupX + 18 + 6 + arrow.width + 6;
        if (hasOut) {
            widgets.addSlot(FissionEmiUtils.resolveItem(outItemId, 1), outX, slotY).recipeContext(this);
        } else {
            FissionEmiUtils.text(widgets, "(consumed)", outX, slotY + 6, 0xFF_FFFFFF);
        }
    }
}
