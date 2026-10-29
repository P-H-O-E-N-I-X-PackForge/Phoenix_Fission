package net.phoenix.phoenix_fission.api.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionModeratorBlock extends ActiveBlock {

    private final IFissionModeratorType moderatorType;

    public FissionModeratorBlock(Properties properties, IFissionModeratorType moderatorType) {
        super(properties);
        this.moderatorType = moderatorType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.info_header")
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.boost",
                moderatorType.getEUBoost()).withStyle(ChatFormatting.GREEN));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.fuel_discount",
                moderatorType.getFuelDiscount()).withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.multiplier",
                String.format("%.2f", moderatorType.getHeatMultiplier())).withStyle(ChatFormatting.GOLD));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.parallel",
                moderatorType.getParallelBonus()).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static class BindableModeratorType implements IFissionModeratorType {

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tintColor;

        private final Supplier<Integer> euBoostSupplier;
        private final Supplier<Integer> fuelDiscountSupplier;
        private final Supplier<Double> heatMultiplierSupplier;
        private final Supplier<Integer> parallelBonusSupplier;

        public BindableModeratorType(String name, int tintColor,
                                     Supplier<Integer> euBoostSupplier,
                                     Supplier<Integer> fuelDiscountSupplier,
                                     Supplier<Double> heatMultiplierSupplier,
                                     Supplier<Integer> parallelBonusSupplier) {
            this.name = name;
            this.tintColor = tintColor;
            this.euBoostSupplier = euBoostSupplier;
            this.fuelDiscountSupplier = fuelDiscountSupplier;
            this.heatMultiplierSupplier = heatMultiplierSupplier;
            this.parallelBonusSupplier = parallelBonusSupplier;
        }

        @Override
        public int getEUBoost() {
            return euBoostSupplier.get();
        }

        @Override
        public int getFuelDiscount() {
            return fuelDiscountSupplier.get();
        }

        @Override
        public double getHeatMultiplier() {
            return heatMultiplierSupplier.get();
        }

        @Override
        public int getParallelBonus() {
            return parallelBonusSupplier.get();
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/moderator_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
