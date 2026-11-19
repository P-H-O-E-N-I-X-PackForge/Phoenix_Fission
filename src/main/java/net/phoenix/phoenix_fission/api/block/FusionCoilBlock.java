package net.phoenix.phoenix_fission.api.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.block.iface.IFusionCoilType;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Base for the two fusion coil blocks; the pattern predicates match on the subclasses. */
public abstract class FusionCoilBlock extends Block {

    @Getter
    private final IFusionCoilType coilType;

    protected FusionCoilBlock(Properties properties, IFusionCoilType type) {
        super(properties);
        this.coilType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        if (coilType.isInner()) {
            tooltip.add(Component.literal("Inner plasma coil").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Plasma power: " + coilType.getPlasmaHeat()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Outer confinement coil").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Field strength: " + coilType.getConfinement())
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Upkeep: " + coilType.getUpkeepEut() + " EU/t")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static class Inner extends FusionCoilBlock {

        public Inner(Properties properties, IFusionCoilType type) {
            super(properties, type);
        }
    }

    public static class Outer extends FusionCoilBlock {

        public Outer(Properties properties, IFusionCoilType type) {
            super(properties, type);
        }
    }
}
