package net.phoenix.phoenix_fission.gtm8.api.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.gtm8.api.block.iface.IFusionComputeType;
import net.phoenix.phoenix_fission.gtm8.common.FissionLang;

import lombok.Getter;

import java.util.List;

public class FusionComputeBlock extends Block {

    @Getter
    private final IFusionComputeType computeType;

    public FusionComputeBlock(Properties properties, IFusionComputeType type) {
        super(properties);
        this.computeType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.level.BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(FissionLang.tr("phoenix_fission.fusion_compute_block.fusion_compute_module")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(FissionLang.tr("phoenix_fission.fusion_compute_block.compute", computeType.getCompute())
                .withStyle(ChatFormatting.GRAY));
    }
}
