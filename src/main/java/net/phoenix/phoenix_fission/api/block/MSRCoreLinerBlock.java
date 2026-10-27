package net.phoenix.phoenix_fission.api.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.phoenix.phoenix_fission.api.block.iface.IMSRCoreLinerType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class MSRCoreLinerBlock extends ActiveBlock {

    private final IMSRCoreLinerType linerType;

    public MSRCoreLinerBlock(Properties props, IMSRCoreLinerType type) {
        super(props);
        this.linerType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.msr_liner.info_header")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        tooltip.add(Component.translatable("phoenix_fission.msr_liner.flow_rate", linerType.getFluidFlowRate())
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component
                .translatable("phoenix_fission.msr_liner.thermal_dissipation",
                        String.format("%.1f", linerType.getHeatPerMb()))
                .withStyle(ChatFormatting.RED));

        String inputFluidName = cleanFluidName(linerType.getInputFluidId());
        String outputFluidName = cleanFluidName(linerType.getOutputFluidId());

        tooltip.add(Component.translatable("phoenix_fission.msr_liner.input_salt", inputFluidName)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("phoenix_fission.msr_liner.output_salt", outputFluidName)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private String cleanFluidName(String fluidId) {
        if (fluidId.isEmpty()) return "None";
        String path = fluidId.contains(":") ? fluidId.split(":")[1] : fluidId;
        return java.util.Arrays.stream(path.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    public static class BindableLinerType implements IMSRCoreLinerType {

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tier;

        private final Supplier<Integer> flowRateSupplier;
        private final Supplier<Double> heatSupplier;
        private final Supplier<String> inputFluidSupplier;
        private final Supplier<String> outputFluidSupplier;
        private final Supplier<ResourceLocation> textureSupplier;

        public BindableLinerType(String name, int tier,
                                 Supplier<Integer> flowRateSupplier,
                                 Supplier<Double> heatSupplier,
                                 Supplier<String> inputFluidSupplier,
                                 Supplier<String> outputFluidSupplier,
                                 Supplier<ResourceLocation> textureSupplier) {
            this.name = name;
            this.tier = tier;
            this.flowRateSupplier = flowRateSupplier;
            this.heatSupplier = heatSupplier;
            this.inputFluidSupplier = inputFluidSupplier;
            this.outputFluidSupplier = outputFluidSupplier;
            this.textureSupplier = textureSupplier;
        }

        @Override
        public int getFluidFlowRate() {
            return flowRateSupplier.get();
        }

        @Override
        public double getHeatPerMb() {
            return heatSupplier.get();
        }

        @Override
        public String getInputFluidId() {
            return inputFluidSupplier.get();
        }

        @Override
        public String getOutputFluidId() {
            return outputFluidSupplier.get();
        }

        @Override
        public ResourceLocation getTexture() {
            return textureSupplier.get();
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
