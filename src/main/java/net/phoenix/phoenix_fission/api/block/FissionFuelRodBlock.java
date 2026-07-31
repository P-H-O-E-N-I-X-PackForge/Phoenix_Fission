package net.phoenix.phoenix_fission.api.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionFuelRodBlock extends ActiveBlock {

    private final IFissionFuelRodType fuelRodType;

    public FissionFuelRodBlock(Properties props, IFissionFuelRodType type) {
        super(props);
        this.fuelRodType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.fission_fuel_rod.info_header")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        tooltip.add(Component
                .translatable("phoenix_fission.fuel_required", getRegistryDisplayName(fuelRodType.getFuelKey()))
                .withStyle(ChatFormatting.WHITE));

        tooltip.add(Component
                .translatable("phoenix_fission.depleted_fuel", getRegistryDisplayName(fuelRodType.getOutputKey()))
                .withStyle(ChatFormatting.DARK_GRAY));

        tooltip.add(Component.translatable("phoenix_fission.heat_production",
                Component.literal(String.valueOf(fuelRodType.getBaseHeatProduction()))
                        .withStyle(ChatFormatting.RED))
                .append(Component.literal(" HU/t").withStyle(ChatFormatting.GRAY)));

        double seconds = fuelRodType.getDurationTicks() / 20.0;
        tooltip.add(Component.translatable("phoenix_fission.fuel_cycle",
                Component.literal(String.valueOf(fuelRodType.getAmountPerCycle()))
                        .withStyle(ChatFormatting.WHITE),
                Component.literal(String.format("%.2f", seconds))
                        .withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GRAY));

        int bias = fuelRodType.getNeutronBias();
        tooltip.add(Component.translatable("phoenix_fission.neutron_bias",
                Component.literal((bias >= 0 ? "+" : "") + bias + "%")
                        .withStyle(bias >= 0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.BLUE))
                .withStyle(ChatFormatting.GRAY));
    }

    public static Component getRegistryDisplayName(String key) {
        if (key.isEmpty()) return Component.literal("None").withStyle(ChatFormatting.GRAY);

        ResourceLocation rl = ResourceLocation.tryParse(key);
        if (rl == null) return Component.literal(key).withStyle(ChatFormatting.YELLOW);

        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item != null && item != Items.AIR) {
            return item.getName(new ItemStack(item));
        }

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
        if (fluid != null && fluid != Fluids.EMPTY) {
            return Component.translatable(fluid.getFluidType().getDescriptionId());
        }

        return Component.literal(key).withStyle(ChatFormatting.YELLOW);
    }

    public static class BindableFuelRodType implements IFissionFuelRodType {

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tintColor;

        private final Supplier<Integer> heatSupplier;
        private final Supplier<Integer> durationSupplier;
        private final Supplier<Integer> amountSupplier;
        private final Supplier<Integer> biasSupplier;
        private final Supplier<String> fuelKeySupplier;
        private final Supplier<String> outputKeySupplier;

        public BindableFuelRodType(String name, int tintColor,
                                   Supplier<Integer> heatSupplier,
                                   Supplier<Integer> durationSupplier,
                                   Supplier<Integer> amountSupplier,
                                   Supplier<Integer> biasSupplier,
                                   Supplier<String> fuelKeySupplier,
                                   Supplier<String> outputKeySupplier) {
            this.name = name;
            this.tintColor = tintColor;
            this.heatSupplier = heatSupplier;
            this.durationSupplier = durationSupplier;
            this.amountSupplier = amountSupplier;
            this.biasSupplier = biasSupplier;
            this.fuelKeySupplier = fuelKeySupplier;
            this.outputKeySupplier = outputKeySupplier;
        }

        @Override
        public int getBaseHeatProduction() {
            return heatSupplier.get();
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
        public int getNeutronBias() {
            return biasSupplier.get();
        }

        @Override
        public @NotNull String getFuelKey() {
            return fuelKeySupplier.get();
        }

        @Override
        public @NotNull String getOutputKey() {
            return outputKeySupplier.get();
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/fuel_rod_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
