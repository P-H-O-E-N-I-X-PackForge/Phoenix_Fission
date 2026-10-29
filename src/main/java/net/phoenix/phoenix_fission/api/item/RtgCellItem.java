package net.phoenix.phoenix_fission.api.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A radioisotope cell for the RTG Array. Wear is the stack's damage: one point per second spent in a running array,
 * so the durability bar doubles as the cell's remaining half-life.
 */
public class RtgCellItem extends Item {

    private final long euPerTick;
    private final int lifespanSeconds;

    public RtgCellItem(Properties properties, long euPerTick, int lifespanSeconds) {
        super(properties.durability(lifespanSeconds));
        this.euPerTick = euPerTick;
        this.lifespanSeconds = lifespanSeconds;
    }

    public long getEuPerTick() {
        return euPerTick;
    }

    public int getLifespanSeconds() {
        return lifespanSeconds;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("tooltip.phoenix_fission.rtg_cell.output", euPerTick)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.phoenix_fission.rtg_cell.remaining", formatDuration(remaining),
                formatDuration(lifespanSeconds)).withStyle(ChatFormatting.GRAY));
    }

    private static String formatDuration(int seconds) {
        int h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        return h > 0 ? String.format("%dh %02dm", h, m) : String.format("%dm %02ds", m, s);
    }
}
