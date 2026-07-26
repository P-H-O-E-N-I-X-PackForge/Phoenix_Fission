package net.phoenix.phoenix_fission.common.data.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.IFissionCoolerType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionCoolerBlock extends ActiveBlock {

    private final IFissionCoolerType coolerType;

    public FissionCoolerBlock(Properties props, IFissionCoolerType type) {
        super(props);
        this.coolerType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.fission_cooler.info_header")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        if (coolerType.isPassive()) {
            tooltip.add(Component.literal("Passive — no coolant required").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("Flat cooling: ")
                    .append(Component.literal(String.format("%.0f HU/t", coolerType.getFlatCoolingHUt()))
                            .withStyle(ChatFormatting.AQUA)));
        } else {
            String inId = coolerType.getRequiredCoolantMaterialId();
            tooltip.add(Component.translatable("phoenix_fission.coolant_required", getFluidDisplayName(inId))
                    .withStyle(ChatFormatting.WHITE));

            String outId = coolerType.getOutputCoolantFluidId();
            if (!outId.equalsIgnoreCase(inId)) {
                tooltip.add(Component.translatable("phoenix_fission.coolant_output", getFluidDisplayName(outId))
                        .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(
                    Component.translatable("phoenix_fission.coolant_usage_value", coolerType.getCoolantUsagePerTick())
                            .withStyle(ChatFormatting.LIGHT_PURPLE));

            if (coolerType.getOutputCoolantPerTick() != coolerType.getCoolantUsagePerTick() &&
                    !outId.equalsIgnoreCase(inId)) {
                tooltip.add(
                        Component.translatable("phoenix_fission.coolant_output_value",
                                coolerType.getOutputCoolantPerTick())
                                .withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            tooltip.add(Component.translatable("phoenix_fission.cooling_power",
                    Component.literal(String.valueOf(coolerType.getCoolerTemperature()))
                            .withStyle(ChatFormatting.BLUE))
                    .append(Component.literal(" K threshold").withStyle(ChatFormatting.GRAY)));
        }
    }

    public static Component getFluidDisplayName(String fluidId) {
        if (fluidId.isEmpty() || "none".equalsIgnoreCase(fluidId)) {
            return Component.literal("None").withStyle(ChatFormatting.GRAY);
        }

        ResourceLocation rl = ResourceLocation.tryParse(fluidId);
        if (rl == null) return Component.translatable(fluidId).withStyle(ChatFormatting.YELLOW);

        Fluid f = ForgeRegistries.FLUIDS.getValue(rl);
        if (f != null && f != Fluids.EMPTY) {
            return Component.translatable(f.getFluidType().getDescriptionId());
        }

        return Component.translatable(fluidId).withStyle(ChatFormatting.YELLOW);
    }

    public static class BindableCoolerType implements StringRepresentable, IFissionCoolerType {

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tier;
        @Getter
        private final int tintColor;

        private final Supplier<Integer> tempSupplier;
        private final Supplier<Integer> usageSupplier;
        private final Supplier<Integer> outputAmountSupplier;
        private final Supplier<String> inputSupplier;
        private final Supplier<String> outputSupplier;
        private final Supplier<Double> flatSupplier;

        public BindableCoolerType(String name, int tier, int tintColor,
                                  Supplier<Integer> tempSupplier,
                                  Supplier<Integer> usageSupplier,
                                  Supplier<String> inputSupplier,
                                  Supplier<String> outputSupplier,
                                  Supplier<Double> flatSupplier) {
            this(name, tier, tintColor, tempSupplier, usageSupplier, usageSupplier,
                    inputSupplier, outputSupplier, flatSupplier);
        }

        public BindableCoolerType(String name, int tier, int tintColor,
                                  Supplier<Integer> tempSupplier,
                                  Supplier<Integer> usageSupplier,
                                  Supplier<Integer> outputAmountSupplier,
                                  Supplier<String> inputSupplier,
                                  Supplier<String> outputSupplier,
                                  Supplier<Double> flatSupplier) {
            this.name = name;
            this.tier = tier;
            this.tintColor = tintColor;
            this.tempSupplier = tempSupplier;
            this.usageSupplier = usageSupplier;
            this.outputAmountSupplier = outputAmountSupplier;
            this.inputSupplier = inputSupplier;
            this.outputSupplier = outputSupplier;
            this.flatSupplier = flatSupplier;
        }

        @Override
        public int getCoolerTemperature() {
            return tempSupplier.get();
        }

        @Override
        public int getCoolantPerTick() {
            return usageSupplier.get();
        }

        @Override
        public int getOutputCoolantPerTick() {
            return outputAmountSupplier.get();
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return inputSupplier.get();
        }

        @Override
        public @NotNull String getOutputCoolantFluidId() {
            return outputSupplier.get();
        }

        @Override
        public double getFlatCoolingHUt() {
            return flatSupplier.get();
        }

        @Override
        public int getCoolantUsagePerTick() {
            return getCoolantPerTick();
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/cooler_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        @Override
        public Material getMaterial() {
            return GTMaterials.NULL;
        }
    }
}
