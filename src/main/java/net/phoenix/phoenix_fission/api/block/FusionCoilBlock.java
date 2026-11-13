package net.phoenix.phoenix_fission.api.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.block.iface.IFusionCoilType;
import net.phoenix.phoenix_fission.common.FissionLang;

import lombok.Getter;

import java.util.List;

public abstract class FusionCoilBlock extends Block {

    @Getter
    private final IFusionCoilType coilType;

    protected FusionCoilBlock(Properties properties, IFusionCoilType type) {
        super(properties);
        this.coilType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        if (coilType.isInner()) {
            tooltip.add(FissionLang.tr("phoenix_fission.fusion_coil_block.inner_plasma_coil")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(FissionLang.tr("phoenix_fission.fusion_coil_block.plasma_power", coilType.getPlasmaHeat())
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(FissionLang.tr("phoenix_fission.fusion_coil_block.outer_confinement_coil")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(FissionLang.tr("phoenix_fission.fusion_coil_block.field_strength", coilType.getConfinement())
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(FissionLang.tr("phoenix_fission.fusion_coil_block.upkeep_eu_t", coilType.getUpkeepEut())
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
