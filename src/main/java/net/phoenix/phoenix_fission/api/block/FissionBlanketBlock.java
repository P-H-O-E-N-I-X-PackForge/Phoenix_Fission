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
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionBlanketBlock extends ActiveBlock {

    private final IFissionBlanketType blanketType;

    public FissionBlanketBlock(Properties properties, IFissionBlanketType blanketType) {
        super(properties);
        this.blanketType = blanketType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.fission_blanket.info_header")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        tooltip.add(Component.literal(" - ")
                .append(Component.translatable("phoenix_fission.tooltip.required_fuel"))
                .append(Component.literal(": "))
                .append(FissionFuelRodBlock.getRegistryDisplayName(blanketType.getRequiredFuelKey())
                        .copy().withStyle(ChatFormatting.DARK_RED)));

        double seconds = blanketType.getDurationTicks() / 20.0;

        tooltip.add(Component.translatable("phoenix_fission.blanket.generation_features")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(" - ")
                .append(Component.translatable("phoenix_fission.tooltip.amount"))
                .append(Component.literal(": " + blanketType.getAmountPerCycle()).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.translatable("gtceu.recipe.duration")
                .append(Component.literal(": " + String.format("%.2f", seconds) + "s").withStyle(ChatFormatting.GOLD)));

        tooltip.add(Component.empty());

        tooltip.add(Component.translatable("phoenix_fission.blanket.potential_outputs")
                .withStyle(ChatFormatting.YELLOW));

        List<IFissionBlanketType.BlanketOutput> outs = blanketType.getOutputs();
        if (outs == null || outs.isEmpty()) {
            tooltip.add(Component.literal(" - (none)").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            for (IFissionBlanketType.BlanketOutput o : outs) {
                Component outName = FissionFuelRodBlock.getRegistryDisplayName(o.key());

                ChatFormatting instabilityColor = o.instability() > 3 ? ChatFormatting.RED :
                        o.instability() > 0 ? ChatFormatting.GOLD :
                                ChatFormatting.BLUE;

                tooltip.add(Component.literal(" - ")
                        .append(outName.copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (")
                                .append(Component.literal("W: " + o.weight()).withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(", "))
                                .append(Component.literal("Inst: " + o.instability()).withStyle(instabilityColor))
                                .append(Component.literal(")"))));
            }
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("phoenix_fission.blanket.bias_hint")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    public static class BindableBlanketType implements IFissionBlanketType {

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tier;
        @Getter
        private final int tintColor;
        @Getter
        @NotNull
        private final List<BlanketOutput> outputs;

        private final Supplier<String> requiredFuelKeySupplier;
        private final Supplier<Integer> durationSupplier;
        private final Supplier<Integer> amountSupplier;
        private final Supplier<String> inputKeySupplier;

        public BindableBlanketType(String name, int tier, int tintColor, List<BlanketOutput> outputs,
                                   Supplier<String> requiredFuelKeySupplier,
                                   Supplier<Integer> durationSupplier,
                                   Supplier<Integer> amountSupplier,
                                   Supplier<String> inputKeySupplier) {
            this.name = name;
            this.tier = tier;
            this.tintColor = tintColor;
            this.outputs = outputs;
            this.requiredFuelKeySupplier = requiredFuelKeySupplier;
            this.durationSupplier = durationSupplier;
            this.amountSupplier = amountSupplier;
            this.inputKeySupplier = inputKeySupplier;
        }

        @Override
        public @NotNull String getRequiredFuelKey() {
            return requiredFuelKeySupplier.get();
        }

        @Override
        public int getDurationTicks() {
            return durationSupplier.get();
        }

        @Override
        public int getAmountPerCycle() {
            return amountSupplier.get();
        }

        @Override
        public @NotNull String getInputKey() {
            return inputKeySupplier.get();
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/blanket_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
